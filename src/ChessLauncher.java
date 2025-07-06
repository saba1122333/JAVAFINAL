/*
 * Decompiled with CFR 0.152.
 */
import View.main.StartMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ChessLauncher {
    public static void main(String[] stringArray) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception exception) {
            // empty catch block
        }
        SwingUtilities.invokeLater(() -> new StartMenu());
    }
}
