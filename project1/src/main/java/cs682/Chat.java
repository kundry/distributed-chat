package cs682;
import java.util.Scanner;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Chat {

    public static final int PORT = 9000;
    public static final String HOST = "localhost";

    public static void main(String[] args) throws IOException, InterruptedException {

        Scanner input = new Scanner(System.in);
        String fullCommand, command;

        System.out.println("Welcome to Chat Peer!");
        //Commands:  list, send, sendAll, get, help, exit
        //System.out.println("Insert PORT: ");
        //System.out.println("Insert HOST NAME: ");

        //Registering
        String group = "/zkdemo";
        String member = "/kyrivero";
        String data = "placeholder";

        //Connect to ZK instance
        final CountDownLatch connectedSignal = new CountDownLatch(1);
        ZooKeeper zk = new ZooKeeper(HOST + ":" + PORT, 1000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    connectedSignal.countDown();
                }
            }
        });
        System.out.println("Connecting...");
        connectedSignal.await();
        System.out.println("Connected");

        //to join a group
        try {
            String createdPath = zk.create(group + member,
                    data.getBytes(),  //probably should be something more interesting here...
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT_SEQUENTIAL);
            System.out.println("Joined group " + group + member);
            System.out.println("Created Path " + createdPath);

        } catch(KeeperException ke) {
            System.out.println("Unable to join group " + group + " as " + member);
        }



        // UI
        System.out.print("Enter a command: ");
        fullCommand = input.next();
        System.out.println("Received: " + fullCommand);
        String[] arg = fullCommand.split("\\s");
        command = arg[0];

        switch (command) {
            case "list":
                System.out.println("List");
                break;
            case "send":
                System.out.println("Send");
                break;
            default:
                break;
        }




    }

}
