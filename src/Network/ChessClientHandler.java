/*
 * Decompiled with CFR 0.152.
 */
package Network;

import Network.ChessServer;
import Network.NetworkMessage;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ChessClientHandler
implements Runnable {
    private Socket clientSocket;
    private ChessServer server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int playerColor;
    private boolean connected;

    public ChessClientHandler(Socket socket, ChessServer chessServer) {
        this.clientSocket = socket;
        this.server = chessServer;
        this.connected = true;
        this.playerColor = -1;
    }

    @Override
    public void run() {
        try {
            this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());
            this.in = new ObjectInputStream(this.clientSocket.getInputStream());
            this.server.registerPlayer(this);
            Thread.sleep(100L);
            while (this.connected) {
                try {
                    NetworkMessage networkMessage = (NetworkMessage)this.in.readObject();
                    System.out.println("Server received: " + String.valueOf(networkMessage));
                    this.handleMessage(networkMessage);
                }
                catch (EOFException eOFException) {
                    System.out.println("Client disconnected (EOF): " + String.valueOf(this.clientSocket.getInetAddress()));
                    break;
                }
                catch (ClassNotFoundException classNotFoundException) {
                    System.err.println("Invalid message received: " + classNotFoundException.getMessage());
                }
                catch (IOException iOException) {
                    if (this.connected) {
                        System.err.println("IO Error reading message: " + iOException.getMessage());
                    }
                    break;
                }
            }
        }
        catch (IOException iOException) {
            System.err.println("Error handling client: " + iOException.getMessage());
        }
        catch (InterruptedException interruptedException) {
            System.err.println("Thread interrupted: " + interruptedException.getMessage());
        }
        finally {
            this.disconnect();
        }
    }

    private void handleMessage(NetworkMessage message) {
        switch (message.getType()) {
            case MOVE:
                if (message.getFromPosition() != null && message.getToPosition() != null) {
                    server.handleMove(this, message.getFromPosition(), message.getToPosition());
                }
                break;
            case PLAYER_INFO:
                server.setPlayerName(this, message.getPlayerName());
                break;
            case RESIGNATION:
                server.handleResignation(this);
                break;
            case PING:
                sendMessage(new NetworkMessage(NetworkMessage.MessageType.PING, "pong"));
                break;
            default:
                System.out.println("Received message: " + message);
                break;
        }
    }

    public void sendMessage(NetworkMessage networkMessage) {
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

    public void setColor(int n) {
        this.playerColor = n;
    }

    public int getColor() {
        return this.playerColor;
    }

    public void disconnect() {
        disconnect(false);
    }
    
    public void disconnect(boolean skipServerNotification) {
        connected = false;
        if (!skipServerNotification) {
            server.removePlayer(this);
        }
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return this.connected;
    }
}
