package cs682;

import cs682.ChatMessages.ZKData;
import cs682.ChatMessages.Reply;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.InputStream;
import java.io.OutputStream;

public class Sender {

    public void prepareMessage( String receiverName, String message, boolean isBcast){
        String group = ZkeeperHandler.GROUP;
        String member = ZkeeperHandler.MEMBER;
        ZooKeeper zk = ZkeeperHandler.zk;
        //ZooKeeper zk = Chat.getZkConnection();
        try {
            Stat s = new Stat();
            byte[] raw = zk.getData(group + "/" + receiverName, false, s);
            if(raw != null) {
                ZKData  receiverInfo = ZKData.parseFrom(raw);
                InetAddress receiverIp = InetAddress.getByName(receiverInfo.getIp());
                int receiverPort = Integer.parseInt(receiverInfo.getPort());
                ChatMessages.Chat chatMessage = createChatMessage(member, message, isBcast);
                System.out.println("Ready to transfer message to " + receiverName + " broadcast: " + isBcast);
                transferMessage(receiverIp, receiverPort, chatMessage);
            } else {
                System.out.println("Receiver not found");
            }
        } catch (InterruptedException ie) {
            System.out.println("Not able to fetch receiver data InterruptedException" + ie);
        } catch (KeeperException ke){
            System.out.println("Not able to fetch receiver data KeeperException" + ke);
        } catch (InvalidProtocolBufferException ipbe){
            System.out.println("Not able to fetch receiver data InvalidProtocolBufferException" + ipbe);
        } catch (UnknownHostException uhe){
            System.out.println("Not able to fetch receiver data UnknownHostException" + uhe);
        }
    } // prepareMessage

    private ChatMessages.Chat createChatMessage(String receiver, String message, boolean isBcast){
        ChatMessages.Chat chatMessage = ChatMessages.Chat.newBuilder()
                .setFrom(receiver)
                .setMessage(message)
                .setIsBcast(isBcast)
                .build();
        return chatMessage;
    }

    private void transferMessage(InetAddress ip, int port,  ChatMessages.Chat chatMessage){
        try (
                Socket senderSock = new Socket(ip,port);
                OutputStream outStream = senderSock.getOutputStream();
                InputStream inStream = senderSock.getInputStream();
        ) {
            chatMessage.writeDelimitedTo(outStream);
            Reply receiverReply = Reply.getDefaultInstance();
            receiverReply = receiverReply.parseDelimitedFrom(inStream);
            System.out.println("Reply: " + receiverReply);
            senderSock.getInputStream();
            //senderSock.close();

        }catch(IOException io){
            System.out.println("Unable to transfer the message to " + ip);
        }
    }

}
