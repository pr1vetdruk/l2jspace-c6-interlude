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

import ru.privetdruk.l2jspace.gameserver.instancemanager.CastleManager;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.StaticObjectInstance;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ChairSit;

public class ChangeWaitType2 extends GameClientPacket {
    private boolean _typeStand;

    @Override
    protected void readImpl() {
        _typeStand = readD() == 1;
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        final WorldObject target = player.getTarget();
        if (getClient() != null) {
            if (player.isOutOfControl()) {
                player.sendPacket(ActionFailed.STATIC_PACKET);
                return;
            }

            if (player.getMountType() != 0) {
                return;
            }

            if ((target != null) && !player.isSitting() && (target instanceof StaticObjectInstance) && (((StaticObjectInstance) target).getType() == 1) && (CastleManager.getInstance().getCastle(target) != null) && player.isInsideRadius(target, StaticObjectInstance.INTERACTION_DISTANCE, false, false)) {
                final ChairSit cs = new ChairSit(player, ((StaticObjectInstance) target).getStaticObjectId());
                player.sendPacket(cs);
                player.sitDown();
                player.broadcastPacket(cs);
            }

            if (_typeStand) {
                player.standUp();
            } else {
                player.sitDown();
            }
        }
    }
}