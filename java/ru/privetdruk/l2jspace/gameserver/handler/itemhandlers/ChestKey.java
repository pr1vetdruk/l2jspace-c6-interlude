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
package ru.privetdruk.l2jspace.gameserver.handler.itemhandlers;

import ru.privetdruk.l2jspace.gameserver.datatables.SkillTable;
import ru.privetdruk.l2jspace.gameserver.handler.IItemHandler;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.ChestInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;

public class ChestKey implements IItemHandler {
    public static final int INTERACTION_DISTANCE = 100;

    private static final int[] ITEM_IDS =
            {
                    6665,
                    6666,
                    6667,
                    6668,
                    6669,
                    6670,
                    6671,
                    6672, // deluxe key
            };

    @Override
    public void useItem(Playable playable, ItemInstance item) {
        if (!(playable instanceof PlayerInstance)) {
            return;
        }

        final PlayerInstance player = (PlayerInstance) playable;
        final int itemId = item.getItemId();
        final WorldObject target = player.getTarget();
        if (!(target instanceof ChestInstance)) {
            player.sendPacket(SystemMessageId.INVALID_TARGET);
            player.sendPacket(ActionFailed.STATIC_PACKET);
        } else {
            final ChestInstance chest = (ChestInstance) target;
            if (chest.isDead() || chest.isInteracted()) {
                player.sendMessage("The chest is empty.");
                player.sendPacket(ActionFailed.STATIC_PACKET);
                return;
            }

            final Skill skill = SkillTable.getInstance().getSkill(2229, itemId - 6664); // box key skill
            player.useMagic(skill, false, false);
        }
    }

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }
}
