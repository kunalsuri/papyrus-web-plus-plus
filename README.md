<!-- Copyright (c) 2026 Kunal Suri (CEA LIST). All rights reserved. -->
<div align="center">

<h1>🛸 Papyrus Web++ </h1>
<h3>The AI-Native Project</h3>

[![Status: experimental](https://img.shields.io/badge/status-experimental%20R%26D-blueviolet?style=for-the-badge)](#-the-experiment)
[![License: EPL-2.0](https://img.shields.io/badge/license-EPL--2.0-informational?style=for-the-badge)](LICENSE)

[![Java 21](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)](pom.xml)
[![Node 22.16](https://img.shields.io/badge/Node-22.16.0-green?style=for-the-badge&logo=nodedotjs&logoColor=white)](package.json)
[![PostgreSQL 15](https://img.shields.io/badge/PostgreSQL-15-blue?style=for-the-badge&logo=postgresql&logoColor=white)](docker-compose.yml)

[![Agent-ready](https://img.shields.io/badge/agents-Claude%20%7C%20Cursor%20%7C%20Copilot%20%7C%20Windsurf-black?style=for-the-badge&logo=anthropic&logoColor=white)](ai/INDEX.md)

</div>

---

> **An experimental sandbox for turning a complex, industrial-grade codebase into an AI-native
> environment** — to test agentic workflows, and to eventually contribute high-confidence
> improvements back to the [Eclipse Foundation](https://www.eclipse.org/) and the MBSE community.

---

## 🔬 The Experiment

**Papyrus Web++** (pronounced *"Papyrus Web Plus Plus"*) is an R&D project by researchers at **CEA LIST**. It is a
**fork of [Eclipse Papyrus Web](https://gitlab.eclipse.org/eclipse/papyrus/org.eclipse.papyrus-web)** — the web-based UML modeling
platform built on [Eclipse Sirius Components](https://www.eclipse.org/sirius/sirius-web.html) —
used as a testbed for making a real, legacy systems-engineering codebase legible and safe for AI coding agents.

We are actively:

1. **Understanding the transformation** — which metadata formats, guardrails, and developer tools actually
   work (and which don't) when introducing AI agents to a complex existing enterprise system.
2. **Building the foundation** — the AI-native context, persistent memory, developer guides, and automated
   verification layers agents need to operate safely.
3. **Paving the way for agentic DevOps** — so a **Verified-by-Humans agentic system** can help build features,
   maintain the codebase, and eventually contribute trusted enhancements upstream.

> 🌱 **Current status:** the `ai/` knowledge layer was bootstrapped via `/cold-start` on **2026-06-29**.
> Every entry is `[inferred]` until a human audits it to `[verified]` — agents must treat any
> un-audited module as **frozen**. This is intentional: the layer fails *cautious*, not *confident*.

---

## ❄️ What's ours vs. what's frozen

This is a fork, so the **single most important rule** is knowing what you may touch. All inherited
upstream code is **frozen**; our experimental work is isolated to `ai/`, `scripts-pwpp/`, and any new modules.

```
papyrus-web-plus-plus/
├── ai/                      🧠 OURS — AI knowledge layer (guide · analysis · lab)
├── scripts-pwpp/            🛠️ OURS — dev tooling & guardrails (Windows PowerShell)
├── backend/                 ❄️ frozen upstream — Java 21 · Spring Boot · Sirius Web
├── frontend/                ❄️ frozen upstream — React 18 · Vite 8 · MUI 7 · Turbo
├── integration-tests/       ❄️ frozen upstream — Playwright & Cypress E2E
└── pom.xml · package.json · docker-compose.yml   ❄️ upstream
```

---

## 🤖 AI Agent Context Engine

For agents working in this repo: **don't guess or crawl the tree.** Start from the knowledge layer in
[`ai/`](ai/INDEX.md), which maps responsibilities, stack boundaries, and module stability.

| Doc | What it gives you |
|---|---|
| 🧭 [`ai/INDEX.md`](ai/INDEX.md) | Role → path manifest (start here) |
| 🌐 [`ai/guide/PROJECT_OVERVIEW.md`](ai/guide/PROJECT_OVERVIEW.md) | Workspace context, scope, glossary |
| 🏛️ [`ai/guide/ARCHITECTURE.md`](ai/guide/ARCHITECTURE.md) | System layers, the GraphQL seam, invariants |
| 🗺️ [`ai/guide/MODULE_MAP.md`](ai/guide/MODULE_MAP.md) | Directory responsibilities + **stability** (`frozen` / `ours`) |
| 📐 [`ai/guide/CONVENTIONS.md`](ai/guide/CONVENTIONS.md) | How to write code that fits |
| 📦 [`ai/analysis/FEATURE_CATALOG.md`](ai/analysis/FEATURE_CATALOG.md) | Feature → files index |
| 🧩 [`ai/analysis/diagrams/`](ai/analysis/diagrams/) | Mermaid: package deps, domain core, frontend↔backend seam |

The layer is tool-agnostic — Claude Code, Cursor, Copilot, and Windsurf all read the same maps
(see [`AGENTS.md`](AGENTS.md)).

---

## ⚡ Core Paradigms

New features are built spec-first, with automated feedback loops — so structure is correct *before* code is written.

* **🎯 Specification-Driven Development (SDD)** — features start as a spec in
  [`ai/lab/specs/`](ai/lab/specs/), then implemented as strict types and schema definitions before operational code.
* **🧪 Evaluation-Driven Development (EDD)** *(goal)* — guardrails and evaluators that run during
  development and CI to give agents and humans immediate, contract-level feedback.

---

## 🧱 AI-Native Guardrails (what actually exists today)

* **Knowledge-layer verification** — Run `node install.mjs verify . --strict` (or equivalent verification script) to ensure that no file paths or references in the `ai/` knowledge documents are broken, preventing knowledge drift.
* **Surgical Diff enforcement** — Coding agents are constrained by the [AGENTS.md](AGENTS.md) rules to keep edits surgical and respect stability boundaries (no touch zones on upstream code).

---

## 🚀 Quick Start

> **Windows-first.** The `scripts-pwpp/` launchers are PowerShell. Prerequisites: **Java 21**,
> **Node 22.16.0** (via [fnm](https://github.com/Schniz/fnm)), **Docker Desktop**, and a GitHub PAT with
> `read:package` (the Maven build pulls dependencies from GitHub Packages).
> Full walkthrough: open [`scripts-pwpp/README.md`](scripts-pwpp/README.md).

```powershell
# First time on a machine — check deps, build, and run everything
.\scripts-pwpp\setup-dev.ps1 -ConfigureAuth

# Daily — start database → backend → frontend
.\scripts-pwpp\start-dev.ps1

# Stop everything (DB container is paused, not deleted)
.\scripts-pwpp\stop-dev.ps1
```

| Service | URL / Location |
|---|---|
| 🖥️ Frontend (the app) | <http://localhost:5173> |
| ⚙️ Backend API | <http://localhost:8080> |
| 🗄️ PostgreSQL | `localhost:5439` (`dbuser` / `dbpwd`) |

### Easiest: double-click (no terminal)

In Windows Explorer, open the `scripts-pwpp` folder and double-click:

* **`setup.bat`**: First-time setup (checks toolchain, configures GitHub credentials, builds).
* **`run.bat`**: Starts the database, backend, and frontend dev servers.
* **`stop.bat`**: Safely stops all running services.

---

## 🛠️ Build & Test (manual)

For standard manual operations:

```powershell
# Rebuild the entire project using our helper script (skips tests)
.\scripts-pwpp\build-all.ps1

# Rebuild and run backend tests
.\scripts-pwpp\build-all.ps1 -WithTests

# Auto-format codebase to pass linting
.\scripts-pwpp\build-all.ps1 -FixFormat
```

Or run manual steps per component:

```bash
# Backend build (at repo root)
mvn clean verify -DskipTests

# Frontend build (in frontend/ folder)
npm ci && npm run build
```

---

## ⚖️ Licensing & Attribution

This fork is maintained by researchers at **CEA LIST** under the **Eclipse Public License v2.0 (EPL-2.0)** —
the same license as upstream [Eclipse Papyrus Web](https://gitlab.eclipse.org/eclipse/papyrus/org.eclipse.papyrus-web). See [`LICENSE`](LICENSE).

> *UML® is a trademark owned by the OMG ([guidelines](https://www.omg.org/legal/tm_guidelines.htm)).
> All credit for the underlying platform goes to the Eclipse Papyrus Web contributors at Obeo and CEA.*

<br>

---

<details>
<summary><b>🗂️ Click to Expand: The Papyrus-Web Project Readme (from upstream)</b></summary>

---

# Papyrus Web

Papyrus web is a web UML modeler to easily create UML diagrams directly from a web browser. It is based on [Eclipse Sirius Web](https://www.eclipse.org/sirius/sirius-web.html).

![Papyrus Web](PapyrusScreenshot.png)

This repository, `papyrus-web` is a mono repo containing both frontend and backend components.

To test *Papyrus Web* you have two possible options:

1. If you just want to run an already built version of Papyrus Web, follow [the Quick Start](#quick-start).
2. If you want to *build* the application yourself, follow [the complete Build instructions](#building).

<a id="quick-start"></a>

## Quick Start

### Running released application with docker compose

To run the application using docker compose, it is necessary to have [Docker Compose](https://docs.docker.com/compose/install/) installed. Then, run:

**On Linux terminal**

```sh
echo '
services:
  database:
    image: postgres:15
    environment:
      POSTGRES_DB: postgres
      POSTGRES_USER: test_username
      POSTGRES_PASSWORD: test_password
  papyrus-web:
    image: eclipsepapyrus/papyrus-web:latest
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://database/postgres
      SPRING_DATASOURCE_USERNAME: test_username
      SPRING_DATASOURCE_PASSWORD: test_password
      SIRIUS_COMPONENTS_CORS_ALLOWEDORIGINPATTERNS: "*"  
    depends_on:
      - database
' | docker compose -f - up
```

**On Windows with PowerShell**

```powershell
@'
services:
  database:
    image: postgres:15
    environment:
      POSTGRES_DB: postgres
      POSTGRES_USER: test_username
      POSTGRES_PASSWORD: test_password
  papyrus-web:
    image: eclipsepapyrus/papyrus-web:latest
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://database/postgres
      SPRING_DATASOURCE_USERNAME: test_username
      SPRING_DATASOURCE_PASSWORD: test_password
      SIRIUS_COMPONENTS_CORS_ALLOWEDORIGINPATTERNS: "*"
    depends_on:
      - database
'@ | docker compose -p papyrusweb -f - up
```

The application is available at <http://localhost:8080>

> **NOTE:** The repository provides a `docker-compose.yml` and a `env.template` environment file, if the user wants to run it after cloning the repository. It may be changed to use different ports or even a different image.

### Running the compiled application jar

If you want a quick overview of how Papyrus Web looks and feels like without building the sample application yourself, you will simply need:

* Java 21
* Docker, or an existing PostgreSQL 15 (or later) installation with a DB user that has admin rights on the database (those are needed by the application to create its schema on first startup).

To actually run the application:

1. Papyrus Web uses PostgreSQL for its database. For development or local testing, the easiest way is to start a PostgreSQL instance using Docker.

   **Docker command:**

   ```sh
   docker run -p 5439:5432 --name papyrus-web-postgres \
                               -e POSTGRES_USER=dbuser \
                               -e POSTGRES_PASSWORD=dbpwd \
                               -e POSTGRES_DB=papyrus-web-db \
                               -d postgres
   ```

   > **WARNING:** This may take a while the first time you run this as Docker will first pull the PostgreSQL image.
   >
   > If you do not have Docker or want to use an existing PostgreSQL installation, adjust the command-line parameters below and make sure the DB user has admin rights on the database; they are needed to automatically create the DB schema.

2. Start the application:

   ```sh
   java -jar papyrus-web-{$version}.jar \
             --spring.datasource.url=jdbc:postgresql://localhost:5439/papyrus-web-db \
             --spring.datasource.username=dbuser \
             --spring.datasource.password=dbpwd \
             --spring.liquibase.change-log=classpath:db/changelog/papyrus-web.db.changelog.xml
   ```

3. Point your browser at <http://localhost:8080> and enjoy!
   > **WARNING:** The initial version of Papyrus Web has some known issues with Firefox. It is recommended to use a Chrome-based browser until these are fixed.

   > **NOTE:** Do not forget to stop the PostgreSQL container once you are done: `docker kill papyrus-web-postgres`. Note that this will remove all the data you have created while testing the application.

## Building

### Requirements

To build the application yourself you will need the following tools:

* Git and a GitHub account
* To build the backend components:
  * [Java 21](https://adoptium.net/temurin/releases/)
  * [Apache Maven 3.9.9](https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/)
  * [Docker](https://www.docker.com/) must be installed and running for some of the backend components tests to run.
* To build the frontend components:
  * LTS versions of [Node and NPM](https://nodejs.org/): in particular, Node >= 22.16.0 is required along with npm >= 10.9.2.

> **WARNING:** Note that there are issues with npm under Windows Subsystem for Linux (WSL). If you use WSL and encounter error messages like *"Maximum call stack size exceeded"* when running NPM, switch to plain Windows where this should work.
>
> **For Windows users:** Due to the Maximum Path Length Limitation of Windows, you may exceed the limit of 260 characters in your PATH. To remove this limitation, apply [this procedure](https://learn.microsoft.com/en-us/windows/win32/fileio/maximum-file-path-limitation?tabs=powershell) with this command line in PowerShell (`New-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem" -Name "LongPathsEnabled" -Value 1 -PropertyType DWORD -Force`), then activate the longpath option in Git in a command line with administrator rights (`git config --system core.longpaths true`).

### GitHub Access Token

The backend of *Papyrus Components* depends on [`sirius-emf-json`](https://github.com/eclipse-sirius/sirius-emf-json) and [`sirius-components`](https://github.com/eclipse-sirius/sirius-components), which are published as Maven artifacts in *GitHub Packages*.
To build `papyrus-web` locally, you need a *GitHub Access Token* so that Maven can download the `sirius-emf-json` artifacts.

1. Create a personal token with a scope of `read:package` by following [the GitHub documentation](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token#creating-a-personal-access-token-classic) if you do not have one already.
   > **WARNING:** Once generated, a token cannot be displayed anymore, so make sure to copy it in a secure location.
2. Create or edit `$HOME/.m2/settings.xml` to tell Maven to use this token when accessing the Sirius EMF JSON repository:

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
     <servers>
       <server>
         <id>github-sirius-emfjson</id>
         <username>$GITHUB_USERNAME</username>
         <password>$GITHUB_ACCESS_TOKEN</password>
       </server>
       <server>
         <id>github-emfjson</id>
         <username>$GITHUB_USERNAME</username>
         <password>$GITHUB_ACCESS_TOKEN</password>
       </server>
       <server>
         <id>github-papyrus-sirius-uml2</id>
         <username>$GITHUB_USERNAME</username>
         <password>$GITHUB_ACCESS_TOKEN</password>
       </server>
       <server>
         <id>papyrus-uml-services</id>
         <username>$GITHUB_USERNAME</username>
         <password>$GITHUB_ACCESS_TOKEN</password>
       </server>
       <server>
         <id>github-sirius-components</id>
         <username>$GITHUB_USERNAME</username>
         <password>$GITHUB_ACCESS_TOKEN</password>
       </server>
     </servers>
   </settings>
   ```

   Be sure to replace `$GITHUB_USERNAME` with your GitHub user id, and `$GITHUB_ACCESS_TOKEN` with the value of your access token generated in the previous step.

   > **IMPORTANT:** The `id` used in your `settings.xml` *must* be the ones mentioned above to match what is used in the POMs.
3. Create or edit `$HOME/.npmrc` and add the following line:

   ```
   //npm.pkg.github.com/:_authToken=$GITHUB_ACCESS_TOKEN
   ```

   Again, be sure to replace `$GITHUB_ACCESS_TOKEN` with the value of your access token.

### Build Papyrus web application

Build steps:

1. Clone the Papyrus Web repository `papyrus-web`
2. Build the frontend (from the `frontend` subfolder of Papyrus web main location):

   ```sh
   npm ci
   npm run build
   ```

   > **NOTE:** In case of npm ERR! Lifecycle script `format-lint` failed with error, try to run from the frontend subfolder the following command: `npx prettier --write .` (don't forget the final dot)
3. Install the frontend artifacts as static resource to be served by the backend. From the root directory of the repository:

   ```sh
   mkdir -p backend/papyrus-web-frontend/src/main/resources/static
   cp -R frontend/papyrus-web/dist/* backend/papyrus-web-frontend/src/main/resources/static
   ```

4. Build the backend (from the `backend` subfolder of Papyrus web main location):

   ```sh
   mvn clean verify
   ```

   or to ignore the basic tests:

   ```sh
   mvn clean verify -DskipTests
   ```

   or to launch all tests (take around 7 hours):

   ```sh
   mvn clean verify -DallTests
   ```

The result is a ready-to-run, Spring Boot "fat JAR" in `backend/papyrus-web/target/papyrus-web-<VERSION>.jar`. Refer to the instructions in the "Quick Start" section above to launch it.

### Development environment

Here are instructions that a new Papyrus Web developer could follow in order to set up their development environment.

> **NOTE:** The setup of the GitHub token is required for setting up the backend and frontend (see above).

#### Backend setup

1. Download and install [Spring Tool Suite (STS)](https://spring.io/tools) for Eclipse.
2. Set up preferences:
   * Uncheck *Maven/Automatically update Maven projects configuration*
   * Set *Maven/'Errors/Warnings'/Plugin execution not covered by lifecycle configuration* to "ignore"
3. Clone Papyrus web repositories:
   * `papyrus-web` repo
   * `Papyrus Domain Services` repo
4. Import Papyrus web projects in the workspace:
   * From the `papyrus-web` repository, import all Eclipse projects located in the `backend` folder.
   * From the `Papyrus Domain Services` repository, import the following projects:
     * `org.eclipse.papyrus-domainservices/plugins/org.eclipse.papyrus.uml.domain.services`
     * `org.eclipse.papyrus-domainservices/plugins/org.eclipse.papyrus.uml.domain.services.test`
     * `org.eclipse.papyrus-domainservices/releng/org.eclipse.papyrus.uml.domain.services.resources`
     * `org.eclipse.papyrus-domainservices/releng/org.eclipse.papyrus.uml.domain.services.releng.target`

> **NOTE:** In order to use the project `org.eclipse.papyrus-domainservices/plugins/org.eclipse.papyrus.uml.domain.services` in your runtime you need to add the following line in `org.eclipse.papyrus-domainservices/plugins/org.eclipse.papyrus.uml.domain.services/.classpath`. This change should not be committed.
>
> ```xml
> <classpathentry kind="output" path="target/classes"/>
> ```

For Windows users, please set your git configuration to:

* `git config core.autocrlf true`
* `git config core.eol lf`
* `git config user.name "$FirstName $SecondName"`
* `git config user.email "$email"`

> **WARNING:** Please ensure that the email used in your commit and in your GitHub account is the one used to sign the Eclipse CLA. If you have not signed the [Eclipse CLA](https://www.eclipse.org/legal/ECA.php), please do before making any contribution.

1. Set up the target platform:
   * Open the `papyrusSiriusServices.target` file located in the `org.eclipse.papyrus.uml.domain.services.releng.target` plugin and wait for the target platform to be resolved.
   * At the top right corner of the Target Definition editor, click on the **Active Target Platform** link. This link should be replaced by *Reload Target Platform* once it is done.
2. Set up Checkstyle addon:
   * Install the Checkstyle addon with the following update site:

     ```
     https://checkstyle.org/eclipse-cs-update-site
     ```

   * Import the project `backend/papyrus-web-resources` into your workspace. This project contains the checkstyle configuration for *Papyrus Web*. All projects are configured to point to this configuration file:

     ```
     backend/papyrus-web-resources/checkstyle/CheckstyleConfiguration.xml
     ```

3. Set up the code formatter:
   * Window > Preferences > Java > Code Style > Formatter > Import...
   * Select the following file from the Papyrus Web local repository:

     ```
     backend/papyrus-web-resources/editor/formatter.xml
     ```

4. Set up code templates for new files/types:
   * Window > Preferences > Java > Code Style > Clean Up > Import...
   * Select the following file from the Papyrus Web local repository:

     ```
     backend/papyrus-web-resources/editor/CleanupProfile.xml
     ```

5. Set up clean up:
   * Window > Preferences > Java > Code Style > Code Templates
   * Select the following file from the Papyrus Web local repository:

     ```
     backend/papyrus-web-resources/editor/
     ```

6. Install Target Platform Definition DSL (Optional):
    * Help > Install New Software...
    * Use the following update site URL:

      ```
      https://download.eclipse.org/cbi/updates/tpd/nightly/latest/
      ```

7. Create Launch Configuration for Papyrus web server:
    * From the Debug toolbar, open Debug Configurations...
    * Select Spring Boot App
    * Click on the New icon to create a Spring Boot launch configuration
    * Name this launch configuration (e.g. `Papyrus web backend`)
    * Choose `papyrus-web` in the project dropdown
    * Search and select `org.eclipse.papyrus.web.PapyrusWeb` as the Main type
    * Choose the `dev` profile in the Profile dropdown
    * In the Arguments tab, add the following VM argument:

      ```
      -Dsirius.components.cors.allowedOriginPatterns=*
      ```

    * Click on Apply to save the launch configuration

#### Frontend setup

1. Open the `frontend` folder in VSCode.
2. Install dependencies using `npm install` in the terminal.

#### Launch Application as a developer

1. Run database docker image (see PostgreSQL Docker command above).
2. Launch the backend from STS.
3. Launch the frontend using `npm start`.

### Building and running with docker compose

To build and run the application using docker compose, perform the following actions:

1. Create a GitHub User and Access Token at <https://github.com/settings/tokens> with the `read:package` scope. Finally, create two environment variables with the username and generated token:

   ```sh
   export GITHUB_USERNAME=<replace by your user name>
   export GITHUB_AUTH_TOKEN=<replace by your access token>
   ```

2. Create a `.env` environment file from the `env.template`. This file contains the environment variables for configuring the services. The default values may be changed:

   ```sh
   cp env.template .env
   ```

3. Build the image and run all the services. This command builds the PapyrusWeb image, pulls the database image, and instantiates both services. The first execution takes several minutes to compile the application. Subsequent executions use the locally compiled image:

   ```sh
   docker compose up
   ```

The application is available at <http://localhost:8080>

## License

Everything in this repository is Open Source. Except when explicitly mentioned otherwise (e.g. for some resources like images), the license is Eclipse Public License v 2.0.

## Coding rules

### Headers

**File Header**

The header of each file should contain the following copyright block:

```java
/*****************************************************************************
 * Copyright (c) $Years $Authors.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  $Contributor - Initial API and implementation
 *****************************************************************************/
```

* `$Years`: Either the year of the file creation or the year of the file creation, a comma, and the year of the last modification.

* `$Authors`: List of names of all the owners separated by a comma.
* `$Contributor`: Name of the contributor.

For instance, a file contributed by Obeo funded by CEA LIST project should contain the following header:

```java
/*****************************************************************************
 * Copyright (c) 2023 CEA LIST
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *****************************************************************************/
```

**Class Type Header**

```java
/**
 * $Javadoc
 *
 * @author $Name
 */
```

* `$Javadoc`: The Javadoc description of the class.

* `$Name`: Your first name and second name with capitalized first letters.

> **WARNING:** Add the `-Duser.name=$Name` in the file `SpringToolSuite4.ini`. (e.g., `-Duser.name=Arthur Daussy`).

For instance, a class contributed by Arthur should have the following header:

```java
/**
 * Some details about the class.
 *
 * @author Arthur Daussy
 */
```

</details>
