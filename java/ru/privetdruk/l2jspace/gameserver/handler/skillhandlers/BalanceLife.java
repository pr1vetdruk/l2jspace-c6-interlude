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

import ru.privetdruk.l2jspace.gameserver.handler.ISkillHandler;
import ru.privetdruk.l2jspace.gameserver.handler.SkillHandler;
import ru.privetdruk.l2jspace.gameserver.instancemanager.GrandBossManager;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.StatusUpdate;

/**
 * @author earendil
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */
public class BalanceLife implements ISkillHandler {
    private static final Skill.SkillType[] SKILL_IDS =
            {
                    Skill.SkillType.BALANCE_LIFE
            };

    @Override
    public void useSkill(Creature creature, Skill skill, List<Creature> targets) {
        // check for other effects
        try {
            final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(Skill.SkillType.BUFF);
            if (handler != null) {
                handler.useSkill(creature, skill, targets);
            }
        } catch (Exception e) {
        }

        Creature target = null;
        PlayerInstance player = null;
        if (creature instanceof PlayerInstance) {
            player = (PlayerInstance) creature;
        }

        double fullHP = 0;
        double currentHPs = 0;
        for (WorldObject target2 : targets) {
            target = (Creature) target2;

            // We should not heal if char is dead
            if ((target == null) || target.isDead()) {
                continue;
            }

            // Avoid characters heal inside Baium lair from outside
            if (((GrandBossManager.getInstance().getZone(creature) == null) && (GrandBossManager.getInstance().getZone(target) != null)) || ((GrandBossManager.getInstance().getZone(target) == null) && (GrandBossManager.getInstance().getZone(creature) != null))) {
                continue;
            }

            // Player holding a cursed weapon can't be healed and can't heal
            if (target != creature) {
                if ((target instanceof PlayerInstance) && ((PlayerInstance) target).isCursedWeaponEquiped()) {
                    continue;
                } else if ((player != null) && player.isCursedWeaponEquiped()) {
                    continue;
                }
            }

            fullHP += target.getMaxHp();
            currentHPs += target.getCurrentHp();
        }

        final double percentHP = currentHPs / fullHP;
        for (WorldObject target2 : targets) {
            target = (Creature) target2;
            if ((target == null) || target.isDead()) {
                continue;
            }

            final double newHP = target.getMaxHp() * percentHP;
            final double totalHeal = newHP - target.getCurrentHp();
            target.setCurrentHp(newHP);

            if (totalHeal > 0) {
                target.setLastHealAmount((int) totalHeal);
            }

            final StatusUpdate su = new StatusUpdate(target.getObjectId());
            su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
            target.sendPacket(su);

            target.sendMessage("HP of the party has been balanced.");
        }
    }

    @Override
    public Skill.SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
