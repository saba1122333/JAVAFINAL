/*
 * Decompiled with CFR 0.152.
 */
package Model;

import java.io.Serializable;

public class Position
implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int column;
    private final int row;

    public Position(int n, int n2) {
        this.column = n;
        this.row = n2;
    }

    public int getColumn() {
        return this.column;
    }

    public int getRow() {
        return this.row;
    }

    public Position offset(int n, int n2) {
        return new Position(this.column + n, this.row + n2);
    }

    public boolean isValid() {
        return this.column >= 0 && this.column < 8 && this.row >= 0 && this.row < 8;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        Position position = (Position)object;
        return this.column == position.column && this.row == position.row;
    }

    public int hashCode() {
        return 31 * this.column + this.row;
    }

    public String toString() {
        char c = (char)(97 + this.column);
        int n = 8 - this.row;
        return "" + c + n;
    }
}
