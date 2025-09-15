# xAmplify PRM — Backend (Spring Boot / Java 8)

> **Server-side APIs** for the open‑source PRM: partner onboarding, deals/opportunities, MDF, content, dashboards, and more.

- Runtime: **Java 8 (JDK 1.8)**
- Build: **Maven 3.6.3**
- Database: **PostgreSQL 12**
- DB Client: **DBeaver**
- App Server: **Apache Tomcat 8.5**
- Main config: **xamplify-prm/xamplify-prm-api/src/main/resources/application.properties**
- DB config: **xamplify-prm/xamplify-prm-api/src/main/resources/config/config-production.properties**
- Default API base: ``

> **MANDATORY:** To use the **Content Module** and send **Onboarding/notification emails**, you **must** provide **AWS credentials** and **SMTP settings** in `application.properties` (and the correct DB settings in `config-production.properties`).

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Step‑by‑Step Setup](#step-by-step-setup)
  - [1) Clone the repository](#1-clone-the-repository)
  - [2) Add Apache Tomcat 8.5](#2-add-apache-tomcat-85)
  - [3) Database setup in DBeaver (PostgreSQL 12)](#3-database-setup-in-dbeaver-postgresql-12)
  - [4) Configure ](#4-configure-applicationproperties--config-productionproperties)[`application.properties`](#4-configure-applicationproperties--config-productionproperties)[ & ](#4-configure-applicationproperties--config-productionproperties)[`config-production.properties`](#4-configure-applicationproperties--config-productionproperties)
  - [5) Build the WAR with Maven](#5-build-the-war-with-maven)
  - [6) Deploy to Apache Tomcat 8.5](#6-deploy-to-apache-tomcat-85)
  - [7) Switch runtime profile to ](#7-switch-runtime-profile-to-production)[**production**](#7-switch-runtime-profile-to-production)
  - [8) Verify / Smoke test](#8-verify--smoke-test)
- [Running tests](#running-tests)
- [Troubleshooting](#troubleshooting)
- [Security Notes](#security-notes)
- [Example files to include in the repo](#example-files-to-include-in-the-repo)

---

## Prerequisites

Install these before you start:

- **Java 8 (JDK 1.8)**
- **Maven 3.6.3**
- **PostgreSQL 12**
- **DBeaver** (DB GUI)
- **Apache Tomcat 8.5**
- **FFmpeg / FFprobe** (for media processing in the Content Module)
- **Git**

**Downloads (Windows):**

- **FFmpeg / FFprobe:** Use an official Windows build (includes both `ffmpeg.exe` and `ffprobe.exe`).
  - Windows builds via FFmpeg site → **gyan.dev**: [https://www.gyan.dev/ffmpeg/builds/](https://www.gyan.dev/ffmpeg/builds/)
  - Alternative: **BtbN builds**: [https://github.com/BtbN/FFmpeg-Builds/releases](https://github.com/BtbN/FFmpeg-Builds/releases)
  - After extracting, add the `bin` folder to your **PATH** (e.g., `C:fmpegin`).
    ```powershell
    # PowerShell (User PATH)
    [Environment]::SetEnvironmentVariable('Path', $env:Path + ';C:\ffmpeg\bin', 'User')
    ffmpeg -version
    ffprobe -version
    ```
- **Apache Tomcat 8.5:**
  - **Windows Service Installer / ZIP** (8.5.x): [https://archive.apache.org/dist/tomcat/tomcat-8/](https://archive.apache.org/dist/tomcat/tomcat-8/)
    - Example version bin directory: [https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.100/bin/](https://archive.apache.org/dist/tomcat/tomcat-8/v8.5.100/bin/)
  - **Official Setup Docs (8.5):** [https://tomcat.apache.org/tomcat-8.5-doc/setup.html](https://tomcat.apache.org/tomcat-8.5-doc/setup.html)
- **DBeaver Community:** [https://dbeaver.io/download/](https://dbeaver.io/download/)
- **PostgreSQL (Windows installers):** [https://www.postgresql.org/download/windows/](https://www.postgresql.org/download/windows/)

---

## Step‑by‑Step Setup

### 1) Clone the repository

```bash
git clone https://github.com/xamplify/xAmplify-prm-core
```

> Project layout assumed below:
>
> ```
> xamplify-prm/
> └─ xamplify-prm-api/
>    └─ src/main/resources/
>       ├─ application.properties
>       └─ config-production.properties
> ```

---

### 2) Add Apache Tomcat 8.5

- **Standalone:** Install Tomcat 8.5 and set `JAVA_HOME` (JDK 1.8). Note `CATALINA_BASE`/`CATALINA_HOME`.
- **IDE (Eclipse/IntelliJ):** Add a **Tomcat 8.5** server configuration and point it to your local Tomcat install.

> You will deploy a WAR named `` so the app is served at `/xamplify-prm-api`.

---

### 3) Database setup in DBeaver (PostgreSQL 12)

1. Open **DBeaver** → **New Database Connection** → select **PostgreSQL**.
2. Create (or restore) the database you will use.
   - **Recommended name/connection:** `xamplify-prm`
   - If you have a provided backup file, restore it now.
3. Verify the hostname, port, DB name, username & password — you will place these in `config-production.properties` next.

---

### 4) Configure `application.properties` & `config-production.properties`

Create/edit the following files, replacing all placeholders (`XXXXX`, usernames, passwords, keys).

#### `xamplify-prm/xamplify-prm-api/src/main/resources/application.properties`

```properties
###############################
# Amazon S3 & CloudFront (Content Module) — REQUIRED
###############################
amazon.bucket.name=XXXXX
amazon.access.id=XXXXX
amazon.secret.key=XXXXX
amazon.cloudfront.url=XXXXX
amazon.cloudfront.enabled=false

###############################
# Mail (SMTP) — REQUIRED
# Used for onboarding emails & content notifications
###############################
mail.host=smtp.gmail.com
mail.port=587
mail.username=xxx@xxx.com
# 16-character app password from Google (Gmail App Password)
mail.password=xxxx xxxx xxxx xxxx
mail.from.name=xAmplify-PRM
# Replace with your real sender email
mail.from.email=no-reply@example.com

###############################
# Google Captcha (optional)
###############################
google.captcha.verification.url=https://www.google.com/recaptcha/api/siteverify
google.captcha.site.key=XXXXX
google.captcha.secret.key=XXXXX

###############################
# Google Maps (optional)
###############################
google.maps.url=https://maps.googleapis.com/maps/api/geocode/json?address=final_address&key=api_key
google.maps.api.key=XXXXX

###############################
# Default "From" Email
###############################
email=no-reply@example.com

###############################
# Spring Profile
# Tomcat 8.5: change this to 'production' for deployment
###############################
spring.profiles.active=dev
```

#### `xamplify-prm/xamplify-prm-api/src/main/resources/config/config-production.properties`

```properties
###############################
# Database Configuration (production)
###############################
db.name=xamplify-dev
db.host=127.0.0.1
db.port=5432

# JDBC Connection
jdbc.url=jdbc:postgresql://${db.host}:${db.port}/${db.name}
# Replace with your DB username/password
jdbc.username=postgres
jdbc.password=root

# Driver & Dialect
jdbc.driver.className=org.postgresql.Driver
jdbc.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

> **IAM tip:** Grant only the needed S3 permissions (e.g., `s3:PutObject`, `s3:GetObject`, `s3:ListBucket`) on the **specific** content bucket/prefix.

---

### 5) Build the WAR with Maven

From the project root (or the `xamplify-prm-api` module), run:

```bash
mvn clean install
```

The build produces a WAR under:

```
xamplify-prm/xamplify-prm-api/target/*.war
```

---

### 6) Deploy to Apache Tomcat 8.5

1. Stop Tomcat if it’s running.
2. Copy the built WAR into Tomcat’s `webapps/` folder and **rename it to** `xamplify-prm-api.war`:
   ```
   CATALINA_BASE/webapps/xamplify-prm-api.war
   ```
3. Start Tomcat. It will expand the WAR to:
   ```
   CATALINA_BASE/webapps/xamplify-prm-api/
   ```
4. Confirm the app is reachable at ``.

---

### 7) Switch runtime profile to **production**

For Tomcat deployment, ensure the app runs with the **production** profile:

- **Option A (in file):** set in `application.properties`
  ```properties
  spring.profiles.active=production
  ```
- **Option B (JVM arg):** set via Tomcat service or `setenv.sh`
  ```
  -Dspring.profiles.active=production
  ```

Make sure `config-production.properties` points to your production DB and the DB is reachable from the Tomcat host.

---

### 8) Verify / Smoke test

- Base URL: ``
- Use **Postman** (or curl) to call a simple endpoint. Example (if a health endpoint exists):
  ```bash
  curl -i http://localhost:8080/xamplify-prm-api/login || true
  ```
- Validate authentication flows and that emails send successfully (check your SMTP provider logs). Ensure content links render using your S3/CloudFront URL.

---

## Running tests

```bash
mvn test
```

---

## Troubleshooting

- **DB connection errors**

  - Re-check `jdbc.url`, `jdbc.username`, `jdbc.password` in ``.
  - Ensure PostgreSQL is running on port **5432** and reachable from the Tomcat host.
  - Test the connection in **DBeaver** with the same credentials.

- **Emails not sending**

  - Verify `mail.host`, `mail.port`, `mail.username`, `mail.password` in ``.
  - For Gmail, ensure a **16-character App Password** and `mail.port=587` with STARTTLS.
  - Check your mail provider logs (e.g., Gmail/SES) for blocks or throttling.

- **Content links not loading**

  - Confirm `amazon.bucket.name`, `amazon.cloudfront.url`, and credentials in ``.
  - Ensure S3 objects are accessible via your CloudFront/S3 policy (public read or signed URLs as intended).

- **Wrong context path**

  - Ensure the WAR name is `` so the API is served at `/xamplify-prm-api`.

- **Profile still on dev**

  - Set `spring.profiles.active=production` (file or JVM arg) and restart Tomcat.

---

## Security Notes

- **Do not commit secrets.** Keep real credentials out of version control.
- Use environment variables/Tomcat JVM args or a secrets manager in production.
- Keep AWS IAM permissions **least‑privilege** for the content bucket.

---

## Example files to include in the repo

- `xamplify-prm/xamplify-prm-api/src/main/resources/application.properties.example` — copy of the properties above with placeholders.
- `xamplify-prm/xamplify-prm-api/src/main/resources/config/config-production.properties.example` — DB settings with placeholders.

> Developers should copy the `*.example` files to the real filenames locally and fill in environment‑specific values.

