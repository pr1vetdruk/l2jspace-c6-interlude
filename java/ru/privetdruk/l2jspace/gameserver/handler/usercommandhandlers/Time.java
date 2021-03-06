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

import ru.privetdruk.l2jspace.gameserver.GameTimeController;
import ru.privetdruk.l2jspace.gameserver.handler.IUserCommandHandler;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

public class Time implements IUserCommandHandler {
    private static final int[] COMMAND_IDS =
            {
                    77
            };

    @Override
    public boolean useUserCommand(int id, PlayerInstance player) {
        if (COMMAND_IDS[0] != id) {
            return false;
        }

        final int t = GameTimeController.getInstance().getGameTime();
        final String h = "" + ((t / 60) % 24);
        String m;
        if ((t % 60) < 10) {
            m = "0" + (t % 60);
        } else {
            m = "" + (t % 60);
        }

        SystemMessage sm;
        if (GameTimeController.getInstance().isNight()) {
            sm = new SystemMessage(SystemMessageId.THE_CURRENT_TIME_IS_S1_S2_IN_THE_NIGHT);
            sm.addString(h);
            sm.addString(m);
        } else {
            sm = new SystemMessage(SystemMessageId.THE_CURRENT_TIME_IS_S1_S2_IN_THE_DAY);
            sm.addString(h);
            sm.addString(m);
        }
        player.sendPacket(sm);

        return true;
    }

    @Override
    public int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
