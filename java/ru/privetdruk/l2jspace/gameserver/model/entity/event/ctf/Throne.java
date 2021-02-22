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

import ru.privetdruk.l2jspace.gameserver.model.base.Npc;
import ru.privetdruk.l2jspace.gameserver.model.spawn.Spawn;

public class Throne {
    protected Npc npc;
    protected Spawn spawn;
    protected int offsetZ;

    public Throne(Npc npc, Spawn spawn, int offsetZ) {
        this.npc = npc;
        this.spawn = spawn;
        this.offsetZ = offsetZ;
    }

    public Npc getNpc() {
        return npc;
    }

    public void setNpc(Npc npc) {
        this.npc = npc;
    }

    public Spawn getSpawn() {
        return spawn;
    }

    public void setSpawn(Spawn spawn) {
        this.spawn = spawn;
    }

    public int getOffsetZ() {
        return offsetZ;
    }

    public void setOffsetZ(int offsetZ) {
        this.offsetZ = offsetZ;
    }
}
