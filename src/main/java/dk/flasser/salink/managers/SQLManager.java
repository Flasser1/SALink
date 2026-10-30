package dk.flasser.salink.managers;

import dk.flasser.salink.SALink;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Component;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import java.io.File;
import java.sql.*;
@Component
public class SQLManager {
    private @Inject SALink saLink;

    public Connection con;

    ConsoleCommandSender console = Bukkit.getConsoleSender();

    public void connect() {
        if (isConnected()) {
            return;
        }

        File dataFolder = Bukkit.getPluginManager().getPlugin("SALINK").getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        try {
            Class.forName("org.sqlite.JDBC");

            File dbFile = new File(Bukkit.getPluginManager().getPlugin("SALINK").getDataFolder(), "database.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            con = DriverManager.getConnection(url);

            con.createStatement().execute("PRAGMA foreign_keys = ON;");

            saLink.getLogger().info("SALINK: DATABASE CONNECTED");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        if (!isConnected()) {
            return;
        }

        try {
            con.close();
            saLink.getLogger().info("SALINK: DATABASE DISCONNECTED");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return (con != null);
    }

    public Connection getConnection() {
        return con;
    }

    public boolean isSetUp() {
        if (!isConnected()) {
            return false;
        }

        try {
            Statement stmt = con.createStatement();

            String[] tables = {"Discord", "Minecraft"};
            for (String table : tables) {
                ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "'");
                if (!rs.next()) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setUp() {
        if (!isConnected()) {
            return;
        }

        try {
            con.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS Discord (" +
                    "ID VARCHAR(20) PRIMARY KEY" +
                    ");");

            con.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS Minecraft (" +
                    "UUID VARCHAR(36) PRIMARY KEY " +
                    ");");


            con.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS Linked (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "DiscordID VARCHAR(20) NOT NULL, " +
                    "MinecraftUUID VARCHAR(36) NOT NULL, " +
                    "Active INTEGER NOT NULL DEFAULT 0, " +
                    "TimeDate BIGINT NOT NULL, " +
                    "FOREIGN KEY (DiscordID) REFERENCES Discord(ID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (MinecraftUUID) REFERENCES Minecraft(UUID) ON DELETE CASCADE" +
                    ");"
            );


            console.sendMessage("SALINK: DATABASE TABLES SETUP COMPLETE");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}