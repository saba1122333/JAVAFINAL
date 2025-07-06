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

public class Knight
extends Piece {
    public Knight(int n, Position position) {
        super(n, position);
    }

    protected Knight(int n, Position position, boolean bl) {
        super(n, position, bl);
    }

    @Override
    protected String generateImagePath() {
        return this.getColor() == 1 ? "images/wn.png" : "images/bn.png";
    }

    @Override
    public List<Move> getLegalMoves(Board board) {
        int[][] nArrayArray;
        ArrayList<Move> arrayList = new ArrayList<Move>();
        Position position = this.getPosition();
        for (int[] nArray : nArrayArray = new int[][]{{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}}) {
            Piece piece;
            Move move;
            Position position2 = position.offset(nArray[0], nArray[1]);
            if (!position2.isValid() || !this.canMoveTo(board, position2) || this.wouldMakeOwnKingVulnerable(board, move = Move.createMove(position, position2, this, piece = board.getPiece(position2)))) continue;
            arrayList.add(move);
        }
        return arrayList;
    }

    @Override
    public List<Position> getAttackPositions(Board board) {
        int[][] nArrayArray;
        ArrayList<Position> arrayList = new ArrayList<Position>();
        Position position = this.getPosition();
        for (int[] nArray : nArrayArray = new int[][]{{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}}) {
            Position position2 = position.offset(nArray[0], nArray[1]);
            if (!position2.isValid()) continue;
            arrayList.add(position2);
        }
        return arrayList;
    }

    @Override
    public String getType() {
        return "Knight";
    }

    @Override
    public Piece duplicate() {
        return new Knight(this.getColor(), this.getPosition(), this.hasMoved());
    }
}
