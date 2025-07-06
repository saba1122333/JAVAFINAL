/*
 * Decompiled with CFR 0.152.
 */
package View.main;

import Controller.GameController;
import Model.DatabaseManager;
import View.dialogs.LoginDialog;
import View.dialogs.NetworkGameDialog;
import View.dialogs.RegisterDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

public class StartMenu
extends JFrame {
    private JComboBox<String> gameModeComboBox;
    private JCheckBox timedGameCheckBox;
    private JSpinner hoursSpinner;
    private JSpinner minutesSpinner;
    private JSpinner secondsSpinner;
    private String currentPlayer = null;
    private JLabel playerLabel;
    private JButton loginButton;
    private JButton registerButton;
    private JButton clearDatabaseButton;

    public StartMenu() {
        super("Chess Game - Start Menu");
        DatabaseManager.initializeDatabases();
        this.setDefaultCloseOperation(3);
        this.setLayout(new BorderLayout());
        this.setSize(300, 250);
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, 1));
        jPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel jPanel2 = new JPanel(new FlowLayout(0));
        jPanel2.add(new JLabel("Game Mode:"));
        String[] stringArray = new String[]{"Player vs Player", "Player vs Computer", "Computer vs Computer", "Network Game"};
        this.gameModeComboBox = new JComboBox<String>(stringArray);
        jPanel2.add(this.gameModeComboBox);
        JPanel jPanel3 = new JPanel(new FlowLayout(0));
        this.timedGameCheckBox = new JCheckBox("Timed Game");
        jPanel3.add(this.timedGameCheckBox);
        JPanel jPanel4 = new JPanel(new FlowLayout(0));
        jPanel4.add(new JLabel("Time Control:"));
        this.hoursSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 24, 1));
        this.minutesSpinner = new JSpinner(new SpinnerNumberModel(15, 0, 59, 1));
        this.secondsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        jPanel4.add(this.hoursSpinner);
        jPanel4.add(new JLabel("h"));
        jPanel4.add(this.minutesSpinner);
        jPanel4.add(new JLabel("m"));
        jPanel4.add(this.secondsSpinner);
        jPanel4.add(new JLabel("s"));
        JPanel jPanel5 = new JPanel(new FlowLayout(1));
        JLabel jLabel = new JLabel("Player: ");
        this.playerLabel = new JLabel("Not logged in");
        this.playerLabel.setForeground(Color.RED);
        JButton jButton = new JButton("Login");
        jButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                StartMenu.this.showLoginDialog();
            }
        });
        JButton jButton2 = new JButton("Register");
        jButton2.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                StartMenu.this.showRegisterDialog();
            }
        });
        this.loginButton = jButton;
        this.registerButton = jButton2;
        JButton jButton3 = new JButton("Clear Game History");
        jButton3.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                StartMenu.this.clearGameDatabase();
            }
        });
        this.clearDatabaseButton = jButton3;
        jPanel5.add(jLabel);
        jPanel5.add(this.playerLabel);
        jPanel5.add(jButton);
        jPanel5.add(jButton2);
        jPanel5.add(jButton3);
        JButton jButton4 = new JButton("Start Game");
        jButton4.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                StartMenu.this.startGame();
            }
        });
        jPanel.add(jPanel5);
        jPanel.add(Box.createVerticalStrut(15));
        jPanel.add(jPanel2);
        jPanel.add(Box.createVerticalStrut(15));
        jPanel.add(jPanel3);
        jPanel.add(Box.createVerticalStrut(5));
        jPanel.add(jPanel4);
        jPanel.add(Box.createVerticalStrut(20));
        JPanel jPanel6 = new JPanel(new FlowLayout(1));
        jPanel6.add(jButton4);
        this.add((Component)jPanel, "Center");
        this.add((Component)jPanel6, "South");
        this.timedGameCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                boolean bl = StartMenu.this.timedGameCheckBox.isSelected();
                StartMenu.this.hoursSpinner.setEnabled(bl);
                StartMenu.this.minutesSpinner.setEnabled(bl);
                StartMenu.this.secondsSpinner.setEnabled(bl);
            }
        });
        this.timedGameCheckBox.setSelected(false);
        this.hoursSpinner.setEnabled(false);
        this.minutesSpinner.setEnabled(false);
        this.secondsSpinner.setEnabled(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void startGame() {
        final String string = (String)this.gameModeComboBox.getSelectedItem();
        if ("Network Game".equals(string)) {
            NetworkGameDialog networkGameDialog = new NetworkGameDialog(this);
            networkGameDialog.setNetworkGameListener(new NetworkGameDialog.NetworkGameListener(){

                @Override
                public void onGameReady(boolean bl, String string4, int n) {
                    String string2;
                    GameWindow gameWindow = new GameWindow(string, bl, string4, n);
                    String string3 = string2 = StartMenu.this.currentPlayer != null ? StartMenu.this.currentPlayer : "Guest";
                    if (bl) {
                        gameWindow.setWhitePlayer(string2);
                        gameWindow.setBlackPlayer("Guest");
                    } else {
                        gameWindow.setWhitePlayer("Guest");
                        gameWindow.setBlackPlayer(string2);
                    }
                    GameController gameController = gameWindow.getController();
                    gameController.startNewGame(string);
                    StartMenu.this.dispose();
                }
            });
            networkGameDialog.setVisible(true);
        } else {
            GameWindow gameWindow = new GameWindow();
            GameController gameController = gameWindow.getController();
            if (this.currentPlayer != null) {
                gameWindow.setWhitePlayer(this.currentPlayer);
                gameWindow.setBlackPlayer("Computer");
            } else {
                gameWindow.setWhitePlayer("Guest");
                gameWindow.setBlackPlayer("Computer");
            }
            if (this.timedGameCheckBox.isSelected()) {
                int n = (Integer)this.hoursSpinner.getValue();
                int n2 = (Integer)this.minutesSpinner.getValue();
                int n3 = (Integer)this.secondsSpinner.getValue();
                gameController.startTimedGame(string, n, n2, n3);
            } else {
                gameController.startNewGame(string);
            }
            this.dispose();
        }
    }

    private void showLoginDialog() {
        LoginDialog loginDialog = new LoginDialog(this);
        loginDialog.setLoginListener(new LoginDialog.LoginListener(){

            @Override
            public void onLoginSuccess(String string) {
                StartMenu.this.currentPlayer = string;
                StartMenu.this.updatePlayerLabel();
                JOptionPane.showMessageDialog(StartMenu.this, "Welcome, " + string + "!", "Login Success", 1);
            }

            @Override
            public void onLoginFailure() {
            }
        });
        loginDialog.setVisible(true);
    }

    private void showRegisterDialog() {
        RegisterDialog registerDialog = new RegisterDialog(this);
        registerDialog.setRegisterListener(new RegisterDialog.RegisterListener(){

            @Override
            public void onRegisterSuccess(String string) {
                JOptionPane.showMessageDialog(StartMenu.this, "Registration successful! Please login to start playing.", "Registration Success", 1);
            }

            @Override
            public void onRegisterFailure() {
            }
        });
        registerDialog.setVisible(true);
    }

    private void updatePlayerLabel() {
        if (this.currentPlayer != null) {
            this.playerLabel.setText("Logged in as: " + this.currentPlayer);
            this.playerLabel.setForeground(Color.GREEN);
            this.loginButton.setVisible(false);
            this.registerButton.setVisible(false);
        } else {
            this.playerLabel.setText("Not logged in");
            this.playerLabel.setForeground(Color.RED);
            this.loginButton.setVisible(true);
            this.registerButton.setVisible(true);
        }
    }

    private void clearGameDatabase() {
        int n = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all game history?\nThis action cannot be undone.", "Clear Game History", 0, 2);
        if (n == 0) {
            boolean bl = DatabaseManager.clearGameDatabase();
            if (bl) {
                JOptionPane.showMessageDialog(this, "Game history has been cleared successfully.", "Success", 1);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to clear game history. Please try again.", "Error", 0);
            }
        }
    }

    public static void main(String[] stringArray) {
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                new StartMenu();
            }
        });
    }
}
