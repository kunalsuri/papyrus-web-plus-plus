# scripts-pwpp — Developer scripts for papyrus-web-plus-plus

Fool-proof PowerShell scripts to set up, build, run and stop **Papyrus-Web** on
Windows. They encode this codebase's actual facts (ports, datasource defaults,
the monorepo build order) so you don't have to remember them.

> Inspired by the `scripts-spp` set in
> [syson-plus-plus](https://github.com/kunalsuri/syson-plus-plus/tree/dev-expt-01/scripts-spp),
> but every command here is derived from **this** repository — see
> [Facts this codebase forced](#facts-this-codebase-forced).

---

## Easiest: just double-click (no terminal)

In Windows Explorer, open the `scripts-pwpp` folder and double-click:

| Double-click | What happens |
| ------------ | ------------ |
| **`setup.bat`** | First-time setup: checks tools, asks for your GitHub username + token (hidden as you type), then builds. Run once. |
| **`run.bat`**   | Starts the whole stack, then points you to http://localhost:5173. Run this every day. |
| **`stop.bat`**  | Stops everything (database data is kept). |

Each window stays open so you can read the output. The `.bat` files just launch
the PowerShell scripts below with the right options, so you never have to think
about execution policies or `pwsh` vs `powershell`.

---

## From a terminal

```powershell
# from the repo root
cd scripts-pwpp

# 1. one-time: check tools, set up the GitHub token, do the first build.
#    -ConfigureAuth prompts for your GitHub username + token (hidden as you
#    type). To run non-interactively instead, set these first:
#       $env:GITHUB_USERNAME = 'your-github-user'
#       $env:GITHUB_TOKEN    = 'ghp_xxx'   # PAT with the read:package scope
.\setup-dev.ps1 -ConfigureAuth

# 2. every day: start the whole stack (db + backend + frontend)
.\start-dev.ps1

# 3. when done
.\stop-dev.ps1
```

Then open **http://localhost:5173**.

---

## Prerequisites

| Tool   | Version            | Why                                              |
| ------ | ------------------ | ------------------------------------------------ |
| Java   | **21+** (Temurin)  | Runs the Spring Boot backend                     |
| Maven  | **3.9.9**          | Builds the backend                               |
| Node   | **22.16.0**        | Pinned in `frontend/package.json`                |
| npm    | **10.9.2**         | Pinned in `frontend/package.json`                |
| Docker | any recent         | Dev database (and some backend tests)            |
| GitHub PAT | scope `read:package` | Backend + some frontend deps live on GitHub Packages |

`setup-dev.ps1 -CheckOnly` verifies all of these without changing anything.

---

## The scripts

These are the scripts you run directly. The last two columns show how they are
wired together — what each one **calls**, and who **calls it**.

| Script | What it does | Calls / uses | Invoked by |
| ------ | ------------ | ------------ | ---------- |
| **`setup-dev.ps1`** | Verify toolchain → check/scaffold GitHub auth → first build. **Start here.** | `shared_core_lib.ps1`, `templates/*`, `build-all.ps1` | you (once) |
| **`build-all.ps1`** | Full build: frontend → copy UI into the backend → backend fat JAR. | `shared_core_lib.ps1`, `npm`, `mvn` | you · `setup-dev.ps1` · `start-dev.ps1 -Build` |
| **`start-db.ps1`** | Start PostgreSQL (Docker) on **5439**, matching the app's defaults. | `shared_core_lib.ps1`, `docker-compose.dev.yml` | you · `start-dev.ps1` |
| **`start-backend.ps1`** | Run the fat JAR on **8080**; record its PID for a clean stop. | `shared_core_lib.ps1`, the fat JAR, `java` | you · `start-dev.ps1` |
| **`start-frontend.ps1`** | Run the Vite dev server on **5173** (builds the components library first). | `shared_core_lib.ps1`, `npm` | you · `start-dev.ps1` |
| **`start-dev.ps1`** | Orchestrate db → backend → frontend, each in its own window. | `shared_core_lib.ps1`, `start-db.ps1`, `start-backend.ps1`, `start-frontend.ps1`, `build-all.ps1` (with `-Build`) | you (daily) |
| **`stop-dev.ps1`** | Stop all three (preserves DB data unless `-RemoveData`). | `shared_core_lib.ps1`, `.logs/backend.pid`, `docker-compose.dev.yml` | you |

Supporting files — **not run directly**, but every script depends on them:

| File | Role | Used by |
| ---- | ---- | ------- |
| `shared_core_lib.ps1` | Shared helpers + the single source of truth for ports/paths/creds. | **dot-sourced by every script above** |
| `docker-compose.dev.yml` | Dev database definition (port 5439, creds match the app). | `start-db.ps1`, `stop-dev.ps1` |
| `templates/settings.xml.template`, `templates/npmrc.template` | GitHub-auth file templates. | `setup-dev.ps1 -ConfigureAuth` |
| `.logs/backend.pid` | Recorded backend PID (runtime artifact, git-ignored). | written by `start-backend.ps1`, read by `stop-dev.ps1` |

Double-click wrappers — thin `.bat` launchers for Explorer (see
[Easiest: just double-click](#easiest-just-double-click-no-terminal)):

| File | Launches | For |
| ---- | -------- | --- |
| `setup.bat` | `setup-dev.ps1 -ConfigureAuth` | one-time setup |
| `run.bat`   | `start-dev.ps1` | daily start |
| `stop.bat`  | `stop-dev.ps1`  | stop everything |

Every script supports `-?` / `Get-Help`, e.g. `Get-Help .\start-backend.ps1 -Detailed`.

> **Changing ports or DB credentials?** They must agree in three places: the
> database (`docker-compose.dev.yml`), the backend
> (`backend/papyrus-web/src/main/resources/application.properties`), and the
> scripts (`shared_core_lib.ps1`).

---

## How the scripts link together

```
shared_core_lib.ps1 ......... dot-sourced by ALL scripts (helpers, ports, paths, creds)

setup-dev.ps1  (run once)
└─ build-all.ps1
   ├─ npm  (frontend build) ──► copies the built UI into the backend
   └─ mvn  (backend build)  ──► produces the fat JAR

start-dev.ps1  (run daily)
├─ build-all.ps1            (only with -Build)
├─ start-db.ps1            ──► docker-compose.dev.yml   → PostgreSQL  :5439
├─ start-backend.ps1 (win) ──► java -jar <fat JAR>      → backend     :8080  → writes .logs/backend.pid
└─ start-frontend.ps1(win) ──► npm run start            → Vite        :5173

stop-dev.ps1  (run to stop)
├─ backend   ◄── reads .logs/backend.pid   (falls back to port 8080)
├─ frontend  ◄── port 5173 owner
└─ database  ◄── docker compose stop
```

Reading it:

- **`shared_core_lib.ps1` underpins everything.** Each script starts with
  `. "$PSScriptRoot\shared_core_lib.ps1"`, so ports, paths and credentials are defined
  once and stay consistent.
- **`setup-dev.ps1` and `start-dev.ps1` are the two entry points.** `setup-dev`
  gets a new machine ready (and calls `build-all`); `start-dev` is the daily
  driver that runs the three `start-*` scripts.
- **The three `start-*` scripts are independent** — run any one on its own, or
  let `start-dev.ps1` sequence them (db ready → backend ready → frontend).
- **Hand-offs between scripts are explicit:** `build-all.ps1` produces the fat
  JAR that `start-backend.ps1` runs; `start-backend.ps1` writes the PID that
  `stop-dev.ps1` reads; `start-db.ps1` and `stop-dev.ps1` share the one
  `docker-compose.dev.yml`.

### Common usage

```powershell
.\setup-dev.ps1 -CheckOnly          # just report environment readiness
.\setup-dev.ps1 -ConfigureAuth      # configure ~/.m2/settings.xml + ~/.npmrc (prompts, or uses env vars)
.\build-all.ps1                     # full rebuild (skips tests; add -WithTests to include them)
.\build-all.ps1 -FixFormat          # auto-format first (avoids the format-lint gate)
.\start-backend.ps1 -Build          # build the backend, then run it
.\start-frontend.ps1 -Rebuild       # force-rebuild the components library
.\start-dev.ps1 -Build              # rebuild everything, then launch the stack
.\stop-dev.ps1 -RemoveData          # stop AND wipe the database volume
```

---

## How the running app fits together

```
 Browser ──> Vite dev server          ──>  Spring Boot backend  ──>  PostgreSQL
             http://localhost:5173    CORS http://localhost:8080      localhost:5439
             (start-frontend.ps1)          (start-backend.ps1)        (start-db.ps1)
```

The frontend talks to the backend **cross-origin** (no proxy); the backend
already allows any origin via `sirius.components.cors.allowedOriginPatterns=*`.

---

## Facts this codebase forced

These are the non-obvious, repo-specific reasons the scripts look the way they
do (and where a naïve copy of another project's scripts would break):

1. **Database defaults are baked into the JAR.**
   `backend/papyrus-web/src/main/resources/application.properties` hardcodes
   `jdbc:postgresql://localhost:5439/papyrus-web-db`, user `dbuser`/`dbpwd` and
   the Liquibase changelog. `docker-compose.dev.yml` matches those exactly, so
   the backend runs with **no datasource arguments**.

2. **The frontend has a build-order trap.** `turbo.json` runs `start` with
   `dependsOn: ["^start"]`, but the `papyrus-web-components` library has no
   `start` script — only `build` — and the app imports it from `./dist`. So a
   fresh checkout **must build the components library before `npm start`**.
   `start-frontend.ps1` does this automatically.

3. **`format-lint` gates every build.** Turbo runs `prettier --list-different`
   before `build`/`start` and fails hard on unformatted files. Use `-FixFormat`
   (runs `npm run format`) when that bites.

4. **The runnable module is `backend/papyrus-web`** (main class
   `org.eclipse.papyrus.web.PapyrusWeb`), not `papyrus-web-application`. The fat
   JAR is `backend/papyrus-web/target/papyrus-web-<version>.jar`; the version
   moves with upstream syncs, so the scripts discover the JAR by pattern rather
   than hardcoding `2026.5.0`.

---

## Troubleshooting

- **`npm ci` / Maven 401 or 403** → GitHub token missing or lacks
  `read:package`. Re-run `setup-dev.ps1 -ConfigureAuth`, or check
  `~/.m2/settings.xml` and `~/.npmrc`.
- **Build fails on `format-lint`** → run with `-FixFormat`, or `npm run format`
  in `frontend/`.
- **Backend exits immediately** → the database isn't up. Run `.\start-db.ps1`
  first (or just use `.\start-dev.ps1`).
- **Wrong Node version** → the project pins **22.16.0**; newer/older versions
  may break native deps. A version manager (fnm/nvm-windows) makes switching
  easy.
- **Windows path-length errors** → enable long paths (see `README.adoc`):
  `New-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem" -Name "LongPathsEnabled" -Value 1 -PropertyType DWORD -Force`
  and `git config --system core.longpaths true`.
- **`running scripts is disabled`** → use the `.bat` wrappers (they set this
  automatically), or launch via
  `pwsh -ExecutionPolicy Bypass -File .\start-dev.ps1`, or set
  `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned`.

---

## How `stop-dev.ps1` decides what to kill

Two tiers, both in `shared_core_lib.ps1`:

- **Backend — recorded PID first.** `start-backend.ps1` writes java's real PID
  to `scripts-pwpp/.logs/backend.pid` at launch. `Stop-TrackedService` stops
  exactly that PID, but **only if it is still a `java` process** — guarding
  against PID reuse (the OS handing that number to something else after java
  exited). If the PID is gone or stale, it falls back to the port owner.
- **Frontend — port owner.** The dev server is an `npm → node → vite` tree with
  no single representative PID, so `Stop-ListenerOnPort` stops whatever `node`
  process is listening on 5173.

Both tiers refuse to kill a process whose name isn't expected, so they can't
terminate an unrelated app holding the port or a reused PID. Pass `-Force` to
bypass the name guard; edit the allowed-names lists if your setup runs these
services under different process names.
