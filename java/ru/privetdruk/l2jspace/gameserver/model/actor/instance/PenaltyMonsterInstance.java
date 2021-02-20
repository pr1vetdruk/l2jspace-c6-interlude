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
package ru.privetdruk.l2jspace.gameserver.model.actor.instance;

import ru.privetdruk.l2jspace.commons.util.Rnd;
import ru.privetdruk.l2jspace.gameserver.ai.CtrlEvent;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.SpawnTable;
import ru.privetdruk.l2jspace.gameserver.enums.ChatType;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.templates.NpcTemplate;
import ru.privetdruk.l2jspace.gameserver.model.spawn.Spawn;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.CreatureSay;

public class PenaltyMonsterInstance extends MonsterInstance {
    private PlayerInstance _ptk;

    public PenaltyMonsterInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public Creature getMostHated() {
        return _ptk;
    }

    @Deprecated
    public void notifyPlayerDead() {
        // Monster kill player and can by deleted
        deleteMe();

        final Spawn spawn = getSpawn();
        if (spawn != null) {
            spawn.stopRespawn();
            SpawnTable.getInstance().deleteSpawn(spawn, false);
        }
    }

    public void setPlayerToKill(PlayerInstance ptk) {
        if (Rnd.get(100) <= 80) {
            broadcastPacket(new CreatureSay(getObjectId(), ChatType.GENERAL, getName(), "mmm your bait was delicious"));
        }
        _ptk = ptk;
        addDamageHate(ptk, 10, 10);
        getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, ptk);
        addAttackerToAttackByList(ptk);
    }

    @Override
    public boolean doDie(Creature killer) {
        if (!super.doDie(killer)) {
            return false;
        }

        if (Rnd.get(100) <= 75) {
            broadcastPacket(new CreatureSay(getObjectId(), ChatType.GENERAL, getName(), "I will tell fishes not to take your bait"));
        }
        return true;
    }
}
