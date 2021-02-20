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
package ru.privetdruk.l2jspace.gameserver.model.skills.conditions;

import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.itemcontainer.Inventory;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.model.skills.Env;

/**
 * @author mkizub
 */
public class ConditionSlotItemType extends ConditionInventory {
    private final int _mask;

    public ConditionSlotItemType(int slot, int mask) {
        super(slot);
        _mask = mask;
    }

    @Override
    public boolean testImpl(Env env) {
        if (!(env.player instanceof PlayerInstance)) {
            return false;
        }
        final Inventory inv = ((PlayerInstance) env.player).getInventory();
        final ItemInstance item = inv.getPaperdollItem(_slot);
        if (item == null) {
            return false;
        }
        return (item.getItem().getItemMask() & _mask) != 0;
    }
}
