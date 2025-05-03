# Publisher-Subscriber Messaging System in Java

This is a Java-based implementation of a **Publisher-Subscriber** architecture using sockets and a GUI for easy interaction.

## Project Description

This system demonstrates the **Pub/Sub messaging model**, where:
- **Publishers** send messages to topics.
- **Subscribers** receive messages from topics they are subscribed to.
- A **Broker Server** facilitates message delivery between publishers and subscribers.
- A **Graphical User Interface (GUI)** enables user-friendly operations.

## Components

- `BrokerServer.java`: Handles all publish-subscribe interactions.
- `Publisher.java`: Sends messages to specific topics.
- `Subscriber.java`: Listens to messages from subscribed topics.
- `PublisherSubscriberGUI.java`: Java Swing GUI for publishing/subscribing.

## Requirements

- Java JDK 8+
- Any Java IDE or command-line interface

## How to Run

1. **Compile all files**:
   ```bash
   javac *.java

