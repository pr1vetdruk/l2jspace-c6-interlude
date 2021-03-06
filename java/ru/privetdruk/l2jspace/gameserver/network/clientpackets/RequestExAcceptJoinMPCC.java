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

import ru.privetdruk.l2jspace.gameserver.model.CommandChannel;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

/**
 * @author -Wooden-
 */
public class RequestExAcceptJoinMPCC extends GameClientPacket {
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

        final PlayerInstance requestor = player.getActiveRequester();
        if (requestor == null) {
            return;
        }

        if (_response == 1) {
            boolean newCc = false;
            if (!requestor.getParty().isInCommandChannel()) {
                new CommandChannel(requestor); // Create new CC
                newCc = true;
            }

            requestor.getParty().getCommandChannel().addParty(player.getParty());
            if (!newCc) {
                player.sendPacket(SystemMessageId.YOU_HAVE_JOINED_THE_COMMAND_CHANNEL);
            }
        } else {
            requestor.sendPacket(new SystemMessage(SystemMessageId.S1_HAS_DECLINED_THE_CHANNEL_INVITATION).addString(player.getName()));
        }

        player.setActiveRequester(null);
        requestor.onTransactionResponse();
    }
}
