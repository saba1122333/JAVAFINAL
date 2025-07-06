/*
 * Decompiled with CFR 0.152.
 */
package Controller;

import Model.Board;
import Model.GameState;
import Model.Move;
import Model.Piece;
import Model.PieceColor;
import Model.Position;
import View.main.GameWindow;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GameController {
    private GameState gameState = new GameState();
    private GameWindow view;
    private String gameMode;
    private Timer clockTimer;
    private boolean clockRunning = false;
    private boolean isNetworkGame = false;

    public void setView(GameWindow gameWindow) {
        this.view = gameWindow;
    }

    public void setNetworkGame(boolean bl) {
        this.isNetworkGame = bl;
    }

    public void setGameMode(String string) {
        this.gameMode = string;
    }

    public void startNewGame(String string) {
        if (this.isNetworkGame) {
            if (this.view != null) {
                this.view.updateStatus("Cannot start new game during network play.");
            }
            return;
        }
        this.gameMode = string;
        this.gameState.resetGame();
        this.stopClock();
        if (this.gameState.isTimedGame()) {
            this.startClock();
        }
        this.updateView();
        if (string.equals("Computer vs Computer") || string.equals("Player vs Computer") && this.gameState.getCurrentPlayerColor() == 0) {
            this.makeComputerMove();
        }
    }

    public void startTimedGame(String string, int n, int n2, int n3) {
        if (this.isNetworkGame) {
            if (this.view != null) {
                this.view.updateStatus("Cannot start timed game during network play.");
            }
            return;
        }
        this.gameMode = string;
        this.gameState = new GameState(n, n2, n3);
        this.startClock();
        this.updateView();
        if (string.equals("Computer vs Computer") || string.equals("Player vs Computer") && this.gameState.getCurrentPlayerColor() == 0) {
            this.makeComputerMove();
        }
    }

    public boolean makeMove(Position position, Position position2) {
        Board board = this.gameState.getBoard();
        Piece piece = board.getPiece(position);
        if (piece == null || piece.getColor() != this.gameState.getCurrentPlayerColor()) {
            return false;
        }
        List<Move> list = this.getLegalMovesForPiece(piece);
        Move move = null;
        for (Move move2 : list) {
            if (!move2.getOrigin().equals(position) || !move2.getDestination().equals(position2)) continue;
            move = move2;
            break;
        }
        if (move == null) {
            return false;
        }
        boolean bl = this.gameState.makeMove(move);
        if (bl) {
            if (this.isNetworkGame) {
                this.updateBoardOnly();
            } else {
                this.updateView();
            }
            if (this.gameState.isGameOver()) {
                this.stopClock();
                this.view.showGameOver(this.gameState.getGameResult());
                return true;
            }
            if (this.gameMode != null && (this.gameMode.equals("Player vs Computer") && this.gameState.getCurrentPlayerColor() == 0 || this.gameMode.equals("Computer vs Computer"))) {
                this.makeComputerMove();
            }
        }
        return bl;
    }

    private void makeComputerMove() {
        List<Move> list = this.getAllLegalMoves(this.gameState.getCurrentPlayerColor());
        if (!list.isEmpty()) {
            Move move = list.get(0);
            new Thread(() -> {
                try {
                    Thread.sleep(500L);
                    this.gameState.makeMove(move);
                    if (this.isNetworkGame) {
                        this.updateBoardOnly();
                    } else {
                        this.updateView();
                    }
                    if (this.gameState.isGameOver()) {
                        this.stopClock();
                        this.view.showGameOver(this.gameState.getGameResult());
                    } else if (this.gameMode != null && this.gameMode.equals("Computer vs Computer")) {
                        this.makeComputerMove();
                    }
                }
                catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }).start();
        }
    }

    public void updateView() {
        if (this.view != null) {
            this.view.refreshBoard();
            if (!this.isNetworkGame) {
                String string = PieceColor.colorName(this.gameState.getCurrentPlayerColor()) + "'s turn";
                if (this.gameState.isInCheck(this.gameState.getCurrentPlayerColor())) {
                    string = string + " (CHECK)";
                }
                if (this.gameState.isTimedGame()) {
                    String string2 = this.gameState.getClock(1).getTime();
                    String string3 = this.gameState.getClock(0).getTime();
                    string = string + " | White: " + string2 + " | Black: " + string3;
                }
                this.view.updateStatus(string);
            }
        }
    }

    public void updateBoardOnly() {
        if (this.view != null) {
            this.view.refreshBoard();
        }
    }

    public Piece getPieceAt(Position position) {
        return this.gameState.getBoard().getPiece(position);
    }

    public List<Position> getLegalMovePositions(Position position) {
        Piece piece = this.getPieceAt(position);
        if (piece == null) {
            return new ArrayList<Position>();
        }
        List<Move> list = this.getLegalMovesForPiece(piece);
        ArrayList<Position> arrayList = new ArrayList<Position>();
        for (Move move : list) {
            arrayList.add(move.getDestination());
        }
        return arrayList;
    }

    private List<Move> getLegalMovesForPiece(Piece piece) {
        return piece.getLegalMoves(this.gameState.getBoard());
    }

    private List<Move> getAllLegalMoves(int n) {
        return this.gameState.getBoard().getAllLegalMoves(n);
    }

    public void surrender() {
        this.gameState.surrender();
        this.stopClock();
        this.view.showGameOver(this.gameState.getGameResult());
    }

    public Board getBoard() {
        return this.gameState.getBoard();
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public int getCurrentTurn() {
        return this.gameState.getCurrentPlayerColor();
    }

    public boolean isGameOver() {
        return this.gameState.isGameOver();
    }

    public void setTimeControl(int n, int n2, int n3) {
        this.gameState.setTimeControl(n, n2, n3);
        if (this.gameState.isTimedGame() && !this.clockRunning) {
            this.startClock();
        }
        this.updateView();
    }

    private void startClock() {
        if (this.clockTimer != null) {
            this.clockTimer.cancel();
        }
        this.clockTimer = new Timer();
        this.clockRunning = true;
        this.clockTimer.scheduleAtFixedRate(new TimerTask(){

            @Override
            public void run() {
                if (GameController.this.gameState.decrementCurrentPlayerClock()) {
                    GameController.this.stopClock();
                    GameController.this.view.showGameOver(GameController.this.gameState.getGameResult());
                }
                GameController.this.updateView();
            }
        }, 1000L, 1000L);
    }

    private void stopClock() {
        if (this.clockTimer != null) {
            this.clockTimer.cancel();
            this.clockTimer = null;
            this.clockRunning = false;
        }
    }

    public boolean toggleClockPause() {
        if (this.clockRunning) {
            this.stopClock();
        } else if (this.gameState.isTimedGame() && !this.gameState.isGameOver()) {
            this.startClock();
        }
        return this.clockRunning;
    }
}
