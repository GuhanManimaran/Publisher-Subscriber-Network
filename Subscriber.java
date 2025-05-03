import java.io.*;
import java.net.*;
import java.util.*;

public class Subscriber {
    private static final Set<String> topics = new HashSet<>();
    private static final Map<String, Set<PrintWriter>> subscribers = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server started, waiting for connections...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket socket;
        private final PrintWriter out;
        private final BufferedReader in;

        public ClientHandler(Socket clientSocket) throws IOException {
            this.socket = clientSocket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received command: " + inputLine);

                    if (inputLine.startsWith("SUBSCRIBE")) {
                        handleSubscribe(inputLine);
                    } else if (inputLine.startsWith("UNSUBSCRIBE")) {
                        handleUnsubscribe(inputLine);
                    } else if (inputLine.startsWith("PUBLISH")) {
                        handlePublish(inputLine);
                    } else if (inputLine.startsWith("DISCONNECT")) {
                        handleDisconnect();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleSubscribe(String command) {
            String topic = command.split(" ")[1];
            subscribers.putIfAbsent(topic, new HashSet<>());
            subscribers.get(topic).add(out);
            System.out.println("Client subscribed to topic: " + topic);
        }

        private void handleUnsubscribe(String command) {
            String topic = command.split(" ")[1];
            if (subscribers.containsKey(topic)) {
                subscribers.get(topic).remove(out);
                System.out.println("Client unsubscribed from topic: " + topic);
            }
        }

        private void handlePublish(String command) {
            String msg = command.substring(8).trim(); // Extract message after "PUBLISH "

            if (command.startsWith("PUBLISH")) {
                // Log the published message to the server console
                System.out.println("Received message to publish: " + msg);

                // Send a response back to the client to acknowledge message
                out.println("Published message: " + msg);
            }
        }


        private void handleDisconnect() {
            System.out.println("Client has disconnected.");
        }
    }
}
