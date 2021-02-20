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

import ru.privetdruk.l2jspace.gameserver.ai.CtrlIntention;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.BoatData;
import ru.privetdruk.l2jspace.gameserver.model.Location;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.BoatInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.type.WeaponType;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;

public class RequestMoveToLocationInVehicle extends GameClientPacket {
    private int _boatId;
    private Location _targetPos;
    private Location _originPos;

    @Override
    protected void readImpl() {
        _boatId = readD(); // objectId of boat
        _targetPos = new Location(readD(), readD(), readD());
        _originPos = new Location(readD(), readD(), readD());
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        if (player.isAttackingNow() && (player.getActiveWeaponItem() != null) && (player.getActiveWeaponItem().getItemType() == WeaponType.BOW)) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        BoatInstance boat;
        if (player.isInBoat()) {
            boat = player.getBoat();
            if (boat.getObjectId() != _boatId) {
                boat = BoatData.getInstance().getBoat(_boatId);
                player.setBoat(boat);
            }
        } else {
            boat = BoatData.getInstance().getBoat(_boatId);
            player.setBoat(boat);
        }

        player.setBoatPosition(_targetPos);
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO_IN_A_BOAT, _targetPos, _originPos);
    }
}
