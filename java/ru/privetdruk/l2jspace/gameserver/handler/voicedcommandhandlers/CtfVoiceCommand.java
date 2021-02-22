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
package ru.privetdruk.l2jspace.gameserver.handler.voicedcommandhandlers;

import ru.privetdruk.l2jspace.gameserver.handler.IVoicedCommandHandler;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.core.State;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.ctf.CTF;

public class CtfVoiceCommand implements IVoicedCommandHandler {
    private static final String[] VOICED_COMMANDS = {"ctfjoin", "ctfleave", "ctfinfo"};

    @Override
    public boolean useVoicedCommand(String command, PlayerInstance player, String target) {
        if (command.startsWith("ctf") && player != null) {
            CTF ctf = CTF.find(State.REGISTRATION);

            if (ctf == null) {
                player.sendMessage("There is no Event in progress.");
            } else {
                if (command.startsWith("ctfjoin")) {
                    ctf.registerPlayer(player, "");
                } else if (command.startsWith("ctfleave")) {
                    ctf.excludePlayer(player);
                } else if (command.startsWith("ctfinfo")) {
                    ctf.displayEventInformation(player);
                }
            }
        }

        return true;
    }

    @Override
    public String[] getVoicedCommandList() {
        return VOICED_COMMANDS;
    }
}