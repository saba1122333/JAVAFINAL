/*
 * Decompiled with CFR 0.152.
 */
package Model;

import Model.Move;
import Model.Piece;
import Model.Position;
import Model.pieces.Bishop;
import Model.pieces.King;
import Model.pieces.Knight;
import Model.pieces.Pawn;
import Model.pieces.Queen;
import Model.pieces.Rook;
import java.util.ArrayList;
import java.util.List;

public class Board {
    private final Piece[][] boardArray = new Piece[8][8];
    private List<Piece> lightPieces = new ArrayList<Piece>();
    private List<Piece> darkPieces = new ArrayList<Piece>();
    private List<Move> moveSequence;
    private King lightKing;
    private King darkKing;

    public Board() {
        this.moveSequence = new ArrayList<Move>();
        this.setupInitialPosition();
    }

    public Board(Board board) {
        Position position;
        Piece piece;
        this.moveSequence = new ArrayList<Move>(board.moveSequence);
        for (Piece piece2 : board.lightPieces) {
            piece = piece2.duplicate();
            this.lightPieces.add(piece);
            position = piece.getPosition();
            if (position != null) {
                this.boardArray[position.getRow()][position.getColumn()] = piece;
            }
            if (!(piece instanceof King)) continue;
            this.lightKing = (King)piece;
        }
        for (Piece piece2 : board.darkPieces) {
            piece = piece2.duplicate();
            this.darkPieces.add(piece);
            position = piece.getPosition();
            if (position != null) {
                this.boardArray[position.getRow()][position.getColumn()] = piece;
            }
            if (!(piece instanceof King)) continue;
            this.darkKing = (King)piece;
        }
    }

    private void setupInitialPosition() {
        for (int i = 0; i < 8; ++i) {
            this.addPieceToBoard(new Pawn(0, new Position(i, 1)));
            this.addPieceToBoard(new Pawn(1, new Position(i, 6)));
        }
        this.addPieceToBoard(new Rook(0, new Position(0, 0)));
        this.addPieceToBoard(new Rook(0, new Position(7, 0)));
        this.addPieceToBoard(new Rook(1, new Position(0, 7)));
        this.addPieceToBoard(new Rook(1, new Position(7, 7)));
        this.addPieceToBoard(new Knight(0, new Position(1, 0)));
        this.addPieceToBoard(new Knight(0, new Position(6, 0)));
        this.addPieceToBoard(new Knight(1, new Position(1, 7)));
        this.addPieceToBoard(new Knight(1, new Position(6, 7)));
        this.addPieceToBoard(new Bishop(0, new Position(2, 0)));
        this.addPieceToBoard(new Bishop(0, new Position(5, 0)));
        this.addPieceToBoard(new Bishop(1, new Position(2, 7)));
        this.addPieceToBoard(new Bishop(1, new Position(5, 7)));
        this.addPieceToBoard(new Queen(0, new Position(3, 0)));
        this.addPieceToBoard(new Queen(1, new Position(3, 7)));
        this.darkKing = new King(0, new Position(4, 0));
        this.lightKing = new King(1, new Position(4, 7));
        this.addPieceToBoard(this.darkKing);
        this.addPieceToBoard(this.lightKing);
    }

    private void addPieceToBoard(Piece piece) {
        Position position = piece.getPosition();
        this.boardArray[position.getRow()][position.getColumn()] = piece;
        if (piece.getColor() == 1) {
            this.lightPieces.add(piece);
        } else {
            this.darkPieces.add(piece);
        }
    }

    public Piece getPiece(Position position) {
        if (!this.isPositionInBounds(position)) {
            return null;
        }
        return this.boardArray[position.getRow()][position.getColumn()];
    }

    public boolean isPositionInBounds(Position position) {
        int n = position.getColumn();
        int n2 = position.getRow();
        return n >= 0 && n < 8 && n2 >= 0 && n2 < 8;
    }

    public List<Piece> getPiecesByColor(int n) {
        return n == 1 ? new ArrayList<Piece>(this.lightPieces) : new ArrayList<Piece>(this.darkPieces);
    }

    public King getKing(int n) {
        return n == 1 ? this.lightKing : this.darkKing;
    }

    public boolean executeMove(Move move) {
        Piece piece;
        Object object;
        Position position = move.getOrigin();
        Position position2 = move.getDestination();
        Piece piece2 = move.getMovingPiece();
        if (move.getTakenPiece() != null) {
            object = move.getTakenPiece();
            if (((Piece)object).getColor() == 1) {
                this.lightPieces.remove(object);
            } else {
                this.darkPieces.remove(object);
            }
        }
        if (move.isEnPassantCapture() && (piece = this.getPiece((Position)(object = new Position(position2.getColumn(), position.getRow())))) != null) {
            if (piece.getColor() == 1) {
                this.lightPieces.remove(piece);
            } else {
                this.darkPieces.remove(piece);
            }
            this.boardArray[((Position)object).getRow()][((Position)object).getColumn()] = null;
        }
        if (move.isCastlingMove()) {
            int n = position.getRow();
            if (position2.getColumn() > position.getColumn()) {
                piece = this.getPiece(new Position(7, n));
                Position position3 = new Position(5, n);
                this.boardArray[n][7] = null;
                this.boardArray[n][5] = piece;
                piece.setPosition(position3);
                piece.setHasMoved(true);
            } else {
                piece = this.getPiece(new Position(0, n));
                Position position4 = new Position(3, n);
                this.boardArray[n][0] = null;
                this.boardArray[n][3] = piece;
                piece.setPosition(position4);
                piece.setHasMoved(true);
            }
        }
        this.boardArray[position.getRow()][position.getColumn()] = null;
        this.boardArray[position2.getRow()][position2.getColumn()] = piece2;
        piece2.setPosition(position2);
        piece2.setHasMoved(true);
        if (move.isPromotion()) {
            Queen queen = new Queen(piece2.getColor(), position2);
            if (piece2.getColor() == 1) {
                this.lightPieces.remove(piece2);
                this.lightPieces.add(queen);
            } else {
                this.darkPieces.remove(piece2);
                this.darkPieces.add(queen);
            }
            this.boardArray[position2.getRow()][position2.getColumn()] = queen;
        }
        this.moveSequence.add(move);
        return true;
    }

    public List<Move> getAllLegalMoves(int n) {
        ArrayList<Move> arrayList = new ArrayList<Move>();
        List<Piece> list = n == 1 ? this.lightPieces : this.darkPieces;
        for (Piece piece : list) {
            arrayList.addAll(piece.getLegalMoves(this));
        }
        return arrayList;
    }

    public boolean isKingInCheck(int n) {
        King king = n == 1 ? this.lightKing : this.darkKing;
        Position position = king.getPosition();
        List<Piece> list = n == 1 ? this.darkPieces : this.lightPieces;
        for (Piece piece : list) {
            List<Position> list2 = piece.getAttackPositions(this);
            if (!list2.contains(position)) continue;
            return true;
        }
        return false;
    }

    public boolean isCheckmate(int n) {
        if (!this.isKingInCheck(n)) {
            return false;
        }
        List<Move> list = this.getAllLegalMoves(n);
        return list.isEmpty();
    }

    public boolean isStalemate(int n) {
        if (this.isKingInCheck(n)) {
            return false;
        }
        List<Move> list = this.getAllLegalMoves(n);
        return list.isEmpty();
    }

    public Move getLastMove() {
        if (this.moveSequence.isEmpty()) {
            return null;
        }
        return this.moveSequence.get(this.moveSequence.size() - 1);
    }

    public List<Move> getMoveHistory() {
        return new ArrayList<Move>(this.moveSequence);
    }

    public boolean isPieceBetween(Position position, Position position2) {
        int n = Integer.compare(position2.getColumn(), position.getColumn());
        int n2 = Integer.compare(position2.getRow(), position.getRow());
        Position position3 = position;
        position3 = new Position(position3.getColumn() + n, position3.getRow() + n2);
        while (!position3.equals(position2)) {
            if (this.getPiece(position3) != null) {
                return true;
            }
            position3 = new Position(position3.getColumn() + n, position3.getRow() + n2);
        }
        return false;
    }

    public void clearBoard() {
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                this.boardArray[i][j] = null;
            }
        }
        this.lightPieces.clear();
        this.darkPieces.clear();
        this.moveSequence.clear();
    }

    public void placePieceForTesting(Piece piece) {
        Position position = piece.getPosition();
        this.boardArray[position.getRow()][position.getColumn()] = piece;
        if (piece.getColor() == 1) {
            this.lightPieces.add(piece);
            if (piece instanceof King) {
                this.lightKing = (King)piece;
            }
        } else {
            this.darkPieces.add(piece);
            if (piece instanceof King) {
                this.darkKing = (King)piece;
            }
        }
    }
}
