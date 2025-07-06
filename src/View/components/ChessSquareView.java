/*
 * Decompiled with CFR 0.152.
 */
package View.components;

import Model.Piece;
import Model.Position;
import View.components.ChessBoardUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class ChessSquareView
extends JPanel {
    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);
    private static final Color HIGHLIGHT_COLOR = new Color(255, 255, 0, 100);
    private static final Color SELECTED_COLOR = new Color(0, 255, 0, 100);
    private static final Color LEGAL_MOVE_COLOR = new Color(0, 0, 255, 100);
    private static final Color CHECK_COLOR = new Color(255, 0, 0, 100);
    private static final String WHITE_KING = "\u2654";
    private static final String WHITE_QUEEN = "\u2655";
    private static final String WHITE_ROOK = "\u2656";
    private static final String WHITE_BISHOP = "\u2657";
    private static final String WHITE_KNIGHT = "\u2658";
    private static final String WHITE_PAWN = "\u2659";
    private static final String BLACK_KING = "\u265a";
    private static final String BLACK_QUEEN = "\u265b";
    private static final String BLACK_ROOK = "\u265c";
    private static final String BLACK_BISHOP = "\u265d";
    private static final String BLACK_KNIGHT = "\u265e";
    private static final String BLACK_PAWN = "\u265f";
    private Position position;
    private Piece piece;
    private boolean isLightSquare;
    private boolean isHighlighted;
    private boolean isSelected;
    private boolean isLegalMove;
    private boolean isInCheck;
    private ChessBoardUI boardUI;

    public ChessSquareView(final Position position, boolean bl, final ChessBoardUI chessBoardUI) {
        this.position = position;
        this.isLightSquare = bl;
        this.boardUI = chessBoardUI;
        this.isHighlighted = false;
        this.isSelected = false;
        this.isLegalMove = false;
        this.isInCheck = false;
        this.setPreferredSize(new Dimension(60, 60));
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.setOpaque(true);
        this.setBackground(bl ? LIGHT_SQUARE : DARK_SQUARE);
        this.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                chessBoardUI.handleSquareClick(position);
            }
        });
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
        this.repaint();
    }

    public void setHighlighted(boolean bl) {
        this.isHighlighted = bl;
        this.repaint();
    }

    public void setSelected(boolean bl) {
        this.isSelected = bl;
        this.repaint();
    }

    public void setLegalMove(boolean bl) {
        this.isLegalMove = bl;
        this.repaint();
    }

    public void setInCheck(boolean bl) {
        this.isInCheck = bl;
        this.repaint();
    }

    public void clearState() {
        this.isHighlighted = false;
        this.isSelected = false;
        this.isLegalMove = false;
        this.isInCheck = false;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.setColor(this.isLightSquare ? LIGHT_SQUARE : DARK_SQUARE);
        graphics.fillRect(0, 0, this.getWidth(), this.getHeight());
        if (this.isHighlighted) {
            graphics.setColor(HIGHLIGHT_COLOR);
            graphics.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
        if (this.isSelected) {
            graphics.setColor(SELECTED_COLOR);
            graphics.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
        if (this.isLegalMove) {
            graphics.setColor(LEGAL_MOVE_COLOR);
            graphics.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
        if (this.isInCheck) {
            graphics.setColor(CHECK_COLOR);
            graphics.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
        if (this.piece != null) {
            Graphics2D graphics2D = (Graphics2D)graphics;
            graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Font font = new Font("Arial Unicode MS", 0, 40);
            graphics2D.setFont(font);
            String string = this.getUnicodeSymbol(this.piece);
            FontMetrics fontMetrics = graphics2D.getFontMetrics(font);
            int n = (this.getWidth() - fontMetrics.stringWidth(string)) / 2;
            int n2 = (this.getHeight() - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent();
            graphics2D.setColor(Color.BLACK);
            graphics2D.drawString(string, n, n2);
        }
    }

    private String getUnicodeSymbol(Piece piece) {
        if (piece == null) {
            return "";
        }
        String string = piece.getType();
        boolean bl = piece.getColor() == 1;
        switch (string) {
            case "King": {
                return bl ? WHITE_KING : BLACK_KING;
            }
            case "Queen": {
                return bl ? WHITE_QUEEN : BLACK_QUEEN;
            }
            case "Rook": {
                return bl ? WHITE_ROOK : BLACK_ROOK;
            }
            case "Bishop": {
                return bl ? WHITE_BISHOP : BLACK_BISHOP;
            }
            case "Knight": {
                return bl ? WHITE_KNIGHT : BLACK_KNIGHT;
            }
            case "Pawn": {
                return bl ? WHITE_PAWN : BLACK_PAWN;
            }
        }
        return string;
    }
}
