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

public class RegisterDialog
extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private boolean registrationSuccessful = false;
    private String registeredUsername = null;
    private RegisterListener listener;

    public RegisterDialog(JFrame jFrame) {
        super(jFrame, "Register", true);
        this.setLayout(new BorderLayout());
        this.setSize(300, 200);
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
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 0.0;
        jPanel.add((Component)new JLabel("Confirm:"), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 1.0;
        this.confirmPasswordField = new JPasswordField(15);
        jPanel.add((Component)this.confirmPasswordField, gridBagConstraints);
        JPanel jPanel2 = new JPanel(new FlowLayout(1));
        JButton jButton = new JButton("Register");
        jButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                RegisterDialog.this.performRegistration();
            }
        });
        JButton jButton2 = new JButton("Cancel");
        jButton2.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                RegisterDialog.this.dispose();
            }
        });
        jPanel2.add(jButton);
        jPanel2.add(jButton2);
        this.add((Component)jPanel, "Center");
        this.add((Component)jPanel2, "South");
        this.getRootPane().setDefaultButton(jButton);
        this.confirmPasswordField.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                RegisterDialog.this.performRegistration();
            }
        });
    }

    public void setRegisterListener(RegisterListener registerListener) {
        this.listener = registerListener;
    }

    private void performRegistration() {
        String string = this.usernameField.getText().trim();
        String string2 = new String(this.passwordField.getPassword());
        String string3 = new String(this.confirmPasswordField.getPassword());
        if (string.isEmpty() || string2.isEmpty() || string3.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Registration Error", 0);
            return;
        }
        if (string.length() < 3) {
            JOptionPane.showMessageDialog(this, "Username must be at least 3 characters long.", "Registration Error", 0);
            return;
        }
        if (string2.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long.", "Registration Error", 0);
            return;
        }
        if (!string2.equals(string3)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Registration Error", 0);
            return;
        }
        if (DatabaseManager.playerExists(string)) {
            JOptionPane.showMessageDialog(this, "Username already exists. Please choose a different username.", "Registration Error", 0);
            return;
        }
        if (DatabaseManager.registerPlayer(string, string2)) {
            this.registrationSuccessful = true;
            this.registeredUsername = string;
            JOptionPane.showMessageDialog(this, "Registration successful! You can now login.", "Registration Success", 1);
            if (this.listener != null) {
                this.listener.onRegisterSuccess(string);
            }
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed. Please try again.", "Registration Error", 0);
            if (this.listener != null) {
                this.listener.onRegisterFailure();
            }
        }
    }

    public boolean isRegistrationSuccessful() {
        return this.registrationSuccessful;
    }

    public String getRegisteredUsername() {
        return this.registeredUsername;
    }

    public static interface RegisterListener {
        public void onRegisterSuccess(String var1);

        public void onRegisterFailure();
    }
}
