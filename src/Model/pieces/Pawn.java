/*
 * Decompiled with CFR 0.152.
 */
package Model.pieces;

import Model.Board;
import Model.Move;
import Model.Piece;
import Model.Position;
import java.util.ArrayList;
import java.util.List;

public class Pawn
extends Piece {
    public Pawn(int n, Position position) {
        super(n, position);
    }

    protected Pawn(int n, Position position, boolean bl) {
        super(n, position, bl);
    }

    @Override
    protected String generateImagePath() {
        return this.getColor() == 1 ? "assets/wpawn.png" : "assets/bpawn.png";
    }

    @Override
    public String getType() {
        return "Pawn";
    }

    @Override
    public List<Move> getLegalMoves(Board board) {
        ArrayList<Move> arrayList = new ArrayList<Move>();
        Position position = this.getPosition();
        if (position == null) {
            return arrayList;
        }
        int n = position.getColumn();
        int n2 = position.getRow();
        int n3 = this.getColor() == 1 ? -1 : 1;
        Position position2 = position.offset(0, n3);
        if (position2.isValid() && board.getPiece(position2) == null) {
            Position position3;
            this.tryAddMove(board, position, position2, null, arrayList);
            if (!this.hasMoved() && (position3 = position.offset(0, 2 * n3)).isValid() && board.getPiece(position3) == null && !board.isPieceBetween(position, position3)) {
                this.tryAddMove(board, position, position3, null, arrayList);
            }
        }
        this.tryDiagonalCapture(board, position.offset(-1, n3), arrayList);
        this.tryDiagonalCapture(board, position.offset(1, n3), arrayList);
        this.tryEnPassantCapture(board, arrayList);
        return arrayList;
    }

    private void tryDiagonalCapture(Board board, Position position, List<Move> list) {
        Piece piece;
        if (position.isValid() && (piece = board.getPiece(position)) != null && piece.getColor() != this.getColor()) {
            this.tryAddMove(board, this.getPosition(), position, piece, list);
        }
    }

    private void tryAddMove(Board board, Position position, Position position2, Piece piece, List<Move> list) {
        Move move;
        boolean bl = this.isPromotionRank(position2.getRow());
        Move move2 = move = bl ? Move.createPromotion(position, position2, this, piece) : Move.createMove(position, position2, this, piece);
        if (!this.wouldMakeOwnKingVulnerable(board, move)) {
            list.add(move);
        }
    }

    private boolean isPromotionRank(int n) {
        return this.getColor() == 1 && n == 0 || this.getColor() == 0 && n == 7;
    }

    private void tryEnPassantCapture(Board board, List<Move> list) {
        Move move = board.getLastMove();
        if (move == null) {
            return;
        }
        Piece piece = move.getMovingPiece();
        if (!(piece instanceof Pawn)) {
            return;
        }
        Position position = move.getOrigin();
        Position position2 = move.getDestination();
        if (Math.abs(position.getRow() - position2.getRow()) != 2) {
            return;
        }
        Position position3 = this.getPosition();
        if (position3.getRow() == position2.getRow() && Math.abs(position3.getColumn() - position2.getColumn()) == 1) {
            int n = position3.getRow() + (this.getColor() == 1 ? -1 : 1);
            Position position4 = new Position(position2.getColumn(), n);
            Move move2 = Move.createEnPassant(position3, position4, this, piece);
            if (!this.wouldMakeOwnKingVulnerable(board, move2)) {
                list.add(move2);
            }
        }
    }

    @Override
    public List<Position> getAttackPositions(Board board) {
        Position position;
        ArrayList<Position> arrayList = new ArrayList<Position>();
        Position position2 = this.getPosition();
        if (position2 == null) {
            return arrayList;
        }
        int n = this.getColor() == 1 ? -1 : 1;
        Position position3 = position2.offset(-1, n);
        if (position3.isValid()) {
            arrayList.add(position3);
        }
        if ((position = position2.offset(1, n)).isValid()) {
            arrayList.add(position);
        }
        return arrayList;
    }

    @Override
    public Piece duplicate() {
        return new Pawn(this.getColor(), this.getPosition() != null ? new Position(this.getPosition().getColumn(), this.getPosition().getRow()) : null, this.hasMoved());
    }

    @Override
    public String toString() {
        return this.getColor() == 1 ? "\u2659" : "\u265f";
    }
}
