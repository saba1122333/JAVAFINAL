/*
 * Decompiled with CFR 0.152.
 */
package View.components;

import Controller.GameController;
import Model.Board;
import Model.Piece;
import Model.Position;
import View.components.BoardView;
import View.components.ChessSquareView;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JPanel;

public class ChessBoardUI
extends JPanel
implements BoardView {
    private static final int BOARD_SIZE = 8;
    private static final int SQUARE_SIZE = 60;
    private ChessSquareView[][] squares;
    private GameController controller;
    private Position selectedPosition;
    private List<Position> legalMovePositions;
    private boolean isFlipped = false;
    private NetworkMoveHandler networkMoveHandler = null;

    public void setNetworkMoveHandler(NetworkMoveHandler networkMoveHandler) {
        this.networkMoveHandler = networkMoveHandler;
    }

    public ChessBoardUI(GameController gameController) {
        this.controller = gameController;
        this.setLayout(new GridLayout(8, 8));
        this.setPreferredSize(new Dimension(480, 480));
        this.squares = new ChessSquareView[8][8];
        this.initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                ChessSquareView chessSquareView;
                boolean bl = (i + j) % 2 != 0;
                Position position = new Position(j, i);
                this.squares[i][j] = chessSquareView = new ChessSquareView(position, bl, this);
                this.add(chessSquareView);
            }
        }
    }

    public void updateBoard() {
        Board board = this.controller.getBoard();
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                Position position = new Position(j, i);
                Piece piece = board.getPiece(position);
                int n = this.isFlipped ? 7 - i : i;
                int n2 = this.isFlipped ? 7 - j : j;
                this.squares[n][n2].setPiece(piece);
            }
        }
        this.repaint();
    }

    @Override
    public void updateBoard(Board board) {
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                Position position = new Position(j, i);
                Piece piece = board.getPiece(position);
                int n = this.isFlipped ? 7 - i : i;
                int n2 = this.isFlipped ? 7 - j : j;
                this.squares[n][n2].setPiece(piece);
            }
        }
        this.repaint();
    }

    @Override
    public void setCurrentPlayer(int n) {
    }

    @Override
    public void showGameOverMessage(String string) {
    }

    @Override
    public void highlightLastMove(Position position, Position position2) {
        this.clearHighlights();
        if (position != null) {
            this.getSquareView(position).setHighlighted(true);
        }
        if (position2 != null) {
            this.getSquareView(position2).setHighlighted(true);
        }
    }

    @Override
    public void showLegalMoves(Position position, List<Position> list) {
        this.selectedPosition = position;
        this.legalMovePositions = list;
        if (position != null) {
            this.getSquareView(position).setSelected(true);
        }
        if (list != null) {
            for (Position position2 : list) {
                this.getSquareView(position2).setLegalMove(true);
            }
        }
        this.repaint();
    }

    @Override
    public void showCheck(Position position, boolean bl) {
        if (position != null && bl) {
            this.getSquareView(position).setInCheck(true);
        }
    }

    @Override
    public void updateStatus(String string) {
    }

    @Override
    public void clearHighlights() {
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                this.squares[i][j].clearState();
            }
        }
        this.repaint();
    }

    public void clearSelection() {
        this.selectedPosition = null;
        this.legalMovePositions = null;
        this.clearHighlights();
    }

    public void handleSquareClick(Position position) {
        if (this.selectedPosition == null) {
            Piece piece = this.controller.getPieceAt(position);
            if (piece != null && piece.getColor() == this.controller.getCurrentTurn()) {
                List<Position> list = this.controller.getLegalMovePositions(position);
                this.showLegalMoves(position, list);
            }
        } else if (position.equals(this.selectedPosition)) {
            this.clearSelection();
        } else if (this.legalMovePositions != null && this.legalMovePositions.contains(position)) {
            if (this.networkMoveHandler != null) {
                this.networkMoveHandler.onMove(this.selectedPosition, position);
            } else {
                this.controller.makeMove(this.selectedPosition, position);
            }
            this.clearSelection();
        } else {
            Piece piece = this.controller.getPieceAt(position);
            if (piece != null && piece.getColor() == this.controller.getCurrentTurn()) {
                this.clearSelection();
                List<Position> list = this.controller.getLegalMovePositions(position);
                this.showLegalMoves(position, list);
            } else {
                this.clearSelection();
            }
        }
    }

    private ChessSquareView getSquareView(Position position) {
        int n = position.getRow();
        int n2 = position.getColumn();
        if (this.isFlipped) {
            n = 7 - n;
            n2 = 7 - n2;
        }
        return this.squares[n][n2];
    }

    public boolean isFlipped() {
        return this.isFlipped;
    }

    public void setFlipped(boolean bl) {
        this.isFlipped = bl;
        this.removeAll();
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                int n = bl ? 7 - i : i;
                int n2 = bl ? 7 - j : j;
                this.add(this.squares[n][n2]);
            }
        }
        this.updateBoard();
        this.revalidate();
        this.repaint();
    }

    public static interface NetworkMoveHandler {
        public void onMove(Position var1, Position var2);
    }
}
