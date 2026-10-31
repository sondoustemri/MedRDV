# Projet Med-RDV - Plateforme de Prise de Rendez-vous Médicaux

## Description du projet
Med-RDV est un agenda médical multi-praticiens basé sur une architecture microservices.
Le système permet à un patient de réserver un créneau, au praticien de consulter le dossier de synthèse (allergies, antécédents) avant la consultation, et met à jour le dossier patient à la fin de la consultation.
L'unité de traitement principale est le Rendez-vous, qui suit un cycle de vie strict : DEMANDE ➔ CONFIRME ➔ REALISE / ANNULE.

## Diagramme de Classes UML Global
Ce diagramme présente les entités de chaque service métier. Les interactions entre services sont matérialisées par des flèches de dépendance en pointillés.

![Diagramme de Classes UML](./docs/uml-global.png)

## Schéma de l'Architecture
L'architecture est composée de 5 applications Spring Boot :

| Application | Rôle | Port |
| :--- | :--- | :--- |
| **eureka-server** | Service Registry (annuaire dynamique) | `8761` |
| **gateway-service** | API Gateway (point d'entrée unique, routage) | `8080` |
| **service-patients** | Gestion des patients et dossiers médicaux | `8081` |
| **service-praticiens** | Gestion des praticiens et des créneaux | `8082` |
| **service-rendezvous** | Gestion du cycle de vie des rendez-vous | `8083` |

![Schéma de l'Architecture](./docs/architecture.png)

## Prérequis et Instructions de lancement

### Prérequis
*   Java 17 (JDK)
*   Maven 3.8+
*   IntelliJ IDEA
*   Postman ou Swagger

### Instructions de lancement
1. Cloner le dépôt : `git clone https://github.com/sondoustemri/MedRDV.git`
2. Ouvrir le projet dans IntelliJ IDEA et recharger les dépendances Maven.
3. Démarrer les applications dans cet ordre strict :
    1.  `eureka-server` (attendre qu'il soit prêt sur http://localhost:8761)
    2.  `service-patients`
    3.  `service-praticiens`
    4.  `service-rendezvous`
    5.  `gateway-service`