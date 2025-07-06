/*
 * Decompiled with CFR 0.152.
 */
package View.main;

import Controller.GameController;
import Model.DatabaseManager;
import Model.Position;
import Network.ChessClient;
import Network.ChessServer;
import View.components.ChessBoardUI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

public class GameWindow
extends JFrame {
    private GameController controller;
    private ChessBoardUI boardUI;
    private JLabel statusLabel;
    private JPanel controlPanel;
    private JButton pauseResumeButton;
    private boolean isNetworkGame = false;
    private boolean isHost = false;
    private String networkAddress = null;
    private int networkPort = 8080;
    private ChessClient chessClient;
    private Thread serverThread;
    private boolean myTurn = false;
    private ChessServer server;
    private JButton newGameButton;
    private String whitePlayer = "Anonymous";
    private String blackPlayer = "Anonymous";

    public GameWindow() {
        super("Chess Game");
        this.controller = new GameController();
        this.controller.setView(this);
        this.setDefaultCloseOperation(3);
        this.setLayout(new BorderLayout());
        this.boardUI = new ChessBoardUI(this.controller);
        this.add((Component)this.boardUI, "Center");
        this.statusLabel = new JLabel("White's turn");
        this.statusLabel.setHorizontalAlignment(0);
        this.statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        this.add((Component)this.statusLabel, "South");
        this.setupControlPanel();
        this.add((Component)this.controlPanel, "East");
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.addWindowListener();
    }

    public GameWindow(String string, boolean bl, String string2, int n) {
        super("Chess Game - Network");
        this.isNetworkGame = true;
        this.isHost = bl;
        this.networkAddress = string2;
        this.networkPort = n;
        this.controller = new GameController();
        this.controller.setView(this);
        this.controller.setNetworkGame(true);
        this.controller.setGameMode("Network Game");
        this.setDefaultCloseOperation(3);
        this.setLayout(new BorderLayout());
        this.boardUI = new ChessBoardUI(this.controller);
        this.add((Component)this.boardUI, "Center");
        this.statusLabel = new JLabel("Connecting to network game...");
        this.statusLabel.setHorizontalAlignment(0);
        this.statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        this.add((Component)this.statusLabel, "South");
        this.setupControlPanel();
        this.add((Component)this.controlPanel, "East");
        this.controller.updateView();
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.addWindowListener();
        if (bl) {
            this.serverThread = new Thread(() -> {
                this.server = new ChessServer(this.networkPort);
                this.server.start();
            });
            this.serverThread.setDaemon(true);
            this.serverThread.start();
            new Thread(() -> {
                try {
                    Thread.sleep(1500L);
                    this.connectClient("localhost");
                }
                catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }).start();
        } else {
            new Thread(() -> {
                try {
                    Thread.sleep(2500L);
                    this.connectClient(string2);
                }
                catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }).start();
        }
        if (this.isNetworkGame) {
            String string3 = bl ? " (White)" : " (Black)";
            this.updateStatus("Waiting for game to start" + string3);
            this.boardUI.setNetworkMoveHandler((position, position2) -> {
                if (!this.myTurn) {
                    String turnStatus = bl ? " (White)" : " (Black)";
                    this.updateStatus("Not your turn" + turnStatus);
                    return;
                }
                if (this.chessClient != null && this.chessClient.isConnected()) {
                    this.myTurn = false;
                    this.updateStatus("Sending move...");
                    this.chessClient.sendMove(position, position2);
                    String waitStatus = bl ? " (White)" : " (Black)";
                    this.updateStatus("Waiting for opponent's move" + waitStatus);
                } else {
                    this.updateStatus("Not connected to server");
                    this.myTurn = true;
                }
            });
        }
    }

    private void setupControlPanel() {
        this.controlPanel = new JPanel();
        this.controlPanel.setLayout(new BoxLayout(this.controlPanel, 1));
        this.controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.newGameButton = new JButton("New Game");
        this.newGameButton.setAlignmentX(0.5f);
        this.newGameButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                GameWindow.this.showGameOptions();
            }
        });
        if (this.isNetworkGame) {
            this.newGameButton.setEnabled(false);
            this.newGameButton.setVisible(false);
        }
        JButton jButton = new JButton("Resign");
        jButton.setAlignmentX(0.5f);
        jButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (GameWindow.this.isNetworkGame) {
                    // Network resign: send resignation message, server will handle DB storage
                    if (GameWindow.this.chessClient != null) {
                        GameWindow.this.chessClient.sendGameOver();
                    }
                } else {
                    GameWindow.this.controller.surrender();
                }
            }
        });
        JButton jButton2 = new JButton("Flip Board");
        jButton2.setAlignmentX(0.5f);
        jButton2.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                GameWindow.this.boardUI.setFlipped(!GameWindow.this.boardUI.isFlipped());
            }
        });
        this.pauseResumeButton = new JButton("Pause");
        this.pauseResumeButton.setAlignmentX(0.5f);
        this.pauseResumeButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                boolean bl = GameWindow.this.controller.toggleClockPause();
                GameWindow.this.pauseResumeButton.setText(bl ? "Pause" : "Resume");
            }
        });
        this.controlPanel.add(this.newGameButton);
        this.controlPanel.add(Box.createVerticalStrut(10));
        this.controlPanel.add(jButton);
        this.controlPanel.add(Box.createVerticalStrut(10));
        this.controlPanel.add(jButton2);
        this.controlPanel.add(Box.createVerticalStrut(10));
        this.controlPanel.add(this.pauseResumeButton);
        this.pauseResumeButton.setVisible(false);
    }

    private void showGameOptions() {
        JDialog jDialog = new JDialog(this, "New Game Options", true);
        jDialog.setLayout(new BorderLayout());
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridLayout(0, 1, 5, 5));
        jPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JComboBox<String> jComboBox = new JComboBox<String>(new String[]{"Player vs Player", "Player vs Computer", "Computer vs Computer"});
        jPanel.add(new JLabel("Game Mode:"));
        jPanel.add(jComboBox);
        JCheckBox jCheckBox = new JCheckBox("Timed Game");
        jPanel.add(jCheckBox);
        JPanel jPanel2 = new JPanel(new FlowLayout(0));
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(0, 0, 10, 1);
        SpinnerNumberModel spinnerNumberModel2 = new SpinnerNumberModel(15, 0, 59, 1);
        SpinnerNumberModel spinnerNumberModel3 = new SpinnerNumberModel(0, 0, 59, 5);
        JSpinner jSpinner = new JSpinner(spinnerNumberModel);
        JSpinner jSpinner2 = new JSpinner(spinnerNumberModel2);
        JSpinner jSpinner3 = new JSpinner(spinnerNumberModel3);
        jPanel2.add(new JLabel("Time: "));
        jPanel2.add(jSpinner);
        jPanel2.add(new JLabel("h "));
        jPanel2.add(jSpinner2);
        jPanel2.add(new JLabel("m "));
        jPanel2.add(jSpinner3);
        jPanel2.add(new JLabel("s"));
        jPanel.add(jPanel2);
        jCheckBox.addActionListener(actionEvent -> {
            boolean bl = jCheckBox.isSelected();
            jSpinner.setEnabled(bl);
            jSpinner2.setEnabled(bl);
            jSpinner3.setEnabled(bl);
        });
        jSpinner.setEnabled(false);
        jSpinner2.setEnabled(false);
        jSpinner3.setEnabled(false);
        JPanel jPanel3 = new JPanel(new FlowLayout(2));
        JButton jButton = new JButton("Start Game");
        JButton jButton2 = new JButton("Cancel");
        jButton.addActionListener(actionEvent -> {
            String string = (String)jComboBox.getSelectedItem();
            if (jCheckBox.isSelected()) {
                int n = (Integer)jSpinner.getValue();
                int n2 = (Integer)jSpinner2.getValue();
                int n3 = (Integer)jSpinner3.getValue();
                this.controller.startTimedGame(string, n, n2, n3);
                this.pauseResumeButton.setVisible(true);
                this.pauseResumeButton.setText("Pause");
            } else {
                this.controller.startNewGame(string);
                this.pauseResumeButton.setVisible(false);
            }
            jDialog.dispose();
        });
        jButton2.addActionListener(actionEvent -> jDialog.dispose());
        jPanel3.add(jButton);
        jPanel3.add(jButton2);
        jDialog.add((Component)jPanel, "Center");
        jDialog.add((Component)jPanel3, "South");
        jDialog.pack();
        jDialog.setLocationRelativeTo(this);
        jDialog.setVisible(true);
    }

    public GameController getController() {
        return this.controller;
    }

    public void refreshBoard() {
        this.boardUI.updateBoard();
    }

    public void updateStatus(String string) {
        this.statusLabel.setText(string);
    }

    public void showGameOver(String result) {
        boardUI.clearSelection();
        if (!isNetworkGame && controller.getGameState() != null && !controller.getGameState().getMoveHistory().isEmpty()) {
            String pgnMoves = controller.getGameState().getPGNMoves();
            String gameResult = extractGameResult(result);
            System.out.println("DEBUG: Saving local game to database - whitePlayer=" + whitePlayer + ", blackPlayer=" + blackPlayer);
            DatabaseManager.saveGame(whitePlayer, blackPlayer, gameResult, pgnMoves, "Local", "Local", "");
        }
        JOptionPane.showMessageDialog(this, result, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    private String extractGameResult(String string) {
        if (string.contains("White wins")) {
            return "1-0";
        }
        if (string.contains("Black wins")) {
            return "0-1";
        }
        return "1/2-1/2";
    }

    public void setWhitePlayer(String string) {
        this.whitePlayer = string;
    }

    public void setBlackPlayer(String string) {
        this.blackPlayer = string;
    }

    public static void main(String[] stringArray) {
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                new StartMenu();
            }
        });
    }

    private void connectClient(String string) {
        this.chessClient = new ChessClient(string, this.networkPort);
        String string2 = this.isHost ? this.whitePlayer : this.blackPlayer;
        this.chessClient.setPlayerName(string2);
        this.statusLabel.setText("Connecting to server at " + string + ":" + this.networkPort + "...");
        boolean bl = this.chessClient.connect();
        if (bl) {
            this.statusLabel.setText("Connected! Waiting for opponent...");
            this.chessClient.setMessageListener(new ChessClient.NetworkMessageListener(){

                @Override
                public void onMoveReceived(Position position, Position position2, String string) {
                    SwingUtilities.invokeLater(() -> {
                        boolean bl = GameWindow.this.controller.makeMove(position, position2);
                        if (bl) {
                            GameWindow.this.myTurn = true;
                            String string2 = GameWindow.this.isHost ? " (White)" : " (Black)";
                            GameWindow.this.updateStatus("Your turn" + string2 + " - " + string);
                        } else {
                            GameWindow.this.updateStatus("Error: Move failed to apply locally");
                        }
                    });
                }

                @Override
                public void onGameStart() {
                    SwingUtilities.invokeLater(() -> {
                        GameWindow.this.myTurn = GameWindow.this.isHost;
                        String string = GameWindow.this.isHost ? " (White)" : " (Black)";
                        GameWindow.this.updateStatus(GameWindow.this.myTurn ? "Your turn" + string : "Opponent's turn" + string);
                    });
                }

                @Override
                public void onGameEnd(String string) {
                    SwingUtilities.invokeLater(() -> GameWindow.this.showGameOver(string));
                }

                @Override
                public void onConnectionEstablished(String string) {
                    SwingUtilities.invokeLater(() -> GameWindow.this.updateStatus(string));
                }

                @Override
                public void onPlayerInfoReceived(String string) {
                    SwingUtilities.invokeLater(() -> {
                        if (GameWindow.this.isHost) {
                            GameWindow.this.blackPlayer = string;
                        } else {
                            GameWindow.this.whitePlayer = string;
                        }
                        System.out.println("Received opponent name: " + string + " (isHost: " + GameWindow.this.isHost + ")");
                    });
                }

                @Override
                public void onError(String string) {
                    SwingUtilities.invokeLater(() -> {
                        GameWindow.this.updateStatus("Error: " + string);
                        if (string.contains("Not your turn")) {
                            GameWindow.this.myTurn = true;
                            GameWindow.this.updateStatus("Error: " + string + " - Turn restored");
                        }
                    });
                }
            });
        } else {
            this.statusLabel.setText("Failed to connect to server.");
        }
    }

    private void addWindowListener() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.out.println("DEBUG: Window closing - isNetworkGame: " + isNetworkGame + ", isHost: " + isHost);
                // For local games, save if game is over. For network games, server handles storage.
                if (!isNetworkGame && controller.getGameState() != null && controller.getGameState().isGameOver()) {
                    showGameOver(controller.getGameState().getGameResult());
                }
                // For network games, disconnect client first to trigger server-side storage
                if (isNetworkGame && chessClient != null) {
                    System.out.println("DEBUG: Disconnecting chess client first");
                    chessClient.disconnect();
                    // Give server time to process the disconnect before stopping
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
                // Now stop the server
                if (server != null) {
                    System.out.println("DEBUG: Stopping server");
                    server.stop();
                }
            }
        });
    }
}