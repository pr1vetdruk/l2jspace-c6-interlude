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
import ru.privetdruk.l2jspace.gameserver.model.skills.Env;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

/**
 * @author mkizub
 */
final class EffectFakeDeath extends Effect {
    public EffectFakeDeath(Env env, EffectTemplate template) {
        super(env, template);
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.FAKE_DEATH;
    }

    @Override
    public void onStart() {
        getEffected().startFakeDeath();
    }

    @Override
    public void onExit() {
        getEffected().stopFakeDeath(this);
    }

    @Override
    public boolean onActionTime() {
        if (getEffected().isDead()) {
            return false;
        }

        final double manaDam = calc();
        if ((manaDam > getEffected().getCurrentMp()) && getSkill().isToggle()) {
            getEffected().sendPacket(new SystemMessage(SystemMessageId.YOUR_SKILL_WAS_REMOVED_DUE_TO_A_LACK_OF_MP));
            return false;
        }

        getEffected().reduceCurrentMp(manaDam);
        return true;
    }
}
