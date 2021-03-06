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

import java.util.ArrayList;
import java.util.List;

import ru.privetdruk.l2jspace.gameserver.instancemanager.CursedWeaponsManager;
import ru.privetdruk.l2jspace.gameserver.model.CursedWeapon;
import ru.privetdruk.l2jspace.gameserver.model.Location;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ExCursedWeaponLocation;

/**
 * Format: (ch)
 *
 * @author -Wooden-
 */
public class RequestCursedWeaponLocation extends GameClientPacket {
    @Override
    protected void readImpl() {
        // ignore read packet
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        final List<ExCursedWeaponLocation.CursedWeaponInfo> list = new ArrayList<>();
        for (CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons()) {
            if (!cw.isActive()) {
                continue;
            }

            final Location location = cw.getWorldPosition();
            if (location != null) {
                list.add(new ExCursedWeaponLocation.CursedWeaponInfo(location, cw.getItemId(), cw.isActivated() ? 1 : 0));
            }
        }

        if (!list.isEmpty()) {
            player.sendPacket(new ExCursedWeaponLocation(list));
        }
    }
}
