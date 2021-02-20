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

import ru.privetdruk.l2jspace.gameserver.model.World;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.Item;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ExVariationCancelResult;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.InventoryUpdate;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

/**
 * Format(ch) d
 *
 * @author -Wooden-
 */
public class RequestRefineCancel extends GameClientPacket {
    private int _targetItemObjId;

    @Override
    protected void readImpl() {
        _targetItemObjId = readD();
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        final ItemInstance targetItem = (ItemInstance) World.getInstance().findObject(_targetItemObjId);
        if (player == null) {
            return;
        }

        if (targetItem == null) {
            player.sendPacket(new ExVariationCancelResult(0));
            return;
        }

        // cannot remove augmentation from a not augmented item
        if (!targetItem.isAugmented()) {
            player.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
            player.sendPacket(new ExVariationCancelResult(0));
            return;
        }

        // get the price
        int price = 0;
        switch (targetItem.getItem().getItemGrade()) {
            case Item.CRYSTAL_C: {
                if (targetItem.getCrystalCount() < 1720) {
                    price = 95000;
                } else if (targetItem.getCrystalCount() < 2452) {
                    price = 150000;
                } else {
                    price = 210000;
                }
                break;
            }
            case Item.CRYSTAL_B: {
                if (targetItem.getCrystalCount() < 1746) {
                    price = 240000;
                } else {
                    price = 270000;
                }
                break;
            }
            case Item.CRYSTAL_A: {
                if (targetItem.getCrystalCount() < 2160) {
                    price = 330000;
                } else if (targetItem.getCrystalCount() < 2824) {
                    price = 390000;
                } else {
                    price = 420000;
                }
                break;
            }
            case Item.CRYSTAL_S: {
                price = 480000;
                break;
            }
            // any other item type is not augmentable
            default: {
                player.sendPacket(new ExVariationCancelResult(0));
                return;
            }
        }

        // try to reduce the players adena
        if (!player.reduceAdena("RequestRefineCancel", price, null, true)) {
            return;
        }

        // unequip item
        final InventoryUpdate iu = new InventoryUpdate();
        if (targetItem.isEquipped()) {
            final ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(targetItem.getLocationSlot());
            for (ItemInstance itm : unequiped) {
                iu.addModifiedItem(itm);
            }
        }

        // remove the augmentation
        targetItem.removeAugmentation();

        // send ExVariationCancelResult
        player.sendPacket(new ExVariationCancelResult(1));

        // send inventory update
        iu.addModifiedItem(targetItem);
        player.sendPacket(iu);

        // send system message
        final SystemMessage sm = new SystemMessage(SystemMessageId.AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1);
        sm.addString(targetItem.getItemName());
        player.sendPacket(sm);
    }
}
