
# GameGauge API : Le Moteur de votre App de Scores 🚀

Bienvenue ! Ce projet contient le "cerveau" de l'application GameGauge. C'est un service backend qui gère toute la logique : les comptes utilisateurs, les tableaux de scores, les joueurs et bien plus encore.

Il est conçu pour communiquer avec une application frontend (un site web ou une application mobile) de manière simple et sécurisée.

## Qu'est-ce que ça fait ? (Fonctionnalités Clés) ✨

*   ✅ **Création de Comptes & Connexion :** Les utilisateurs peuvent s'inscrire et se connecter de manière sécurisée.
*   ✅ **Connexion Sécurisée :** Utilise un système moderne de "tokens" (JWT) pour s'assurer que seuls les utilisateurs connectés peuvent accéder à leurs données.
*   ✅ **Gestion des Tableaux de Scores :** Chaque utilisateur peut créer, voir, modifier et supprimer ses propres tableaux de scores.
*   ✅ **Gestion des Joueurs :** Ajoutez, modifiez ou retirez des participants de n'importe quel tableau.
*   ✅ **Suivi des Scores :** Enregistrez les scores pour chaque joueur, tour par tour. Le classement est mis à jour automatiquement !
*   ✅ **Documentation Interactive :** Une "carte" de l'API (Swagger) est générée automatiquement, vous permettant de tester chaque fonctionnalité directement depuis votre navigateur.

## Notre Boîte à Outils (Les Technologies) 🛠️

*   **Chef d'orchestre :** [Spring Boot](https://spring.io/projects/spring-boot) - Le framework qui organise toute notre application.
*   **Langage :** [Java 17+](https://www.java.com/)
*   **Mémoire (Base de données) :** [MySQL](https://www.mysql.com/) - Pour stocker toutes les informations.
*   **Installation Facile de la BDD :** [Docker](https://www.docker.com/) - Pour lancer notre base de données dans une "boîte" virtuelle, ce qui rend l'installation identique pour tout le monde.
*   **Garde du Corps :** [Spring Security](https://spring.io/projects/spring-security) - Pour gérer la sécurité, les connexions et les permissions.
*   **Traducteur Automatique :** [MapStruct](https://mapstruct.org/) - Pour convertir nos objets de données internes en un format propre pour le client.
*   **Filet de Sécurité :** [JUnit 5](https://junit.org/junit5/) & [Mockito](https://site.mockito.org/) - Pour tester notre code et s'assurer que tout fonctionne comme prévu.
*   **Carte Interactive de l'API :** [Swagger](https://swagger.io/) - Pour visualiser et tester notre API facilement.

---

## Faire Démarrer le Moteur : Guide de Lancement Rapide 🏁

Suivez ces 4 étapes pour lancer le projet sur votre machine.

### Étape 1 : Avoir les bons outils

Assurez-vous d'avoir installé :
1.  **Java (JDK 17 ou +)**
2.  **Docker** et **Docker Compose**
3.  **Maven** (généralement inclus avec les IDEs comme IntelliJ)

### Étape 2 : Lancer la Base de Données

Nous utilisons Docker pour que vous n'ayez pas à installer MySQL manuellement.
1.  Ouvrez un terminal à la racine du projet.
2.  Lancez cette commande :
    ```bash
    docker-compose up -d
    ```
    Et voilà ! Votre base de données est prête et tourne en arrière-plan.

### Étape 3 : Configurer l'API

Avant de lancer, il y a deux petites choses à configurer dans le fichier `src/main/resources/application.properties` :

1.  **Identifiants de la base de données :** Mettez les mêmes que ceux dans `docker-compose.yml` (par défaut : `gamegauge_user` / `gamegauge_pwd`).
    ```properties
    spring.datasource.username=gamegauge_user
    spring.datasource.password=gamegauge_pwd
    ```

2.  **Clé secrète :** C'est une phrase secrète pour sécuriser les tokens de connexion. Générez la vôtre en ouvrant un terminal et en tapant `openssl rand -base64 32`, puis copiez le résultat.
    ```properties
    application.security.jwt.secret-key=COPIEZ_VOTRE_CLE_GENeree_ICI
    ```

### Étape 4 : Démarrer l'Application !

*   **Depuis votre IDE (recommandé) :** Ouvrez le projet, trouvez le fichier `GamegaugeApiApplication.java` et cliquez sur "Run".
*   **Depuis le terminal :**
    ```bash
    mvn spring-boot:run
    ```

🎉 Votre API est maintenant en ligne et accessible sur **[http://localhost:8080](http://localhost:8080)** !

---

## Le Plan de l'API : Jouer avec Swagger 🗺️

Le meilleur moyen de comprendre et de tester l'API est d'utiliser l'interface Swagger, un véritable terrain de jeu interactif.

➡️ **Allez ici :** [**http://localhost:8080/swagger-ui.html**](http://localhost:8080/swagger-ui.html)

Vous y verrez tous les "circuits" (endpoints) de notre API. Pour tester ceux qui sont protégés (marqués d'un cadenas) :
1.  **Connectez-vous :** Utilisez l'endpoint `POST /api/auth/login` pour obtenir un "token" (un long texte).
2.  **Autorisez-vous :** Cliquez sur le bouton vert **"Authorize"** en haut de la page Swagger.
3.  **Collez le token :** Dans la fenêtre, collez le token que vous avez reçu et validez.

Vous pouvez maintenant tester n'importe quel circuit de l'API !

## Lancer le Filet de Sécurité (Tests) 🛡️

Pour s'assurer que tout fonctionne toujours parfaitement après chaque modification, nous avons des tests automatiques. Pour les lancer :
```bash
mvn test
```

## Où Trouver les Choses ? (Structure du Projet) 📂

*   `src/main/java`
    *   `.../controller` ➡️ **Les Portes d'Entrée :** Reçoit les requêtes du client.
    *   `.../service` ➡️ **Le Cerveau :** Contient toute la logique métier.
    *   `.../model` ➡️ **Les Plans :** Définit à quoi ressemblent nos données (Utilisateur, Tableau, etc.).
    *   `.../repository` ➡️ **Les Messagers :** Parle avec la base de données.
    *   `.../mapper` ➡️ **Les Traducteurs :** Convertit les données entre le format "interne" et le format "client".
    *   `.../config` ➡️ **Le Panneau de Contrôle :** Configure la sécurité, Swagger, etc.
*   `src/main/resources` ➡️ **Les Fichiers de Configuration** (`application.properties`, etc.).
*   `src/test/java` ➡️ **Le Centre de Contrôle Qualité** (tous nos tests).
*   `docker-compose.yml` ➡️ **Le Lanceur** de notre base de données.
*   `pom.xml` ➡️ **La Liste des Ingrédients** du projet (dépendances Maven).