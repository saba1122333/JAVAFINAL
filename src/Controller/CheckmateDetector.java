/*
 * Decompiled with CFR 0.152.
 */
package Controller;

import Model.Board;
import Model.Move;
import Model.Piece;
import Model.Position;
import Model.pieces.King;
import java.util.ArrayList;
import java.util.List;

public class CheckmateDetector {
    private Board board;
    private List<Position> availableMovePositions;

    public CheckmateDetector(Board board) {
        this.board = board;
        this.availableMovePositions = new ArrayList<Position>();
    }

    public boolean isBlackInCheck() {
        return this.board.isKingInCheck(0);
    }

    public boolean isWhiteInCheck() {
        return this.board.isKingInCheck(1);
    }

    public boolean isBlackCheckmated() {
        return this.board.isCheckmate(0);
    }

    public boolean isWhiteCheckmated() {
        return this.board.isCheckmate(1);
    }

    public List<Position> getValidDestinations(boolean bl) {
        int n = bl ? 1 : 0;
        List<Move> list = this.board.getAllLegalMoves(n);
        this.availableMovePositions.clear();
        for (Move move : list) {
            Position position = move.getDestination();
            if (this.availableMovePositions.contains(position)) continue;
            this.availableMovePositions.add(position);
        }
        return this.availableMovePositions;
    }

    public boolean isMoveLegal(Piece piece, Position position) {
        Position position2 = piece.getPosition();
        Piece piece2 = this.board.getPiece(position);
        Move move = Move.createMove(position2, position, piece, piece2);
        Board board = new Board(this.board);
        board.executeMove(move);
        return !board.isKingInCheck(piece.getColor());
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    private boolean canKingEscape(King king) {
        List<Move> list = king.getLegalMoves(this.board);
        return !list.isEmpty();
    }

    private boolean canAttackerBeCaptured(int n) {
        int n2;
        King king = this.board.getKing(n);
        List<Piece> list = this.findCheckingPieces(king, n2 = n == 1 ? 0 : 1);
        if (list.size() != 1) {
            return false;
        }
        Piece piece = list.get(0);
        List<Piece> list2 = this.board.getPiecesByColor(n);
        for (Piece piece2 : list2) {
            List<Move> list3 = piece2.getLegalMoves(this.board);
            for (Move move : list3) {
                if (!move.getDestination().equals(piece.getPosition())) continue;
                Board board = new Board(this.board);
                board.executeMove(move);
                if (board.isKingInCheck(n)) continue;
                return true;
            }
        }
        return false;
    }

    private boolean canCheckBeBlocked(int n) {
        int n2;
        King king = this.board.getKing(n);
        List<Piece> list = this.findCheckingPieces(king, n2 = n == 1 ? 0 : 1);
        if (list.size() != 1) {
            return false;
        }
        Piece piece = list.get(0);
        String string = piece.getType();
        if (!(string.equals("Queen") || string.equals("Rook") || string.equals("Bishop"))) {
            return false;
        }
        List<Position> list2 = this.findSquaresBetween(piece.getPosition(), king.getPosition());
        List<Piece> list3 = this.board.getPiecesByColor(n);
        for (Piece piece2 : list3) {
            if (piece2 instanceof King) continue;
            List<Move> list4 = piece2.getLegalMoves(this.board);
            for (Move move : list4) {
                if (!list2.contains(move.getDestination())) continue;
                Board board = new Board(this.board);
                board.executeMove(move);
                if (board.isKingInCheck(n)) continue;
                return true;
            }
        }
        return false;
    }

    private List<Piece> findCheckingPieces(King king, int n) {
        ArrayList<Piece> arrayList = new ArrayList<Piece>();
        List<Piece> list = this.board.getPiecesByColor(n);
        Position position = king.getPosition();
        for (Piece piece : list) {
            List<Position> list2 = piece.getAttackPositions(this.board);
            if (!list2.contains(position)) continue;
            arrayList.add(piece);
        }
        return arrayList;
    }

    private List<Position> findSquaresBetween(Position position, Position position2) {
        ArrayList<Position> arrayList;
        block4: {
            int n;
            int n2;
            int n3;
            int n4;
            block5: {
                block3: {
                    arrayList = new ArrayList<Position>();
                    n4 = position.getColumn();
                    n3 = position.getRow();
                    n2 = position2.getColumn();
                    n = position2.getRow();
                    if (n3 != n) break block3;
                    int n5 = Math.min(n4, n2);
                    int n6 = Math.max(n4, n2);
                    for (int i = n5 + 1; i < n6; ++i) {
                        arrayList.add(new Position(i, n3));
                    }
                    break block4;
                }
                if (n4 != n2) break block5;
                int n7 = Math.min(n3, n);
                int n8 = Math.max(n3, n);
                for (int i = n7 + 1; i < n8; ++i) {
                    arrayList.add(new Position(n4, i));
                }
                break block4;
            }
            if (Math.abs(n4 - n2) != Math.abs(n3 - n)) break block4;
            int n9 = n2 > n4 ? 1 : -1;
            int n10 = n > n3 ? 1 : -1;
            int n11 = n4 + n9;
            for (int i = n3 + n10; n11 != n2 && i != n; n11 += n9, i += n10) {
                arrayList.add(new Position(n11, i));
            }
        }
        return arrayList;
    }
}
