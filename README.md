# LeaderElection
Leader Election in a cluster of nodes using Apache Zookeeper. This was created as an assignment in a Udemy course on Distributed Computing.

Prerequisite:
1. Apache Zookeeper and Maven downloaded and setup.
2. Basic understanding of how to run a zookeeper server and manage z-nodes.
3. Understanding of how to import and run maven project on IntelliJ or Eclipse(To make changes)

How to run:
1. Build project using this command: mvn clean package
2. Start zookeeper and create a znode with path- "/election"
3. Goto project directory, open CMD and start a node using this command:  java -jar target/leader.election-1.0-SNAPSHOT-jar-with-dependencies.jar
4. Follow step 3 to spawn as many nodes as required, terminate the nodes to see how leader re-election happens. 
