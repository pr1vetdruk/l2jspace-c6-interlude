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
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.EffectPointInstance;
import ru.privetdruk.l2jspace.gameserver.model.skills.Env;

public class EffectSignetNoise extends Effect {
    private EffectPointInstance _actor;

    public EffectSignetNoise(Env env, EffectTemplate template) {
        super(env, template);
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.SIGNET_GROUND;
    }

    @Override
    public void onStart() {
        _actor = (EffectPointInstance) getEffected();
    }

    @Override
    public boolean onActionTime() {
        if (getCount() == (getTotalCount() - 1)) {
            return true; // do nothing first time
        }

        for (Creature target : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius())) {
            if (target == null) {
                continue;
            }

            final Effect[] effects = target.getAllEffects();
            if (effects != null) {
                for (Effect effect : effects) {
                    if (effect.getSkill().isDance()) {
                        effect.exit(true);
                    }
                }
                // there doesn't seem to be a visible effect?
            }
        }
        return true;
    }

    @Override
    public void onExit() {
        if (_actor != null) {
            _actor.deleteMe();
        }
    }
}
