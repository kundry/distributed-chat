package cs682;
import java.util.Scanner;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import cs682.ChatMessages.ZKData;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Chat {

    public static final int PORT = 9000;
    public static final String HOST = "localhost";

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner input = new Scanner(System.in);
        String fullCommand, command;
        String group = "/CS682_Test"; // String group = "/zkdemo";
        String member = "/kyrivero";
        byte[] data = createZKData("localhost","9000");

        System.out.println("Welcome to Chat Peer!");
        //Commands:  list, send, sendAll, get, help, exit
        //System.out.println("Insert PORT: ");
        //System.out.println("Insert HOST NAME: ");

        //Connecting Joining
        ZooKeeper zk = connectToZK();
        joinGroup(zk, group, member, data);

        // UI
        System.out.print("Enter a command: ");
        fullCommand = input.nextLine();
        System.out.println("Received: " + fullCommand);
        String[] arg = fullCommand.split("\\s");
        command = arg[0];

        switch (command) {
            case "list":
                listNodes(zk,group);
                break;
            case "send":
                System.out.println("Send");
                System.out.println(arg[1]);
                System.out.println(arg[2]);
                break;
            default:
                break;
        }

    }//main

    public static ZooKeeper connectToZK() throws IOException, InterruptedException {
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
        return zk;
    }

    public static byte[] createZKData(String ip, String port) {
        ZKData regInfo = ZKData.newBuilder()
                .setIp(ip)
                .setPort(port)
                .build();
        byte[] zkDataObj = regInfo.toByteArray();
        return zkDataObj;
    }

    public static void joinGroup(ZooKeeper zk, String group, String member, byte[] data){
        try {
            String createdPath = zk.create(group + member,
                    data,  //ip and port
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT_SEQUENTIAL);
            System.out.println("Joined group " + group + member);
            System.out.println("Created Path " + createdPath);

        } catch(KeeperException ke) {
            System.out.println("Unable to join group " + group + " as " + member);
        }catch(InterruptedException ie){
            System.out.println("Unable to join group " + group + " as " + member + "InterruptedException");
        }
    }

    public static void listNodes(ZooKeeper zk, String group){
        try {
            List<String> children = zk.getChildren(group, false);
            System.out.println(group + " group has the following members: ");
            for(String child: children) {
                System.out.println("Node: "+ child);
//                Stat s = new Stat();
//                byte[] raw = zk.getData(group + "/" + child, false, s);
//                if(raw != null) {
//                    ZKData nodeInfo = ZKData.parseFrom(raw);
//                    String nodeIp = nodeInfo.getIp();
//                    String nodePort = nodeInfo.getPort();
//                    System.out.println("Ip: "+ nodeIp);
//                    System.out.println("Port: "+ nodePort);
//                    System.out.printf("\n");
//                } else {
//                    System.out.println("\tNO DATA");
//                }
            }
        } catch(KeeperException ke) {
            System.out.println("Unable to list members of group " + group);
        }catch(InterruptedException ie){
            System.out.println("Unable to list members of the group: InterruptedException ");
        }
//        catch(InvalidProtocolBufferException ipbe){
//            System.out.println("Unable to list members of the group: InvalidProtocolBufferException");
//        }
    }

}//Chat
