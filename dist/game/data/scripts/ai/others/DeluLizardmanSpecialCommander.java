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
public class DeluLizardmanSpecialCommander extends Quest {
    // NPC
    private static final int DELU_LIZARDMAN_SPECIAL_COMMANDER = 21107;

    private DeluLizardmanSpecialCommander() {
        super(-1, "ai/others");

        addAttackId(DELU_LIZARDMAN_SPECIAL_COMMANDER);
    }

    @Override
    public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet) {
        if (npc.isScriptValue(1)) {
            if (Rnd.get(100) < 40) {
                npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), "Come on, Ill take you on!"));
            }
        } else {
            npc.setScriptValue(1);
            npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), "How dare you interrupt a sacred duel! You must be taught a lesson!"));
        }
        return super.onAttack(npc, attacker, damage, isPet);
    }

    public static void main(String[] args) {
        new DeluLizardmanSpecialCommander();
    }
}
