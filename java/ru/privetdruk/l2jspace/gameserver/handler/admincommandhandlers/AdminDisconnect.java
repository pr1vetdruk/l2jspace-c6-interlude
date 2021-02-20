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
package ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers;

import ru.privetdruk.l2jspace.gameserver.handler.IAdminCommandHandler;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.LeaveWorld;
import ru.privetdruk.l2jspace.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands: - character_disconnect = disconnects target player
 *
 * @version $Revision: 1.2.4.4 $ $Date: 2005/04/11 10:06:00 $
 */
public class AdminDisconnect implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS =
            {
                    "admin_character_disconnect"
            };

    @Override
    public boolean useAdminCommand(String command, PlayerInstance activeChar) {
        if (command.equals("admin_character_disconnect")) {
            disconnectCharacter(activeChar);
        }
        return true;
    }

    @Override
    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }

    private void disconnectCharacter(PlayerInstance activeChar) {
        final WorldObject target = activeChar.getTarget();
        PlayerInstance player = null;
        if (target instanceof PlayerInstance) {
            player = (PlayerInstance) target;
        } else {
            return;
        }

        if (player.getObjectId() == activeChar.getObjectId()) {
            BuilderUtil.sendSysMessage(activeChar, "You cannot logout your character.");
        } else {
            BuilderUtil.sendSysMessage(activeChar, "Character " + player.getName() + " disconnected from server.");

            // Logout Character
            player.sendPacket(new LeaveWorld());
            player.closeNetConnection();
        }
    }
}
