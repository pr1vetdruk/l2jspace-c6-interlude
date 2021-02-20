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
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SendTradeDone;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

public class AnswerTradeRequest extends GameClientPacket {
    private int _response;

    @Override
    protected void readImpl() {
        _response = readD();
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        if (!player.getAccessLevel().allowTransaction()) {
            player.sendMessage("Unsufficient privileges.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        final PlayerInstance partner = player.getActiveRequester();
        if ((partner == null) || (World.getInstance().findObject(partner.getObjectId()) == null)) {
            // Trade partner not found, cancel trade
            player.sendPacket(new SendTradeDone(0));
            player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
            player.setActiveRequester(null);
            return;
        }

        if ((_response == 1) && !partner.isRequestExpired()) {
            player.startTrade(partner);
        } else {
            partner.sendPacket(new SystemMessage(SystemMessageId.S1_HAS_DENIED_YOUR_REQUEST_TO_TRADE).addString(player.getName()));
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }

        // Clears requesting status
        player.setActiveRequester(null);
        partner.onTransactionResponse();
    }
}
