/*
 * Decompiled with CFR 0.152.
 */
package View.dialogs;

import Network.ChessClient;
import Network.ChessServer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class NetworkGameDialog
extends JDialog {
    private JTextField serverAddressField;
    private JTextField portField;
    private JButton hostButton;
    private JButton joinButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private ChessServer server;
    private Thread serverThread;
    private boolean isHost = false;
    private String serverAddress = "localhost";
    private int serverPort = 8080;
    private NetworkGameListener listener;

    public NetworkGameDialog(JFrame jFrame) {
        super(jFrame, "Network Chess Game", true);
        this.setLayout(new BorderLayout());
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, 1));
        jPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel jPanel2 = new JPanel(new GridLayout(3, 2, 10, 10));
        jPanel2.add(new JLabel("Server Address:"));
        this.serverAddressField = new JTextField("localhost");
        jPanel2.add(this.serverAddressField);
        jPanel2.add(new JLabel("Port:"));
        this.portField = new JTextField("8080");
        jPanel2.add(this.portField);
        jPanel2.add(new JLabel("Status:"));
        this.statusLabel = new JLabel("Ready to host or join");
        this.statusLabel.setForeground(Color.BLUE);
        jPanel2.add(this.statusLabel);
        JPanel jPanel3 = new JPanel(new FlowLayout(1, 10, 10));
        this.hostButton = new JButton("Host Game");
        this.joinButton = new JButton("Join Game");
        this.cancelButton = new JButton("Cancel");
        jPanel3.add(this.hostButton);
        jPanel3.add(this.joinButton);
        jPanel3.add(this.cancelButton);
        jPanel.add(new JLabel("Network Chess Game Setup", 0));
        jPanel.add(Box.createVerticalStrut(20));
        jPanel.add(jPanel2);
        jPanel.add(Box.createVerticalStrut(20));
        jPanel.add(jPanel3);
        this.add((Component)jPanel, "Center");
        this.hostButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                NetworkGameDialog.this.handleHostButtonClick();
            }
        });
        this.joinButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                NetworkGameDialog.this.joinGame();
            }
        });
        this.cancelButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                NetworkGameDialog.this.cleanup();
                NetworkGameDialog.this.dispose();
            }
        });
        this.setDefaultCloseOperation(2);
        this.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent windowEvent) {
                NetworkGameDialog.this.cleanup();
            }
        });
        this.setSize(400, 250);
        this.setLocationRelativeTo(jFrame);
        this.setResizable(false);
    }

    private void startHosting() {
        try {
            this.serverPort = Integer.parseInt(this.portField.getText().trim());
        }
        catch (NumberFormatException numberFormatException) {
            Random random = new Random();
            this.serverPort = 8080 + random.nextInt(1000);
            this.portField.setText(String.valueOf(this.serverPort));
        }
        this.hostButton.setEnabled(false);
        this.joinButton.setEnabled(false);
        this.statusLabel.setText("Starting server...");
        this.statusLabel.setForeground(Color.ORANGE);
        this.serverThread = new Thread(() -> {
            try {
                this.server = new ChessServer(this.serverPort);
                SwingUtilities.invokeLater(() -> {
                    this.statusLabel.setText("Server running on port " + this.serverPort);
                    this.statusLabel.setForeground(Color.GREEN);
                    if (this.listener != null) {
                        this.listener.onGameReady(true, "localhost", this.serverPort);
                    }
                    this.dispose();
                });
                this.server.start();
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> {
                    this.statusLabel.setText("Failed to start server: " + exception.getMessage());
                    this.statusLabel.setForeground(Color.RED);
                    this.hostButton.setEnabled(true);
                    this.joinButton.setEnabled(true);
                });
            }
        });
        this.serverThread.setDaemon(true);
        this.serverThread.start();
        this.isHost = true;
    }

    private void joinGame() {
        this.serverAddress = this.serverAddressField.getText().trim();
        if (this.serverAddress.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a server address", "Error", 0);
            return;
        }
        try {
            this.serverPort = Integer.parseInt(this.portField.getText().trim());
        }
        catch (NumberFormatException numberFormatException) {
            JOptionPane.showMessageDialog(this, "Invalid port number", "Error", 0);
            return;
        }
        this.statusLabel.setText("Testing connection...");
        this.statusLabel.setForeground(Color.ORANGE);
        this.joinButton.setEnabled(false);
        new Thread(() -> {
            try {
                ChessClient chessClient = new ChessClient(this.serverAddress, this.serverPort);
                boolean bl = chessClient.connect();
                chessClient.disconnect();
                SwingUtilities.invokeLater(() -> {
                    if (bl) {
                        this.statusLabel.setText("Connection successful!");
                        this.statusLabel.setForeground(Color.GREEN);
                        if (this.listener != null) {
                            this.listener.onGameReady(false, this.serverAddress, this.serverPort);
                        }
                        this.dispose();
                    } else {
                        this.statusLabel.setText("Connection failed");
                        this.statusLabel.setForeground(Color.RED);
                        this.joinButton.setEnabled(true);
                    }
                });
            }
            catch (Exception exception) {
                SwingUtilities.invokeLater(() -> {
                    this.statusLabel.setText("Connection error: " + exception.getMessage());
                    this.statusLabel.setForeground(Color.RED);
                    this.joinButton.setEnabled(true);
                });
            }
        }).start();
    }

    private void cleanup() {
        if (this.server != null) {
            this.server.stop();
        }
        if (this.serverThread != null && this.serverThread.isAlive()) {
            this.serverThread.interrupt();
        }
    }

    public void setNetworkGameListener(NetworkGameListener networkGameListener) {
        this.listener = networkGameListener;
    }

    private void handleHostButtonClick() {
        if (this.server != null && this.serverThread != null && this.serverThread.isAlive()) {
            if (this.listener != null) {
                this.listener.onGameReady(true, "localhost", this.serverPort);
            }
            this.dispose();
        } else {
            this.startHosting();
        }
    }

    public static interface NetworkGameListener {
        public void onGameReady(boolean var1, String var2, int var3);
    }
}
