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
package ru.privetdruk.l2jspace.gameserver.model.itemcontainer;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.clan.Clan;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;

public class ClanWarehouse extends Warehouse {
    private final Clan _clan;

    public ClanWarehouse(Clan clan) {
        _clan = clan;
    }

    @Override
    public int getOwnerId() {
        return _clan.getLeader().getObjectId();
    }

    @Override
    public PlayerInstance getOwner() {
        return _clan.getLeader().getPlayerInstance();
    }

    @Override
    public ItemInstance.ItemLocation getBaseLocation() {
        return ItemInstance.ItemLocation.CLANWH;
    }

    @Override
    public boolean validateCapacity(int slots) {
        return (_items.size() + slots) <= Config.WAREHOUSE_SLOTS_CLAN;
    }
}
