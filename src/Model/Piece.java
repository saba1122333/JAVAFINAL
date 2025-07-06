/*
 * Decompiled with CFR 0.152.
 */
package Model;

import Model.Board;
import Model.Move;
import Model.PieceColor;
import Model.Position;
import java.util.List;

public abstract class Piece {
    private final int color;
    private Position position;
    private final String pieceImage;
    private boolean hasMoved;

    public Piece(int n, Position position) {
        this.color = n;
        this.position = position;
        this.pieceImage = this.generateImagePath();
        this.hasMoved = false;
    }

    protected Piece(int n, Position position, boolean bl) {
        this.color = n;
        this.position = position;
        this.pieceImage = this.generateImagePath();
        this.hasMoved = bl;
    }

    protected String generateImagePath() {
        String string = this.color == 1 ? "white" : "black";
        String string2 = this.getType().toLowerCase();
        return "resources/pieces/" + string + "_" + string2 + ".png";
    }

    public int getColor() {
        return this.color;
    }

    public Position getPosition() {
        return this.position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getPieceImage() {
        return this.pieceImage;
    }

    public boolean hasMoved() {
        return this.hasMoved;
    }

    public void setHasMoved(boolean bl) {
        this.hasMoved = bl;
    }

    public abstract List<Move> getLegalMoves(Board var1);

    public abstract List<Position> getAttackPositions(Board var1);

    public abstract String getType();

    public boolean moveTo(Position position) {
        Position position2 = this.position;
        this.position = position;
        this.hasMoved = true;
        return true;
    }

    protected boolean wouldMakeOwnKingVulnerable(Board board, Move move) {
        Board board2 = new Board(board);
        Piece piece = board2.getPiece(move.getOrigin());
        Move move2 = Move.createMove(move.getOrigin(), move.getDestination(), piece, board2.getPiece(move.getDestination()));
        board2.executeMove(move2);
        return board2.isKingInCheck(this.getColor());
    }

    public abstract Piece duplicate();

    protected boolean canMoveTo(Board board, Position position) {
        if (!position.isValid()) {
            return false;
        }
        Piece piece = board.getPiece(position);
        return piece == null || piece.getColor() != this.color;
    }

    public String toString() {
        return PieceColor.colorName(this.color) + " " + this.getType() + " at " + String.valueOf(this.position);
    }
}
