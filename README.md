# Spring Starter

> Template de référence pour le développement d'applications **Spring Boot** modernes, sécurisées, testables et observables.

Ce projet fournit un socle backend réutilisable intégrant les principaux composants nécessaires au développement, aux tests, à la sécurité, à l'observabilité et à l'exécution locale d'une application Spring Boot.

> **Statut :** socle backend fonctionnel et validé.  
> L'observabilité locale couvre les métriques, logs, traces et leur corrélation.  
> Les évolutions restantes concernent principalement le CI/CD, Kubernetes, l'industrialisation des dashboards, les SLI/SLO et l'alerting.

---

## Table des matières

- [Vue d'ensemble](#vue-densemble)
- [Architecture](#architecture)
- [Stack technique](#stack-technique)
- [Prérequis](#prérequis)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API REST](#api-rest)
- [Pagination](#pagination)
- [Gestion des conflits de version](#gestion-des-conflits-de-version)
- [Base de données](#base-de-données)
- [Tests et qualité](#tests-et-qualité)
- [Observabilité](#observabilité)
- [Sécurité](#sécurité)
- [CI/CD](#cicd)
- [Structure du projet](#structure-du-projet)
- [Bonnes pratiques](#bonnes-pratiques)
- [Roadmap](#roadmap)

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
- une corrélation entre métriques, logs et traces ;
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

### Loki

```text
http://localhost:3100
```

### Tempo

```text
http://localhost:3200
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
OTEL_SERVICE_NAME=springstarter
OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4318
OTEL_TRACES_EXPORTER=otlp
OTEL_METRICS_EXPORTER=none
OTEL_LOGS_EXPORTER=otlp

# Tracing
TRACING_SAMPLING_PROBABILITY=1.0
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
- erreurs fonctionnelles et techniques ;
- métriques Micrometer ;
- timers et compteurs ;
- instrumentation AOP ;
- classification des statuts ;
- collecte des `401` et `403` ;
- configuration de sécurité.

Les tests d'intégration utilisent **Testcontainers** avec les dépendances réelles nécessaires, notamment PostgreSQL et Redis.

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

Le socle fournit une stack d'observabilité basée sur les trois piliers :

```text
Metrics
Logs
Traces
```

Architecture :

```text
                         springstarter
                              │
              ┌───────────────┼────────────────┐
              │               │                │
              ▼               ▼                ▼
          Micrometer         Logs        OpenTelemetry
              │               │                │
              ▼               └──────┬─────────┘
 /actuator/prometheus                │ OTLP
              │                      ▼
              ▼              OpenTelemetry Collector
         Prometheus              │            │
              │                  │            │
              │                  ▼            ▼
              │                Tempo         Loki
              │                  │            │
              └──────────────────┴─────┬──────┘
                                       ▼
                                    Grafana
```

La séparation des responsabilités est volontaire :

- **Prometheus** stocke et interroge les métriques ;
- **Loki** centralise les logs ;
- **Tempo** stocke les traces distribuées ;
- **OpenTelemetry Collector** reçoit et route les signaux OTLP ;
- **Grafana** fournit une vue unifiée sur les métriques, logs et traces.

---

## OpenTelemetry Collector

L'application envoie ses logs et traces au Collector via OTLP :

```text
springstarter
     │
     │ OTLP HTTP
     ▼
otel-collector:4318
```

Le Collector route ensuite les signaux :

```text
OpenTelemetry Collector
        │
        ├── traces ──► Tempo
        │
        └── logs ────► Loki
```

Les métriques applicatives ne passent volontairement pas par OTLP.

Elles sont exposées par Micrometer et scrapées directement par Prometheus :

```text
Micrometer
    │
    ▼
/actuator/prometheus
    │
    ▼
Prometheus
```

Cela permet de conserver une architecture simple et facilement exploitable avec PromQL.

---

# Métriques Micrometer / Prometheus

Les métriques métier utilisent **Micrometer** et sont exposées via :

```text
/springstarter/actuator/prometheus
```

Prometheus scrape périodiquement cet endpoint.

Les métriques principales sont :

```text
sample_operations_total
sample_operation_duration_*
sample_technical_errors_total
```

## Opérations suivies

Le label :

```text
operation
```

peut actuellement prendre les valeurs :

```text
create
find_by_id
find_all
update
delete
```

Le label :

```text
status
```

peut prendre les valeurs :

```text
success
error
not_found
conflict
unauthorized
forbidden
```

Exemples :

```text
sample_operations_total{
    operation="find_all",
    status="success"
}
```

```text
sample_operations_total{
    operation="update",
    status="conflict"
}
```

```text
sample_operations_total{
    operation="find_all",
    status="unauthorized"
}
```

Cette modélisation évite de créer une métrique différente pour chaque opération ou type d'erreur.

---

# Instrumentation par annotation

Les méthodes REST sont instrumentées de manière déclarative :

```java
@SampleMetricAnnotation(operation = MetricsConstants.FIND_ALL)
```

Un aspect Spring AOP intercepte les méthodes annotées.

Le fonctionnement est :

```text
Méthode REST
    │
    ▼
@SampleMetricAnnotation
    │
    ▼
SampleMetricsAspect
    │
    ├── démarre le timer
    │
    ▼
exécution de l'opération
    │
    ├── success
    ├── not_found
    ├── conflict
    └── error
    │
    ▼
incrément du compteur
    │
    ▼
arrêt du timer
```

Cela permet de conserver la logique d'observabilité hors de la logique métier.

---

# Métriques de sécurité

Les erreurs Spring Security peuvent survenir avant l'appel du controller.

Un filtre dédié collecte donc les réponses :

```text
401 Unauthorized
403 Forbidden
```

et les rattache à l'opération correspondante.

Exemples :

```text
sample_operations_total{
    operation="find_all",
    status="unauthorized"
}
```

```text
sample_operations_total{
    operation="create",
    status="forbidden"
}
```

La classification complète est :

```text
success       → traitement réussi
not_found     → ressource inexistante
conflict      → conflit de version
unauthorized  → authentification absente ou invalide
forbidden     → utilisateur authentifié mais non autorisé
error         → erreur technique
```

Cette distinction simplifie fortement les dashboards et les futures alertes.

---

# Requêtes Prometheus utiles

## Nombre total d'appels par opération

```promql
sum by (operation) (
  increase(
    sample_operations_total[$__range]
  )
)
```

## Nombre de succès par opération

```promql
sum by (operation) (
  increase(
    sample_operations_total{
      status="success"
    }[$__range]
  )
)
```

## Nombre d'erreurs par opération

```promql
sum by (operation) (
  increase(
    sample_operations_total{
      status!="success"
    }[$__range]
  )
)
```

## Répartition des erreurs par opération et type

```promql
sum by (operation, status) (
  increase(
    sample_operations_total{
      status!="success"
    }[$__range]
  )
)
```

## Nombre de 401

```promql
sum(
  increase(
    sample_operations_total{
      status="unauthorized"
    }[$__range]
  )
)
or vector(0)
```

## Nombre de 403

```promql
sum(
  increase(
    sample_operations_total{
      status="forbidden"
    }[$__range]
  )
)
or vector(0)
```

## 401 et 403 par opération

```promql
sum by (operation, status) (
  increase(
    sample_operations_total{
      status=~"unauthorized|forbidden"
    }[$__range]
  )
)
```

## Erreurs techniques

```promql
sum(
  increase(
    sample_technical_errors_total[$__range]
  )
)
or vector(0)
```

---

# Taux de succès et d'erreur

## Taux de succès

```promql
100 *
sum(
  increase(
    sample_operations_total{
      status="success"
    }[$__range]
  )
)
/
sum(
  increase(
    sample_operations_total[$__range]
  )
)
or vector(100)
```

## Taux d'erreur

```promql
100 *
sum(
  increase(
    sample_operations_total{
      status!="success"
    }[$__range]
  )
)
/
sum(
  increase(
    sample_operations_total[$__range]
  )
)
or vector(0)
```

---

# Timers

Les temps d'exécution sont mesurés par un Timer Micrometer :

```text
sample_operation_duration
```

L'export Prometheus fournit notamment :

```text
sample_operation_duration_seconds_count
sample_operation_duration_seconds_sum
sample_operation_duration_seconds_max
sample_operation_duration_seconds_bucket
```

Les histogrammes et percentiles sont activés afin d'analyser les distributions de latence.

## Durée moyenne par opération

En millisecondes :

```promql
1000 *
(
  sum by (operation) (
    increase(
      sample_operation_duration_seconds_sum{
        status="success"
      }[$__range]
    )
  )
  /
  sum by (operation) (
    increase(
      sample_operation_duration_seconds_count{
        status="success"
      }[$__range]
    )
  )
)
```

## Durée maximale par opération

```promql
1000 *
max by (operation) (
  sample_operation_duration_seconds_max{
    status="success"
  }
)
```

## P50

```promql
1000 *
histogram_quantile(
  0.50,
  sum by (le, operation) (
    rate(
      sample_operation_duration_seconds_bucket{
        status="success"
      }[$__rate_interval]
    )
  )
)
```

## P95

```promql
1000 *
histogram_quantile(
  0.95,
  sum by (le, operation) (
    rate(
      sample_operation_duration_seconds_bucket{
        status="success"
      }[$__rate_interval]
    )
  )
)
```

## P99

```promql
1000 *
histogram_quantile(
  0.99,
  sum by (le, operation) (
    rate(
      sample_operation_duration_seconds_bucket{
        status="success"
      }[$__rate_interval]
    )
  )
)
```

La moyenne seule ne suffit pas toujours à détecter les ralentissements.

Les percentiles permettent notamment d'identifier les requêtes lentes qui peuvent être masquées par une moyenne globalement correcte.

---

# Logs Loki

Les logs applicatifs sont exportés via OpenTelemetry puis stockés dans Loki.

La requête de base est :

```logql
{service_name="springstarter"}
```

Les logs métier permettent notamment de suivre le début et la fin des traitements :

```text
Finding SampleEntity by id=1
SampleEntity found successfully with id=1
```

```text
Creating SampleEntity...
SampleEntity created successfully with id=6
```

```text
Finding SampleEntities page=4, size=1, sort=id: DESC
SampleEntities retrieved successfully: page=4, size=1, totalElements=1
```

```text
Deleting SampleEntity id=1
SampleEntity deleted successfully with id=1
```

Les erreurs techniques sont également journalisées :

```text
Error during sample creation
Technical error on /springstarter/api/v1/samples
```

---

# Corrélation des logs

OpenTelemetry ajoute aux logs des informations de corrélation :

```text
trace_id
span_id
```

Elles sont également visibles dans la sortie console :

```text
[traceId=ba3faeda0b65d488ad362cad3d541516 spanId=b613e34f7f40ebda]
```

Lors de leur ingestion OTLP dans Loki, les identifiants sont conservés sous forme de métadonnées structurées.

Une requête permet de retrouver tous les logs possédant une trace :

```logql
{service_name="springstarter"}
| trace_id=~".+"
```

Une trace précise peut être recherchée avec :

```logql
{service_name="springstarter"}
| trace_id="ba3faeda0b65d488ad362cad3d541516"
```

Un span précis peut également être recherché :

```logql
{service_name="springstarter"}
| span_id="b613e34f7f40ebda"
```

Le `trace_id` permet donc de regrouper tous les logs produits pendant le traitement d'une même requête.

Le `span_id` permet de descendre à une portion plus précise de cette requête.

---

# Tracing Tempo

Les traces sont envoyées via OpenTelemetry :

```text
springstarter
     │
     ▼
OpenTelemetry Collector
     │
     ▼
Tempo
```

L'instrumentation OpenTelemetry fournit automatiquement de nombreux spans techniques.

Une requête REST peut par exemple produire :

```text
GET /springstarter/api/v1/samples/{id}
│
├── HTTP GET
│
├── Spring Security
│   ├── security filterchain before
│   ├── authenticate bearer token
│   ├── authorize request
│   ├── secured request
│   └── security filterchain after
│
├── Redis GET
│
├── SampleRepository.findById
│
├── Hibernate Session.find
│
├── PostgreSQL SELECT
│
└── Transaction.commit
```

Cette représentation permet notamment d'identifier rapidement :

- une authentification lente ;
- un appel Redis lent ;
- une requête SQL coûteuse ;
- un accès repository lent ;
- une transaction lente ;
- une dépendance externe lente ;
- une erreur sur un span spécifique.

Il n'est pas nécessaire de créer manuellement des spans dans chaque service pour disposer d'une première observabilité exploitable.

L'instrumentation automatique couvre déjà une grande partie du parcours technique d'une requête.

---

# Corrélation Tempo vers Loki

Grafana est configuré pour permettre la navigation depuis une trace Tempo vers les logs Loki associés.

Le mapping principal est :

```text
Tempo                       Loki

service.name=springstarter  →  service_name="springstarter"
traceId                     →  trace_id
```

Depuis un span Tempo, l'action :

```text
Logs for this span
```

ouvre automatiquement les logs appartenant à la même trace.

Le workflow devient :

```text
Tempo
  │
  ▼
Trace
  │
  ▼
Span
  │
  ▼
Logs for this span
  │
  ▼
Loki
  │
  ▼
Logs de la même trace
```

Cela permet de passer immédiatement d'un span lent ou en erreur à son contexte applicatif.

---

# Investigation d'un incident

La combinaison Prometheus, Loki et Tempo permet un workflow complet d'investigation.

## Exemple de problème de performance

```text
Grafana
   │
   ▼
Prometheus
   │
   ▼
P95 anormal
   │
   ▼
Tempo
   │
   ▼
trace lente
   │
   ▼
span SQL / Redis / HTTP suspect
   │
   ▼
Logs for this span
   │
   ▼
Loki
   │
   ▼
contexte applicatif
```

## Exemple d'erreur

```text
Dashboard Grafana
   │
   ▼
augmentation du taux d'erreur
   │
   ▼
analyse par operation / status
   │
   ▼
Loki
   │
   ▼
logs de l'opération
   │
   ▼
trace_id / span_id
   │
   ▼
Tempo
   │
   ▼
analyse de l'exécution complète
```

## Exemple d'erreur SQL

Une même trace peut regrouper plusieurs logs et plusieurs spans :

```text
trace_id
   │
   ├── Hibernate / JDBC
   │     └── erreur SQL
   │
   ├── SampleService
   │     └── erreur métier / technique
   │
   └── GlobalExceptionHandler
         └── réponse HTTP
```

Cette corrélation évite de raisonner uniquement à partir d'un message d'erreur isolé.

---

# Dashboard Grafana recommandé

Un dashboard applicatif peut être organisé en plusieurs sections.

## Santé générale

```text
Nombre total d'appels
Nombre de succès
Nombre d'erreurs
Taux de succès
Taux d'erreur
```

## Sécurité

```text
Nombre de 401
Nombre de 403
401 / 403 par opération
```

## Erreurs métier et techniques

```text
404 Not Found
409 Conflict
Erreurs techniques
Répartition des erreurs par opération
Répartition des erreurs par statut
```

## Performance

```text
Durée moyenne par opération
Durée maximale
P50
P95
P99
```

## Activité

```text
Débit de requêtes
Appels par opération
Évolution des succès
Évolution des erreurs
```

## Investigation

```text
Logs Loki
trace_id
span_id
traces Tempo
Logs for this span
```

---

# Monitoring en production

Pour aller plus loin qu'un dashboard technique, il est recommandé de définir des indicateurs de service.

Exemples :

```text
Disponibilité
Taux de succès
Taux d'erreur
Latence P95
Latence P99
Débit
401 / 403
Erreurs techniques
```

Ces indicateurs peuvent servir de base à des **SLI**.

Exemple :

```text
SLI disponibilité =
requêtes réussies / requêtes totales
```

Des objectifs de service peuvent ensuite être définis.

Exemples :

```text
SLO disponibilité ≥ 99.9 %
P95 < 500 ms
Taux d'erreur < 1 %
```

Les seuils doivent être adaptés au contexte métier et aux exigences réelles de production.

---

# Alerting recommandé

Une évolution naturelle consiste à définir des alertes Grafana / Prometheus sur les signaux réellement actionnables.

Exemples :

```text
Taux d'erreur anormalement élevé
P95 supérieur au seuil attendu
P99 supérieur au seuil attendu
Hausse soudaine des 401
Hausse soudaine des 403
Erreurs techniques récurrentes
Absence totale de trafic inattendue
Backend indisponible
Prometheus target DOWN
```

Il est préférable d'alerter sur un symptôme utilisateur ou applicatif significatif plutôt que sur chaque événement technique individuel.

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

Les erreurs de sécurité `401` et `403` sont également comptabilisées dans les métriques applicatives afin de permettre leur suivi dans Grafana.

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
├── grafana/
├── opentelemetry/
├── prometheus/
├── scripts/
├── src/
│   ├── main/
│   │   ├── java/com/qbe/springstarter/
│   │   │   ├── config/
│   │   │   ├── constants/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── enums/
│   │   │   ├── error/
│   │   │   ├── mapper/
│   │   │   ├── metrics/
│   │   │   │   ├── annotation/
│   │   │   │   ├── aspect/
│   │   │   │   └── filter/
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

L'observabilité est gérée transversalement :

```text
                  Controller
                      │
             @SampleMetricAnnotation
                      │
                      ▼
             SampleMetricsAspect
                      │
                      ▼
                   Service
                      │
                      ▼
                  Repository
                      │
                      ▼
                  PostgreSQL


HTTP Request
     │
     ▼
SecurityMetricsFilter
     │
     ├── 401
     └── 403
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
- Ne jamais journaliser les JWT ou credentials.
- Surveiller séparément les `401` et les `403`.

## Tests

- Tester la logique métier avec des TU.
- Tester JPA avec Testcontainers.
- Tester le contrat HTTP avec MockMvc.
- Couvrir les cas limites et les conflits de version.
- Tester les métriques et leur classification.
- Tester les mécanismes transverses AOP.
- Tester les filtres de sécurité.

## Observabilité

- Séparer erreurs métier et erreurs techniques.
- Distinguer `401`, `403`, `404`, `409` et erreurs serveur.
- Corréler logs et traces avec un `trace_id`.
- Conserver également le `span_id` pour une analyse plus fine.
- Mesurer les opérations critiques.
- Mesurer les latences avec des timers et histogrammes.
- Utiliser P50, P95 et P99 en complément de la moyenne.
- Utiliser des labels de faible cardinalité.
- Ne pas utiliser les `trace_id` comme labels Prometheus.
- Utiliser les métriques pour les tendances et les alertes.
- Utiliser les logs pour comprendre le contexte.
- Utiliser les traces pour comprendre le chemin d'exécution.
- Journaliser les fins d'opérations importantes lorsqu'elles apportent de la valeur.
- Ne jamais journaliser de secrets ou données sensibles.

Un bon workflow d'observabilité suit généralement :

```text
Metrics
  ↓
détecter

Traces
  ↓
localiser

Logs
  ↓
comprendre
```

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
- ✅ OpenTelemetry
- ✅ Métriques Micrometer / Prometheus
- ✅ Compteurs par opération et statut
- ✅ Timers et histogrammes
- ✅ Métriques de sécurité 401 / 403
- ✅ Logs Loki avec `trace_id` / `span_id`
- ✅ Traces Tempo
- ✅ Corrélation Tempo → Loki
- ✅ Dashboard Grafana de base
- ✅ Docker Compose

## En cours / prévu

- 🚧 GitLab CI/CD
- 🚧 Scans de sécurité
- 📋 Kubernetes / AKS
- 📋 Dashboards Grafana avancés
- 📋 SLI / SLO
- 📋 Alerting Prometheus / Grafana
- 📋 Industrialisation du monitoring pour les environnements Kubernetes