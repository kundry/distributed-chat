package cs682;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import cs682.ChatMessages.Reply;

public class Receiver {
    public static final int PORT = 9007;
    public static  boolean running = true;

    public void startListening(){
        final ExecutorService threads = Executors.newFixedThreadPool(10);
        try (
                ServerSocket receiverServer = new ServerSocket(PORT);
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
                //Parse the message and show it

                // send reply Fix Parameters
                Reply reply = createReply(200,"OK");

            }catch(IOException ie){
                System.out.println("Unable to communicate with the sender" + ie);
            }
        }
        public  Reply createReply(int status, String message) {
            Reply reply = Reply.newBuilder()
                    .setStatus(status)
                    .setMessage(message)
                    .build();
            return reply;
        }
    }


}
