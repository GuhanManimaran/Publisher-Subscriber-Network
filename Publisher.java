import java.io.*;
import java.net.*;

public class Publisher {
    private static final String BROKER_ADDRESS = "localhost";
    private static final int BROKER_PORT = 8080;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(BROKER_ADDRESS, BROKER_PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.println("Publisher");  // Sending role as Publisher
        out.println("Topic1");     // Topic to publish to

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String message;
        while (true) {
            System.out.print("Enter message to publish: ");
            message = reader.readLine();
            out.println(message);  // Send the message to the broker
            System.out.println("PUBLISHER: " + message);  // Display published message with heading
        }
    }
}