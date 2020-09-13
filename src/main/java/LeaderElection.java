import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class LeaderElection implements Watcher {
    private static final String ZOOKEEPER_ADDRESS   = "localhost:2181";
    private static final int    SESSION_TIMEOUT     = 3000;
    private static final String ELECTION_NAMESPACE  = "/election";
    private ZooKeeper           zooKeeper;
    private String              currentZnodeName;

    public void connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS,SESSION_TIMEOUT,this);
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }


    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Connected successfully to zookeeper");
                } else {
                    synchronized (zooKeeper) {
                        System.out.println("Disconnecting from zookeeper");
                        zooKeeper.notifyAll();
                    }
                }
                break;
            case NodeDeleted:
                try {
                    reelectLeader();
                } catch (KeeperException e) {
                } catch (InterruptedException e) {
                }

        }
    }

    public void volunteerForLeadership() throws KeeperException, InterruptedException {
        String znodePrefix      = ELECTION_NAMESPACE + "/c_";
        String znodeFullPath    = zooKeeper.create(
                znodePrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL
        );
        System.out.println("Created znode: " +  znodeFullPath);
        this.currentZnodeName   = znodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
    }

    public void reelectLeader() throws KeeperException, InterruptedException {
        Stat    predecessorStat        = null;
        String  predecessorZNodeName   = "";

        while (predecessorStat == null) {
            List<String> childern      = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
            Collections.sort(childern);
            String       smallestChild = childern.get(0);
            if (smallestChild.equals(currentZnodeName)) {
                System.out.println("I have been selected as the leader!");
                return;
            } else {
                System.out.println("I am not the leader, " + smallestChild + " is.");
                int predecessorIndex = Collections.binarySearch(childern, currentZnodeName) - 1;
                predecessorZNodeName = childern.get(predecessorIndex);
                predecessorStat      = zooKeeper.exists(
                        ELECTION_NAMESPACE + "/" + predecessorZNodeName, this
                );
            }
        }
        System.out.println("Watching: " + predecessorZNodeName);
    }

    public static void main(String [] args) throws IOException, InterruptedException, KeeperException {
        LeaderElection leaderElection = new LeaderElection();
        leaderElection.connectToZookeeper();
        leaderElection.volunteerForLeadership();
        leaderElection.reelectLeader();
        leaderElection.run();
        leaderElection.close();
    }
}
