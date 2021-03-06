package cs682;

import java.util.*;
import org.apache.zookeeper.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Chat {
    public static final int PORT = 2181; //2181 9000
    public static final String HOST = "mc01"; //mc01 localhost
    private static final ArrayList<String> CHAT_COMMANDS = new ArrayList<>();
    private static boolean RUNNING = true;
    private static ExecutorService bcastThreads = Executors.newFixedThreadPool(30);

    public static void main(String[] args) {
        //Setting available commands
        CHAT_COMMANDS.add("list");
        CHAT_COMMANDS.add("send");
        CHAT_COMMANDS.add("sendAll");
        CHAT_COMMANDS.add("showHistory");
        CHAT_COMMANDS.add("help");
        CHAT_COMMANDS.add("exit");

        //Getting args
        String member = "";
        String port = "";

        for (int i= 0; i< args.length; i++){
            if(args[i].equals("-user")){
                member = "/" + args[i+1];
            }
            else if (args[i].equals("-port")){
                    port = args[i+1];
                }
        }

        System.out.println("Member: " + member);
        System.out.println("Port: " + port);
        ZkeeperHandler.MEMBER = member;
        ZkeeperHandler.MEMBER_PORT = port;


        //Start Listening
        Receiver receiverInit = new Receiver();
        new Thread(new Runnable() {
            @Override
            public void run() {
                receiverInit.startListening(ZkeeperHandler.MEMBER_PORT);
            }
        }).start();

        //Registering
        ZooKeeper zk = getZkConnection();
        ZkeeperHandler.zk = zk;
        ZkeeperHandler zkHandler = new ZkeeperHandler();
        zkHandler.joinGroup();

        // UI
        System.out.println("Welcome to Chat Peer!");
        Scanner input = new Scanner(System.in);
        String fullCommand, command;
        String currentThreadId = String.valueOf(Thread.currentThread().getId());
        System.out.println("Main Thread: " + currentThreadId);

        while(RUNNING) {
            System.out.print("Enter a command: ");
            fullCommand = input.nextLine();
            String[] fullCommandSplit = fullCommand.split("\\s");
            command = fullCommandSplit[0];

            if (!CHAT_COMMANDS.contains(command)) {
                System.out.println("Command not found! Check right syntax. Type help");
            } else {
                switch (command) {
                    case "list":
                        zkHandler.listNodes();
                        break;
                    case "send":
                        sendOneMessage(fullCommandSplit);
                        break;
                    case "sendAll":
                        List<String> nodesNamesList = zkHandler.getNodesNameList();
                        sendBcastMessage(fullCommandSplit , nodesNamesList);
                        break;
                    case "showHistory":
                        Receiver receiver = new Receiver();
                        receiver.listBcastMessages();
                        break;
                    case "help":
                        showCommandsHelp();
                        break;
                    case "exit":
                        System.out.println("Exiting ....");
                        RUNNING = false;
                        shutdownSender();
                        receiverInit.shutdownReceiver();
                        //System.exit(0);
                        break;
                    default:
                        break;
                }
            }
        }// end while
    }//main

    public static ZooKeeper getZkConnection() {
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

    private static void sendOneMessage(String[] arguments){
        String name = arguments[1];
        StringBuilder message = new StringBuilder();
        message.append(arguments[2]);
        if (arguments.length > 3) {
            for (int i = 3; i < arguments.length; i++) {
                message.append(" ").append(arguments[i]);
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                new Sender().prepareMessage(name, message.toString(), false);
            }
        }).start();
    }

    private static void sendBcastMessage(String[] arguments, List<String> nodesNamesList){
        StringBuilder bcastMessage = new StringBuilder();
        bcastMessage.append(arguments[1]);
        if (arguments.length > 2) {
            for (int i = 2; i < arguments.length; i++) {
                bcastMessage.append(" ").append(arguments[i]);
            }
        }

        for(String node: nodesNamesList){
            bcastThreads.submit(new Thread(new Runnable() {
                @Override
                public void run() {
                    new Sender().prepareMessage(node, bcastMessage.toString(), true);
                }
            }));
        }

    }

    private static void showCommandsHelp(){
        System.out.println("list");
        System.out.println("send <name> <message>");
        System.out.println("sendAll <message>");
        System.out.println("help");
        System.out.println("exit");
    }

    private static void shutdownSender(){
        System.out.println("Shuting down the Sender ...");
        bcastThreads.shutdown();
        try {
            bcastThreads.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Unable to do await Termination");
        }
    }


}//Chat
