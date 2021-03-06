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
package ru.privetdruk.l2jspace.gameserver.network.clientpackets;

import ru.privetdruk.l2jspace.gameserver.model.World;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ExAskJoinPartyRoom;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

/**
 * Format: (ch) S
 *
 * @author -Wooden-
 */
public class RequestAskJoinPartyRoom extends GameClientPacket {
    private static String _name;

    @Override
    protected void readImpl() {
        _name = readS();
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        // Send PartyRoom invite request (with activeChar) name to the target
        final PlayerInstance target = World.getInstance().getPlayer(_name);
        if (target != null) {
            if (!target.isProcessingRequest()) {
                player.onTransactionRequest(target);
                target.sendPacket(new ExAskJoinPartyRoom(player.getName()));
            } else {
                player.sendPacket(new SystemMessage(SystemMessageId.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addString(target.getName()));
            }
        } else {
            player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
        }
    }
}
