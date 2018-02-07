package cs682;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import cs682.ChatMessages.ZKData;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import org.apache.zookeeper.data.Stat;


public class ZkeeperHandler {

    public static final  String GROUP = "/CS682_Chat";  // "/zkdemo" or "CS682_Test" "CS682_Chat"
    public static String MEMBER;
    public static String MEMBER_PORT;
    public static ZooKeeper zk;


    //ZooKeeper API
    public  void joinGroup(){
        String myIP = getLocalHostIp();
        ZKData data = createZKData(myIP,MEMBER_PORT);
        try {
            String createdPath = zk.create(GROUP + MEMBER,
                    data.toByteArray(),  //ip and port
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println("Joined group " + GROUP + MEMBER);
            System.out.println("Created Path " + createdPath);

        } catch(KeeperException ke) {
            System.out.println("Unable to join group " + GROUP + " as " + MEMBER);
        }catch(InterruptedException ie){
            System.out.println("Unable to join group " + GROUP + " as " + MEMBER + "InterruptedException");
        }
    }

    private ZKData createZKData(String ip, String port) {
        ChatMessages.ZKData regInfo = ChatMessages.ZKData.newBuilder()
                .setIp(ip)
                .setPort(port)
                .build();
        return regInfo;
    }

    private String getLocalHostIp(){
        String myIp = "";
        try{
            InetAddress localhost = InetAddress.getLocalHost();
            myIp = localhost.getHostAddress();
        } catch( UnknownHostException uhe) {
            System.out.println("Unable to get the local IP Address" + uhe);
        }
        return myIp;
    }

    public void listNodes(){
        try {
            List<String> children = zk.getChildren(GROUP, false);
            System.out.println(GROUP + " group has the following members: ");
            for(String child: children) {
                System.out.println("Node: "+ child);
                Stat s = new Stat();
                byte[] raw = zk.getData(GROUP + "/" + child, false, s);
                if(raw != null) {
                    String nodeInfo = new String(raw);
                    System.out.println(nodeInfo);
                } else {
                    System.out.println("\tNO DATA");
                }
            }
        } catch(KeeperException ke) {
            System.out.println("Unable to list members of group: KeeperException ");
        }catch(InterruptedException ie){
            System.out.println("Unable to list members of the group: InterruptedException ");
        }
    }

    public List<String> getNodesNameList(){
        List<String> children = null;
        try {
            children = zk.getChildren(GROUP, false);
        } catch(KeeperException ke) {
            System.out.println("Unable to list members of group: KeeperException ");
        }catch(InterruptedException ie){
            System.out.println("Unable to list members of the group: InterruptedException ");
        }
        return children;
    }

}
