# ═══════════════════════════════════════════
# ÉTAPE 1 — BUILD
# On compile l'application dans une image JDK
# ═══════════════════════════════════════════

# Image de base Java 21 avec JDK
# eclipse-temurin = distribution Java officielle
# alpine = version légère de Linux (5MB au lieu de 200MB)
# AS build = nom de cette étape (réutilisé plus bas)
FROM eclipse-temurin:21-jdk-alpine AS build

# Définir le dossier de travail dans le conteneur
# Tous les fichiers seront copiés ici
# Équivalent de "cd /app" dans un terminal
WORKDIR /app

# Copier le fichier de dépendances Maven en premier
# Optimisation : si pom.xml ne change pas,
# Docker réutilise le cache des dépendances
# → compilation plus rapide !
COPY pom.xml .

# Copier tout le code source Java
# src/ contient entities, services, controllers...
COPY src ./src

# Installer Maven dans l'image Alpine
# Alpine n'a pas Maven par défaut
# apk = gestionnaire de paquets Alpine (comme apt sur Ubuntu)
RUN apk add --no-cache maven

# Compiler le projet Spring Boot
# clean    = nettoyer les anciens fichiers compilés
# package  = créer le fichier .jar final
# -DskipTests = ignorer les tests pour aller plus vite
# Résultat : /app/target/serviprox-backend-0.0.1-SNAPSHOT.jar
RUN mvn clean package -DskipTests

# ═══════════════════════════════════════════
# ÉTAPE 2 — IMAGE FINALE LÉGÈRE
# On garde seulement le .jar compilé
# Plus besoin du JDK ni du code source !
# ═══════════════════════════════════════════

# Image Java 21 avec JRE seulement
# JRE = exécuter seulement (pas compiler)
# JDK = 400MB / JRE = 150MB → image 2x plus légère !
FROM eclipse-temurin:21-jre-alpine

# Dossier de travail de l'image finale
WORKDIR /app

# Copier SEULEMENT le .jar depuis l'étape build
# --from=build = prendre depuis l'étape "build" ci-dessus
# *.jar = n'importe quel fichier .jar dans target/
# app.jar = nom simplifié dans l'image finale
COPY --from=build /app/target/*.jar app.jar

# Déclarer le port utilisé par Spring Boot
# EXPOSE = documentation, pas une ouverture réelle
# L'ouverture réelle se fait dans docker-compose.yml
EXPOSE 7090

# Commande exécutée au démarrage du conteneur
# Équivalent de : java -jar app.jar
# Format tableau = plus sûr que format string
ENTRYPOINT ["java", "-jar", "app.jar"]