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
package ai.others;

import ru.privetdruk.l2jspace.commons.util.Rnd;
import ru.privetdruk.l2jspace.gameserver.enums.ChatType;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.NpcInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.quest.Quest;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.CreatureSay;

/**
 * @author Mobius
 * @note Based on python script
 */
public class TimakOrcOverlord extends Quest {
    // NPC
    private static final int TIMAK_ORC_OVERLORD = 20588;

    private TimakOrcOverlord() {
        super(-1, "ai/others");

        addAttackId(TIMAK_ORC_OVERLORD);
    }

    @Override
    public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet) {
        if (npc.isScriptValue(1)) {
            if (Rnd.get(100) < 50) {
                npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), "Dear ultimate power!!!"));
            }
        } else {
            npc.setScriptValue(1);
        }
        return super.onAttack(npc, attacker, damage, isPet);
    }

    public static void main(String[] args) {
        new TimakOrcOverlord();
    }
}
