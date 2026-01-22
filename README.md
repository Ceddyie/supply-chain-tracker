# Supply-Chain-Tracker

Bei diesem Projekt handelt es sich um eine studentische Projektarbeit im Bereich "Cloud Native Software Engineering" des Studiengangs Angewandte Informatik an der Hochschule Kaiserslautern.

Es besteht aus einer Microservice Anwendung zum **Erstellen und Tracken von Sendungen**.

**Live-Demo (Frontend / Cloud Run):** https://frontend-pc5vhdie3a-ew.a.run.app

---

## Überblick

Die Anwendung besteht aus:

- **Frontend**: React (Vite) + TypeScript + Tailwind  
- **API Gateway**: Spring Cloud Gateway (WebFlux) als zentraler Einstiegspunkt (`/api/**`)
- **Shipment Service**: Spring Boot + PostgreSQL (Persistenz von Sendungen & Checkpoints)
- **Tracking Service**: Spring Boot (Entgegennahme von Tracking-Updates & Publizieren via Pub/Sub)
- **Messaging**: Google **Pub/Sub** (lokal via Emulator, in der Cloud als Managed Service)
- **Auth**: **Firebase Authentication** + **Cloud Functions** (Custom Claims „role“)

> Lokal wird alles über Docker Compose gestartet: PostgreSQL, Pub/Sub Emulator, Firebase Auth/Functions Emulator sowie die Services & das Frontend.

---

## Features

### Nutzer-Features (Frontend)

- **Öffentliches Tracking** einer Sendung über Tracking-ID (`/track/:trackingId`)
- **Registrierung / Login** via Firebase Auth
- **Rollenbasierte UI**
  - **CUSTOMER**: Tracken von Sendungen (ohne Erstellung)
  - **SENDER**: Sendungen erstellen + eigene Sendungen ansehen (Dashboard + Statistiken)
  - **STATION**: Tracking-Updates für Sendungen senden (Status + optional Geo-Koordinaten)
  - **ADMIN**: im Frontend wie „SENDER“ behandelt (z. B. für Demo-Zwecke)

### Technische Features

- **API Gateway Routing**:
  - `/api/shipment/**` → Shipment Service  
  - `/api/tracking/**` → Tracking Service
- **Asynchrone Kommunikation** via Pub/Sub (Tracking-Updates als Events)
- **Persistenz** (PostgreSQL) inkl. Migrationen (Flyway) für Shipments/Checkpoints
- **CI/CD** via GitHub Actions (Build/Test pro Service + Deploy nach Cloud Run)

---

## Architektur (High-Level)

```text
                ┌─────────────────────────────┐
                │         Frontend            │
                │  React (Vite) + Firebase    │
                └──────────────┬──────────────┘
                               │ HTTPS
                               ▼
                       ┌───────────────┐
                       │  API Gateway   │
                       │ Spring Gateway │
                       └───────┬───────┘
               /api/shipment   │   /api/tracking
                     ▼         │         ▼
        ┌─────────────────┐    │   ┌─────────────────┐
        │ ShipmentService │    │   │ TrackingService │
        │  Spring Boot    │    │   │  Spring Boot    │
        │  + PostgreSQL   │    │   │   + Pub/Sub     │
        └───────┬─────────┘    │   └────────┬────────┘
                │              │            │
                │   Pub/Sub sub│            │ Pub/Sub topic
                └──────────────┴────────────┘
                         Google Pub/Sub
```

---

## Tech Stack

- **Backend:** Java 21, Spring Boot, Spring CLoud Gateway (Webflux), Flyway
- **Frontend:** React, Typescript, Vite, Tailwind, Axios, React Router
- **Auth:** Firebase Auth, Firebase Functions (Callable) für Rollen Claims
- **Local Dev:** Docker / Docker Compose, Pub/Sub Emulator, Firebase Emulator Suite
- **Cloud:** Google Cloud Run, Google Pub/Sub, Cloud SQL (PostgreSQL)

---

## Quickstart (lokal)

### Voraussetzungen

- Docker & Docker Compose
- (optional) zum Start der Services lokal ohne Docker: Node 20+ und Java 21

### Start

Im Projekt Root:
```text
  docker compose up --build
```

Danach sollten folgende Services erscheinen:
| Komponente    | URL                  |
| ------------- | -------------------- |
| Frontend      | http://localhost:5173|
| API Gateway   | http://localhost:8080|
| Shipment Service (direkt) | http://localhost:8081|
| Tracking Service (direkt) | http://localhost:8082|
| PostgreSQL | localhost:5432 (User/Pass: supplychain)|
| Pub/Sub Emulator | http://localhost:8085|
| Firebase Emulator UI | http://localhost:4000|
| Firebase Auth Emulator | http://localhost:9099|
| Firebase Functions Emulator | http://localhost:5001|

> Hinweis: Beim lokalen Start legt pubsub-init Topic und Subscription im Emulator an

---

## Nutzung (Demo-Flow)

### 1) Account erstellen
- Öffne http://localhost:5173/register
- Registriere dich als:
    - **CUSTOMER** (Standard) oder
    - **SENDER** (Checkbox "Business Account")

Nach der Registrierung kommst du zum Dashboard.

### 2) Sendung erstellen (SENDER)
- Dashboard → Create New Shipment
- Daten eingeben → "Create Shipment"
- Du erhältst eine Tracking-ID (Toast)

### 3) Tracking (öffentlich)
Öffne /track/<TRACKING_ID> oder nutze die "Track"-Eingabe auf Dashboard/Tracking-Page.

### 4) Tracking-Update senden (STATION)

Die UI bietet keinen direkten "STATION"-Register-Flow, die Rolle kann aber via Callable Function gesetzt werden.

Hinweis: In dieser Demo darf ein User seine Rolle für die eigene UID setzen (siehe functions/src/index.ts).
Für produktive Szenarien würde man Rollen i. d. R. ausschließlich serverseitig/adminseitig vergeben.

Danach:
-	Dashboard → **Send Tracking Update**
-	shipmentId (UUID) + Status + Message (+ optional Lat/Lng) eingeben
- Update wird gepublished; Shipment Service verarbeitet es und die Timeline im Tracking aktualisiert sich.

---

## Ändern der Rollen nach der Registrierung

Um nach der Registrierung die Rolle eines Users zu ändern, etwa um ihn als STATION-Account nutzen zu können, muss man die Firebase Emulator Suite nutzen. 
Hierzu folgt eine kleine Anleitung:

### 1) Firebase Emulator Suite öffnen

Nachdem alle Docker Container gestartet, wurden, lässt sich die Firebase Emulator Suite unter der Webadresse http://localhost:4000/ finden.
Diese sollte dann wie folgt aussehen:
<img width="1920" height="963" alt="image" src="https://github.com/user-attachments/assets/db034049-e44c-4731-9407-4f6311700610" />

### 2) Auth Emulator öffnen

In diesem Dashboard dann unter "Authentication emulator" den Button "Go to auth emulator" anklicken.
<img width="423" height="268" alt="image" src="https://github.com/user-attachments/assets/6faff4a7-072f-4b36-94b9-c859c431f1dd" />

### 3) User bearbeiten

Im dann erscheinenden Fenster den entsprechenden User suchen und die 3 Punkte am Ende der Zeile anklicken:
<img width="1258" height="113" alt="image" src="https://github.com/user-attachments/assets/d56fdaa7-014e-4e29-8397-422aa3f3f0b7" />

Und bei Custom claims den Wert von "role" ändern, etwa zu "STATION", "SENDER" oder "CUSTOMER":
<img width="520" height="109" alt="image" src="https://github.com/user-attachments/assets/7a5aff79-2201-4d26-8a05-faa9bdf05a56" />

Anschließend speichern und mit dem Account neu einloggen.

---

## API (über Gateway)

Basis: http://localhost:8080/api

### Shipment

- POST /shipment/create
  Body:
  ```code
  { "sender": "...", "receiver": "...", "receiverStreet": "...", "receiverCity": "...", "expectedDelivery": "ISO-8601" }
  ```
- GET /shipment - Liste "meiner" Shipments
- GET /shipment/{id} - Shipment per ID
- GET /shipment/track/{trackingId} - öffentliches Tracking (Timeline + Status)

### Tracking

- POST /tracking/update
  Body:
  ```code
  { "shipmentId": "uuid", "status": "IN_TRANSIT", "message": "...", "lat": 49.0, "lng": 7.0, "timestamp": "ISO-8601" }
  ```

> Das Frontend setzt automatisch ein Firebase ID Token als (`Authorization: Bearer <token>`) Header, sobald ein User eingeloggt ist.

---

## Konfiguration

### Docker Compose (lokal)

Die lokale Konfiguration ist in (`docker-compose.yml`) hinterlegt, u.a.:

-	**DB**: POSTGRES_USER/POSTGRES_PASSWORD/POSTGRES_DB
-	**Pub/Sub Emulator**: PUBSUB_EMULATOR_HOST, PUBSUB_TOPIC, PUBSUB_SUBSCRIPTION
-	**Gateway**: SHIPMENT_SERVICE_URL, TRACKING_SERVICE_URL, Firebase Emulator Settings
-	**Frontend Build-Args**: VITE_API_BASE_URL, VITE_USE_FIREBASE_EMULATOR, …

### Wichtige Environment-Variablen

#### API Gateway
- SHIPMENT_SERVICE_URL
-	TRACKING_SERVICE_URL
-	FRONTEND_URL (Prod CORS-Allow-Origin)

#### Shipment Service
-	DATABASE_URL / (Prod) Cloud SQL Variablen
-	PUBSUB_EMULATOR_HOST (lokal)
-	GCP_PROJECT_ID

#### Tracking Service
-	PUBSUB_EMULATOR_HOST (lokal)
-GCP_PROJECT_ID

#### Frontend (Build-Time)
-	VITE_API_BASE_URL
-	VITE_FIREBASE_PROJECT_ID
-	VITE_FIREBASE_API_KEY
-	VITE_FIREBASE_AUTH_DOMAIN
-	VITE_USE_FIREBASE_EMULATOR

---

## CI/CD

GitHub Actions Workflows (Auszug):
	-	shipment-service-ci.yml, tracking-service-ci.yml, api-gateway-ci.yml
    → Maven Build + Tests
	-	frontend-ci.yml
    → npm ci, Lint/Typecheck (optional), Build
	-	deploy-prod.yml
    → Build & Push Docker Images (Artifact Registry) + Deploy nach Cloud Run

Für das Deploy werden u.a. folgende Secrets benötigt:
	-	GCP_SA_KEY (Service Account JSON)
	-	FIREBASE_API_KEY
	-	(Cloud SQL) db-password als Secret Manager Secret

---

## Screenshots

Login:
<img width="1917" height="956" alt="image" src="https://github.com/user-attachments/assets/38ad111f-7884-46c5-9a87-b291edc0def9" />

Register:
<img width="1918" height="959" alt="image" src="https://github.com/user-attachments/assets/eed034bb-c7c2-4a5c-a43b-1f06d7e3d312" />

Dashboard (SENDER):
<img width="1918" height="959" alt="image" src="https://github.com/user-attachments/assets/00237d55-4729-4bc7-a210-efeda3809ff7" />

Tracking Timeline:
<img width="1917" height="957" alt="image" src="https://github.com/user-attachments/assets/25848ec5-fe1e-41cd-9daf-c69168089cbe" />

---

## Projektstruktur

```text
.
├─ docker-compose.yml
├─ firebase.json                 # Emulator-Konfig (Auth + Functions)
├─ functions/                    # Firebase Cloud Function (setUserRole)
└─ services/
   ├─ frontend/                  # React + Vite + Tailwind
   ├─ api-gateway/               # Spring Cloud Gateway
   ├─ shipment-service/          # Shipments + DB + Pub/Sub Subscriber
   └─ tracking-service/          # Tracking Updates + Pub/Sub Publisher
```

---

## Lizenz/Hinweise

Dieses Repository ist primär für Lern-/Projektzwecke gedacht. Für eine Produktion sollten u. a. Rollenvergabe, Security (Claims), Logging, Tracing und Rechtekonzepte härter abgesichert werden.

