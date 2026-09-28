# Spring Starter

> Template de référence pour le développement d'applications **Spring Boot** modernes, sécurisées, testables et observables.

Ce projet fournit un socle technique réutilisable pour les applications backend d'entreprise.

Il intègre les principaux composants nécessaires au développement, aux tests, à la qualité du code, à la sécurité, à l'observabilité et à l'exécution locale d'une application Spring Boot.

L'objectif est de disposer d'un **template standardisé, reproductible, maintenable et facilement déployable**, servant de point de départ aux futurs projets backend.

> **Statut :** socle backend fonctionnel et validé.  
> Les évolutions restantes concernent principalement le CI/CD, Kubernetes, les dashboards et les alertes.

---

## Table des matières

## Table des matières

- [Vue d'ensemble](#vue-densemble)
- [Architecture](#architecture)
- [Stack technique](#stack-technique)
- [Prérequis](#prérequis)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Services locaux](#services-locaux)
- [Base de données](#base-de-données)
- [Tests](#tests)
- [Qualité du code](#qualité-du-code)
- [Observabilité](#observabilité)
- [Sécurité](#sécurité)
- [Environnements](#environnements)
- [CI/CD](#cicd)
- [Structure du projet](#structure-du-projet)
- [Dépannage](#dépannage)
- [Roadmap](#roadmap)
- [Bonnes pratiques](#bonnes-pratiques)
- [Licence](#licence)

---

# Vue d'ensemble

Le projet constitue un **socle Spring Boot générique** destiné à accélérer la création de nouveaux services backend.

Le template fournit notamment :

- une configuration Spring Boot standardisée ;
- une API REST documentée avec OpenAPI / Swagger ;
- une persistance PostgreSQL ;
- la gestion des migrations avec Liquibase ;
- un mécanisme de cache avec Redis ;
- une sécurité basée sur Spring Security Resource Server et des JWT Bearer ;
- la validation des JWT émis par `springstarter-auth` ;
- le contrôle de la signature RSA, de l'issuer, de l'audience et de l'expiration ;
- une autorisation basée sur les authorities `READ` et `WRITE` ;
- des tests unitaires avec JUnit ;
- des tests d'intégration avec Testcontainers ;
- des tests d'architecture avec ArchUnit ;
- une mesure de couverture avec JaCoCo ;
- un formatage automatique avec Spotless ;
- une analyse de qualité avec SonarQube ;
- une exécution locale conteneurisée avec Docker Compose ;
- de l'observabilité avec OpenTelemetry ;
- du tracing distribué avec OpenTelemetry et Tempo ;
- des métriques applicatives avec Micrometer et Prometheus ;
- de la centralisation des logs avec Loki ;
- de la visualisation des métriques, traces et logs avec Grafana.

---

# Architecture

L'authentification est séparée du backend métier.

`springstarter-auth` authentifie les utilisateurs et émet les JWT.  
`springstarter` agit exclusivement comme **Resource Server** et valide les tokens reçus.

```text
                              ┌──────────────────┐
                              │      Client      │
                              └────────┬─────────┘
                                       │
                              Login / Refresh
                                       │
                                       ▼
                         ┌─────────────────────────┐
                         │   springstarter-auth    │
                         │                         │
                         │ • Authentication        │
                         │ • JWT generation        │
                         │ • RSA signing           │
                         │ • Refresh tokens        │
                         └────────────┬────────────┘
                                      │
                                  Bearer JWT
                                      │
                                      ▼
                         ┌─────────────────────────┐
                         │      springstarter      │
                         │                         │
                         │ • REST API              │
                         │ • Resource Server       │
                         │ • JWT validation        │
                         │ • READ / WRITE          │
                         │ • Micrometer            │
                         │ • OpenTelemetry         │
                         └───────┬────────┬────────┘
                                 │        │
                                 ▼        ▼
                         ┌───────────┐ ┌───────┐
                         │PostgreSQL │ │ Redis │
                         └───────────┘ └───────┘
```

En environnement local, `springstarter-auth` et `springstarter` partagent le même serveur PostgreSQL via le réseau Docker :

```text
springstarter-network
```

Les données Auth et Backend restent logiquement séparées.

L'observabilité est organisée ainsi :

```text
springstarter
      │
      ├── traces ─────► OpenTelemetry Collector ─────► Tempo
      │
      ├── logs ───────► OpenTelemetry Collector ─────► Loki
      │
      └── metrics ────► Actuator ───► Prometheus
                                        │
                                        ▼
                                     Grafana
```

Le backend valide localement les JWT à l'aide de la clé publique RSA de `springstarter-auth`. Aucun appel vers le service Auth n'est nécessaire pour chaque requête métier.

PostgreSQL assure la persistance des données tandis que Redis fournit les capacités de cache applicatif.

---

# Stack technique

## Composants disponibles

| Domaine | Technologie | Statut |
|---|---|:---:|
| Runtime | Java 25 | ✅ |
| Framework | Spring Boot | ✅ |
| Build | Maven 3.9+ | ✅ |
| API | OpenAPI / Swagger | ✅ |
| HTTP Client | Spring WebClient | ✅ |
| Base de données | PostgreSQL | ✅ |
| Migrations | Liquibase | ✅ |
| Cache | Redis | ✅ |
| Sécurité | Spring Security Resource Server / JWT | ✅ |
| Authentification | springstarter-auth / JWT RSA | ✅ |
| Tests | JUnit | ✅ |
| Tests d'intégration | Testcontainers | ✅ |
| Couverture | JaCoCo | ✅ |
| Architecture | ArchUnit | ✅ |
| Formatage | Spotless | ✅ |
| Observabilité | OpenTelemetry | ✅ |
| Collecte télémétrie | OpenTelemetry Collector | ✅ |
| Tracing | Tempo | ✅ |
| Métriques | Micrometer / Prometheus | ✅ |
| Logs | Loki | ✅ |
| Visualisation | Grafana | ✅ |
| Qualité | SonarQube | ✅ |
| Conteneurisation | Docker / Docker Compose | ✅ |

## Composants en cours ou prévus

| Domaine | Technologie | Statut |
|---|---|:---:|
| CI/CD | GitLab CI/CD | 🚧 |
| Orchestration | Kubernetes / AKS | 📋 |

### Légende

- ✅ Disponible
- 🚧 En cours d'intégration
- 📋 Prévu

---

# Prérequis

Les outils suivants doivent être installés sur la machine de développement.

| Outil | Version minimale |
|---|---|
| Java | 25 |
| Maven | 3.9+ |
| Docker | Version récente |
| Docker Compose | Version récente |
| `curl` | Version récente |
| `jq` | Version récente |

Vérifier les versions installées :

```bash
java --version
mvn --version
docker --version
docker compose version
curl --version
jq --version
```

---

# Quick Start

L'environnement local repose sur deux applications :

1. `springstarter-auth`, responsable de l'authentification et du PostgreSQL partagé ;
2. `springstarter`, le backend Resource Server.

Le projet Auth doit être démarré en premier afin de créer :

- le conteneur PostgreSQL ;
- le réseau Docker `springstarter-network` ;
- le service d'authentification.

## 1. Démarrer springstarter-auth

Depuis le projet `springstarter-auth` :

```bash
docker compose -f docker/docker-compose.yml up -d
```

Vérifier les services :

```bash
docker compose -f docker/docker-compose.yml ps
```

Vérifier le réseau partagé :

```bash
docker network inspect springstarter-network
```

## 2. Construire springstarter

Depuis le projet backend :

```bash
docker compose -f docker/docker-compose.yml build --no-cache
```

## 3. Démarrer springstarter

```bash
docker compose -f docker/docker-compose.yml up -d --force-recreate
```

## 4. Vérifier l'état des services

```bash
docker compose -f docker/docker-compose.yml ps
```

## 5. Vérifier l'application

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

La configuration de l'application est externalisée afin de permettre l'utilisation du même artefact dans différents environnements.

Les informations sensibles ne doivent jamais être stockées directement dans le code source ou dans le repository.

## Variables d'environnement

Les paramètres tels que :

- credentials de base de données ;
- mots de passe ;
- tokens ;
- clés d'API ;
- paramètres spécifiques aux environnements ;

doivent être fournis via des variables d'environnement ou un gestionnaire de secrets adapté.

Un fichier `.env.example` permet de documenter les variables nécessaires.

Exemple :

```dotenv
# JWT validation
JWT_PUBLIC_KEY=classpath:certs/public-key.pem
JWT_ISSUER=http://localhost:8081/authstarter
JWT_AUDIENCE=springstarter-api

# Shared PostgreSQL
SPRING_DATASOURCE_URL=jdbc:postgresql://shared-postgres:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SPRING_DATASOURCE_SCHEMA=mydb

# Redis
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379

# OpenTelemetry
OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4318
OTEL_TRACES_EXPORTER=otlp
OTEL_METRICS_EXPORTER=none
OTEL_LOGS_EXPORTER=otlp
```

## Clés RSA

Le backend utilise uniquement la clé publique :

```text
src/main/resources/certs/public-key.pem
```

La clé privée RSA n'est jamais présente dans `springstarter`.

Elle reste exclusivement dans `springstarter-auth`, qui l'utilise pour signer les JWT.

```text
springstarter-auth
├── private-key.pem
└── public-key.pem

springstarter
└── public-key.pem
```

> ⚠️ Le fichier `.env` contenant des secrets ou credentials locaux ne doit jamais être versionné.

La convention recommandée est :

```text
.env                 ❌ non versionné
.env.example         ✅ versionné
private-key.pem      ❌ jamais dans le backend
public-key.pem       ✅ clé publique uniquement
```

---

# Services locaux

L'environnement local repose sur Docker Compose.

## Services backend

Les services propres au backend sont notamment :

- Spring Boot (`backend-app`) ;
- Liquibase ;
- Redis ;
- OpenTelemetry Collector ;
- Tempo ;
- Loki ;
- Prometheus ;
- Grafana ;
- SonarQube.

PostgreSQL est fourni par la stack `springstarter-auth` et rejoint le backend via le réseau Docker partagé `springstarter-network`.

## Démarrer

```bash
docker compose -f docker/docker-compose.yml up -d
```

## Reconstruire les images

```bash
docker compose -f docker/docker-compose.yml build --no-cache
```

## Vérifier l'état

```bash
docker compose -f docker/docker-compose.yml ps
```

## Consulter les logs

Tous les services :

```bash
docker compose -f docker/docker-compose.yml logs -f
```

Application uniquement :

```bash
docker compose -f docker/docker-compose.yml logs -f backend-app
```

OpenTelemetry Collector :

```bash
docker compose -f docker/docker-compose.yml logs -f otel-collector
```

## Arrêter les services

```bash
docker compose -f docker/docker-compose.yml down
```

## Supprimer les volumes propres au backend

```bash
docker compose -f docker/docker-compose.yml down -v
```

> ⚠️ Cette commande supprime les volumes appartenant au Compose backend.
>
> Le volume PostgreSQL est géré par `springstarter-auth` et n'est pas supprimé par le Compose backend.

---

# Base de données

## PostgreSQL partagé

En environnement local, `springstarter` ne démarre plus son propre serveur PostgreSQL.

PostgreSQL est fourni par `springstarter-auth` et accessible depuis le backend via :

```text
springstarter-network
```

Le hostname Docker utilisé est :

```text
shared-postgres
```

La datasource locale utilise donc :

```text
jdbc:postgresql://shared-postgres:5432/postgres
```

La base de données utilisée est :

```text
postgres
```

Le schéma applicatif du backend est :

```text
mydb
```

Le partage concerne uniquement l'infrastructure PostgreSQL. Les données et responsabilités Auth et Backend restent logiquement séparées.

## Vérifier la résolution depuis le backend

```bash
docker exec springstarter getent hosts shared-postgres
```

## Connexion locale

Le conteneur PostgreSQL étant géré par `springstarter-auth`, la connexion peut être ouverte avec :

```bash
docker exec -it authstarter-postgres psql -U postgres -d postgres
```

Puis, par exemple :

```sql
SET search_path TO mydb;
```

## Liquibase

Liquibase est utilisé pour versionner et appliquer les évolutions du schéma de base de données.

Les modifications du schéma doivent être réalisées via les changelogs Liquibase plutôt que par des modifications manuelles de la base.

Les migrations sont versionnées avec le code source afin de garantir la reproductibilité des environnements.

Le conteneur Liquibase du backend rejoint également `springstarter-network` afin d'accéder à PostgreSQL.

---

# Tests

Les tests font partie intégrante du cycle de développement.

## Tests unitaires

```bash
mvn test
```

## Build complet

```bash
mvn clean verify
```

Cette commande exécute notamment :

- la compilation ;
- les tests ;
- les tests d'intégration configurés ;
- les contrôles de qualité intégrés au build ;
- la vérification de la couverture lorsque configurée.

## Tests d'intégration

Les tests d'intégration utilisent **Testcontainers** afin d'exécuter les dépendances nécessaires dans des conteneurs isolés.

Les principales dépendances concernées sont notamment :

- PostgreSQL ;
- Redis ;
- les services nécessaires aux scénarios d'intégration.

L'objectif est de disposer de tests :

- reproductibles ;
- isolés ;
- indépendants de l'environnement Docker local ;
- exécutables automatiquement dans la CI.

---

# Qualité du code

Le projet applique plusieurs mécanismes complémentaires afin de maintenir un niveau de qualité homogène.

## JaCoCo

JaCoCo est utilisé pour mesurer la couverture des tests.

Le projet impose un objectif minimal de :

```text
80 %
```

Le rapport HTML est généré dans :

```text
target/site/jacoco/index.html
```

Le répertoire complet est :

```text
target/site/jacoco/
```

---

## Spotless

Vérifier le formatage :

```bash
mvn spotless:check
```

Appliquer automatiquement le formatage :

```bash
mvn spotless:apply
```

---

## ArchUnit

ArchUnit permet de vérifier automatiquement les règles architecturales du projet.

Les règles sont exécutées avec la suite de tests :

```bash
mvn test
```

Toute évolution importante de l'architecture doit être accompagnée d'une mise à jour des règles ArchUnit lorsque cela est nécessaire.

---

## SonarQube

SonarQube est utilisé pour analyser :

- la qualité du code ;
- la maintenabilité ;
- les bugs potentiels ;
- les vulnérabilités ;
- les code smells ;
- la couverture des tests.

### Accès

```text
http://localhost:9000
```

Sur une installation locale neuve, les credentials initiaux sont généralement :

```text
Utilisateur : admin
Mot de passe : admin
```

Le mot de passe doit être modifié lorsqu'il est demandé.

### Création du projet

Dans SonarQube :

1. Aller dans **Projects**.
2. Sélectionner **Create Project**.
3. Choisir **Manually**.
4. Renseigner :

```text
Project Key  : spring-starter
Project Name : spring-starter
```

5. Configurer les permissions.
6. Générer un token d'analyse.

### Analyse

Il est préférable de placer le token dans une variable d'environnement :

```bash
export SONAR_TOKEN='<TOKEN>'
```

Puis :

```bash
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=spring-starter \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token="$SONAR_TOKEN" \
  -Dsonar.scm.disabled=true
```

> ⚠️ Le token SonarQube ne doit jamais être versionné dans le repository.

---

# Observabilité

L'observabilité repose sur :

- OpenTelemetry ;
- Micrometer ;
- Prometheus ;
- OpenTelemetry Collector ;
- Tempo ;
- Loki ;
- Grafana.

Les signaux sont répartis ainsi :

```text
springstarter
├── traces ── OTLP ──► OpenTelemetry Collector ──► Tempo
├── logs   ── OTLP ──► OpenTelemetry Collector ──► Loki
└── metrics ─────────► Actuator ──► Prometheus ──►