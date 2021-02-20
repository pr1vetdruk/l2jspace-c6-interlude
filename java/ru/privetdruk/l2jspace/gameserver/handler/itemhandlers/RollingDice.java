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
package ru.privetdruk.l2jspace.gameserver.handler.itemhandlers;

import ru.privetdruk.l2jspace.commons.util.Rnd;
import ru.privetdruk.l2jspace.gameserver.geoengine.GeoEngine;
import ru.privetdruk.l2jspace.gameserver.handler.IItemHandler;
import ru.privetdruk.l2jspace.gameserver.model.Location;
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.model.zone.ZoneId;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.Dice;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;
import ru.privetdruk.l2jspace.gameserver.util.Broadcast;
import ru.privetdruk.l2jspace.gameserver.util.Util;

public class RollingDice implements IItemHandler {
    private static final int[] ITEM_IDS =
            {
                    4625,
                    4626,
                    4627,
                    4628
            };

    @Override
    public void useItem(Playable playable, ItemInstance item) {
        if (!(playable instanceof PlayerInstance)) {
            return;
        }

        final PlayerInstance player = (PlayerInstance) playable;
        final int itemId = item.getItemId();
        if (!player.getFloodProtectors().getRollDice().tryPerformAction("RollDice")) {
            final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
            sm.addItemName(itemId);
            player.sendPacket(sm);
            return;
        }

        if (player.isInOlympiadMode()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH);
            return;
        }

        if ((itemId == 4625) || (itemId == 4626) || (itemId == 4627) || (itemId == 4628)) {
            final int number = rollDice();
            if (number == 0) {
                player.sendPacket(SystemMessageId.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER);
                return;
            }

            // Mobius: Retail dice position land calculation.
            final double angle = Util.convertHeadingToDegree(player.getHeading());
            final double radian = Math.toRadians(angle);
            final double course = Math.toRadians(180);
            final int x1 = (int) (Math.cos(Math.PI + radian + course) * 40);
            final int y1 = (int) (Math.sin(Math.PI + radian + course) * 40);
            final int x = player.getX() + x1;
            final int y = player.getY() + y1;
            final int z = player.getZ();
            final Location destination = GeoEngine.getInstance().canMoveToTargetLoc(player.getX(), player.getY(), player.getZ(), x, y, z, player.getInstanceId());
            Broadcast.toSelfAndKnownPlayers(player, new Dice(player.getObjectId(), item.getItemId(), number, destination.getX(), destination.getY(), destination.getZ()));

            final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_ROLLED_S2);
            sm.addString(player.getName());
            sm.addNumber(number);
            player.sendPacket(sm);
            if (player.isInsideZone(ZoneId.PEACE)) {
                Broadcast.toKnownPlayers(player, sm);
            } else if (player.isInParty()) {
                player.getParty().broadcastToPartyMembers(player, sm);
            }
        }
    }

    private int rollDice() {
        // Check if the dice is ready
        return Rnd.get(1, 6);
    }

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }
}
