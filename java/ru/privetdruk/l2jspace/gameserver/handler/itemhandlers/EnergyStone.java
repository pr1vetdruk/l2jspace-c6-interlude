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
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PetInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.model.skills.effects.EffectCharge;
import ru.privetdruk.l2jspace.gameserver.model.skills.handlers.SkillCharge;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.EtcStatusUpdate;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.MagicSkillUse;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

public class EnergyStone implements IItemHandler {
    private static final int[] ITEM_IDS =
            {
                    5589
            };

    @Override
    public void useItem(Playable playable, ItemInstance item) {
        PlayerInstance player;
        if (playable instanceof PlayerInstance) {
            player = (PlayerInstance) playable;
        } else if (playable instanceof PetInstance) {
            player = ((PetInstance) playable).getOwner();
        } else {
            return;
        }
        if (item.getItemId() != 5589) {
            return;
        }
        if (player.isAllSkillsDisabled()) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.isSitting()) {
            player.sendPacket(SystemMessageId.YOU_CANNOT_MOVE_WHILE_SITTING);
            return;
        }

        final SkillCharge skill = getChargeSkill(player);
        if (skill == null) {
            final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
            sm.addItemName(5589);
            player.sendPacket(sm);
            return;
        }

        final SystemMessage sm1 = new SystemMessage(SystemMessageId.USE_S1);
        sm1.addItemName(5589);
        player.sendPacket(sm1);

        final EffectCharge effect = player.getChargeEffect();
        if (effect == null) {
            final Skill dummy = SkillTable.getInstance().getSkill(skill.getId(), skill.getLevel());
            if (dummy != null) {
                dummy.getEffects(player, player);
                player.sendPacket(new MagicSkillUse(playable, player, skill.getId(), 1, 1, 0));
                player.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
            }
            return;
        }

        if (effect.numCharges < 2) {
            effect.addNumCharges(1);
            final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_FORCE_HAS_INCREASED_TO_S1_LEVEL);
            sm.addNumber(effect.getLevel());
            player.sendPacket(sm);
        } else if (effect.numCharges == 2) {
            player.sendPacket(SystemMessageId.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY);
        }

        final MagicSkillUse msu = new MagicSkillUse(playable, player, skill.getId(), 1, 1, 0);
        player.sendPacket(msu);
        player.broadcastPacket(msu);
        player.sendPacket(new EtcStatusUpdate(player));
        player.destroyItem("Consume", item.getObjectId(), 1, null, false);
    }

    private SkillCharge getChargeSkill(PlayerInstance player) {
        for (Skill s : player.getAllSkills()) {
            if ((s.getId() == 50) || (s.getId() == 8)) {
                return (SkillCharge) s;
            }
        }
        return null;
    }

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }
}