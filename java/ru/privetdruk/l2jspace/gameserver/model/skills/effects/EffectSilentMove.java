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
package ru.privetdruk.l2jspace.gameserver.model.skills.effects;

import ru.privetdruk.l2jspace.gameserver.model.Effect;
import ru.privetdruk.l2jspace.gameserver.model.Skill.SkillType;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.skills.Env;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

final class EffectSilentMove extends Effect {
    public EffectSilentMove(Env env, EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();

        final Creature effected = getEffected();
        if (effected instanceof PlayerInstance) {
            ((PlayerInstance) effected).setSilentMoving(true);
        }
    }

    @Override
    public void onExit() {
        super.onExit();

        final Creature effected = getEffected();
        if (effected instanceof PlayerInstance) {
            ((PlayerInstance) effected).setSilentMoving(false);
        }
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.SILENT_MOVE;
    }

    @Override
    public boolean onActionTime() {
        // Only cont skills shouldn't end
        if (getSkill().getSkillType() != SkillType.CONT) {
            return false;
        }

        if (getEffected().isDead()) {
            return false;
        }

        final double manaDam = calc();
        if (manaDam > getEffected().getCurrentMp()) {
            getEffected().sendPacket(new SystemMessage(SystemMessageId.YOUR_SKILL_WAS_REMOVED_DUE_TO_A_LACK_OF_MP));
            return false;
        }

        getEffected().reduceCurrentMp(manaDam);
        return true;
    }
}