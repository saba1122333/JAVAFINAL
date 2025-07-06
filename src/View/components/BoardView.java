/*
 * Decompiled with CFR 0.152.
 */
package View.components;

import Model.Board;
import Model.Position;
import java.util.List;

public interface BoardView {
    public void updateBoard(Board var1);

    public void setCurrentPlayer(int var1);

    public void showGameOverMessage(String var1);

    public void highlightLastMove(Position var1, Position var2);

    public void showLegalMoves(Position var1, List<Position> var2);

    public void showCheck(Position var1, boolean var2);

    public void updateStatus(String var1);

    public void clearHighlights();
}
