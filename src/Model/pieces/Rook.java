/*
 * Decompiled with CFR 0.152.
 */
package Model.pieces;

import Model.Board;
import Model.Move;
import Model.Piece;
import Model.Position;
import Model.pieces.King;
import java.util.ArrayList;
import java.util.List;

public class Rook
extends Piece {
    private static final int[][] MOVEMENT_DIRECTIONS = new int[][]{{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

    public Rook(int n, Position position) {
        super(n, position);
    }

    protected Rook(int n, Position position, boolean bl) {
        super(n, position, bl);
    }

    @Override
    protected String generateImagePath() {
        return this.getColor() == 1 ? "assets/wrook.png" : "assets/brook.png";
    }

    @Override
    public String getType() {
        return "Rook";
    }

    @Override
    public List<Move> getLegalMoves(Board board) {
        ArrayList<Move> arrayList = new ArrayList<Move>();
        Position position = this.getPosition();
        if (position == null) {
            return arrayList;
        }
        for (int[] nArray : MOVEMENT_DIRECTIONS) {
            this.explorePathway(board, position, nArray[0], nArray[1], arrayList);
        }
        return arrayList;
    }

    private void explorePathway(Board board, Position position, int n, int n2, List<Move> list) {
        Position position2;
        Position position3 = position;
        for (int i = 1; i < 8 && (position2 = position3.offset(n, n2)).isValid(); ++i) {
            Piece piece = board.getPiece(position2);
            if (piece != null) {
                if (piece.getColor() == this.getColor()) break;
                this.evaluateAndAddMove(board, position, position2, piece, list);
                break;
            }
            this.evaluateAndAddMove(board, position, position2, null, list);
            position3 = position2;
        }
    }

    private void evaluateAndAddMove(Board board, Position position, Position position2, Piece piece, List<Move> list) {
        Move move = Move.createMove(position, position2, this, piece);
        if (!this.wouldMakeOwnKingVulnerable(board, move)) {
            list.add(move);
        }
    }

    @Override
    public List<Position> getAttackPositions(Board board) {
        ArrayList<Position> arrayList = new ArrayList<Position>();
        Position position = this.getPosition();
        if (position == null) {
            return arrayList;
        }
        block0: for (int[] nArray : MOVEMENT_DIRECTIONS) {
            Position position2;
            Position position3 = position;
            for (int i = 1; i < 8 && (position2 = position3.offset(nArray[0], nArray[1])).isValid(); ++i) {
                arrayList.add(position2);
                if (board.getPiece(position2) != null) continue block0;
                position3 = position2;
            }
        }
        return arrayList;
    }

    @Override
    public Piece duplicate() {
        return new Rook(this.getColor(), this.getPosition() != null ? new Position(this.getPosition().getColumn(), this.getPosition().getRow()) : null, this.hasMoved());
    }

    public boolean canParticipateInCastling(Board board, King king) {
        if (this.hasMoved() || king.hasMoved()) {
            return false;
        }
        if (this.getPosition().getRow() != king.getPosition().getRow()) {
            return false;
        }
        if (board.isPieceBetween(this.getPosition(), king.getPosition())) {
            return false;
        }
        return !board.isKingInCheck(king.getColor());
    }

    @Override
    public String toString() {
        return this.getColor() == 1 ? "\u2656" : "\u265c";
    }
}
