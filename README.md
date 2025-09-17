# xAmplify PRM — Backend (Spring Boot / Java 8)

> **Server-side APIs** for the open-source PRM: partner onboarding, deals/opportunities, MDF, content(DAM,Tracks,Playbooks), dashboards, and more.

- Runtime: **Java 8 (JDK 1.8)**
- Build: **Maven 3.6.3**
- Database: **PostgreSQL 12**
- DB Client: **DBeaver**
- Main config: **xamplify-prm/xamplify-prm-api/src/main/resources/application.properties**
- DB config: **xamplify-prm/xamplify-prm-api/src/main/resources/config/config-production.properties**


> **MANDATORY:** To use the **Content Module** and send **Onboarding/notification emails**, you **must** provide **AWS credentials** and **SMTP settings** in `application.properties` (and the correct DB settings in `config-production.properties`).

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Step-by-Step Setup](#step-by-step-setup)
  - [1) Clone the repository](#1-clone-the-repository)
  - [2) Database setup in DBeaver (PostgreSQL 12)](#2-database-setup-in-dbeaver-postgresql-12)
  - [3) Configure ](#3-configure-applicationproperties--config-productionproperties)[`application.properties`](#3-configure-applicationproperties--config-productionproperties)[ & ](#3-configure-applicationproperties--config-productionproperties)[`config-production.properties`](#3-configure-applicationproperties--config-productionproperties)
  - [4) Build the WAR with Maven](#4-build-the-war-with-maven)
  - [5) Switch runtime profile to ](#5-switch-runtime-profile-to-production)[**production**](#5-switch-runtime-profile-to-production)
  - [6) Verify / Smoke test](#6-verify--smoke-test)
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
- **FFmpeg / FFprobe** (for media processing in the Content Module)
- **Git**
- **Docker** (for containerized deployment)

**Downloads (Windows):**

- **FFmpeg / FFprobe:**  
  [https://www.gyan.dev/ffmpeg/builds/](https://www.gyan.dev/ffmpeg/builds/)  
  [https://github.com/BtbN/FFmpeg-Builds/releases](https://github.com/BtbN/FFmpeg-Builds/releases)

- **DBeaver:** [https://dbeaver.io/download/](https://dbeaver.io/download/)

- **PostgreSQL:** [https://www.postgresql.org/download/windows/](https://www.postgresql.org/download/windows/)

- **Docker Desktop:** [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)

---

## Step-by-Step Setup

### 1) Clone the repository

```bash
git clone https://github.com/xamplify/xAmplify-prm-core
```

Project layout assumed below:

```
xamplify-prm/
└─ xamplify-prm-api/
   └─ src/main/resources/
      ├─ application.properties
      └─ config-production.properties
```

---

### 2) Database setup in DBeaver (PostgreSQL 12)

1. Open **DBeaver** → **New Database Connection** → select **PostgreSQL**.
2. Create (or restore) the database you will use.  
   Recommended name: `xamplify-prm`
3. Restore backup if provided:  
   ```
   xAmplify-prm-core/Database/xamplify-prm-schema.backup
   ```

Restore with pg_restore:

```bash
pg_restore -h localhost -U postgres -d xamplify-prm /path/to/xamplify-prm-schema.backup
```

---

### 3) Configure `application.properties` & `config-production.properties`

#### `application.properties`

```properties
amazon.bucket.name=XXXXX
amazon.access.id=XXXXX
amazon.secret.key=XXXXX
amazon.cloudfront.url=XXXXX
amazon.cloudfront.enabled=false

mail.host=smtp.gmail.com
mail.port=587
mail.username=xxx@xxx.com
mail.password=xxxx xxxx xxxx xxxx
mail.from.name=xAmplify-PRM
mail.from.email=no-reply@example.com

google.captcha.verification.url=https://www.google.com/recaptcha/api/siteverify
google.captcha.site.key=XXXXX
google.captcha.secret.key=XXXXX

google.maps.url=https://maps.googleapis.com/maps/api/geocode/json?address=final_address&key=api_key
google.maps.api.key=XXXXX

email=no-reply@example.com

spring.profiles.active=dev
```

#### `config-production.properties`

```properties
db.name=xamplify-dev
db.host=127.0.0.1
db.port=5432

jdbc.url=jdbc:postgresql://${db.host}:${db.port}/${db.name}
jdbc.username=postgres
jdbc.password=root

jdbc.driver.className=org.postgresql.Driver
jdbc.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

---

### 4) Build the WAR with Maven

```bash
mvn clean install
```

Artifact:

```
xamplify-prm/xamplify-prm-api/target/*.war
```

---

### 5) Switch runtime profile to **production**

- **Option A (file):**

```properties
spring.profiles.active=production
```

- **Option B (JVM arg):**

```
-Dspring.profiles.active=production
```

---

### 6) Verify / Smoke test

```bash
curl -i http://localhost:8080/xamplify-prm-api/login || true
```

---

---

## Running tests

```bash
mvn test
```

---

## Troubleshooting

- **DB connection errors** → check `jdbc.url`, `jdbc.username`, `jdbc.password`
- **Emails not sending** → verify SMTP config and Gmail App Password
- **Content links not loading** → check AWS S3/CloudFront setup
- **Profile still on dev** → set `spring.profiles.active=production`

---

## Security Notes

- Do **not** commit secrets (AWS keys, SMTP creds).
- Use environment variables or a secrets manager in production.
- Apply **least privilege** IAM permissions for S3.

---

## Example files to include in the repo

- `application.properties.example`
- `config-production.properties.example`

Developers copy them locally and fill in environment-specific values.
