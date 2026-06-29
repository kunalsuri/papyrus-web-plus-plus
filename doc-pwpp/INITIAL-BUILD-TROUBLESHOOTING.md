# Initial Build & Onboarding Troubleshooting

During onboarding or first-time setup via `.\scripts-pwpp\setup-dev.ps1`, the initial build may take a significant amount of time or appear to hang. This guide details what happens during this phase, why it is slow the first time, and how to resolve common setup issues.

---

## 1. Why the Initial Build is Slow

The initial setup triggers a full production package of both the frontend and the backend via `.\scripts-pwpp\build-all.ps1`.

* **Frontend dependencies (`npm ci`):** Downloads and installs the entire React/TypeScript dependency tree from scratch into `node_modules`.
* **Frontend compilation (`npm run build`):** Uses **TurboRepo** to compile both the custom components library and the main application. Without build cache artifacts, every component must compile.
* **Backend dependencies (Maven download):** The backend is a large multi-module Java application consisting of **more than 20 sub-modules** (such as `papyrus-web-infra`, `papyrus-web-domain`, `papyrus-web-sirius-contributions`, etc.). On the first run, Maven must download all dependencies (Spring Boot, Eclipse Sirius, EMF, etc.) into the local cache (`~/.m2/repository`). This network and I/O phase is typically the longest step.
* **Java Compilation:** Compiles class files for all sub-modules sequentially and packages them into the final executable JAR.

---

## 2. Common Hanging & Performance Issues on Windows

If the terminal output is unresponsive or seems stuck at a spinner (like `⠇` during `npm ci`), check the following issues:

### A. Terminal is in "Select/Mark" Mode (Most Common)
* **Symptom:** The cursor changes shape or the terminal window title has `Select` or `Mark` in it.
* **Why:** If you click inside a Windows PowerShell or CMD terminal, Windows freezes the execution of the running process to allow text selection.
* **Fix:** Click in the terminal window and press the **`Escape`** key (or **`Enter`**) to resume execution.

### B. Network/Proxy and GitHub Packages Authentication
* **Symptom:** npm gets stuck indefinitely during dependency downloads.
* **Why:** Scoped packages (`@eclipse-sirius` and `@ObeoNetwork`) are retrieved from the private GitHub Package Registry (`npm.pkg.github.com`). If you are behind a corporate proxy/VPN that blocks this domain, or if the GitHub Token configured in `~/.npmrc` is incorrect, npm connection requests will time out or hang.
* **Fix:** Disconnect/reconnect your VPN or run `npm ci --verbose` directly in the `frontend` folder to diagnose the exact HTTP request that is failing.

### C. Windows Defender Real-Time Protection
* **Symptom:** Processing files takes an extremely long time with high CPU usage by `Antivirus Service Executable` (MsMpEng.exe).
* **Why:** Real-time scanners inspect every single file written to `node_modules` and target directories.
* **Fix:** Temporarily disable real-time protection or add an exclusion for the repository folder in Windows Security settings.

### D. Force-Clearing npm Cache and Retrying Verbose Install
* **Symptom:** npm gets stuck on old cached package metadata, or fails to fetch updates.
* **Fix:** If the process is hung or failing, abort it with `Ctrl + C`, clear the npm cache, and run `npm ci` with verbose logging from the `frontend` directory to see exactly which download is failing:
  ```bash
  cd frontend
  npm cache clean --force
  npm ci --verbose
  ```

---

## 3. Daily Development & Subsequent Runs

Once the initial onboarding build succeeds, subsequent runs will be much faster:

* **Intelligent Dependency Checking:** `build-all.ps1` computes a SHA-256 hash of `package-lock.json` and caches it inside `node_modules/.package-lock.hash`. It will only run `npm ci` if `package-lock.json` changes (e.g. after a git pull or manually adding packages). Otherwise, it bypasses installation and goes straight to compilation.
* **Caching:** Subsequent Maven compilations and TurboRepo builds are heavily cached (saving download and build times).
* **Developer Startup:** For daily active development, run the stack in hot-reload mode:
  ```powershell
  .\scripts-pwpp\start-dev.ps1
  ```
  This starts the database container and runs the backend and frontend dev servers. UI changes hot-reload instantly.
