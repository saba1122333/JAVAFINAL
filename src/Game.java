/*
 * Decompiled with CFR 0.152.
 */
import View.main.StartMenu;
import javax.swing.SwingUtilities;

public class Game
implements Runnable {
    @Override
    public void run() {
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                new StartMenu();
            }
        });
    }

    public static void main(String[] stringArray) {
        SwingUtilities.invokeLater(new Game());
    }
}
