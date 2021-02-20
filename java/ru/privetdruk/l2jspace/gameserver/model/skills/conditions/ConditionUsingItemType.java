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
import ru.privetdruk.l2jspace.gameserver.model.items.Item;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.type.ArmorType;
import ru.privetdruk.l2jspace.gameserver.model.skills.Env;

/**
 * The Class ConditionUsingItemType.
 *
 * @author mkizub
 */
public class ConditionUsingItemType extends Condition {
    private final boolean _armor;
    private final int _mask;

    /**
     * Instantiates a new condition using item type.
     *
     * @param mask the mask
     */
    public ConditionUsingItemType(int mask) {
        _mask = mask;
        _armor = (_mask & (ArmorType.MAGIC.mask() | ArmorType.LIGHT.mask() | ArmorType.HEAVY.mask())) != 0;
    }

    @Override
    public boolean testImpl(Env env) {
        if (!(env.player instanceof PlayerInstance)) {
            return false;
        }
        final Inventory inv = ((PlayerInstance) env.player).getInventory();

        // If ConditionUsingItemType is one between Light, Heavy or Magic
        if (_armor) {
            // Get the itemMask of the weared chest (if exists)
            final ItemInstance chest = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
            if (chest == null) {
                return false;
            }
            final int chestMask = chest.getItem().getItemMask();

            // If chest armor is different from the condition one return false
            if ((_mask & chestMask) == 0) {
                return false;
            }

            // So from here, chest armor matches conditions

            final int chestBodyPart = chest.getItem().getBodyPart();
            // return True if chest armor is a Full Armor
            if (chestBodyPart == Item.SLOT_FULL_ARMOR) {
                return true;
            }

            final ItemInstance legs = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
            if (legs == null) {
                return false;
            }
            final int legMask = legs.getItem().getItemMask();
            // return true if legs armor matches too
            return (_mask & legMask) != 0;
        }
        return (_mask & inv.getWearedMask()) != 0;
    }
}
