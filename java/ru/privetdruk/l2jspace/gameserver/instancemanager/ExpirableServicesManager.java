package ru.privetdruk.l2jspace.gameserver.instancemanager;

import org.javolution.util.FastMap;
import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.gameserver.enums.ServiceType;
import ru.privetdruk.l2jspace.gameserver.model.ExpirableService;

import ru.privetdruk.l2jspace.gameserver.model.World;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages time expirable services.
 *
 * @author GKR
 */

public class ExpirableServicesManager {
    private final Map<ServiceType, Map<Integer, ExpirableService>> holder; // store service maps

    protected ExpirableServicesManager() {
        holder = new HashMap<>();
        for (ServiceType type : ServiceType.values()) {
            holder.put(type, new FastMap<Integer, ExpirableService>().shared());
        }
    }

    /**
     * Register service for player in expiration date holder
     *
     * @param player  player to register
     * @param service type of service
     */
    public void registerService(PlayerInstance player, ExpirableService service) {
        holder.get(service.getType()).put(player.getObjectId(), service);
    }

    public ExpirableService getService(ServiceType type, PlayerInstance player) {
        return holder.get(type).get(player.getObjectId());
    }

    /**
     * @param type   type of service
     * @param player to check
     * @return {@code true} if given service type is registered for given player, {@code false} otherwise
     */
    public boolean hasService(ServiceType type, PlayerInstance player) {
        return holder.get(type).containsKey(player.getObjectId());
    }

    /**
     * Send message and premium state packet to player with given id, when service of given type is expiring
     *
     * @param type     type of service
     * @param playerId objectId of player to send info
     */
    private void expireService(ServiceType type, int playerId) {
        PlayerInstance player = World.getInstance().getPlayer(playerId);

        if (player != null && player.isOnline()) {
            if (type == ServiceType.PREMIUM) {
                player.sendMessage("Premium service is expired");

                if (Config.SHOW_PREMIUM_STATUS) {
//                    player.sendPacket(new ExBrPremiumState(player.getObjectId(), 0));
                }
            }
        }
    }

    /**
     * Unregister service of given type for given player
     *
     * @param type   type of service
     * @param player to process
     */
    public void expireService(ServiceType type, PlayerInstance player) {
        holder.get(type).remove(player.getObjectId());
        expireService(type, player.getObjectId()); // show appropriate things
    }

    /**
     * Iterate over service stores, check for service expiration and unregister expired services
     */
    public void checkExpiration() {
        if (!Config.PREMIUM_SERVICE_ENABLED) {
            return;
        }

        for (ServiceType type : holder.keySet()) {
            Map<Integer, ExpirableService> charMap = holder.get(type);
            for (int charId : charMap.keySet()) {
                if (charMap.get(charId).isExpired()) { // Do not touch unlimited and temporary services
                    expireService(type, charId);
                    charMap.remove(charId);
                }
            }
        }
    }

    public static ExpirableServicesManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        protected static final ExpirableServicesManager instance = new ExpirableServicesManager();
    }
}