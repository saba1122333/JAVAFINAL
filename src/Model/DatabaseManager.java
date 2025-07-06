/*
 * Decompiled with CFR 0.152.
 */
package Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String PLAYERS_DB = "players.db";
    private static final String GAMES_DB = "games.db";

    public static void initializeDatabases() {
        DatabaseManager.initializePlayersDatabase();
        DatabaseManager.initializeGamesDatabase();
    }

    private static void initializePlayersDatabase() {
        String string = "    CREATE TABLE IF NOT EXISTS players (\n        id INTEGER PRIMARY KEY AUTOINCREMENT,\n        username TEXT UNIQUE NOT NULL,\n        password TEXT NOT NULL,\n        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n    )\n";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:players.db");
             Statement statement = connection.createStatement();){
            statement.execute(string);
        }
        catch (SQLException sQLException) {
            System.err.println("Error initializing players database: " + sQLException.getMessage());
        }
    }

    private static void initializeGamesDatabase() {
        String string = "    CREATE TABLE IF NOT EXISTS games (\n        id INTEGER PRIMARY KEY AUTOINCREMENT,\n        event TEXT DEFAULT 'Network',\n        site TEXT DEFAULT 'Local',\n        date TEXT NOT NULL,\n        round TEXT DEFAULT '1',\n        white_player TEXT NOT NULL,\n        black_player TEXT NOT NULL,\n        result TEXT,\n        white_elo TEXT DEFAULT '',\n        black_elo TEXT DEFAULT '',\n        eco TEXT DEFAULT '',\n        moves TEXT NOT NULL,\n        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n    )\n";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:games.db");
             Statement statement = connection.createStatement();){
            statement.execute(string);
        }
        catch (SQLException sQLException) {
            System.err.println("Error initializing games database: " + sQLException.getMessage());
        }
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static boolean registerPlayer(String string, String string2) {
        String string3 = "INSERT INTO players (username, password) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:players.db");){
            boolean bl;
            block16: {
                PreparedStatement preparedStatement = connection.prepareStatement(string3);
                try {
                    preparedStatement.setString(1, string);
                    preparedStatement.setString(2, string2);
                    int n = preparedStatement.executeUpdate();
                    boolean bl2 = bl = n > 0;
                    if (preparedStatement == null) break block16;
                }
                catch (Throwable throwable) {
                    if (preparedStatement != null) {
                        try {
                            preparedStatement.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                preparedStatement.close();
            }
            return bl;
        }
        catch (SQLException sQLException) {
            if (sQLException.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("Username already exists: " + string);
            } else {
                System.err.println("Error registering player: " + sQLException.getMessage());
            }
            return false;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static boolean authenticatePlayer(String string, String string2) {
        String string3 = "SELECT password FROM players WHERE username = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:players.db");
             PreparedStatement preparedStatement = connection.prepareStatement(string3);){
            preparedStatement.setString(1, string);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) return false;
            String string4 = resultSet.getString("password");
            boolean bl = string2.equals(string4);
            return bl;
        }
        catch (SQLException sQLException) {
            System.err.println("Error authenticating player: " + sQLException.getMessage());
        }
        return false;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static boolean playerExists(String string) {
        String string2 = "SELECT COUNT(*) FROM players WHERE username = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:players.db");
             PreparedStatement preparedStatement = connection.prepareStatement(string2);){
            preparedStatement.setString(1, string);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) return false;
            boolean bl = resultSet.getInt(1) > 0;
            return bl;
        }
        catch (SQLException sQLException) {
            System.err.println("Error checking player existence: " + sQLException.getMessage());
        }
        return false;
    }

    public static void saveGame(String string, String string2, String string3, String string4, String string5, String string6, String string7) {
        String string8 = "    INSERT INTO games (event, site, date, white_player, black_player, result, eco, moves)\n    VALUES (?, ?, date('now'), ?, ?, ?, ?, ?)\n";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:games.db");
             PreparedStatement preparedStatement = connection.prepareStatement(string8);){
            preparedStatement.setString(1, string5 != null ? string5 : "Network");
            preparedStatement.setString(2, string6 != null ? string6 : "Local");
            preparedStatement.setString(3, string);
            preparedStatement.setString(4, string2);
            preparedStatement.setString(5, string3);
            preparedStatement.setString(6, string7 != null ? string7 : "");
            preparedStatement.setString(7, string4);
            preparedStatement.executeUpdate();
        }
        catch (SQLException sQLException) {
            System.err.println("Error saving game: " + sQLException.getMessage());
        }
    }

    public static List<String> getPlayerGames(String string) {
        ArrayList<String> arrayList = new ArrayList<String>();
        String string2 = "    SELECT event, site, date, white_player, black_player, result, eco, moves\n    FROM games\n    WHERE white_player = ? OR black_player = ?\n    ORDER BY date DESC, created_at DESC\n";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:games.db");
             PreparedStatement preparedStatement = connection.prepareStatement(string2);){
            preparedStatement.setString(1, string);
            preparedStatement.setString(2, string);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("[Event \"").append(resultSet.getString("event")).append("\"]\n");
                stringBuilder.append("[Site \"").append(resultSet.getString("site")).append("\"]\n");
                stringBuilder.append("[Date \"").append(resultSet.getString("date")).append("\"]\n");
                stringBuilder.append("[Round \"1\"]\n");
                stringBuilder.append("[White \"").append(resultSet.getString("white_player")).append("\"]\n");
                stringBuilder.append("[Black \"").append(resultSet.getString("black_player")).append("\"]\n");
                stringBuilder.append("[Result \"").append(resultSet.getString("result")).append("\"]\n");
                stringBuilder.append("[WhiteElo \"\"]\n");
                stringBuilder.append("[BlackElo \"\"]\n");
                stringBuilder.append("[ECO \"").append(resultSet.getString("eco")).append("\"]\n\n");
                stringBuilder.append(resultSet.getString("moves"));
                arrayList.add(stringBuilder.toString());
            }
        }
        catch (SQLException sQLException) {
            System.err.println("Error retrieving player games: " + sQLException.getMessage());
        }
        return arrayList;
    }

    public static List<String> getAllGames() {
        ArrayList<String> arrayList = new ArrayList<String>();
        String string = "    SELECT event, site, date, white_player, black_player, result, eco, moves\n    FROM games\n    ORDER BY date DESC, created_at DESC\n";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:games.db");
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(string);){
            while (resultSet.next()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("[Event \"").append(resultSet.getString("event")).append("\"]\n");
                stringBuilder.append("[Site \"").append(resultSet.getString("site")).append("\"]\n");
                stringBuilder.append("[Date \"").append(resultSet.getString("date")).append("\"]\n");
                stringBuilder.append("[Round \"1\"]\n");
                stringBuilder.append("[White \"").append(resultSet.getString("white_player")).append("\"]\n");
                stringBuilder.append("[Black \"").append(resultSet.getString("black_player")).append("\"]\n");
                stringBuilder.append("[Result \"").append(resultSet.getString("result")).append("\"]\n");
                stringBuilder.append("[WhiteElo \"\"]\n");
                stringBuilder.append("[BlackElo \"\"]\n");
                stringBuilder.append("[ECO \"").append(resultSet.getString("eco")).append("\"]\n\n");
                stringBuilder.append(resultSet.getString("moves"));
                arrayList.add(stringBuilder.toString());
            }
        }
        catch (SQLException sQLException) {
            System.err.println("Error retrieving all games: " + sQLException.getMessage());
        }
        return arrayList;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static boolean clearGameDatabase() {
        String string = "DELETE FROM games";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:games.db");){
            boolean bl;
            block14: {
                Statement statement = connection.createStatement();
                try {
                    int n = statement.executeUpdate(string);
                    System.out.println("Cleared " + n + " games from database");
                    bl = true;
                    if (statement == null) break block14;
                }
                catch (Throwable throwable) {
                    if (statement != null) {
                        try {
                            statement.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                statement.close();
            }
            return bl;
        }
        catch (SQLException sQLException) {
            System.err.println("Error clearing game database: " + sQLException.getMessage());
            return false;
        }
    }

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException classNotFoundException) {
            System.err.println("SQLite JDBC driver not found: " + classNotFoundException.getMessage());
        }
    }
}
