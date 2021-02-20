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
package ru.privetdruk.l2jspace.gameserver.model.spawn;

import java.lang.reflect.Constructor;

import ru.privetdruk.l2jspace.commons.util.Rnd;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.TerritoryTable;
import ru.privetdruk.l2jspace.gameserver.instancemanager.IdManager;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.NpcInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.templates.NpcTemplate;

/**
 * @author littlecrow A special spawn implementation to spawn controllable mob
 */
public class GroupSpawn extends Spawn {
    private final Constructor<?> _constructor;
    private final NpcTemplate _template;

    public GroupSpawn(NpcTemplate mobTemplate) throws ClassNotFoundException, NoSuchMethodException {
        super(mobTemplate);
        _constructor = Class.forName("ru.privetdruk.l2jspace.gameserver.model.actor.instance.ControllableMobInstance").getConstructors()[0];
        _template = mobTemplate;
        setAmount(1);
    }

    public NpcInstance doGroupSpawn() {
        NpcInstance mob = null;

        try {
            if (_template.getType().equalsIgnoreCase("Pet") || _template.getType().equalsIgnoreCase("Minion")) {
                return null;
            }

            final Object[] parameters =
                    {
                            IdManager.getInstance().getNextId(),
                            _template
                    };
            final Object tmp = _constructor.newInstance(parameters);
            if (!(tmp instanceof NpcInstance)) {
                return null;
            }

            mob = (NpcInstance) tmp;
            int newlocx;
            int newlocy;
            int newlocz;
            if ((getX() == 0) && (getY() == 0)) {
                if (getLocation() == 0) {
                    return null;
                }

                final int[] p = TerritoryTable.getInstance().getRandomPoint(getLocation());
                newlocx = p[0];
                newlocy = p[1];
                newlocz = p[2];
            } else {
                newlocx = getX();
                newlocy = getY();
                newlocz = getZ();
            }

            mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
            if (getHeading() == -1) {
                mob.setHeading(Rnd.get(61794));
            } else {
                mob.setHeading(getHeading());
            }

            mob.setSpawn(this);
            mob.spawnMe(newlocx, newlocy, newlocz);
            mob.onSpawn();

            return mob;
        } catch (Exception e) {
            LOGGER.warning("NPC class not found: " + e);
            return null;
        }
    }
}
