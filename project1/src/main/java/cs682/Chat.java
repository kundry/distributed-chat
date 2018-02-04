package cs682;

import java.util.*;
import org.apache.zookeeper.data.Stat;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.zookeeper.*;
import cs682.ChatMessages.ZKData;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.net.*;


public class Chat {

    public static final int PORT = 9000;
    public static final String HOST = "localhost";
    public static final ArrayList<String> CHAT_COMMANDS = new ArrayList<String>();

    public static void main(String[] args) {

        CHAT_COMMANDS.add("list");
        CHAT_COMMANDS.add("send");
        CHAT_COMMANDS.add("sendAll");
        //CHAT_COMMANDS.add("get");
        CHAT_COMMANDS.add("help");
        CHAT_COMMANDS.add("exit");

        Scanner input = new Scanner(System.in);
        String fullCommand, command;
        String group = "/CS682_Test"; // String group = "/zkdemo";
        String member = "/hey"; //taken from args user kyrivero
        String port = "9007"; // taken from args port
        String myIP = getLocalHostIp();
        ZKData data = createZKData(myIP,port);

        System.out.println("Welcome to Chat Peer!");

        //Getting user and port from the args


        //Start Listening
        new Thread(new Runnable() {
            @Override
            public void run() {
                new Receiver().startListening();
            }
        }).start();

        //Registering
        ZooKeeper zk = connectToZK();
        joinGroup(zk, group, member, data);

        // UI
        String currentThreadId = String.valueOf(Thread.currentThread().getId());
        System.out.println("Main Thread: " + currentThreadId);

        while(true) {
            System.out.print("Enter a command: ");
            fullCommand = input.nextLine();
            System.out.println("Received: " + fullCommand);
            String[] fullCommandSplit = fullCommand.split("\\s");
            command = fullCommandSplit[0];

            if (!CHAT_COMMANDS.contains(command)) {
                System.out.println("Command not found!");
            } else {
                switch (command) {
                    case "list":
                        listNodes(zk, group);
                        break;
                    case "send":
                        System.out.println("Send");
                        System.out.println(fullCommandSplit[1]);
                        System.out.println(fullCommandSplit[2]);
                        String name = fullCommandSplit[1];
                        String message = fullCommandSplit[2];
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                new Sender().prepareMessage(group, name, message, false);
                            }
                        }).start();
                        break;
                    default:
                        break;
                }
            }
        }// end while
    }//main

    public static ZooKeeper connectToZK() {
        final CountDownLatch connectedSignal = new CountDownLatch(1);
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(HOST + ":" + PORT, 1000, new Watcher() {
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
        } catch(InterruptedException ie){
            System.out.println("Unable connect to ZooKeeper" + ie );
        } catch (IOException ioe){
            System.out.println("Unable connect to ZooKeeper IOException " + ioe);
        }
        return zk;
    }

    public static ZKData createZKData(String ip, String port) {
        ZKData regInfo = ZKData.newBuilder()
                .setIp(ip)
                .setPort(port)
                .build();
        return regInfo;
    }

    public static void joinGroup(ZooKeeper zk, String group, String member, ZKData data){
        try {
            String createdPath = zk.create(group + member,
                    data.toByteArray(),  //ip and port
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
                Stat s = new Stat();
                byte[] raw = zk.getData(group + "/" + child, false, s);
                if(raw != null) {
                    String nodeInfo = new String(raw);
                    System.out.println(nodeInfo);
//                    ZKData nodeInfo = ZKData.parseFrom(raw);
//                    String nodeIp = nodeInfo.getIp();
//                    String nodePort = nodeInfo.getPort();
//                    System.out.println("Ip: "+ nodeIp);
//                    System.out.println("Port: "+ nodePort);
//                    System.out.printf("\n");
                } else {
                    System.out.println("\tNO DATA");
                }
            }
        } catch(KeeperException ke) {
            System.out.println("Unable to list members of group: KeeperException ");
        }catch(InterruptedException ie){
            System.out.println("Unable to list members of the group: InterruptedException ");
        }
//        catch(InvalidProtocolBufferException ipbe){
//            System.out.println("Unable to list members of the group: InvalidProtocolBufferException");
//        }
    }

    private static String getLocalHostIp(){
        String myIp = "";
        try{
            InetAddress localhost = InetAddress.getLocalHost();
            myIp = localhost.getHostAddress();
        } catch( UnknownHostException uhe) {
            System.out.println("Unable to get the local IP Address" + uhe);
        }
        return myIp;
    }

}//Chat
