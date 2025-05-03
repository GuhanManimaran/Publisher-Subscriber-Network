import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class PublisherSubscriberGUI extends JFrame {

    private JTextField topicField;
    private JButton createTopicButton, subscribeButton, unsubscribeButton, publishButton, disconnectButton;
    private JTextArea messageArea;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Set<String> subscribedTopics = new HashSet<>();
    private JComboBox<String> topicDropdown;
    private volatile boolean running = true;


    public PublisherSubscriberGUI() {
        setTitle("Publisher Subscriber Network");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE); // Set main window background to white

        initializeUI();
        connectToServer();
    }

    private void initializeUI() {
        // Main vertical layout
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        // Section 1: Create & Select Topics
        JPanel createTopicPanel = new JPanel();
        createTopicPanel.setBackground(new Color(240, 240, 255)); // Light blue background
        createTopicPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(30, 144, 255)), "Create Topics"));

        topicField = new JTextField(15);
        topicField.setForeground(Color.BLACK);
        topicField.setBackground(new Color(230, 230, 250)); // Light lavender

        createTopicButton = new JButton("Create Topic");
        createTopicButton.setForeground(Color.WHITE);
        createTopicButton.setBackground(new Color(30, 144, 255)); // Dodger Blue
        createTopicButton.addActionListener(e -> handleCreateTopic());

        createTopicPanel.add(topicField);
        createTopicPanel.add(createTopicButton);

        // Section 2: Subscribe/Unsubscribe Multi-topic
        JPanel subPanel = new JPanel();
        subPanel.setBackground(new Color(255, 250, 205)); // Lemon chiffon
        subPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(255, 99, 71)), "Subscribe/Unsubscribe Topics"));

        topicDropdown = new JComboBox<>(new String[]{"Sports", "Politics", "Technology", "Weather"});
        topicDropdown.setBackground(new Color(255, 250, 240)); // Floral white
        topicDropdown.setForeground(Color.BLACK);

        subscribeButton = new JButton("Subscribe");
        subscribeButton.setForeground(Color.WHITE);
        subscribeButton.setBackground(new Color(34, 139, 34)); // Forest green
        subscribeButton.addActionListener(e -> handleSubscribe());

        unsubscribeButton = new JButton("Unsubscribe");
        unsubscribeButton.setForeground(Color.WHITE);
        unsubscribeButton.setBackground(new Color(255, 99, 71)); // Tomato
        unsubscribeButton.addActionListener(e -> handleUnsubscribe());

        subPanel.add(topicDropdown);
        subPanel.add(subscribeButton);
        subPanel.add(unsubscribeButton);

        // Section 3: Publish Messages
        JPanel publishPanel = new JPanel();
        publishPanel.setBackground(new Color(240, 248, 255)); // Alice blue
        publishPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(255, 165, 0)), "Publish Message"));

        publishButton = new JButton("Publish Message");
        publishButton.setForeground(Color.WHITE);
        publishButton.setBackground(new Color(255, 165, 0)); // Orange
        publishButton.addActionListener(e -> handlePublish());

        publishPanel.add(publishButton);

        // Section 4: Message Display
        JPanel messagePanel = new JPanel();
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 191, 255)), "Received Messages"));
        messageArea = new JTextArea(10, 50);
        messageArea.setEditable(false);
        messageArea.setForeground(Color.BLACK);
        messageArea.setBackground(new Color(240, 240, 255)); // Light blue
        JScrollPane scrollPane = new JScrollPane(messageArea);
        messagePanel.add(scrollPane);

        // Section 5: Disconnect Button
        JPanel disconnectPanel = new JPanel();
        disconnectPanel.setBackground(new Color(240, 255, 240)); // Honeydew
        disconnectPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(255, 20, 147)), "Disconnect"));

        disconnectButton = new JButton("Disconnect");
        disconnectButton.setForeground(Color.WHITE);
        disconnectButton.setBackground(new Color(255, 20, 147)); // Deep pink
        disconnectButton.addActionListener(e -> handleDisconnect());
        disconnectPanel.add(disconnectButton);

        // Add all sections to the main panel
        panel.add(createTopicPanel);
        panel.add(subPanel);
        panel.add(publishPanel);
        panel.add(messagePanel);
        panel.add(disconnectPanel);

        add(panel);
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            listenForMessages();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server.");
            e.printStackTrace();
        }
    }

    private void handleCreateTopic() {
        String topicName = topicField.getText().trim();
        if (!topicName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Created topic: " + topicName);
            topicDropdown.addItem(topicName);
        } else {
            JOptionPane.showMessageDialog(this, "Please enter a valid topic name.");
        }
    }

    private void handleSubscribe() {
        String topic = (String) topicDropdown.getSelectedItem();
        if (topic != null) {
            out.println("SUBSCRIBE " + topic);
            subscribedTopics.add(topic);
            JOptionPane.showMessageDialog(this, "Subscribed to: " + topic);
        }
    }

    private void handleUnsubscribe() {
        String topic = (String) topicDropdown.getSelectedItem();
        if (topic != null && subscribedTopics.contains(topic)) {
            out.println("UNSUBSCRIBE " + topic);
            subscribedTopics.remove(topic);
            JOptionPane.showMessageDialog(this, "Unsubscribed from: " + topic);
        }
    }

    private void handlePublish() {
        String msg = JOptionPane.showInputDialog(this, "Enter message to publish:");
        if (msg != null && !msg.trim().isEmpty()) {
            out.println("PUBLISH " + msg);
            SwingUtilities.invokeLater(() -> messageArea.append("Published: " + msg + "\n"));
        } else {
            JOptionPane.showMessageDialog(this, "Invalid message.");
        }
    }

    private void listenForMessages() {
        new Thread(() -> {
            try {
                String receivedMessage;
                while (running && in != null && (receivedMessage = in.readLine()) != null) {
                    final String finalReceivedMessage = receivedMessage;
                    SwingUtilities.invokeLater(() -> {
                        messageArea.append("Received: " + finalReceivedMessage + "\n");
                    });
                }
            } catch (IOException e) {
                if (running) { 
                    e.printStackTrace();
                }
            }
        }).start();
}


private void handleDisconnect() {
    try {
        if (socket != null) {
            running = false; // Signal the listener thread to stop
            out.println("DISCONNECT");
            out.flush();
            socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
            JOptionPane.showMessageDialog(this, "Disconnected successfully.");
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PublisherSubscriberGUI().setVisible(true));
    }
}
