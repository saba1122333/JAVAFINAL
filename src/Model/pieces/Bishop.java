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

public class Bishop
extends Piece {
    public Bishop(int n, Position position) {
        super(n, position);
    }

    protected Bishop(int n, Position position, boolean bl) {
        super(n, position, bl);
    }

    @Override
    protected String generateImagePath() {
        return this.getColor() == 1 ? "images/wb.png" : "images/bb.png";
    }

    @Override
    public List<Move> getLegalMoves(Board board) {
        int[][] nArrayArray;
        ArrayList<Move> arrayList = new ArrayList<Move>();
        Position position = this.getPosition();
        for (int[] nArray : nArrayArray = new int[][]{{1, 1}, {1, -1}, {-1, -1}, {-1, 1}}) {
            this.exploreDiagonalPath(board, position, nArray[0], nArray[1], arrayList);
        }
        return arrayList;
    }

    private void exploreDiagonalPath(Board board, Position position, int n, int n2, List<Move> list) {
        Position position2 = position.offset(n, n2);
        while (position2.isValid()) {
            Piece piece = board.getPiece(position2);
            if (piece != null) {
                if (piece.getColor() == this.getColor()) break;
                this.addMoveIfLegal(board, position, position2, piece, list);
                break;
            }
            this.addMoveIfLegal(board, position, position2, null, list);
            position2 = position2.offset(n, n2);
        }
    }

    private void addMoveIfLegal(Board board, Position position, Position position2, Piece piece, List<Move> list) {
        Move move = Move.createMove(position, position2, this, piece);
        if (!this.wouldMakeOwnKingVulnerable(board, move)) {
            list.add(move);
        }
    }

    @Override
    public List<Position> getAttackPositions(Board board) {
        int[][] nArrayArray;
        ArrayList<Position> arrayList = new ArrayList<Position>();
        Position position = this.getPosition();
        for (int[] nArray : nArrayArray = new int[][]{{1, 1}, {1, -1}, {-1, -1}, {-1, 1}}) {
            this.collectAttackSquares(board, position, nArray[0], nArray[1], arrayList);
        }
        return arrayList;
    }

    private void collectAttackSquares(Board board, Position position, int n, int n2, List<Position> list) {
        Position position2 = position.offset(n, n2);
        while (position2.isValid()) {
            list.add(position2);
            if (board.getPiece(position2) != null) break;
            position2 = position2.offset(n, n2);
        }
    }

    @Override
    public String getType() {
        return "Bishop";
    }

    @Override
    public Piece duplicate() {
        return new Bishop(this.getColor(), this.getPosition(), this.hasMoved());
    }
}
