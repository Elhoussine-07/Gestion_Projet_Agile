# Application de Gestion de Projets Agile

Application Spring Boot pour gérer des projets selon la méthodologie Agile/Scrum, permettant de suivre les User Stories, Epics, Sprints et Tasks à travers Product Backlog et Sprint Backlogs.

[![Java](https://img.shields.io/badge/Java-23-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Table des Matières

- [Fonctionnalites](#fonctionnalites)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Prerequis](#prerequis)
- [Installation](#installation)
- [Configuration](#configuration)
- [Lancement](#lancement)
- [API Documentation](#api-documentation)
- [Structure du Projet](#structure-du-projet)
- [Modele de Donnees](#modele-de-donnees)
- [Strategies de Priorisation](#strategies-de-priorisation)
- [Tests](#tests)
- [AOP et Aspects](#aop-et-aspects)
- [Contributeurs](#contributeurs)
- [Licence](#licence)
- [Contributing](#contributing)
- [Support](#support)

---

## Fonctionnalites

### Domaine Planning (Product Owner)
- Gestion de projets et membres d'équipe
- Création et organisation d'Epics
- Gestion complète des User Stories avec description structurée (format Agile)
- Critères d'acceptation au format Gherkin (Given-When-Then)
- Product Backlog avec priorisation automatique
- 3 stratégies de priorisation : **MoSCoW**, **WSJF**, **Value/Effort**
- Métriques personnalisables par User Story

### Domaine Execution (Scrum Master & Développeurs)
- Gestion des Sprints avec cycle de vie complet
- Sprint Backlog avec suivi en temps réel
- Gestion des Tasks avec assignation
- Suivi de la vélocité et progression
- Burndown chart et métriques de sprint

### Fonctionnalités Transversales
- Authentification et gestion des rôles (Product Owner, Scrum Master, Développeur)
- API RESTful complète
- Validation métier automatique (Spring AOP)
- Monitoring des performances

---

## Architecture

### Principes de Conception
- **Clean Architecture** - Séparation claire des responsabilités
- **Domain-Driven Design (DDD)** - Entités riches avec comportement métier
- **SOLID Principles** - Code maintenable et extensible
- **Design Patterns** : Strategy, Factory, Builder, Proxy (AOP)

### Couches de l'Application
```
┌─────────────────────────────────────┐
│     Controllers (API REST)          │  ← Exposition des endpoints
├─────────────────────────────────────┤
│     DTOs & Mappers                  │  ← Transfert de données
├─────────────────────────────────────┤
│     Services (Logique métier)       │  ← Orchestration
├─────────────────────────────────────┤
│     Repositories (JPA)              │  ← Accès données
├─────────────────────────────────────┤
│     Entities (Modèle de domaine)    │  ← Modèle métier
└─────────────────────────────────────┘
      ↕ Aspects (AOP)
```

---

## Technologies

### Backend
- **Java 23** - Langage de programmation
- **Spring Boot 3.3.5** - Framework principal
- **Spring Data JPA** - Persistence des données
- **Spring Security** - Sécurité et authentification
- **Spring AOP** - Programmation orientée aspect
- **Hibernate** - ORM

### Base de Données
- **MySQL 8.0+** - Base de données principale (dev/prod)
- **H2 Database** - Base de données en mémoire (tests)

### Outils & Librairies
- **Lombok** - Réduction du code boilerplate
- **Maven** - Gestion des dépendances
- **JUnit 5** - Tests unitaires
- **Mockito** - Mocks pour les tests
- **AssertJ** - Assertions fluides

---

## Prerequis

- **Java JDK 23** ou supérieur
- **Maven 3.8+**
- **MySQL 8.0+** (ou MariaDB)
- **IDE** (IntelliJ IDEA, Eclipse, VS Code)
- **Postman** (pour tester l'API)
- **Git**

---

## Installation

### 1. Cloner le Projet
```bash
git clone https://github.com/votre-repo/gestion-projet-agile.git
cd gestion-projet-agile
```

### 2. Créer la Base de Données
```sql
CREATE DATABASE agile_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE agile_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Installer les Dépendances
```bash
mvn clean install
```

---

## Configuration

### application.properties (src/main/resources)
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/agile_dev
spring.datasource.username=root
spring.datasource.password=votre_mot_de_passe
spring.jpa.hibernate.ddl-auto=update

# JPA
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Server
server.port=8080

# Logging
logging.level.com.Agile.demo=DEBUG
```

### application-test.properties (src/test/resources)
```properties
# H2 Database pour les tests
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
```

---

## Lancement

### Mode Développement
```bash
mvn spring-boot:run
```

L'application sera accessible sur : `http://localhost:8080`

### Exécuter les Tests
```bash
# Tous les tests
mvn test

# Tests d'une classe spécifique
mvn test -Dtest=UserStoryServiceTest

# Tests avec couverture
mvn clean test jacoco:report
```

### Packaging
```bash
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

---

## API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Endpoints Principaux

#### Projects
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/projects` | Créer un projet |
| `GET` | `/projects` | Liste tous les projets |
| `GET` | `/projects/{id}` | Détails d'un projet |
| `PUT` | `/projects/{id}` | Modifier un projet |
| `DELETE` | `/projects/{id}` | Supprimer un projet |
| `POST` | `/projects/{id}/members/{userId}` | Ajouter un membre |

#### Epics
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/epics` | Créer un epic |
| `GET` | `/epics` | Liste tous les epics |
| `GET` | `/epics/{id}` | Détails d'un epic |
| `POST` | `/epics/{id}/stories/{storyId}` | Lier une story à un epic |

#### User Stories
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/user-stories` | Créer une user story |
| `POST` | `/user-stories/with-criteria` | Créer avec critères Gherkin |
| `GET` | `/user-stories/{id}` | Détails d'une story |
| `PUT` | `/user-stories/{id}` | Modifier une story |
| `PATCH` | `/user-stories/{id}/metrics` | Mettre à jour les métriques |
| `GET` | `/user-stories/{id}/gherkin` | Format Gherkin |
| `GET` | `/user-stories/backlog/{id}/ready` | Stories prêtes pour sprint |

#### Product Backlog
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/product-backlogs/{id}` | Détails du backlog |
| `POST` | `/product-backlogs/{id}/prioritize` | Appliquer priorisation |
| `GET` | `/product-backlogs/{id}/top-stories?limit=10` | Top stories |

### Exemples de Requêtes

#### Créer un Projet
```http
POST /api/v1/projects
Content-Type: application/json

{
  "name": "Plateforme E-Commerce",
  "description": "Site de vente en ligne",
  "startDate": "2025-01-15",
  "endDate": "2025-12-31"
}
```

#### Créer une User Story avec Critères Gherkin
```http
POST /api/v1/user-stories/with-criteria
Content-Type: application/json

{
  "productBacklogId": 1,
  "title": "Connexion utilisateur",
  "role": "utilisateur",
  "action": "me connecter avec email et mot de passe",
  "purpose": "accéder à mon compte",
  "givenClauses": ["Je suis sur la page de connexion"],
  "whenClauses": ["Je saisis mes identifiants", "Je clique sur Se connecter"],
  "thenClauses": ["Je suis redirigé vers mon tableau de bord"],
  "storyPoints": 5
}
```

#### Appliquer une Priorisation
```http
POST /api/v1/product-backlogs/1/prioritize
Content-Type: application/json

{
  "method": "MOSCOW"
}
```

**Méthodes disponibles :** `MOSCOW`, `WSJF`, `VALUE_EFFORT`

---

## Structure du Projet
```
src/
├── main/
│   ├── java/com/Agile/demo/
│   │   ├── aspect/
│   │   │   ├── logging/
│   │   │   └── performance/
│   │   ├── exception/
│   │   ├── execution/
│   │   │   ├── controllers/
│   │   │   ├── dto/
│   │   │   │   ├── mapper/
│   │   │   │   ├── task/
│   │   │   │   └── user/
│   │   │   ├── repositories/
│   │   │   └── services/
│   │   ├── model/
│   │   ├── planning/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   │   ├── epic/
│   │   │   │   ├── productbacklog/
│   │   │   │   ├── project/
│   │   │   │   └── userstory/
│   │   │   ├── mapper/
│   │   │   ├── prioritization/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── security/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── jwt/
│   │       └── service/
│   └── resources/
│       ├── application.properties
│       └── application-test.properties
└── test/
    └── java/com/Agile/demo/
        ├── aspect/
        ├── execution/
        │   └── services/
        └── planning/
            └── service/

```

---

## Modele de Donnees

### Diagramme de Classes Simplifié
```
Project
  ├─ ProductBacklog (1:1, composition)
  │    ├─ Epic (1:N, agrégation)
  │    │    └─ UserStory (1:N, agrégation)
  │    └─ UserStory (1:N)
  │         ├─ UserStoryDescription (1:1, composition, @Embeddable)
  │         ├─ AcceptanceCriteria (1:1, composition, @Embeddable)
  │         └─ Task (1:N, composition)
  └─ SprintBacklog (1:N, composition)
       ├─ UserStory (N:M, agrégation)
       └─ Task (1:N, agrégation)
```

### Entités Principales

#### UserStory
```java
- UserStoryDescription description (Value Object)
  ├─ String role        // "En tant que..."
  ├─ String action      // "Je veux..."
  └─ String purpose     // "Afin de..."

- AcceptanceCriteria acceptanceCriteria (Value Object)
  ├─ List<String> givenClauses
  ├─ List<String> whenClauses
  └─ List<String> thenClauses

- Map<String, Integer> customMetrics  // Métriques flexibles
- Integer storyPoints
- Integer priority
```

---

## Strategies de Priorisation

### 1. MoSCoW
**Formule :** `(businessValue × 3) + (urgency × 2)`

**Catégories :**
- **Must Have** : Essentiel (score > 15)
- **Should Have** : Important (score 10-15)
- **Could Have** : Souhaitable (score 5-10)
- **Won't Have** : Reporté (score < 5)

### 2. WSJF (Weighted Shortest Job First)
**Formule :** `(businessValue + timeCriticality + riskReduction) × 100 / storyPoints`

**Favorise :** Les petites stories à haute valeur (quick wins)

### 3. Value/Effort
**Formule :** `businessValue × 100 / storyPoints`

**Favorise :** Le meilleur ratio valeur/effort

### Exemple d'Utilisation
```http
# Définir les métriques
PATCH /api/v1/user-stories/1/metrics
{
  "metrics": {
    "businessValue": 10,
    "urgency": 8,
    "timeCriticality": 9,
    "riskReduction": 7
  }
}

# Appliquer la stratégie
POST /api/v1/product-backlogs/1/prioritize
{
  "method": "WSJF"
}

# Voir le résultat
GET /api/v1/product-backlogs/1/top-stories?limit=10
```

---

## Tests

### Architecture de Tests
```
tests/
├── Unitaires (70%)      # Services, stratégies, validations
├── Intégration (20%)    # Repositories, aspects
└── End-to-End (10%)     # API complète
```

### Coverage Visé
- **Services** : 90%+
- **Controllers** : 80%+
- **Repositories** : Tests ciblés sur requêtes custom

### Exécuter les Tests
```bash
# Tous les tests
mvn test

# Tests d'un package
mvn test -Dtest="com.Agile.demo.planning.service.*"

# Tests avec rapport de couverture
mvn clean test jacoco:report
# Rapport : target/site/jacoco/index.html
```

### Exemple de Test
```java
@Test
void shouldCreateUserStory() {
    // Given
    when(productBacklogRepository.findById(1L))
        .thenReturn(Optional.of(backlog));
    
    // When
    UserStory story = userStoryService.createUserStory(
        1L, "Login", "user", "login", "access", 5
    );
    
    // Then
    assertThat(story).isNotNull();
    assertThat(story.getTitle()).isEqualTo("Login");
    verify(userStoryRepository).save(any(UserStory.class));
}
```

---

## AOP et Aspects

### Aspects Implémentés

#### 1. @LogExecutionTime - Monitoring de Performance
```java
@LogExecutionTime(threshold = 500)  // Warning si > 500ms
public UserStory createUserStory(...) {
    // Code...
}
```

#### 2. @ValidateEntity - Validation Métier
```java
@ValidateEntity(entityType = UserStory.class)
public UserStory updateUserStory(...) {
    // Validation automatique avant exécution
}
```

#### 3. LoggingAspect - Logs Automatiques
- Log avant chaque méthode de service
- Log après succès
- Log en cas d'exception

### Résultat dans les Logs
```
[DEBUG] [BEFORE] UserStoryService.createUserStory - Args: [1, Login, ...]
[INFO]  [PERFORMANCE] UserStoryService.createUserStory took 245ms
[DEBUG] [SUCCESS] UserStoryService.createUserStory - Result: UserStory
```

---

## Contributeurs

 Mesad El Ayam Hafida — [GitHub](https://github.com/Hafidamesad) - Domaine Planning (Product Backlog, Epics, User Stories)
 Lahoussine EL HOSSNI— [GitHub](https://github.com/Elhoussine-07) Domaine Execution (Sprints, Tasks, Workflow)

 Projet académique réalisé dans le cadre du module “Framework Avancés”
---

## Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

## Contributing

Les contributions sont les bienvenues ! Veuillez suivre ces étapes :

1. Forkez le projet
2. Créez une branche feature (`git checkout -b feature/AmazingFeature`)
3. Committez vos changements (`git commit -m 'Add AmazingFeature'`)
4. Pushez vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

---

<p align="center">
  Made with care by the DevlaSpark team
</p>
