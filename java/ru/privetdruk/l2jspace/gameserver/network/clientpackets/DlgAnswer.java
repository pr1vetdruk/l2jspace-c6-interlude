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
import ru.privetdruk.l2jspace.commons.util.Chronos;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;

/**
 * @author Dezmond_snz - Packet Format: cddd
 */
public class DlgAnswer extends GameClientPacket {
    private int _messageId;
    private int _answer;
    private int _requesterId;

    @Override
    protected void readImpl() {
        _messageId = readD();
        _answer = readD();
        _requesterId = readD();
    }

    @Override
    public void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        final Long answerTime = player.getConfirmDlgRequestTime(_requesterId);
        if ((_answer == 1) && (answerTime != null) && (Chronos.currentTimeMillis() > answerTime)) {
            _answer = 0;
        }
        player.removeConfirmDlgRequestTime(_requesterId);

        if (_messageId == SystemMessageId.S1_IS_MAKING_AN_ATTEMPT_AT_RESURRECTION_DO_YOU_WANT_TO_CONTINUE_WITH_THIS_RESURRECTION.getId()) {
            player.reviveAnswer(_answer);
        } else if (_messageId == SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId()) {
            player.teleportAnswer(_answer, _requesterId);
        } else if (_messageId == SystemMessageId.WOULD_YOU_LIKE_TO_OPEN_THE_GATE.getId()) {
            player.gatesAnswer(_answer, 1);
        } else if (_messageId == SystemMessageId.WOULD_YOU_LIKE_TO_CLOSE_THE_GATE.getId()) {
            player.gatesAnswer(_answer, 0);
        } else if ((_messageId == SystemMessageId.S1_S2.getId()) && Config.ALLOW_WEDDING) {
            player.engageAnswer(_answer);
        } else if (_messageId == SystemMessageId.S1.getId()) {
            if (player.dialog != null) {
                player.dialog.onDlgAnswer(player);
                player.dialog = null;
            }
        }
    }
}