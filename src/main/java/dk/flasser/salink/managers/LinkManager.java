package dk.flasser.salink.managers;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

@Component
public class LinkManager {
    private @Inject SQLManager SQLManager;

    public static class LinkedAccount {
        public String ID;
        public long timeDate;
    }

    public void addLink(String discordID, UUID minecraftUUID) {
        if (!SQLManager.isConnected()) {
            return;
        }

        if (isLinked(discordID, minecraftUUID)) {
            removeLink(discordID, minecraftUUID);
        }

        String query = "INSERT INTO Linked (DiscordID, MinecraftUUID, Active, TimeDate) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = SQLManager.getConnection().prepareStatement(query)) {
            ps.setString(1, discordID);
            ps.setString(2, minecraftUUID.toString());
            ps.setInt(3, 1);
            ps.setLong(4, System.currentTimeMillis());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void removeLink(String discordID, UUID minecraftUUID) {
        if (!SQLManager.isConnected()) {
            return;
        }

        String query = "UPDATE Linked SET Active = 0 WHERE DiscordID = ? AND MinecraftUUID = ? AND Active = 1";
        try (PreparedStatement ps = SQLManager.getConnection().prepareStatement(query)) {
            ps.setString(1, discordID);
            ps.setString(2, minecraftUUID.toString());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isLinked(String discordID, UUID minecraftUUID) {
        if (!SQLManager.isConnected()) {
            return false;
        }

        String query = "SELECT 1 FROM Linked WHERE DiscordID = ? AND MinecraftUUID = ? AND Active = 1";

        try (PreparedStatement ps = SQLManager.getConnection().prepareStatement(query)) {
            ps.setString(1, discordID);
            ps.setString(2, minecraftUUID.toString());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<String> getLinkedActive(UUID minecraftUUID) {
        List<String> discordIDs = new ArrayList<>();

        if (!SQLManager.isConnected()) {
            return null;
        }

        String query = "SELECT DiscordID FROM Linked WHERE MinecraftUUID = ? AND Active = 1";

        try (PreparedStatement ps = SQLManager.getConnection().prepareStatement(query)) {
            ps.setString(1, minecraftUUID.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    discordIDs.add(rs.getString("DiscordID"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return discordIDs;
    }

    public List<String> getLinkedActive(String discordID) {
        List<String> minecraftUUIDs = new ArrayList<>();

        if (!SQLManager.isConnected()) {
            return null;
        }

        String query = "SELECT DiscordID FROM Linked WHERE DiscordID = ? AND Active = 1";

        try (PreparedStatement ps = SQLManager.getConnection().prepareStatement(query)) {
            ps.setString(1, discordID.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    minecraftUUIDs.add(rs.getString("DiscordID"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return minecraftUUIDs;
    }

    public List<String> getLinkedAll(UUID minecraftUUID) {
        List<LinkedAccount> accounts = new ArrayList<>();

        if (!SQLManager.isConnected()) {
            return null;
        }

        String query = "SELECT DiscordID, TimeDate FROM Linked WHERE MinecraftUUID = ?";

        try (PreparedStatement ps = SQLManager.getConnection().prepareStatement(query)) {
            ps.setString(1, minecraftUUID.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LinkedAccount linkedAccount = new LinkedAccount();
                    linkedAccount.ID = rs.getString("DiscordID");
                    linkedAccount.timeDate = rs.getLong("TimeDate");

                    accounts.add(linkedAccount);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return accounts;
    }

    public List<LinkedAccount> getLinkedAll(String discordID) {
        List<LinkedAccount> accounts = new ArrayList<>();

        if (!SQLManager.isConnected()) {
            return null;
        }

        String query = "SELECT DiscordID, TimeDate FROM Linked WHERE DiscordID = ?";

        try (PreparedStatement ps = SQLManager.getConnection().prepareStatement(query)) {
            ps.setString(1, discordID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LinkedAccount linkedAccount = new LinkedAccount();
                    linkedAccount.ID = rs.getString("MinecraftUUID");
                    linkedAccount.timeDate = rs.getLong("TimeDate");

                    accounts.add(linkedAccount);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return accounts;
    }

}
