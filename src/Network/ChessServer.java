/*
 * Decompiled with CFR 0.152.
 */
package Network;

import Model.Board;
import Model.DatabaseManager;
import Model.GameState;
import Model.Move;
import Model.Piece;
import Model.PieceColor;
import Model.Position;
import Network.ChessClientHandler;
import Network.NetworkMessage;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChessServer {
    private static final int DEFAULT_PORT = 8080;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private boolean running;
    private GameState gameState;
    private ChessClientHandler whitePlayer;
    private ChessClientHandler blackPlayer;
    private boolean hostRegistered = false;
    private String whitePlayerName = "Guest";
    private String blackPlayerName = "Guest";
    private boolean gameStarted = false;

    public ChessServer() {
        this(8080);
    }

    public ChessServer(int n) {
        try {
            this.serverSocket = new ServerSocket(n);
            this.executorService = Executors.newCachedThreadPool();
            this.running = false;
            this.gameState = new GameState();
            this.hostRegistered = false;
            
            // Initialize databases
            DatabaseManager.initializeDatabases();
            
            System.out.println("Chess server started on port " + n);
        }
        catch (IOException iOException) {
            System.err.println("Failed to start server: " + iOException.getMessage());
        }
    }

    public void start() {
        this.running = true;
        System.out.println("Waiting for players to connect...");
        while (this.running) {
            try {
                Socket socket = this.serverSocket.accept();
                System.out.println("New client connected: " + String.valueOf(socket.getInetAddress()));
                ChessClientHandler chessClientHandler = new ChessClientHandler(socket, this);
                this.executorService.execute(chessClientHandler);
            }
            catch (IOException iOException) {
                if (!this.running) continue;
                System.err.println("Error accepting client: " + iOException.getMessage());
            }
        }
    }

    public synchronized void registerHostPlayer() {
        if (this.whitePlayer == null && !this.hostRegistered) {
            this.hostRegistered = true;
            System.out.println("Host player registered as WHITE");
        }
    }

    public synchronized void registerPlayer(ChessClientHandler chessClientHandler) {
        System.out.println("Registering player... White: " + (this.whitePlayer != null) + ", Black: " + (this.blackPlayer != null) + ", Host: " + this.hostRegistered);
        if (this.whitePlayer == null && !this.hostRegistered) {
            this.whitePlayer = chessClientHandler;
            chessClientHandler.setColor(1);
            System.out.println("White player connected");
            chessClientHandler.sendMessage(new NetworkMessage(NetworkMessage.MessageType.CONNECTION_ESTABLISHED, "You are playing as WHITE"));
        } else if (this.blackPlayer == null) {
            this.blackPlayer = chessClientHandler;
            chessClientHandler.setColor(0);
            System.out.println("Black player connected");
            chessClientHandler.sendMessage(new NetworkMessage(NetworkMessage.MessageType.CONNECTION_ESTABLISHED, "You are playing as BLACK"));
            this.exchangePlayerInfo();
            this.startGame();
        } else {
            System.out.println("Game is full, rejecting connection");
            chessClientHandler.sendMessage(new NetworkMessage(NetworkMessage.MessageType.ERROR, "Game is full"));
            chessClientHandler.disconnect();
        }
    }

    public synchronized void setPlayerName(ChessClientHandler chessClientHandler, String string) {
        if (chessClientHandler == this.whitePlayer) {
            this.whitePlayerName = string;
            System.out.println("White player name set to: " + string);
        } else if (chessClientHandler == this.blackPlayer) {
            this.blackPlayerName = string;
            System.out.println("Black player name set to: " + string);
        }
    }

    private void exchangePlayerInfo() {
        if (this.blackPlayer != null) {
            this.blackPlayer.sendMessage(new NetworkMessage(NetworkMessage.MessageType.PLAYER_INFO, this.whitePlayerName, 1));
        }
        if (this.whitePlayer != null) {
            this.whitePlayer.sendMessage(new NetworkMessage(NetworkMessage.MessageType.PLAYER_INFO, this.blackPlayerName, 1));
        }
    }

    private void startGame() {
        System.out.println("Both players connected. Starting game...");
        gameStarted = true;
        NetworkMessage networkMessage = new NetworkMessage(NetworkMessage.MessageType.GAME_START, "Game started");
        System.out.println("Sending game start to white player");
        this.whitePlayer.sendMessage(networkMessage);
        System.out.println("Sending game start to black player");
        this.blackPlayer.sendMessage(networkMessage);
        System.out.println("Game started successfully");
    }

    public synchronized void handleMove(ChessClientHandler chessClientHandler, Position position, Position position2) {
        if (chessClientHandler.getColor() != this.gameState.getCurrentPlayerColor()) {
            String string = "Not your turn. Current player: " + (this.gameState.getCurrentPlayerColor() == 1 ? "White" : "Black") + ", You are: " + (chessClientHandler.getColor() == 1 ? "White" : "Black");
            chessClientHandler.sendMessage(new NetworkMessage(NetworkMessage.MessageType.ERROR, string));
            return;
        }
        Board board = this.gameState.getBoard();
        Piece piece = board.getPiece(position);
        if (piece == null) {
            chessClientHandler.sendMessage(new NetworkMessage(NetworkMessage.MessageType.ERROR, "No piece at position " + String.valueOf(position)));
            return;
        }
        if (piece.getColor() != chessClientHandler.getColor()) {
            chessClientHandler.sendMessage(new NetworkMessage(NetworkMessage.MessageType.ERROR, "Cannot move opponent's piece"));
            return;
        }
        Piece piece2 = board.getPiece(position2);
        Move move = Move.createMove(position, position2, piece, piece2);
        System.out.println(move.toString());
        if (this.gameState.makeMove(move)) {
            boolean bl;
            int n = PieceColor.opponent(chessClientHandler.getColor());
            boolean bl2 = this.gameState.isInCheck(n);
            boolean bl3 = bl = this.gameState.isGameOver() && this.gameState.getGameResult().contains("checkmate");
            if (bl) {
                move = move.withFlag(2);
            } else if (bl2) {
                move = move.withFlag(1);
            }
            NetworkMessage networkMessage = new NetworkMessage(NetworkMessage.MessageType.MOVE, position, position2, move.toString());
            this.broadcastMessage(networkMessage);
            if (this.gameState.isGameOver()) {
                // Store completed game in database
                storeCompletedGame();
                NetworkMessage networkMessage2 = new NetworkMessage(NetworkMessage.MessageType.GAME_END, this.gameState.getGameResult(), true);
                this.broadcastMessage(networkMessage2);
                this.stop();
            }
        } else {
            chessClientHandler.sendMessage(new NetworkMessage(NetworkMessage.MessageType.ERROR, "Invalid move"));
        }
    }

    public synchronized void handleHostMove(Position position, Position position2) {
        if (this.gameState.getCurrentPlayerColor() != 1) {
            return;
        }
        Board board = this.gameState.getBoard();
        Piece piece = board.getPiece(position);
        if (piece == null || piece.getColor() != 1) {
            return;
        }
        Piece piece2 = board.getPiece(position2);
        Move move = Move.createMove(position, position2, piece, piece2);
        if (this.gameState.makeMove(move)) {
            boolean bl;
            int n = PieceColor.opponent(1);
            boolean bl2 = this.gameState.isInCheck(n);
            boolean bl3 = bl = this.gameState.isGameOver() && this.gameState.getGameResult().contains("checkmate");
            if (bl) {
                move = move.withFlag(2);
            } else if (bl2) {
                move = move.withFlag(1);
            }
            NetworkMessage networkMessage = new NetworkMessage(NetworkMessage.MessageType.MOVE, position, position2, move.toString());
            this.broadcastMessage(networkMessage);
            if (this.gameState.isGameOver()) {
                // Store completed game in database
                storeCompletedGame();
                NetworkMessage networkMessage2 = new NetworkMessage(NetworkMessage.MessageType.GAME_END, this.gameState.getGameResult(), true);
                this.broadcastMessage(networkMessage2);
                this.stop();
            }
        }
    }

    private void broadcastMessage(NetworkMessage networkMessage) {
        if (this.whitePlayer != null) {
            this.whitePlayer.sendMessage(networkMessage);
        }
        if (this.blackPlayer != null) {
            this.blackPlayer.sendMessage(networkMessage);
        }
    }

    public synchronized void removePlayer(ChessClientHandler handler) {
        System.out.println("DEBUG: removePlayer called - gameStarted: " + gameStarted + ", moves: " + gameState.getMoveHistory().size());
        boolean wasGameActive = (whitePlayer != null && blackPlayer != null);
        int disconnectedPlayerColor = -1;
        if (handler == whitePlayer) {
            disconnectedPlayerColor = PieceColor.WHITE;
            whitePlayer = null;
            System.out.println("White player disconnected");
        } else if (handler == blackPlayer) {
            disconnectedPlayerColor = PieceColor.BLACK;
            blackPlayer = null;
            System.out.println("Black player disconnected");
        }
        if (gameStarted && gameState.getMoveHistory().size() > 0 && wasGameActive && (whitePlayer == null || blackPlayer == null)) {
            System.out.println("DEBUG: Storing game on disconnection");
            storeGameOnDisconnection(disconnectedPlayerColor);
            disconnectRemainingPlayer();
            NetworkMessage endMessage = new NetworkMessage(NetworkMessage.MessageType.GAME_END, "Opponent disconnected", true);
            broadcastMessage(endMessage);
            gameStarted = false;
        } else {
            System.out.println("DEBUG: Not storing game - gameStarted: " + gameStarted + ", moves: " + gameState.getMoveHistory().size() + ", wasGameActive: " + wasGameActive);
        }
    }

    public synchronized void handleResignation(ChessClientHandler resigningPlayer) {
        if (gameStarted && gameState.getMoveHistory().size() > 0) {
            storeGameOnResignation(resigningPlayer);
            disconnectRemainingPlayer();
            NetworkMessage endMessage = new NetworkMessage(NetworkMessage.MessageType.GAME_END, "Opponent resigned", true);
            broadcastMessage(endMessage);
            gameStarted = false;
        }
    }

    private void storeGameOnDisconnection(int disconnectedPlayerColor) {
        try {
            String result;
            String winner;
            String loser;
            if (disconnectedPlayerColor == PieceColor.WHITE) {
                result = "0-1";
                winner = blackPlayerName;
                loser = whitePlayerName;
            } else {
                result = "1-0";
                winner = whitePlayerName;
                loser = blackPlayerName;
            }
            String pgnMoves = gameState.getPGNMoves();
            DatabaseManager.saveGame(whitePlayerName, blackPlayerName, result, pgnMoves, "Network", "Local", "");
            System.out.println("Game stored in database - " + winner + " wins by disconnection");
        } catch (Exception e) {
            System.err.println("Error storing game on disconnection: " + e.getMessage());
        }
    }

    private void storeGameOnResignation(ChessClientHandler resigningPlayer) {
        try {
            String result;
            String winner;
            String loser;
            if (resigningPlayer.getColor() == PieceColor.WHITE) {
                result = "0-1";
                winner = blackPlayerName;
                loser = whitePlayerName;
            } else {
                result = "1-0";
                winner = whitePlayerName;
                loser = blackPlayerName;
            }
            String pgnMoves = gameState.getPGNMoves();
            DatabaseManager.saveGame(whitePlayerName, blackPlayerName, result, pgnMoves, "Network", "Local", "");
            System.out.println("Game stored in database - " + winner + " wins by resignation");
        } catch (Exception e) {
            System.err.println("Error storing game on resignation: " + e.getMessage());
        }
    }

    private void disconnectRemainingPlayer() {
        if (whitePlayer != null) {
            System.out.println("Disconnecting remaining white player");
            whitePlayer.disconnect(true);
            whitePlayer = null;
        }
        if (blackPlayer != null) {
            System.out.println("Disconnecting remaining black player");
            blackPlayer.disconnect(true);
            blackPlayer = null;
        }
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void stop() {
        this.running = false;
        try {
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
            if (this.executorService != null) {
                this.executorService.shutdown();
            }
        }
        catch (IOException iOException) {
            System.err.println("Error stopping server: " + iOException.getMessage());
        }
    }

    // Store completed game in DB when game ends normally
    private void storeCompletedGame() {
        try {
            String result = getResultStringForDatabase();
            String pgnMoves = gameState.getPGNMoves();
            DatabaseManager.saveGame(whitePlayerName, blackPlayerName, result, pgnMoves, "Network", "Local", "");
            System.out.println("Game stored in database - result: " + result);
            gameStarted = false;
        } catch (Exception e) {
            System.err.println("Error storing completed game: " + e.getMessage());
        }
    }

    // Helper to convert result string to PGN result format
    private String getResultStringForDatabase() {
        String res = gameState.getGameResult();
        if (res == null) return "*";
        if (res.contains("White wins")) return "1-0";
        if (res.contains("Black wins")) return "0-1";
        if (res.contains("Draw")) return "1/2-1/2";
        return "*";
    }

    public static void main(String[] stringArray) {
        int n = 8080;
        if (stringArray.length > 0) {
            try {
                n = Integer.parseInt(stringArray[0]);
            }
            catch (NumberFormatException numberFormatException) {
                System.err.println("Invalid port number, using default: 8080");
            }
        }
        ChessServer chessServer = new ChessServer(n);
        chessServer.start();
    }
}
