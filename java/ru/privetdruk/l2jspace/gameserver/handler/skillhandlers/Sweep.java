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
package ru.privetdruk.l2jspace.gameserver.handler.skillhandlers;

import java.util.List;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.gameserver.handler.ISkillHandler;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Attackable;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.base.RewardItem;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.InventoryUpdate;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ItemList;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

/**
 * @author _drunk_ TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class Sweep implements ISkillHandler {
    private static final Skill.SkillType[] SKILL_IDS =
            {
                    Skill.SkillType.SWEEP
            };

    @Override
    public void useSkill(Creature creature, Skill skill, List<Creature> targets) {
        if (!(creature instanceof PlayerInstance)) {
            return;
        }

        final PlayerInstance player = (PlayerInstance) creature;
        final InventoryUpdate iu = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
        boolean send = false;
        for (WorldObject target1 : targets) {
            if (!(target1 instanceof Attackable)) {
                continue;
            }

            final Attackable target = (Attackable) target1;
            RewardItem[] items = null;
            boolean isSweeping = false;
            synchronized (target) {
                if (target.isSweepActive()) {
                    items = target.takeSweep();
                    isSweeping = true;
                }
            }

            if (isSweeping) {
                if ((items == null) || (items.length == 0)) {
                    continue;
                }
                for (RewardItem ritem : items) {
                    if (player.isInParty()) {
                        player.getParty().distributeItem(player, ritem, true, target);
                    } else {
                        final ItemInstance item = player.getInventory().addItem("Sweep", ritem.getId(), ritem.getAmount(), player, target);
                        if (iu != null) {
                            iu.addItem(item);
                        }
                        send = true;
                        SystemMessage smsg;
                        if (ritem.getAmount() > 1) {
                            smsg = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S); // earned $s2$s1
                            smsg.addItemName(ritem.getId());
                            smsg.addNumber(ritem.getAmount());
                        } else {
                            smsg = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1); // earned $s1
                            smsg.addItemName(ritem.getId());
                        }
                        player.sendPacket(smsg);
                    }
                }
            }
            target.endDecayTask();

            if (send) {
                if (iu != null) {
                    player.sendPacket(iu);
                } else {
                    player.sendPacket(new ItemList(player, false));
                }
            }
        }
    }

    @Override
    public Skill.SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
