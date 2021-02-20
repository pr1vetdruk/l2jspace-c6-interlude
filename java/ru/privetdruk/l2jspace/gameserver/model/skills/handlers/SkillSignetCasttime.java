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
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;

public class SkillSignetCasttime extends Skill {
    public int _effectNpcId;
    public int effectId;

    public SkillSignetCasttime(StatSet set) {
        super(set);
        _effectNpcId = set.getInt("effectNpcId", -1);
        effectId = set.getInt("effectId", -1);
    }

    @Override
    public void useSkill(Creature caster, List<Creature> targets) {
        if (caster.isAlikeDead()) {
            return;
        }

        getEffectsSelf(caster);
    }
}
