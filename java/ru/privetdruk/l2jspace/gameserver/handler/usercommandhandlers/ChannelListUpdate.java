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

import ru.privetdruk.l2jspace.gameserver.handler.IUserCommandHandler;
import ru.privetdruk.l2jspace.gameserver.model.CommandChannel;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ExMultiPartyCommandChannelInfo;

/**
 * @author chris_00 when User press the "List Update" button in CCInfo window
 */
public class ChannelListUpdate implements IUserCommandHandler {
    private static final int[] COMMAND_IDS =
            {
                    97
            };

    @Override
    public boolean useUserCommand(int id, PlayerInstance player) {
        if (id != COMMAND_IDS[0]) {
            return false;
        }

        if (player == null) {
            return false;
        }

        if ((player.getParty() == null) || (player.getParty().getCommandChannel() == null)) {
            return false;
        }

        final CommandChannel channel = player.getParty().getCommandChannel();
        player.sendPacket(new ExMultiPartyCommandChannelInfo(channel));
        return true;
    }

    @Override
    public int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
