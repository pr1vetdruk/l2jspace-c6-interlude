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
package ru.privetdruk.l2jspace.gameserver.model.skills.conditions;

import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.skills.Env;
import ru.privetdruk.l2jspace.gameserver.model.skills.Stat;

/**
 * The Class ConditionPlayerWeight.
 *
 * @author Kerberos
 */

public class ConditionTargetWeight extends Condition {
    private final int _weight;

    /**
     * Instantiates a new condition player weight.
     *
     * @param weight the weight
     */
    public ConditionTargetWeight(int weight) {
        _weight = weight;
    }

    @Override
    public boolean testImpl(Env env) {
        final Creature targetObj = env.getTarget();
        if ((targetObj != null) && targetObj.isPlayer()) {
            final PlayerInstance target = targetObj.getActingPlayer();
            if (!target.getDietMode() && (target.getMaxLoad() > 0)) {
                final int weightproc = (int) (((target.getCurrentLoad() - target.calcStat(Stat.WEIGHT_PENALTY, 1, target, null)) * 100) / target.getMaxLoad());
                return (weightproc < _weight);
            }
        }
        return false;
    }
}