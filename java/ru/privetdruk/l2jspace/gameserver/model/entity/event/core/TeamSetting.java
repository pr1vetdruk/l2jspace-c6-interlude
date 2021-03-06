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
package ru.privetdruk.l2jspace.gameserver.model.entity.event.core;

import ru.privetdruk.l2jspace.gameserver.model.Location;

public class TeamSetting {
    protected String name;
    protected int players;
    protected int points;
    protected int color;
    protected Location spawnLocation;

    public TeamSetting(String name, Integer color, Location spawnLocation) {
        this.name = name;
        this.color = color;
        this.spawnLocation = spawnLocation;
    }

    public String getName() {
        return name;
    }

    public Integer getPlayers() {
        return players;
    }

    public Integer getPoints() {
        return points;
    }

    public Integer getColor() {
        return color;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void addPlayer() {
        players++;
    }

    public void removePlayer() {
        players--;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void addPoint() {
        points++;
    }

    public void removePoint() {
        points--;
    }
}
