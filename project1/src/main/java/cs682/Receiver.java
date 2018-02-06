package cs682;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import cs682.ChatMessages.Reply;
import java.util.concurrent.CopyOnWriteArrayList;

public class Receiver {
    //Data Structure found on: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html
    private static CopyOnWriteArrayList<ChatMessages.Chat> bcastHistory = new CopyOnWriteArrayList<>();


    public void startListening(String port){
        boolean running = true;
        ExecutorService threads = Executors.newFixedThreadPool(10);
        //String currentThreadId = String.valueOf(Thread.currentThread().getId());
        //System.out.println("Thread in Receiver: " + currentThreadId);
        try (
                ServerSocket receiverServer = new ServerSocket(Integer.parseInt(port))
        ){
            while(running){
                Socket receiverSock = receiverServer.accept();
                threads.submit(new ReceiverWorker(receiverSock));
            }
        } catch(IOException ie) {
            System.out.println("Unable to open the socket " + ie);
        }
    }

    public class ReceiverWorker implements Runnable {
        private Socket connectionSock;
        public ReceiverWorker(Socket sock){
            this.connectionSock = sock;
        }
        @Override
        public void run(){
            System.out.println("A sender connecting...");
            try(
                    InputStream inStream = connectionSock.getInputStream();
                    OutputStream outstream = connectionSock.getOutputStream();
            ){
                ChatMessages.Chat upcommingMssg = ChatMessages.Chat.parseDelimitedFrom(inStream);
                String from = upcommingMssg.getFrom();
                String message = upcommingMssg.getMessage();
                boolean is_bcast = upcommingMssg.getIsBcast();
                if(!is_bcast) {
                    System.out.println("The following message was received: ");
                    System.out.println(message);
                    System.out.println("from: " + from);
                } else {
                    addBcastToHistory(upcommingMssg);
                }
                Reply reply = createReply(200,"OK");
                reply.writeDelimitedTo(outstream);
                connectionSock.close();
            }catch(IOException ie){
                System.out.println("Unable to communicate with the sender" + ie);
            }
        }


        private  Reply createReply(int status, String message) {
            Reply reply = Reply.newBuilder()
                    .setStatus(status)
                    .setMessage(message)
                    .build();
            return reply;
        }
    }

    private void addBcastToHistory(ChatMessages.Chat message){
            bcastHistory.add(message);
    }

    public  void listBcastMessages(){
        System.out.println("Printing History: ");
        for (ChatMessages.Chat mssg: bcastHistory) {
            System.out.println(mssg.getMessage());
            System.out.println(mssg.getFrom());
            System.out.println("-------------------------------");
        }
    }
}
