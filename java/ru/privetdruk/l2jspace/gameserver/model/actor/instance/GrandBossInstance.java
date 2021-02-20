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

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.concurrent.ThreadPool;
import ru.privetdruk.l2jspace.commons.util.Rnd;
import ru.privetdruk.l2jspace.gameserver.instancemanager.GrandBossManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.RaidBossPointsManager;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.Summon;
import ru.privetdruk.l2jspace.gameserver.model.actor.templates.NpcTemplate;
import ru.privetdruk.l2jspace.gameserver.model.spawn.Spawn;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

/**
 * This class manages all Grand Bosses.
 *
 * @version $Revision: 1.0.0.0 $ $Date: 2006/06/16 $
 */
public class GrandBossInstance extends MonsterInstance {
    /**
     * Constructor for GrandBossInstance. This represent all grandbosses.
     *
     * @param objectId ID of the instance
     * @param template NpcTemplate of the instance
     */
    public GrandBossInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public boolean doDie(Creature killer) {
        if (!super.doDie(killer)) {
            return false;
        }

        PlayerInstance player = null;
        if (killer instanceof PlayerInstance) {
            player = (PlayerInstance) killer;
        } else if (killer instanceof Summon) {
            player = ((Summon) killer).getOwner();
        }

        if (player != null) {
            broadcastPacket(new SystemMessage(SystemMessageId.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL));
            if (player.getParty() != null) {
                for (PlayerInstance member : player.getParty().getPartyMembers()) {
                    RaidBossPointsManager.addPoints(member, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
                }
            } else {
                RaidBossPointsManager.addPoints(player, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
            }
        }
        return true;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        GrandBossManager.getInstance().addBoss(this);
    }

    @Override
    protected void manageMinions() {
        _minionList.spawnMinions();
        _minionMaintainTask = ThreadPool.scheduleAtFixedRate(() ->
        {
            // Teleport raid boss home if it's too far from home location
            final Spawn bossSpawn = getSpawn();
            int rbLockRange = Config.RBLOCKRAGE;
            if (Config.RBS_SPECIFIC_LOCK_RAGE.get(bossSpawn.getNpcId()) != null) {
                rbLockRange = Config.RBS_SPECIFIC_LOCK_RAGE.get(bossSpawn.getNpcId());
            }

            if ((rbLockRange >= 100) && !isInsideRadius(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), rbLockRange, true, false)) {
                teleToLocation(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), true);
                // healFull(); // Prevents minor exploiting with it
            }

            _minionList.maintainMinions();
        }, 60000, 20000);
    }

    @Override
    public boolean isRaid() {
        return true;
    }

    public void healFull() {
        super.setCurrentHp(super.getMaxHp());
        super.setCurrentMp(super.getMaxMp());
    }
}
