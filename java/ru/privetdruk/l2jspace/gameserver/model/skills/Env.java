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
package ru.privetdruk.l2jspace.gameserver.model.skills;

import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;

/**
 * @author ProGramMoS, eX1steam, An Env object is just a class to pass parameters to a calculator such as PlayerInstance, ItemInstance, Initial value.
 */
public class Env {
    public Creature player;
    public Creature target;
    public ItemInstance item;
    public Skill skill;
    public double value;
    public double baseValue;
    public boolean skillMastery = false;
    private Creature creature;
    private Creature _target;

    public Creature getCharacter() {
        return creature;
    }

    public PlayerInstance getPlayer() {
        return creature == null ? null : creature.getActingPlayer();
    }

    public Creature getTarget() {
        return _target;
    }
}
