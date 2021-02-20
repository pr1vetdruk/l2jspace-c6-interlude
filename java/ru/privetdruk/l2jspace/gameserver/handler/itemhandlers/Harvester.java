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
import ru.privetdruk.l2jspace.gameserver.instancemanager.CastleManorManager;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.MonsterInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;

/**
 * @author l3x
 */
public class Harvester implements IItemHandler {
    private static final int[] ITEM_IDS =
            {
                    5125
            };

    @Override
    public void useItem(Playable playable, ItemInstance item) {
        if (!(playable instanceof PlayerInstance)) {
            return;
        }

        if (CastleManorManager.getInstance().isDisabled()) {
            return;
        }

        final PlayerInstance player = (PlayerInstance) playable;
        if (!(player.getTarget() instanceof MonsterInstance)) {
            player.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        final MonsterInstance target = (MonsterInstance) player.getTarget();
        if ((target == null) || !target.isDead()) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        final Skill skill = SkillTable.getInstance().getSkill(2098, 1); // harvesting skill
        player.useMagic(skill, false, false);
    }

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }
}
