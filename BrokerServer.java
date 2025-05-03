import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BrokerServer {

    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private static Map<String, Set<ClientHandler>> topicSubscribers = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server is listening on port 12345...");
            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.submit(new ClientHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private Set<String> subscribedTopics = new HashSet<>();

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String command;
                while ((command = in.readLine()) != null) {
                    System.out.println("Command received: " + command);

                    if (command.startsWith("SUBSCRIBE ")) {
                        handleSubscribe(command.split(" ")[1]);
                    } else if (command.startsWith("UNSUBSCRIBE ")) {
                        handleUnsubscribe(command.split(" ")[1]);
                    } else if (command.startsWith("PUBLISH ")) {
                        handlePublish(command.substring(8));
                    } else if (command.equals("DISCONNECT")) {
                        handleDisconnect();
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected unexpectedly.");
            } finally {
                cleanup();
            }
        }

        private void handleSubscribe(String topic) {
            subscribedTopics.add(topic);
            topicSubscribers.computeIfAbsent(topic, k -> new HashSet<>()).add(this);
            System.out.println("Subscribed to: " + topic);
        }

        private void handleUnsubscribe(String topic) {
            subscribedTopics.remove(topic);
            if (topicSubscribers.containsKey(topic)) {
                topicSubscribers.get(topic).remove(this);
                if (topicSubscribers.get(topic).isEmpty()) {
                    topicSubscribers.remove(topic);
                }
            }
            System.out.println("Unsubscribed from: " + topic);
        }

        private void handlePublish(String message) {
            String[] parts = message.split(" ", 2);
            if (parts.length == 2) {
                String topic = parts[0];
                String content = parts[1];
                if (topicSubscribers.containsKey(topic)) {
                    for (ClientHandler client : topicSubscribers.get(topic)) {
                        client.sendMessage("Received on topic [" + topic + "]: " + content);
                    }
                }
            }
        }

        private void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        private void handleDisconnect() {
            for (String topic : subscribedTopics) {
                if (topicSubscribers.containsKey(topic)) {
                    topicSubscribers.get(topic).remove(this);
                    if (topicSubscribers.get(topic).isEmpty()) {
                        topicSubscribers.remove(topic);
                    }
                }
            }
            System.out.println("Disconnected client.");
        }

        private void cleanup() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
