# TP2 Sécurité réseau UQAC

Déployés au Pôle-Sud, nous avons développés une infrastructure sécurisée client/serveur pour communiquer avec Ottawa.

## Mise en route

### Prérequis

Il faut posséder Java SE JRE 8 sur sa **machine client**. Pour vérifier la version de java, utilisez sur votre terminal:
```
java -version
```

S'il n'est pas installé, obtenez-le sur le site d'[Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

Il faut posséder Java SE server JRE 9 sur son **serveur**. Pour l'installer sur un serveur Ubuntu à l'aide de *apt-get* il faut, une fois loggé sur le serveur, procéder comme suis:
```
sudo apt-get update
sudo apt-get install default-jre
```

## Lancer des test en local

Si vous ne possédez pas de serveur, il est toujours possible d'en émuler le fonctionnement.

### Via IntelliJ IDEA

Cet IDE permet de lancer plusieurs instances simultanées au sein du meme projet. Cette particularité
permet de lancer le code source d'un ou plusieurs client, ainsi que des deux serveurs pour voir en direct
si la communication fonctionne.

Pour commencer il faut ouvrir le projet avec IntelliJ IDEA, il est possible qu'il faille configurrer l'emplacement
de JAVA . Il suffit alors de lancer une instance de client, et une de chaque serveur.

Pour le SouthPoleServer:
```
127.0.0.0:29
127.0.0.1
```

Pour le SouthPoleClient:
```
127.0.0.1:29
```

Le OttawaServer n'a pas besoin d'etre configuré.
 

### Via un terminal

Lancer le terminal depuis la racine du projet, puis compiler les fichiers à l'aide des commandes :
```
javac src/com/g8/tp2/server/SouthPoleServer.java
javac src/com/g8/tp2/server/OttawaServer.java
javac src/com/g8/tp2/client/SouthPoleClient.java
```

Les fichiers sont maintenant prêt à être executés. Il faut ouvrir 2 nouveaux terminaux, et executer une de ces commandes dans chacuns. 
```
java src/com/g8/tp2/server/SouthPoleServer 127.0.0.0:29 127.0.0.1
java src/com/g8/tp2/server/OttawaServer 
java src/com/g8/tp2/client/SouthPoleClient 127.0.0.1:29
```

## Dévellopé avec

* [Java 9](http://www.dropwizard.io/1.0.2/docs/) - Le langage utilisé
* [IntelliJ IDEA](https://www.jetbrains.com/idea/) - IDE utilisé

## Auteurs

* **Guillaume Haerinck** 
* **Azis Tekaya**
* **Alexandre Noret**

## License

Ce projet est licencié sous la licence MIT
