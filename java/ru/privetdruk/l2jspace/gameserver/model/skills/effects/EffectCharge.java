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
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.skills.Env;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.EtcStatusUpdate;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

public class EffectCharge extends Effect {
    public int numCharges;

    public EffectCharge(Env env, EffectTemplate template) {
        super(env, template);
        numCharges = 1;
        if (env.target instanceof PlayerInstance) {
            env.target.sendPacket(new EtcStatusUpdate((PlayerInstance) env.target));
            final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_FORCE_HAS_INCREASED_TO_S1_LEVEL);
            sm.addNumber(numCharges);
            getEffected().sendPacket(sm);
        }
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.CHARGE;
    }

    @Override
    public boolean onActionTime() {
        // ignore
        return true;
    }

    @Override
    public int getLevel() {
        return numCharges;
    }

    public void addNumCharges(int i) {
        numCharges = numCharges + i;
    }
}
