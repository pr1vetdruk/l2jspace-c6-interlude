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

import ru.privetdruk.l2jspace.gameserver.model.base.Npc;
import ru.privetdruk.l2jspace.gameserver.model.base.RewardItem;
import ru.privetdruk.l2jspace.gameserver.model.spawn.Spawn;

public class GeneralSetting {
    protected String eventName;
    protected String eventDescription;
    protected String registrationLocationName;
    protected int minLevel;
    protected int maxLevel;
    protected int minPlayers;
    protected int maxPlayers;
    protected Npc mainNpc;
    protected Spawn spawnMainNpc;
    protected RewardItem reward;
    protected int timeRegistration;
    protected int durationEvent;
    protected long intervalBetweenMatches;

    public GeneralSetting() {
    }

    public GeneralSetting(String eventName, String eventDescription, String registrationLocationName, int minLevel, int maxLevel, Npc mainNpc, RewardItem reward, int timeRegistration, int durationEvent, int minPlayers, int maxPlayers, long intervalBetweenMatches) {
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.registrationLocationName = registrationLocationName;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.mainNpc = mainNpc;
        this.reward = reward;
        this.timeRegistration = timeRegistration;
        this.durationEvent = durationEvent;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.intervalBetweenMatches = intervalBetweenMatches;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public String getRegistrationLocationName() {
        return registrationLocationName;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public Npc getMainNpc() {
        return mainNpc;
    }

    public RewardItem getReward() {
        return reward;
    }

    public int getTimeRegistration() {
        return timeRegistration;
    }

    public int getDurationEvent() {
        return durationEvent;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public long getIntervalBetweenMatches() {
        return intervalBetweenMatches;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public void setRegistrationLocationName(String registrationLocationName) {
        this.registrationLocationName = registrationLocationName;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public void setMainNpc(Npc mainNpc) {
        this.mainNpc = mainNpc;
    }

    public void setReward(RewardItem reward) {
        this.reward = reward;
    }

    public void setTimeRegistration(int timeRegistration) {
        this.timeRegistration = timeRegistration;
    }

    public void setDurationEvent(int durationEvent) {
        this.durationEvent = durationEvent;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setIntervalBetweenMatches(long intervalBetweenMatches) {
        this.intervalBetweenMatches = intervalBetweenMatches;
    }

    public Spawn getSpawnMainNpc() {
        return spawnMainNpc;
    }

    public void setSpawnMainNpc(Spawn spawnMainNpc) {
        this.spawnMainNpc = spawnMainNpc;
    }
}
