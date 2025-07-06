/*
 * Decompiled with CFR 0.152.
 */
package Model;

import Model.Board;
import Model.Clock;
import Model.Move;
import Model.Piece;
import Model.PieceColor;
import java.util.ArrayList;
import java.util.List;

public class GameState {
    private Board chessBoard = new Board();
    private int currentPlayerColor = 1;
    private boolean gameOver = false;
    private String result = "";
    private int nonCaptureMoveCounter = 0;
    private int moveNumber = 1;
    private Clock whiteTimer = new Clock(0, 15, 0);
    private Clock blackTimer = new Clock(0, 15, 0);
    private boolean timedGame = false;
    private List<Move> moveHistory = new ArrayList<Move>();

    public GameState() {
    }

    public GameState(int n, int n2, int n3) {
        this();
        this.whiteTimer = new Clock(n, n2, n3);
        this.blackTimer = new Clock(n, n2, n3);
        this.timedGame = true;
    }

    public Board getBoard() {
        return this.chessBoard;
    }

    public int getCurrentPlayerColor() {
        return this.currentPlayerColor;
    }

    public boolean isGameOver() {
        return this.gameOver;
    }

    public String getGameResult() {
        return this.result;
    }

    public Clock getClock(int n) {
        return n == 1 ? this.whiteTimer : this.blackTimer;
    }

    public boolean isTimedGame() {
        return this.timedGame;
    }

    public void setTimedGame(boolean bl) {
        this.timedGame = bl;
    }

    public void setTimeControl(int n, int n2, int n3) {
        this.whiteTimer = new Clock(n, n2, n3);
        this.blackTimer = new Clock(n, n2, n3);
        this.timedGame = true;
    }

    public boolean decrementCurrentPlayerClock() {
        if (!this.timedGame || this.gameOver) {
            return false;
        }
        Clock clock = this.currentPlayerColor == 1 ? this.whiteTimer : this.blackTimer;
        clock.decr();
        if (clock.outOfTime()) {
            this.gameOver = true;
            int n = PieceColor.opponent(this.currentPlayerColor);
            this.result = PieceColor.colorName(n) + " wins on time";
            return true;
        }
        return false;
    }

    public boolean makeMove(Move move) {
        if (move.getMovingPiece().getColor() != this.currentPlayerColor) {
            return false;
        }
        boolean bl = this.chessBoard.executeMove(move);
        if (!bl) {
            return false;
        }
        this.moveHistory.add(move);
        this.nonCaptureMoveCounter = move.getMovingPiece().getType().equals("Pawn") || move.getTakenPiece() != null ? 0 : ++this.nonCaptureMoveCounter;
        if (this.currentPlayerColor == 0) {
            ++this.moveNumber;
        }
        this.currentPlayerColor = PieceColor.opponent(this.currentPlayerColor);
        this.checkEndConditions();
        return true;
    }

    private void checkEndConditions() {
        if (this.chessBoard.isCheckmate(this.currentPlayerColor)) {
            this.gameOver = true;
            int n = PieceColor.opponent(this.currentPlayerColor);
            this.result = PieceColor.colorName(n) + " wins by checkmate";
        } else if (this.chessBoard.isStalemate(this.currentPlayerColor)) {
            this.gameOver = true;
            this.result = "Draw by stalemate";
        } else if (this.nonCaptureMoveCounter >= 100) {
            this.gameOver = true;
            this.result = "Draw by 50-move rule";
        } else if (this.isInsufficientMaterial()) {
            this.gameOver = true;
            this.result = "Draw by insufficient material";
        }
    }

    private boolean isInsufficientMaterial() {
        List<Piece> list = this.chessBoard.getPiecesByColor(1);
        List<Piece> list2 = this.chessBoard.getPiecesByColor(0);
        if (list.size() == 1 && list2.size() == 1) {
            return true;
        }
        if (list.size() == 2 && list2.size() == 1 || list.size() == 1 && list2.size() == 2) {
            List<Piece> list3 = list.size() == 2 ? list : list2;
            for (Piece piece : list3) {
                if (piece.getType().equals("King")) continue;
                return piece.getType().equals("Knight") || piece.getType().equals("Bishop");
            }
        }
        return false;
    }

    public void resetGame() {
        this.chessBoard = new Board();
        this.currentPlayerColor = 1;
        this.gameOver = false;
        this.result = "";
        this.nonCaptureMoveCounter = 0;
        this.moveNumber = 1;
        this.moveHistory.clear();
        if (this.timedGame) {
            int n = this.whiteTimer.getHours();
            int n2 = this.whiteTimer.getMinutes();
            int n3 = this.whiteTimer.getSeconds();
            this.whiteTimer = new Clock(n, n2, n3);
            this.blackTimer = new Clock(n, n2, n3);
        }
    }

    public void surrender() {
        this.gameOver = true;
        int n = PieceColor.opponent(this.currentPlayerColor);
        this.result = PieceColor.colorName(n) + " wins by resignation";
    }

    public boolean isInCheck(int n) {
        return this.chessBoard.isKingInCheck(n);
    }

    public List<Move> getMoveHistory() {
        return new ArrayList<Move>(this.moveHistory);
    }

    public String getPGNMoves() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.moveHistory.size(); i += 2) {
            stringBuilder.append(i / 2 + 1).append(".");
            if (i < this.moveHistory.size()) {
                stringBuilder.append(this.moveHistory.get(i).toString()).append(" ");
            }
            if (i + 1 >= this.moveHistory.size()) continue;
            stringBuilder.append(this.moveHistory.get(i + 1).toString()).append(" ");
        }
        return stringBuilder.toString().trim();
    }
}
