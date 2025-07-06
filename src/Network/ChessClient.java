/*
 * Decompiled with CFR 0.152.
 */
package Network;

import Model.Position;
import Network.NetworkMessage;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChessClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String serverAddress;
    private int serverPort;
    private boolean connected;
    private NetworkMessageListener messageListener;
    private List<String> pgnMoves = new ArrayList<String>();
    private String playerName;

    public ChessClient(String string, int n) {
        this.serverAddress = string;
        this.serverPort = n;
        this.connected = false;
        this.playerName = "Guest";
    }

    public void setPlayerName(String string) {
        this.playerName = string;
    }

    public void setMessageListener(NetworkMessageListener networkMessageListener) {
        this.messageListener = networkMessageListener;
    }

    public boolean connect() {
        try {
            this.socket = new Socket(this.serverAddress, this.serverPort);
            this.out = new ObjectOutputStream(this.socket.getOutputStream());
            this.in = new ObjectInputStream(this.socket.getInputStream());
            this.connected = true;
            System.out.println("Connected to server at " + this.serverAddress + ":" + this.serverPort);
            this.sendMessage(new NetworkMessage(NetworkMessage.MessageType.PLAYER_INFO, this.playerName, 1));
            new Thread(this::listenForMessages).start();
            return true;
        }
        catch (IOException iOException) {
            System.err.println("Failed to connect to server: " + iOException.getMessage());
            return false;
        }
    }

    private void listenForMessages() {
        try {
            Thread.sleep(100L);
        }
        catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            return;
        }
        while (this.connected) {
            try {
                NetworkMessage networkMessage = (NetworkMessage)this.in.readObject();
                System.out.println("Client received: " + String.valueOf(networkMessage));
                this.handleMessage(networkMessage);
            }
            catch (EOFException eOFException) {
                System.out.println("Server disconnected (EOF)");
                break;
            }
            catch (IOException | ClassNotFoundException exception) {
                if (!this.connected) break;
                System.err.println("Error reading message: " + exception.getMessage());
                break;
            }
        }
        System.out.println("Client disconnecting from " + this.serverAddress + ":" + this.serverPort);
        this.disconnect();
    }

    private void handleMessage(NetworkMessage networkMessage) {
        if (this.messageListener == null) {
            return;
        }
        switch (networkMessage.getType()) {
            case MOVE: {
                String string = networkMessage.getMoveDescription();
                if (string != null && !string.isEmpty()) {
                    this.pgnMoves.add(string);
                    this.printPGNMoves();
                }
                this.messageListener.onMoveReceived(networkMessage.getFromPosition(), networkMessage.getToPosition(), networkMessage.getMoveDescription());
                break;
            }
            case GAME_START: {
                this.messageListener.onGameStart();
                break;
            }
            case GAME_END: {
                this.messageListener.onGameEnd(networkMessage.getGameResult());
                break;
            }
            case CONNECTION_ESTABLISHED: {
                this.messageListener.onConnectionEstablished(networkMessage.getData());
                break;
            }
            case ERROR: {
                this.messageListener.onError(networkMessage.getData());
                break;
            }
            case PING: {
                this.sendMessage(new NetworkMessage(NetworkMessage.MessageType.PING, "pong"));
                break;
            }
            case PLAYER_INFO: {
                this.messageListener.onPlayerInfoReceived(networkMessage.getPlayerName());
            }
        }
    }

    public void sendMove(Position position, Position position2) {
        NetworkMessage networkMessage = new NetworkMessage(NetworkMessage.MessageType.MOVE, position, position2);
        this.sendMessage(networkMessage);
    }

    private void sendMessage(NetworkMessage networkMessage) {
        if (this.connected && this.out != null) {
            try {
                this.out.writeObject(networkMessage);
                this.out.flush();
            }
            catch (IOException iOException) {
                System.err.println("Error sending message: " + iOException.getMessage());
                this.disconnect();
            }
        }
    }

    public void disconnect() {
        this.connected = false;
        try {
            if (this.out != null) {
                this.out.close();
            }
            if (this.in != null) {
                this.in.close();
            }
            if (this.socket != null) {
                this.socket.close();
            }
        }
        catch (IOException iOException) {
            System.err.println("Error closing connection: " + iOException.getMessage());
        }
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void printPGNMoves() {
        System.out.println("\nPGN Move List:");
        for (int i = 0; i < this.pgnMoves.size(); ++i) {
            if (i % 2 == 0) {
                System.out.print(i / 2 + 1 + ". ");
            }
            System.out.print(this.pgnMoves.get(i) + " ");
            if (i % 2 != 1) continue;
            System.out.println();
        }
        System.out.println();
    }

    public void sendGameOver() {
        sendMessage(new NetworkMessage(NetworkMessage.MessageType.RESIGNATION, "Opponent resigned"));
        disconnect();
    }

    public static interface NetworkMessageListener {
        public void onMoveReceived(Position var1, Position var2, String var3);

        public void onGameStart();

        public void onGameEnd(String var1);

        public void onConnectionEstablished(String var1);

        public void onError(String var1);

        public void onPlayerInfoReceived(String var1);
    }
}
