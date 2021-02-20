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
import ru.privetdruk.l2jspace.gameserver.model.actor.Summon;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.skills.Env;

/**
 * @author demonia
 */
final class EffectImobilePetBuff extends Effect {
    private Summon _pet;

    public EffectImobilePetBuff(Env env, EffectTemplate template) {
        super(env, template);
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.BUFF;
    }

    @Override
    public void onStart() {
        _pet = null;
        if ((getEffected() instanceof Summon) && (getEffector() instanceof PlayerInstance) && (((Summon) getEffected()).getOwner() == getEffector())) {
            _pet = (Summon) getEffected();
            _pet.setImmobilized(true);
        }
    }

    @Override
    public void onExit() {
        if (_pet != null) {
            _pet.setImmobilized(false);
        }
    }

    @Override
    public boolean onActionTime() {
        // just stop this effect
        return false;
    }
}
