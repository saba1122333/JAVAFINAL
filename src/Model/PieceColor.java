/*
 * Decompiled with CFR 0.152.
 */
package Model;

public class PieceColor {
    public static final int WHITE = 1;
    public static final int BLACK = 0;

    private PieceColor() {
    }

    public static String colorName(int n) {
        return n == 1 ? "White" : "Black";
    }

    public static int opponent(int n) {
        return n == 1 ? 0 : 1;
    }
}
