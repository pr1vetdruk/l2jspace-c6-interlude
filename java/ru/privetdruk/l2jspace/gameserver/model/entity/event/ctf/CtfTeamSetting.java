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
package ru.privetdruk.l2jspace.gameserver.model.entity.event.ctf;

import ru.privetdruk.l2jspace.gameserver.model.Location;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.core.TeamSetting;

public class CtfTeamSetting extends TeamSetting {
    protected int id;
    protected Flag flag;
    protected Throne throne;

    public CtfTeamSetting(int id, String name, Integer playersCount, Location spawnLocation, Flag flag, Throne throne) {
        super(name, playersCount, spawnLocation);

        this.id = id;
        this.flag = flag;
        this.throne = throne;
    }

    public int getId() {
        return id;
    }

    public Flag getFlag() {
        return flag;
    }

    public void setFlag(Flag flag) {
        this.flag = flag;
    }

    public Throne getThrone() {
        return throne;
    }

    public void setThrone(Throne throne) {
        this.throne = throne;
    }
}
