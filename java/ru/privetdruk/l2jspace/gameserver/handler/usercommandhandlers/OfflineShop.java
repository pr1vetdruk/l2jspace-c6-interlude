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
package ru.privetdruk.l2jspace.gameserver.handler.usercommandhandlers;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.gameserver.datatables.SkillTable;
import ru.privetdruk.l2jspace.gameserver.handler.IUserCommandHandler;
import ru.privetdruk.l2jspace.gameserver.model.Party;
import ru.privetdruk.l2jspace.gameserver.model.TradeList;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.entity.olympiad.Olympiad;
import ru.privetdruk.l2jspace.gameserver.model.entity.sevensigns.SevenSignsFestival;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;
import ru.privetdruk.l2jspace.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * Command /offline_shop like L2OFF
 *
 * @author Nefer
 */
public class OfflineShop implements IUserCommandHandler {
    private static final int[] COMMAND_IDS =
            {
                    114
            };

    @Override
    public synchronized boolean useUserCommand(int id, PlayerInstance player) {
        if (player == null) {
            return false;
        }

        // Message like L2OFF
        if ((!player.isInStoreMode() && (!player.isCrafting())) || !player.isSitting()) {
            player.sendMessage("You are not running a private store or private work shop.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        if (player.isInFunEvent() && !player.isGM()) {
            player.sendMessage("You cannot Logout while in registered in an Event.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        final TradeList storeListBuy = player.getBuyList();
        if ((storeListBuy == null) || (storeListBuy.getItemCount() == 0)) {
            player.sendMessage("Your buy list is empty.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        final TradeList storeListSell = player.getSellList();
        if ((storeListSell == null) || (storeListSell.getItemCount() == 0)) {
            player.sendMessage("Your sell list is empty.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        player.getInventory().updateDatabase();

        if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) && (!player.isGM() || !Config.GM_RESTART_FIGHTING)) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_EXIT_WHILE_IN_COMBAT);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        // Dont allow leaving if player is in combat
        if (player.isInCombat() && !player.isGM()) {
            player.sendMessage("You cannot Logout while is in Combat mode.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        // Dont allow leaving if player is teleporting
        if (player.isTeleporting() && !player.isGM()) {
            player.sendMessage("You cannot Logout while is Teleporting.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        if (player.atEvent) {
            player.sendPacket(SystemMessage.sendString("A superior power doesn't allow you to leave the event."));
            return false;
        }

        if (player.isInOlympiadMode() || Olympiad.getInstance().isRegistered(player)) {
            player.sendMessage("You can't Logout in Olympiad mode.");
            return false;
        }

        // Prevent player from logging out if they are a festival participant nd it is in progress, otherwise notify party members that the player is not longer a participant.
        if (player.isFestivalParticipant()) {
            if (SevenSignsFestival.getInstance().isFestivalInitialized()) {
                player.sendMessage("You cannot Logout while you are a participant in a Festival.");
                return false;
            }

            final Party playerParty = player.getParty();
            if (playerParty != null) {
                player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming Festival."));
            }
        }

        if (player.isFlying()) {
            player.removeSkill(SkillTable.getInstance().getSkill(4289, 1));
        }

        if ((player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE) || (player.isCrafting() && Config.OFFLINE_CRAFT_ENABLE)) {
            // Sleep effect, not official feature but however L2OFF features (like offline trade)
            if (Config.OFFLINE_SLEEP_EFFECT) {
                player.startAbnormalEffect(Creature.ABNORMAL_EFFECT_SLEEP);
            }

            player.sendMessage("Your private store has succesfully been flagged as an offline shop and will remain active for ever.");
            player.logout();

            return true;
        }
        return false;
    }

    @Override
    public int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}