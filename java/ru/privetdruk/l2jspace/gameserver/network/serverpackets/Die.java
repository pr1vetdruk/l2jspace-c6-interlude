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
package ru.privetdruk.l2jspace.gameserver.network.serverpackets;

import ru.privetdruk.l2jspace.gameserver.instancemanager.CastleManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.FortManager;
import ru.privetdruk.l2jspace.gameserver.model.SiegeClan;
import ru.privetdruk.l2jspace.gameserver.model.actor.Attackable;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.clan.Clan;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.core.State;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.ctf.CTF;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.DM;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.TvT;
import ru.privetdruk.l2jspace.gameserver.model.entity.siege.Castle;
import ru.privetdruk.l2jspace.gameserver.model.entity.siege.Fort;

public class Die extends GameServerPacket {
    private final int _objectId;
    private final boolean _fake;
    private boolean _sweepable;
    private boolean _canTeleport;
    private boolean _allowFixedRes;
    private Clan _clan;
    Creature _creature;

    public Die(Creature creature) {
        _creature = creature;

        if (creature instanceof PlayerInstance) {
            PlayerInstance player = creature.getActingPlayer();
            _allowFixedRes = player.getAccessLevel().allowFixedRes();
            _clan = player.getClan();
            _canTeleport = (!TvT.isStarted() || !player._inEventTvT) &&
                    (!DM.hasStarted() || !player._inEventDM) &&
                    (!player.inEventCtf || CTF.find(State.START) == null) &&
                    !player.isInFunEvent() &&
                    !player.isPendingRevive();
        }

        _objectId = creature.getObjectId();
        _fake = !creature.isDead();

        if (creature instanceof Attackable) {
            _sweepable = ((Attackable) creature).isSweepActive();
        }
    }

    @Override
    protected final void writeImpl() {
        if (_fake) {
            return;
        }

        writeC(0x06);
        writeD(_objectId);

        // NOTE:
        // 6d 00 00 00 00 - to nearest village
        // 6d 01 00 00 00 - to hide away
        // 6d 02 00 00 00 - to castle
        // 6d 03 00 00 00 - to siege HQ
        // sweepable
        // 6d 04 00 00 00 - FIXED
        writeD(_canTeleport ? 0x01 : 0); // 6d 00 00 00 00 - to nearest village

        if (_canTeleport && (_clan != null)) {
            SiegeClan siegeClan = null;
            Boolean isInDefense = false;
            final Castle castle = CastleManager.getInstance().getCastle(_creature);
            final Fort fort = FortManager.getInstance().getFort(_creature);
            if ((castle != null) && castle.getSiege().isInProgress()) {
                // siege in progress
                siegeClan = castle.getSiege().getAttackerClan(_clan);
                if ((siegeClan == null) && castle.getSiege().checkIsDefender(_clan)) {
                    isInDefense = true;
                }
            } else if ((fort != null) && fort.getSiege().isInProgress()) {
                // siege in progress
                siegeClan = fort.getSiege().getAttackerClan(_clan);
                if ((siegeClan == null) && fort.getSiege().checkIsDefender(_clan)) {
                    isInDefense = true;
                }
            }

            writeD(_clan.getHideoutId() > 0 ? 0x01 : 0x00); // 6d 01 00 00 00 - to hide away
            writeD((_clan.getCastleId() > 0) || (_clan.getFortId() > 0) || isInDefense ? 0x01 : 0x00); // 6d 02 00 00 00 - to castle
            writeD((siegeClan != null) && !isInDefense && !siegeClan.getFlag().isEmpty() ? 0x01 : 0x00); // 6d 03 00 00 00 - to siege HQ
        } else {
            writeD(0x00); // 6d 01 00 00 00 - to hide away
            writeD(0x00); // 6d 02 00 00 00 - to castle
            writeD(0x00); // 6d 03 00 00 00 - to siege HQ
        }

        writeD(_sweepable ? 0x01 : 0x00); // sweepable (blue glow)
        writeD(_allowFixedRes ? 0x01 : 0x00); // 6d 04 00 00 00 - to FIXED
    }
}
