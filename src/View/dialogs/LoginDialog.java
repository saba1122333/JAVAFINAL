/*
 * Decompiled with CFR 0.152.
 */
package View.dialogs;

import Model.DatabaseManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginDialog
extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private boolean loginSuccessful = false;
    private String loggedInUsername = null;
    private LoginListener listener;

    public LoginDialog(JFrame jFrame) {
        super(jFrame, "Login", true);
        this.setLayout(new BorderLayout());
        this.setSize(300, 150);
        this.setLocationRelativeTo(jFrame);
        this.setResizable(false);
        JPanel jPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        gridBagConstraints.fill = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel.add((Component)new JLabel("Username:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        this.usernameField = new JTextField(15);
        jPanel.add((Component)this.usernameField, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)new JLabel("Password:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        this.passwordField = new JPasswordField(15);
        jPanel.add((Component)this.passwordField, gridBagConstraints);
        JPanel jPanel2 = new JPanel(new FlowLayout(1));
        JButton jButton = new JButton("Login");
        jButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                LoginDialog.this.performLogin();
            }
        });
        JButton jButton2 = new JButton("Cancel");
        jButton2.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                LoginDialog.this.dispose();
            }
        });
        jPanel2.add(jButton);
        jPanel2.add(jButton2);
        this.add((Component)jPanel, "Center");
        this.add((Component)jPanel2, "South");
        this.getRootPane().setDefaultButton(jButton);
        this.passwordField.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                LoginDialog.this.performLogin();
            }
        });
    }

    public void setLoginListener(LoginListener loginListener) {
        this.listener = loginListener;
    }

    private void performLogin() {
        String string = this.usernameField.getText().trim();
        String string2 = new String(this.passwordField.getPassword());
        if (string.isEmpty() || string2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Login Error", 0);
            return;
        }
        if (DatabaseManager.authenticatePlayer(string, string2)) {
            this.loginSuccessful = true;
            this.loggedInUsername = string;
            if (this.listener != null) {
                this.listener.onLoginSuccess(string);
            }
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Error", 0);
            if (this.listener != null) {
                this.listener.onLoginFailure();
            }
        }
    }

    public boolean isLoginSuccessful() {
        return this.loginSuccessful;
    }

    public String getLoggedInUsername() {
        return this.loggedInUsername;
    }

    public static interface LoginListener {
        public void onLoginSuccess(String var1);

        public void onLoginFailure();
    }
}
