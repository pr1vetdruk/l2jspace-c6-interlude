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

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.gameserver.ai.CtrlIntention;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SocialAction;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;
import ru.privetdruk.l2jspace.gameserver.util.Util;

public class RequestSocialAction extends GameClientPacket {
    private int _actionId;

    @Override
    protected void readImpl() {
        _actionId = readD();
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        // You cannot do anything else while fishing
        if (player.isFishing()) {
            player.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_DO_THAT_WHILE_FISHING_3));
            return;
        }

        // check if its the actionId is allowed
        if ((_actionId < 2) || (_actionId > 13)) {
            Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " requested an internal Social Action.", Config.DEFAULT_PUNISH);
            return;
        }

        if ((player.getPrivateStoreType() == 0) && (player.getActiveRequester() == null) && !player.isAlikeDead() && (!player.isAllSkillsDisabled() || player.isInDuel()) && (player.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)) {
            player.broadcastPacket(new SocialAction(player.getObjectId(), _actionId));
        }
    }
}
