package cs682;

import cs682.ChatMessages.Chat;
import cs682.ChatMessages.ZKData;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import com.google.protobuf.InvalidProtocolBufferException;



public class Sender {
    //kyrivero0000000475


    public void prepareMessage(String group, String receiverName, String message, boolean isBcast){
        String currentThreadId = String.valueOf(Thread.currentThread().getId());
        System.out.println("Finding receiver info");
        System.out.println(receiverName +" / " + message + " / " + group);
        System.out.println("Thread in Sender Finding Info: " + currentThreadId);
        ZooKeeper zk = cs682.Chat.connectToZK();
        try {
            Stat s = new Stat();
            byte[] raw = zk.getData(group + "/" + receiverName, false, s);
            if(raw != null) {
                ZKData  receiverInfo = ZKData.parseFrom(raw);
                String receiverIp = receiverInfo.getIp();
                String receiverPort = receiverInfo.getPort();
                byte[] chatMessage = createChatMessage(receiverName, message, isBcast);
                System.out.println("IP: " +receiverIp +" Port: "+ receiverPort + " Message: "+ chatMessage.toString());
                

            } else {
                System.out.println("Receiver not found");
            }
        } catch (InterruptedException ie) {
            System.out.println("Not able to fetch receiver data InterruptedException" + ie);
        } catch (KeeperException ke){
            System.out.println("Not able to fetch receiver data KeeperException" + ke);
        } catch (InvalidProtocolBufferException ipbe){
            System.out.println("Not able to fetch receiver data InvalidProtocolBufferException" + ipbe);
        }
    } // prepareMessage

    private byte[] createChatMessage(String receiver, String message, boolean isBcast){
        cs682.ChatMessages.Chat content = cs682.ChatMessages.Chat.newBuilder()
                .setFrom(receiver)
                .setMessage(message)
                .setIsBcast(isBcast)
                .build();
        return content.toByteArray();
    }

}
