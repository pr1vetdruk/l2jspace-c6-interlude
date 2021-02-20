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

import ru.privetdruk.l2jspace.gameserver.datatables.sql.NpcTable;
import ru.privetdruk.l2jspace.gameserver.instancemanager.IdManager;
import ru.privetdruk.l2jspace.gameserver.model.Location;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.StatSet;
import ru.privetdruk.l2jspace.gameserver.model.World;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.EffectPointInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.templates.NpcTemplate;

public class SkillSignet extends Skill {
    private final int _effectNpcId;
    public int effectId;

    public SkillSignet(StatSet set) {
        super(set);
        _effectNpcId = set.getInt("effectNpcId", -1);
        effectId = set.getInt("effectId", -1);
    }

    @Override
    public void useSkill(Creature caster, List<Creature> targets) {
        if (caster.isAlikeDead()) {
            return;
        }

        final NpcTemplate template = NpcTable.getInstance().getTemplate(_effectNpcId);
        final EffectPointInstance effectPoint = new EffectPointInstance(IdManager.getInstance().getNextId(), template, caster);
        effectPoint.getStatus().setCurrentHp(effectPoint.getMaxHp());
        effectPoint.getStatus().setCurrentMp(effectPoint.getMaxMp());
        World.getInstance().storeObject(effectPoint);

        int x = caster.getX();
        int y = caster.getY();
        int z = caster.getZ();
        if ((caster instanceof PlayerInstance) && (getTargetType() == Skill.SkillTargetType.TARGET_GROUND)) {
            final Location wordPosition = ((PlayerInstance) caster).getCurrentSkillWorldPosition();
            if (wordPosition != null) {
                x = wordPosition.getX();
                y = wordPosition.getY();
                z = wordPosition.getZ();
            }
        }
        getEffects(caster, effectPoint, false, false, false);
        effectPoint.setInvul(true);
        effectPoint.spawnMe(x, y, z);
    }
}