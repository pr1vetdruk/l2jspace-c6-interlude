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
package ru.privetdruk.l2jspace.gameserver.model;

import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;

public class Location {
    public static final Location DUMMY_LOC = new Location(0, 0, 0);

    protected int x;
    protected int y;
    protected int z;
    private int _heading = 0;

    public Location(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location(int x, int y, int z, int heading) {
        this.x = x;
        this.y = y;
        this.z = z;
        _heading = heading;
    }

    public Location(WorldObject obj) {
        x = obj.getX();
        y = obj.getY();
        z = obj.getZ();
    }

    public Location(Creature obj) {
        x = obj.getX();
        y = obj.getY();
        z = obj.getZ();
        _heading = obj.getHeading();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getHeading() {
        return _heading;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void setHeading(int head) {
        _heading = head;
    }

    public void setXYZ(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(int x, int y, int z) {
        return (this.x == x) && (this.y == y) && (this.z == z);
    }
}
