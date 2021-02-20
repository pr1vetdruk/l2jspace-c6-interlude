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
package ru.privetdruk.l2jspace.gameserver.model.zone.type;

import ru.privetdruk.l2jspace.gameserver.enums.TeleportWhereType;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.zone.ZoneId;
import ru.privetdruk.l2jspace.gameserver.model.zone.ZoneRespawn;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;

/**
 * A PVP Zone
 *
 * @author durgus
 */
public class ArenaZone extends ZoneRespawn {
    public ArenaZone(int id) {
        super(id);
    }

    @Override
    protected void onEnter(Creature creature) {
        if (creature.isPlayer() && !creature.isInsideZone(ZoneId.PVP)) {
            creature.getActingPlayer().sendPacket(SystemMessageId.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
        }
        creature.setInsideZone(ZoneId.PVP, true);
        creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
    }

    @Override
    protected void onExit(Creature creature) {
        creature.setInsideZone(ZoneId.PVP, false);
        creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
        if (creature.isPlayer() && !creature.isInsideZone(ZoneId.PVP)) {
            creature.getActingPlayer().sendPacket(SystemMessageId.YOU_HAVE_LEFT_A_COMBAT_ZONE);
        }
    }

    @Override
    protected void onDieInside(Creature creature) {
    }

    @Override
    protected void onReviveInside(Creature creature) {
    }

    public void oustAllPlayers() {
        for (Creature creature : getCharactersInside()) {
            if (creature == null) {
                continue;
            }

            if (creature instanceof PlayerInstance) {
                final PlayerInstance player = (PlayerInstance) creature;
                if (player.isOnline()) {
                    player.teleToLocation(TeleportWhereType.TOWN);
                }
            }
        }
    }
}
