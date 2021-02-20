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
package ru.privetdruk.l2jspace.gameserver.model.skills.handlers;

import java.util.List;

import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.StatSet;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.skills.effects.EffectCharge;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.EtcStatusUpdate;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

public class SkillChargeEffect extends Skill {
    final int chargeSkillId;

    public SkillChargeEffect(StatSet set) {
        super(set);
        chargeSkillId = set.getInt("charge_skill_id");
    }

    @Override
    public boolean checkCondition(Creature creature, WorldObject target, boolean itemOrWeapon) {
        if (creature instanceof PlayerInstance) {
            final PlayerInstance player = (PlayerInstance) creature;
            final EffectCharge e = (EffectCharge) player.getFirstEffect(chargeSkillId);
            if ((e == null) || (e.numCharges < getNumCharges())) {
                final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
                sm.addSkillName(getId());
                creature.sendPacket(sm);
                return false;
            }
        }
        return super.checkCondition(creature, target, itemOrWeapon);
    }

    @Override
    public void useSkill(Creature creature, List<Creature> targets) {
        if (creature.isAlikeDead()) {
            return;
        }

        // get the effect
        final EffectCharge effect = (EffectCharge) creature.getFirstEffect(chargeSkillId);
        if ((effect == null) || (effect.numCharges < getNumCharges())) {
            final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
            sm.addSkillName(getId());
            creature.sendPacket(sm);
            return;
        }

        // decrease?
        effect.numCharges -= getNumCharges();

        // maybe exit? no charge
        if (effect.numCharges == 0) {
            effect.exit(false);
        }

        // apply effects
        if (hasEffects()) {
            for (WorldObject target : targets) {
                getEffects(creature, (Creature) target, false, false, false);
            }
        }
        if (creature instanceof PlayerInstance) {
            creature.sendPacket(new EtcStatusUpdate((PlayerInstance) creature));
        }
    }
}
