package cs682;
import java.util.Scanner;

public class Chat {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);
        String fullCommand, command;

        System.out.println("Welcome to Chat Peer!");
        //Commands:  list, send, sendAll, get, help, exit
        //System.out.println("Insert PORT: ");
        //System.out.println("Insert HOST NAME: ");

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
