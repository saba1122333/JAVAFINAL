/*
 * Decompiled with CFR 0.152.
 */
package Network;

import Model.Position;
import java.io.Serializable;

public class NetworkMessage
implements Serializable {
    private static final long serialVersionUID = 1L;
    private MessageType type;
    private String data;
    private Position fromPosition;
    private Position toPosition;
    private String gameResult;
    private String moveDescription;
    private String playerName;

    public NetworkMessage(MessageType messageType, Position position, Position position2, String string) {
        this.type = messageType;
        this.fromPosition = position;
        this.toPosition = position2;
        this.moveDescription = string;
    }

    public NetworkMessage(MessageType messageType, Position position, Position position2) {
        this.type = messageType;
        this.fromPosition = position;
        this.toPosition = position2;
        this.moveDescription = "";
    }

    public NetworkMessage(MessageType messageType, String string) {
        this.type = messageType;
        this.data = string;
    }

    public NetworkMessage(MessageType messageType, String string, boolean bl) {
        this.type = messageType;
        this.gameResult = string;
    }

    public NetworkMessage(MessageType messageType, String string, int n) {
        this.type = messageType;
        this.playerName = string;
    }

    public MessageType getType() {
        return this.type;
    }

    public String getData() {
        return this.data;
    }

    public Position getFromPosition() {
        return this.fromPosition;
    }

    public Position getToPosition() {
        return this.toPosition;
    }

    public String getGameResult() {
        return this.gameResult;
    }

    public String getMoveDescription() {
        return this.moveDescription;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public String toString() {
        switch (this.type.ordinal()) {
            case 0: {
                if (this.moveDescription != null && !this.moveDescription.isEmpty()) {
                    return "MOVE: " + this.moveDescription;
                }
                return "MOVE: " + String.valueOf(this.fromPosition) + " to " + String.valueOf(this.toPosition);
            }
            case 1: {
                return "GAME_START: " + this.data;
            }
            case 2: {
                return "GAME_END: " + this.gameResult;
            }
            case 3: {
                return "CONNECTION_ESTABLISHED: " + this.data;
            }
            case 4: {
                return "ERROR: " + this.data;
            }
            case 5: {
                return "PING";
            }
            case 6: {
                return "PLAYER_INFO: " + this.playerName;
            }
            case 7: {
                return "RESIGNATION: " + this.data;
            }
        }
        return "UNKNOWN: " + this.data;
    }

    public static enum MessageType {
        MOVE,
        GAME_START,
        GAME_END,
        CONNECTION_ESTABLISHED,
        ERROR,
        PING,
        PLAYER_INFO,
        RESIGNATION;

    }
}
