/*
 * This file is part of the L2jSpace project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.privetdruk.l2jspace.gameserver.taskmanager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.concurrent.ThreadPool;
import ru.privetdruk.l2jspace.gameserver.handler.ItemHandler;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;

/**
 * @author Mobius, Gigi
 */
public class AutoPotionTaskManager {
    private static final Set<PlayerInstance> PLAYERS = ConcurrentHashMap.newKeySet();
    private static boolean _working = false;

    public AutoPotionTaskManager() {
        ThreadPool.scheduleAtFixedRate(() ->
        {
            if (_working) {
                return;
            }
            _working = true;

            PLAYER:
            for (PlayerInstance player : PLAYERS) {
                if ((player == null) || player.isAlikeDead() || !player.isOnline() || player.isInOfflineMode() || (!Config.AUTO_POTIONS_IN_OLYMPIAD && player.isInOlympiadMode())) {
                    remove(player);
                    continue PLAYER;
                }

                boolean success = false;
                if (Config.AUTO_HP_ENABLED) {
                    final boolean restoreHP = ((player.getStatus().getCurrentHp() / player.getMaxHp()) * 100) < Config.AUTO_HP_PERCENTAGE;
                    HP:
                    for (int itemId : Config.AUTO_HP_ITEM_IDS) {
                        final ItemInstance hpPotion = player.getInventory().getItemByItemId(itemId);
                        if ((hpPotion != null) && (hpPotion.getCount() > 0)) {
                            success = true;
                            if (restoreHP) {
                                ItemHandler.getInstance().getItemHandler(hpPotion.getItemId()).useItem(player, hpPotion);
                                player.sendMessage("Auto potion: Restored HP.");
                                break HP;
                            }
                        }
                    }
                }
                if (Config.AUTO_CP_ENABLED) {
                    final boolean restoreCP = ((player.getStatus().getCurrentCp() / player.getMaxCp()) * 100) < Config.AUTO_CP_PERCENTAGE;
                    CP:
                    for (int itemId : Config.AUTO_CP_ITEM_IDS) {
                        final ItemInstance cpPotion = player.getInventory().getItemByItemId(itemId);
                        if ((cpPotion != null) && (cpPotion.getCount() > 0)) {
                            success = true;
                            if (restoreCP) {
                                ItemHandler.getInstance().getItemHandler(cpPotion.getItemId()).useItem(player, cpPotion);
                                player.sendMessage("Auto potion: Restored CP.");
                                break CP;
                            }
                        }
                    }
                }
                if (Config.AUTO_MP_ENABLED) {
                    final boolean restoreMP = ((player.getStatus().getCurrentMp() / player.getMaxMp()) * 100) < Config.AUTO_MP_PERCENTAGE;
                    MP:
                    for (int itemId : Config.AUTO_MP_ITEM_IDS) {
                        final ItemInstance mpPotion = player.getInventory().getItemByItemId(itemId);
                        if ((mpPotion != null) && (mpPotion.getCount() > 0)) {
                            success = true;
                            if (restoreMP) {
                                ItemHandler.getInstance().getItemHandler(mpPotion.getItemId()).useItem(player, mpPotion);
                                player.sendMessage("Auto potion: Restored MP.");
                                break MP;
                            }
                        }
                    }
                }

                if (!success) {
                    player.sendMessage("Auto potion: You are out of potions!");
                }
            }

            _working = false;
        }, 0, 1000);
    }

    public void add(PlayerInstance player) {
        if (!PLAYERS.contains(player)) {
            PLAYERS.add(player);
        }
    }

    public void remove(PlayerInstance player) {
        PLAYERS.remove(player);
    }

    public static AutoPotionTaskManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        protected static final AutoPotionTaskManager INSTANCE = new AutoPotionTaskManager();
    }
}
