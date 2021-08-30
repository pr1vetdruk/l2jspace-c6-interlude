package ru.privetdruk.l2jspace.gameserver.datatables.custom;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.database.DatabaseFactory;
import ru.privetdruk.l2jspace.commons.util.Chronos;
import ru.privetdruk.l2jspace.gameserver.enums.ServiceType;
import ru.privetdruk.l2jspace.gameserver.instancemanager.ExpirableServicesManager;
import ru.privetdruk.l2jspace.gameserver.model.ExpirableService;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.holders.ItemHolder;
import ru.privetdruk.l2jspace.gameserver.util.TimeConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

/**
 * This class manages storing of premium service inforamtion.
 *
 * @author GKR
 */

public class PremiumTable {
    private static Logger LOGGER = Logger.getLogger(PremiumTable.class.getName());

    public static float PREMIUM_BONUS_EXP = 1;
    public static float PREMIUM_BONUS_SP = 1;

    private static final String LOAD_ACCOUNT_RECORD = "SELECT expiration FROM account_services WHERE charLogin = ? AND serviceName = ?";
    private static final String LOAD_CHAR_RECORD = "SELECT expiration FROM character_services WHERE charId = ? AND serviceName = ?";
    private static final String INSERT_ACCOUNT_RECORD = "REPLACE INTO account_services(charLogin, serviceName, expiration) VALUES (?,?,?)";
    private static final String INSERT_CHAR_RECORD = "REPLACE INTO character_services(charId, serviceName, expiration) VALUES (?,?,?)";
    private static final String REMOVE_RECORD = "DELETE FROM character_services WHERE charId = ? AND serviceName = ?";

    public PremiumTable() {
        if (Config.PREMIUM_SERVICE_ENABLED) {
            PREMIUM_BONUS_EXP = Math.max((Config.PREMIUM_RATE_XP / Config.RATE_XP), 1);
            PREMIUM_BONUS_SP = Math.max((Config.PREMIUM_RATE_SP / Config.RATE_SP), 1);
        }
    }

    /**
     * Load player's premium state from database
     *
     * @param player player to load info
     */
    public static void loadState(PlayerInstance player) {
        if (!Config.PREMIUM_SERVICE_ENABLED || (player == null)) {
            return;
        }

        long expirationDate = 0;
        boolean isAccountBased = false;

        // Check account settings first
        try (Connection connection = DatabaseFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(LOAD_ACCOUNT_RECORD)) {
            statement.setString(1, player.getAccountName());
            statement.setString(2, ServiceType.PREMIUM.toString());
            
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                expirationDate = resultSet.getLong("expiration");
                isAccountBased = true;
            }
            
            resultSet.close();
        } catch (Exception e) {
            LOGGER.severe(PremiumTable.class.getName() + ": Error while loading account premium data for character " + player.getName() + ", account: " + player.getAccountName() + ": " + e);
        }

        // check personal data, if account data either is not available, or expired
        if (expirationDate == 0 || expirationDate < Chronos.currentTimeMillis()) {
            try (Connection con = DatabaseFactory.getConnection();
                 PreparedStatement statement = con.prepareStatement(LOAD_CHAR_RECORD)) {
                statement.setInt(1, player.getObjectId());
                statement.setString(2, ServiceType.PREMIUM.toString());
                
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    expirationDate = resultSet.getLong("expiration");
                }
                
                resultSet.close();
            } catch (Exception e) {
                LOGGER.severe(PremiumTable.class.getName() + ": Error while loading character premium data for character " + player.getName() + ": " + e);
            }
        }

        // Register service, if exists and isn't expired
        if (expirationDate < 0 || expirationDate > Chronos.currentTimeMillis()) {
            ExpirableServicesManager.getInstance().registerService(player, new ExpirableService(ServiceType.PREMIUM, expirationDate, isAccountBased));
        }
    }

    /**
     * Extends premium period for given time
     *
     * @param player         player to operate
     * @param millisCount    time in milliseconds
     * @param unlimited      {@code true} if service should be time-unlimited
     * @param isAccountBased {@code true} if service is account-based, {@code false} if service is character-based
     * @param store          {@code true} if premium state should be stored in database, {@code false} if it will expire at logout
     * @return {@code true} if operation is successfull, {@code false} otherwise
     */
    private static boolean addTime(PlayerInstance player, long millisCount, boolean unlimited, boolean isAccountBased, boolean store) {
        if (!Config.PREMIUM_SERVICE_ENABLED || (player == null)) {
            return false;
        }

        // Unlimited premium service will newer touched. There is also no need to touch parameter, if no-store is choosen for already enabled service
        if (ExpirableServicesManager.getInstance().hasService(ServiceType.PREMIUM, player)
                && (!store || ExpirableServicesManager.getInstance().getService(ServiceType.PREMIUM, player).isUnlimited())) {
            return false;
        }

        long expirationDate;
        boolean success = !store;

        if (unlimited) {
            expirationDate = -1;
        } else if (!store) {
            expirationDate = 0;
        } else {
            expirationDate = ExpirableServicesManager.getInstance().hasService(ServiceType.PREMIUM, player) ? ExpirableServicesManager.getInstance().getService(ServiceType.PREMIUM, player).getExpirationDate() : 0;
            expirationDate = Math.max(expirationDate, Chronos.currentTimeMillis()) + millisCount;
        }

        // Store info in database, if needed
        if (store) {
            if (isAccountBased) {
                try (Connection connec = DatabaseFactory.getConnection();
                     PreparedStatement statement = connec.prepareStatement(INSERT_ACCOUNT_RECORD)) {
                    statement.setString(1, player.getAccountName());
                    statement.setString(2, ServiceType.PREMIUM.toString());
                    statement.setLong(3, expirationDate);
                    statement.executeUpdate();
                    success = true;
                } catch (Exception e) {
                    LOGGER.severe(PremiumTable.class.getName() + ":  Could not save data for account " + player.getAccountName() + ", player " + player.getName() + ": " + e);
                }
            } else {
                try (Connection connection = DatabaseFactory.getConnection();
                     PreparedStatement statement = connection.prepareStatement(INSERT_CHAR_RECORD)) {
                    statement.setInt(1, player.getObjectId());
                    statement.setString(2, ServiceType.PREMIUM.toString());
                    statement.setLong(3, expirationDate);
                    statement.executeUpdate();
                    success = true;
                } catch (Exception e) {
                    LOGGER.severe(PremiumTable.class.getName() + ":  Could not save data for character " + player.getName() + ": " + e);
                }
            }
        }

        // if store was successfull, or store doesn't required
        if (success) {
            // register service
            ExpirableServicesManager.getInstance().registerService(player, new ExpirableService(ServiceType.PREMIUM, expirationDate, isAccountBased));

            // Send Premium packet, if needed
            if (Config.SHOW_PREMIUM_STATUS) {
                // TODO player.sendPacket(new ExBrPremiumState(player.getObjectId(), 1));
            }

            // Send text message
            player.sendMessage("Premium service is activated");
        }

        return success;
    }

    /**
     * Wrapper for {@link #addTime(PlayerInstance, long, boolean, boolean, boolean)}. Extends premium period for given time, store state in database
     *
     * @param player         player to operate
     * @param millisCount    time in milliseconds
     * @param isAccountBased {@code true} if service is account-based, {@code false} if service is character-based
     * @return {@code true} if operation is successfull, {@code false} otherwise
     */
    public static boolean addTime(PlayerInstance player, long millisCount, boolean isAccountBased) {
        return addTime(player, millisCount, false, isAccountBased, true);
    }

    /**
     * Wrapper for {@link #addTime(PlayerInstance, long, boolean, boolean, boolean)}. Extends premium period for given time, store state in database
     *
     * @param player      player to operate
     * @param millisCount time in milliseconds
     * @return {@code true} if operation is successfull, {@code false} otherwise
     */
    public static boolean addTime(PlayerInstance player, long millisCount) {
        if ((millisCount <= 0) || !ExpirableServicesManager.getInstance().hasService(ServiceType.PREMIUM, player)) {
            return false;
        }

        return addTime(player, millisCount, ExpirableServicesManager.getInstance().getService(ServiceType.PREMIUM, player).isAccountBased());
    }

    /**
     * Wrapper for {@link #addTime(PlayerInstance, long, boolean, boolean, boolean)}. Extends premium period for given time, store state in database
     *
     * @param player player to operate
     * @param typeTimePeriod    type of time period
     * @param count  number of given period
     * @return {@code true} if operation is successfull, {@code false} otherwise
     */
    public static boolean addTime(PlayerInstance player, TimeConfig typeTimePeriod, int count) {
        return addTime(player, typeTimePeriod.getTimeInMillis() * count);
    }

    /**
     * Wrapper for {@link #addTime(PlayerInstance, long, boolean, boolean, boolean)}. Extends premium period for unlimited time, store state in database
     *
     * @param player         player to operate
     * @param isAccountBased {@code true} if service is account-based, {@code false} if service is character-based
     * @return {@code true} if operation is successfull, {@code false} otherwise
     */
    public static boolean setUnlimitedPremium(PlayerInstance player, boolean isAccountBased) {
        return addTime(player, 0, true, isAccountBased, true);
    }

    /**
     * Wrapper for {@link #addTime(PlayerInstance, long, boolean, boolean, boolean)}. Activates account premium service for player - give temporary premium, expiring at logout
     *
     * @param player player to operate
     * @return {@code true} if operation is successfull, {@code false} otherwise
     */
    public static boolean setTemporaryPremium(PlayerInstance player) {
        return addTime(player, 0, false, false, false);
    }

    /**
     * Unregister premium service from given player and remove it from database
     *
     * @param player player to operate
     */
    public static void removeService(PlayerInstance player) {
        /*if ((player == null) || !player.hasPremium()) {
            return;
        }

        try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(REMOVE_RECORD)) {
            statement.setInt(1, player.getObjectId());
            statement.setString(2, ServiceType.PREMIUM.toString());
            statement.execute();
        } catch (Exception e) {
            _log.warning(PremiumTable.class.getName() + ":  Could not remove data for character " + player.getName() + ": " + e);
        }*/

        ExpirableServicesManager.getInstance().expireService(ServiceType.PREMIUM, player);
    }

    /**
     * Gets price of given premium period
     *
     * @param period         period to calculate
     * @param isAccountBased {@code true} if service is account-based, {@code false} if service is character-based
     * @return correspondent ItemHolder for given period and base
     */
    public static ItemHolder getPrice(String period, boolean isAccountBased) {
        return isAccountBased ? Config.ACCOUNT_PREMIUM_PRICE.get(period) : Config.CHAR_PREMIUM_PRICE.get(period);
    }

    /**
     * Activates premium service for player
     *
     * @param player         player to activate
     * @param period         time period for activation
     * @param seller         reference character (for logging puproses)
     * @param isAccountBased {@code true} if service is account-based, {@code false} if service is character-based
     * @return {@code true} if operation is successfull, {@code false} otherwise
     */
    private static boolean givePremium(PlayerInstance player, String period, WorldObject seller, boolean isAccountBased) {
        /*ItemHolder payItem = getPrice(period, isAccountBased);
        if ((player == null) || !player.isOnline() || (payItem == null)) {
            if (payItem == null) {
                _log.warning(PremiumTable.class.getName() + ":  No payitem is defined for period " + period);
            }
            return false;
        }

        // do not allow player to buy another type of service (character VS account), if already has one
        if (ExpirableServicesManager.getInstance().hasService(ServiceType.PREMIUM, player) && (ExpirableServicesManager.getInstance().getService(ServiceType.PREMIUM, player).isAccountBased() != isAccountBased)) {
            player.sendMessage("Incompatible premium modes");
        } else if ((payItem.getCount() > 0) && (player.getInventory().getInventoryItemCount(payItem.getId(), -1, false) < payItem.getCount())) {
            player.sendMessage("Not enough items to use");
        } else if (addTime(player, Util.toMillis(period), isAccountBased)) {
            player.destroyItemByItemId("Premium service", payItem.getId(), payItem.getCount(), seller, true);
            return true;
        }*/

        return false;
    }

    /**
     * Wrapper for {@link #givePremium(PlayerInstance, String, WorldObject, boolean)}. Activates account premium service for player
     *
     * @param player player to activate
     * @param period time period for activation
     * @param seller reference character (for logging puproses)
     * @return {@code true} if operation is successfull, {@code false} otherwise
     */
    public static boolean giveAccountPremium(PlayerInstance player, String period, WorldObject seller) {
        return givePremium(player, period, seller, true);
    }

    /**
     * Wrapper for {@link #givePremium(PlayerInstance, String, WorldObject, boolean)}. Activates character premium service for player
     *
     * @param player player to activate
     * @param period time period for activation
     * @param seller reference character (for logging puproses)
     * @return {@code true} if operation is successfull, {@code false} otherwise
     */
    public static boolean giveCharPremium(PlayerInstance player, String period, WorldObject seller) {
        return givePremium(player, period, seller, false);
    }

    public static final PremiumTable getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final PremiumTable _instance = new PremiumTable();
    }
}