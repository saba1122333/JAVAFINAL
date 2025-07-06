/*
 * Decompiled with CFR 0.152.
 */
package Model;

import Model.Piece;
import Model.Position;

public class Move {
    private final Position origin;
    private final Position destination;
    private final Piece movingPiece;
    private final Piece takenPiece;
    private final boolean promotion;
    private final boolean castlingMove;
    private final boolean enPassantCapture;
    private final int moveFlags;
    public static final int FLAG_CHECK = 1;
    public static final int FLAG_CHECKMATE = 2;
    public static final int FLAG_FIRST_MOVE = 4;

    public Move(Position position, Position position2, Piece piece) {
        this(position, position2, piece, null, false, false, false, 0);
    }

    public Move(Position position, Position position2, Piece piece, Piece piece2, boolean bl, boolean bl2, boolean bl3, int n) {
        this.origin = position;
        this.destination = position2;
        this.movingPiece = piece;
        this.takenPiece = piece2;
        this.promotion = bl;
        this.castlingMove = bl2;
        this.enPassantCapture = bl3;
        this.moveFlags = n;
    }

    public static Move createMove(Position position, Position position2, Piece piece, Piece piece2) {
        return new Move(position, position2, piece, piece2, false, false, false, 0);
    }

    public static Move createPromotion(Position position, Position position2, Piece piece, Piece piece2) {
        return new Move(position, position2, piece, piece2, true, false, false, 0);
    }

    public static Move createCastling(Position position, Position position2, Piece piece) {
        return new Move(position, position2, piece, null, false, true, false, 0);
    }

    public static Move createEnPassant(Position position, Position position2, Piece piece, Piece piece2) {
        return new Move(position, position2, piece, piece2, false, false, true, 0);
    }

    public Position getOrigin() {
        return this.origin;
    }

    public Position getDestination() {
        return this.destination;
    }

    public Piece getMovingPiece() {
        return this.movingPiece;
    }

    public Piece getTakenPiece() {
        return this.takenPiece;
    }

    public boolean isPromotion() {
        return this.promotion;
    }

    public boolean isCastlingMove() {
        return this.castlingMove;
    }

    public boolean isEnPassantCapture() {
        return this.enPassantCapture;
    }

    public boolean hasFlag(int n) {
        return (this.moveFlags & n) != 0;
    }

    public int getMoveFlags() {
        return this.moveFlags;
    }

    public Move withFlag(int n) {
        return new Move(this.origin, this.destination, this.movingPiece, this.takenPiece, this.promotion, this.castlingMove, this.enPassantCapture, this.moveFlags | n);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (this.castlingMove) {
            if (this.destination.getColumn() > this.origin.getColumn()) {
                stringBuilder.append("O-O");
            } else {
                stringBuilder.append("O-O-O");
            }
        } else {
            String string;
            String string2 = this.getPieceSymbol(this.movingPiece.getType());
            if (!string2.isEmpty()) {
                stringBuilder.append(string2);
            }
            if (!(string = this.getDisambiguation()).isEmpty()) {
                stringBuilder.append(string);
            }
            if (this.takenPiece != null) {
                if (string2.isEmpty()) {
                    stringBuilder.append((char)(97 + this.origin.getColumn()));
                }
                stringBuilder.append("x");
            }
            stringBuilder.append(this.destination.toString());
            if (this.promotion) {
                stringBuilder.append("=Q");
            }
        }
        if (this.hasFlag(2)) {
            stringBuilder.append("#");
        } else if (this.hasFlag(1)) {
            stringBuilder.append("+");
        }
        return stringBuilder.toString();
    }

    private String getPieceSymbol(String string) {
        switch (string) {
            case "King": {
                return "K";
            }
            case "Queen": {
                return "Q";
            }
            case "Rook": {
                return "R";
            }
            case "Bishop": {
                return "B";
            }
            case "Knight": {
                return "N";
            }
            case "Pawn": {
                return "";
            }
        }
        return "";
    }

    private String getDisambiguation() {
        if (this.movingPiece.getType().equals("Pawn")) {
            return "";
        }
        return "";
    }
}
