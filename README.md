# Spring Starter

> Template de référence pour le développement d'applications **Spring Boot** modernes, sécurisées, testables et observables.

Ce projet fournit un socle backend réutilisable intégrant les principaux composants nécessaires au développement, aux tests, à la sécurité, à l'observabilité et à l'exécution locale d'une application Spring Boot.

> **Statut :** socle backend fonctionnel et validé.  
> Les évolutions restantes concernent principalement le CI/CD, Kubernetes, les dashboards et les alertes.

---

## Table des matières

- #vue-densemble
- #architecture
- #stack-technique
- #prérequis
- #quick-start
- #configuration
- #api-rest
- #pagination
- #gestion-des-conflits-de-version
- #base-de-données
- #tests-et-qualité
- #observabilité
- #sécurité
- #cicd
- #structure-du-projet
- #bonnes-pratiques

---

# Vue d'ensemble

Le template fournit notamment :

- une API REST documentée avec OpenAPI / Swagger ;
- une persistance PostgreSQL avec Spring Data JPA ;
- des migrations avec Liquibase ;
- une pagination et un tri avec Spring Data `Pageable` ;
- un cache Redis ;
- une authentification JWT via `springstarter-auth` ;
- une sécurisation avec Spring Security Resource Server ;
- des authorities `READ` et `WRITE` ;
- une gestion des conflits de modification avec JPA `@Version` ;
- de la validation avec Jakarta Bean Validation ;
- des tests unitaires et d'intégration ;
- une mesure de couverture avec JaCoCo ;
- des contrôles Spotless et ArchUnit ;
- une analyse SonarQube ;
- une observabilité avec OpenTelemetry, Micrometer, Prometheus, Tempo, Loki et Grafana ;
- une exécution locale avec Docker Compose.

---

# Architecture

L'authentification est séparée du backend métier.

```text
Client
  │
  │ Login / Refresh
  ▼
springstarter-auth
  │
  │ JWT signé RSA
  ▼
Client
  │
  │ Authorization: Bearer <token>
  ▼
springstarter
  ├── REST API
  ├── JWT validation
  ├── READ / WRITE
  ├── PostgreSQL
  ├── Redis
  └── Observabilité
```

`springstarter-auth` authentifie les utilisateurs et émet les JWT.

`springstarter` agit comme **Resource Server** et valide localement :

- la signature RSA ;
- l'issuer ;
- l'audience ;
- l'expiration ;
- les authorities.

En local, les deux applications utilisent le réseau Docker partagé :

```text
springstarter-network
```

---

# Stack technique

| Domaine | Technologie |
|---|---|
| Runtime | Java 25 |
| Framework | Spring Boot |
| Build | Maven |
| API | REST / OpenAPI / Swagger |
| Persistence | Spring Data JPA / Hibernate |
| Pagination | Spring Data Pageable / Page |
| Base de données | PostgreSQL |
| Migrations | Liquibase |
| Cache | Redis |
| Sécurité | Spring Security / JWT |
| Authentification | springstarter-auth |
| Validation | Jakarta Bean Validation |
| Tests | JUnit / Mockito / Testcontainers |
| Couverture | JaCoCo |
| Architecture | ArchUnit |
| Formatage | Spotless |
| Qualité | SonarQube |
| Observabilité | OpenTelemetry / Micrometer |
| Tracing | Tempo |
| Métriques | Prometheus |
| Logs | Loki |
| Visualisation | Grafana |
| Conteneurisation | Docker / Docker Compose |
| CI/CD | GitLab CI/CD |
| Orchestration | Kubernetes / AKS 🚧 |

---

# Prérequis

| Outil | Version |
|---|---|
| Java | 25 |
| Maven | 3.9+ |
| Docker | Version récente |
| Docker Compose | Version récente |
| curl | Version récente |
| jq | Version récente |

Vérification :

```bash
java --version
mvn --version
docker --version
docker compose version
```

---

# Quick Start

## 1. Démarrer springstarter-auth

Depuis le projet `springstarter-auth` :

```bash
docker compose -f docker/docker-compose.yml up -d
```

Le projet Auth fournit notamment :

- le service d'authentification ;
- PostgreSQL ;
- le réseau `springstarter-network`.

## 2. Construire springstarter

```bash
docker compose -f docker/docker-compose.yml build --no-cache
```

## 3. Démarrer le backend

```bash
docker compose -f docker/docker-compose.yml up -d --force-recreate
```

## 4. Vérifier les services

```bash
docker compose -f docker/docker-compose.yml ps
```

### Health Check

```text
http://localhost:8080/springstarter/actuator/health
```

### Swagger UI

```text
http://localhost:8080/springstarter/swagger-ui/index.html
```

### Grafana

```text
http://localhost:3000
```

### Prometheus

```text
http://localhost:9090
```

### SonarQube

```text
http://localhost:9000
```

---

# Configuration

La configuration est externalisée afin de permettre l'utilisation du même artefact sur plusieurs environnements.

Exemple :

```dotenv
# JWT
JWT_PUBLIC_KEY=classpath:certs/public-key.pem
JWT_ISSUER=http://localhost:8081/authstarter
JWT_AUDIENCE=springstarter-api

# PostgreSQL
SPRING_DATASOURCE_URL=jdbc:postgresql://shared-postgres:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SPRING_DATASOURCE_SCHEMA=mydb

# Redis
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379

# OpenTelemetry
OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4318
```

Les secrets ne doivent jamais être versionnés.

```text
.env                 ❌
private-key.pem      ❌
.env.example         ✅
public-key.pem       ✅
```

La clé privée RSA reste exclusivement dans `springstarter-auth`.

---

# API REST

La ressource d'exemple est disponible sous :

```text
/api/v1/samples
```

| Méthode | Endpoint | Description | Authority |
|---|---|---|---|
| POST | `/api/v1/samples` | Création | WRITE |
| GET | `/api/v1/samples/{id}` | Consultation | READ |
| GET | `/api/v1/samples` | Liste paginée | READ |
| PUT | `/api/v1/samples/{id}` | Modification | WRITE |
| DELETE | `/api/v1/samples/{id}` | Suppression | WRITE |

Principaux codes HTTP :

```text
200 OK
201 Created
202 Accepted
204 No Content
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
500 Internal Server Error
```

---

# Pagination

`GET /api/v1/samples` utilise Spring Data `Pageable`.

Les paramètres sont facultatifs :

| Paramètre | Défaut | Description |
|---|---:|---|
| `page` | `0` | Numéro de page |
| `size` | `20` | Nombre d'éléments |
| `sort` | `id,asc` | Tri |

Exemple :

```http
GET /api/v1/samples?page=0&size=5&sort=id,asc
```

Sans paramètre :

```http
GET /api/v1/samples
```

le backend utilise :

```text
page=0
size=20
sort=id,asc
```

Le format du tri est :

```text
property,direction
```

Exemples :

```text
id,asc
name,desc
createdAt,desc
```

La réponse contient les données dans `content` ainsi que les métadonnées de pagination :

```json
{
  "content": [],
  "number": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

Exemple avec curl :

```bash
curl \
  --header "Authorization: Bearer ${TOKEN}" \
  "http://localhost:8080/springstarter/api/v1/samples?page=0&size=5&sort=id,asc"
```

---

# Gestion des conflits de version

Les entités modifiables utilisent le verrouillage optimiste JPA :

```java
@Version
private Long version;
```

Le client conserve la version reçue lors de la lecture et la transmet lors du `PUT`.

Exemple :

```text
Client A lit version 3
Client B lit version 3

Client A modifie
→ version 4

Client B tente de modifier avec version 3
→ 409 Conflict
```

Une modification obsolète retourne :

```text
409 Conflict
```

Le client doit alors recharger la dernière version avant de poursuivre.

Hibernate reste responsable de l'incrément du champ `@Version`.

---

# Base de données

PostgreSQL est fourni en local par `springstarter-auth`.

Le backend le rejoint via :

```text
springstarter-network
```

Datasource :

```text
jdbc:postgresql://shared-postgres:5432/postgres
```

Schéma applicatif :

```text
mydb
```

Connexion :

```bash
docker exec -it authstarter-postgres psql -U postgres -d postgres
```

Puis :

```sql
SET search_path TO mydb;
```

## Liquibase

Toutes les évolutions du schéma doivent être réalisées avec Liquibase.

Les modifications manuelles du schéma sont à éviter afin de garantir la reproductibilité des environnements.

---

# Tests et qualité

## Tests

Tests unitaires :

```bash
mvn test
```

Build complet :

```bash
mvn clean verify
```

Les tests couvrent notamment :

- controllers ;
- services ;
- validation ;
- pagination ;
- persistance ;
- conflits de version ;
- erreurs fonctionnelles et techniques.

Les tests d'intégration utilisent **Testcontainers** avec les dépendances réelles nécessaires, notamment PostgreSQL.

## Pagination

Les tests vérifient notamment :

- le contenu des pages ;
- `page` et `size` ;
- `totalElements` ;
- `totalPages` ;
- les pages vides ;
- le passage du `Pageable` au repository.

## JaCoCo

Objectif minimal :

```text
80 %
```

Rapport :

```text
target/site/jacoco/index.html
```

## Spotless

Vérification :

```bash
mvn spotless:check
```

Correction :

```bash
mvn spotless:apply
```

## SonarQube

Analyse :

```bash
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=spring-starter \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token="$SONAR_TOKEN" \
  -Dsonar.scm.disabled=true
```

Le token SonarQube ne doit jamais être versionné.

---

# Observabilité

Le socle utilise :

```text
                ┌────────► Tempo
                │
springstarter ──┼────────► Loki
                │
                └────────► Prometheus
                              │
                              ▼
                           Grafana
```

## OpenTelemetry

Utilisé pour :

- les traces ;
- les logs ;
- la corrélation `traceId` / `spanId`.

## Micrometer / Prometheus

Les métriques suivent les opérations métier :

```text
create
findById
findAll
update
delete
```

avec notamment :

- succès ;
- erreurs ;
- ressources non trouvées ;
- erreurs techniques ;
- temps d'exécution.

## Grafana

```text
http://localhost:3000
```

Les dashboards agrègent les métriques Prometheus, les traces Tempo et les logs Loki.

---

# Sécurité

`springstarter` fonctionne comme **OAuth2 Resource Server**.

Le flux est :

```text
Client
   │
   │ credentials
   ▼
springstarter-auth
   │
   │ JWT RSA
   ▼
Client
   │
   │ Bearer JWT
   ▼
springstarter
```

Le backend vérifie :

- signature ;
- issuer ;
- audience ;
- expiration ;
- authorities.

Authorities principales :

```text
READ
WRITE
```

Répartition :

```text
GET       → READ
POST      → WRITE
PUT       → WRITE
DELETE    → WRITE
```

---

# CI/CD

Le pipeline GitLab contient les principales étapes :

```text
lint
test
sonar
docker
security
promote
deploy
```

Les contrôles comprennent notamment :

- compilation ;
- tests ;
- couverture ;
- SonarQube ;
- build Docker ;
- Xray ;
- Checkov ;
- promotion ;
- déploiement.

Les secrets CI/CD sont récupérés depuis HashiCorp Vault lorsque nécessaire.

Ils ne doivent jamais être stockés directement dans le repository.

---

# Structure du projet

```text
spring-starter/
├── docker/
├── scripts/
├── src/
│   ├── main/
│   │   ├── java/com/qbe/springstarter/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── enums/
│   │   │   ├── error/
│   │   │   ├── mapper/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── validator/
│   │   └── resources/
│   └── test/
├── .gitlab-ci.yml
├── pom.xml
└── README.md
```

Architecture générale :

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

---

# Bonnes pratiques

## API

- Utiliser des DTO pour le contrat REST.
- Valider les données entrantes.
- Utiliser des codes HTTP cohérents.
- Paginer les collections.
- Utiliser un tri déterministe.

## Persistance

- Utiliser Liquibase pour les évolutions du schéma.
- Ne pas modifier manuellement `@Version`.
- Laisser Hibernate gérer le verrouillage optimiste.

## Sécurité

- Ne jamais versionner de secrets.
- Ne jamais stocker la clé privée dans le backend.
- Utiliser des authorities explicites.
- Externaliser les credentials.

## Tests

- Tester la logique métier avec des TU.
- Tester JPA avec Testcontainers.
- Tester le contrat HTTP avec MockMvc.
- Couvrir les cas limites et les conflits de version.

## Observabilité

- Séparer erreurs métier et erreurs techniques.
- Corréler logs et traces.
- Mesurer les opérations critiques.
- Ne jamais journaliser de secrets.

---

# Roadmap

## Disponible

- ✅ API REST / OpenAPI
- ✅ PostgreSQL / Liquibase
- ✅ Redis
- ✅ JWT / Resource Server
- ✅ Pagination / tri
- ✅ Optimistic locking / 409 Conflict
- ✅ Tests / Testcontainers
- ✅ JaCoCo / Spotless / ArchUnit
- ✅ SonarQube
- ✅ OpenTelemetry / Prometheus / Tempo / Loki / Grafana
- ✅ Docker Compose

## En cours / prévu

- 🚧 GitLab CI/CD
- 🚧 Scans de sécurité
- 📋 Kubernetes / AKS
- 📋 Dashboards métier
- 📋 Alerting