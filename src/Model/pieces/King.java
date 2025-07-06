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

public class King
extends Piece {
    public King(int n, Position position) {
        super(n, position);
    }

    protected King(int n, Position position, boolean bl) {
        super(n, position, bl);
    }

    @Override
    protected String generateImagePath() {
        return this.getColor() == 1 ? "images/wk.png" : "images/bk.png";
    }

    @Override
    public List<Move> getLegalMoves(Board board) {
        ArrayList<Move> arrayList = new ArrayList<Move>();
        Position position = this.getPosition();
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                Piece piece;
                Move move;
                Position position2;
                if (i == 0 && j == 0 || !(position2 = position.offset(i, j)).isValid() || !this.canMoveTo(board, position2) || this.wouldMakeOwnKingVulnerable(board, move = Move.createMove(position, position2, this, piece = board.getPiece(position2)))) continue;
                arrayList.add(move);
            }
        }
        this.addCastlingMoves(board, arrayList);
        return arrayList;
    }

    private void addCastlingMoves(Board board, List<Move> list) {
        if (this.hasMoved()) {
            return;
        }
        if (board.isKingInCheck(this.getColor())) {
            return;
        }
        Position position = this.getPosition();
        int n = position.getRow();
        this.checkKingsideCastling(board, list, n);
        this.checkQueensideCastling(board, list, n);
    }

    private void checkKingsideCastling(Board board, List<Move> list, int n) {
        Position position = new Position(7, n);
        Piece piece = board.getPiece(position);
        if (piece != null && piece.getType().equals("Rook") && piece.getColor() == this.getColor() && !piece.hasMoved()) {
            boolean bl = true;
            for (int i = 5; i <= 6; ++i) {
                Position position2 = new Position(i, n);
                if (board.getPiece(position2) != null) {
                    bl = false;
                    break;
                }
                if (!this.isSquareUnderAttack(board, position2)) continue;
                bl = false;
                break;
            }
            if (bl) {
                Position position3 = new Position(6, n);
                list.add(Move.createCastling(this.getPosition(), position3, this));
            }
        }
    }

    private void checkQueensideCastling(Board board, List<Move> list, int n) {
        Position position = new Position(0, n);
        Piece piece = board.getPiece(position);
        if (piece != null && piece.getType().equals("Rook") && piece.getColor() == this.getColor() && !piece.hasMoved()) {
            boolean bl = true;
            for (int i = 1; i <= 3; ++i) {
                Position position2 = new Position(i, n);
                if (board.getPiece(position2) != null) {
                    bl = false;
                    break;
                }
                if (i < 2 || !this.isSquareUnderAttack(board, position2)) continue;
                bl = false;
                break;
            }
            if (bl) {
                Position position3 = new Position(2, n);
                list.add(Move.createCastling(this.getPosition(), position3, this));
            }
        }
    }

    private boolean isSquareUnderAttack(Board board, Position position) {
        int n = this.getColor() == 1 ? 0 : 1;
        List<Piece> list = board.getPiecesByColor(n);
        for (Piece piece : list) {
            List<Position> list2 = piece.getAttackPositions(board);
            if (!list2.contains(position)) continue;
            return true;
        }
        return false;
    }

    @Override
    public List<Position> getAttackPositions(Board board) {
        ArrayList<Position> arrayList = new ArrayList<Position>();
        Position position = this.getPosition();
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                Position position2;
                if (i == 0 && j == 0 || !(position2 = position.offset(i, j)).isValid()) continue;
                arrayList.add(position2);
            }
        }
        return arrayList;
    }

    @Override
    public String getType() {
        return "King";
    }

    @Override
    public Piece duplicate() {
        return new King(this.getColor(), this.getPosition(), this.hasMoved());
    }
}
