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
import ru.privetdruk.l2jspace.gameserver.instancemanager.QuestManager;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.quest.Quest;
import ru.privetdruk.l2jspace.gameserver.model.quest.QuestState;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.QuestList;

public class RequestQuestAbort extends GameClientPacket {
    private int _questId;

    @Override
    protected void readImpl() {
        _questId = readD();
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        Quest qe = null;
        if (!Config.ALT_DEV_NO_QUESTS) {
            qe = QuestManager.getInstance().getQuest(_questId);
        }

        if (qe != null) {
            if ((_questId == 503) && (player.getClan() != null) && player.isClanLeader()) {
                qe.finishQuestToClan(player);
            }

            final QuestState qs = player.getQuestState(qe.getName());
            if (qs != null) {
                qs.exitQuest(true);
                player.sendMessage("Quest aborted.");
                player.sendPacket(new QuestList(player));
            }
        }
    }
}
