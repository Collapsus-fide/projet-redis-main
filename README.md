Bienvenue dans notre projet redis.
Vous trouverez ci-dessous des instructions d'utilisation pour lancer correctement notre serveur.

Ce projet à été réalisé sous Maven. En cas de problème avec les exécutables fournis vous devrez l'installer pour recompiler les classes.

Vous disposez, dans le dossier target/ de trois fichiers .jar (client, server et slave-server).
Pour les lancer, rendez vous dans le dossier target et rentrez les commandes suivantes dans 3 invites de commandes

java -jar slave-server.jar
java -jar server.jar
java -jar client.jar

Faites bien attention à les lancer dans cet ordre sans quoi le serveur principal ne sera pas connecté à son slave.