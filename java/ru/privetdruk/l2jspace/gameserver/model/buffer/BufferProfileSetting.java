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
package ru.privetdruk.l2jspace.gameserver.model.buffer;

import java.util.List;

public class BufferProfileSetting {
    private final String name;
    private Boolean isLastUsed;
    private List<Integer> skills;

    public BufferProfileSetting(String name, Boolean isLastUsed, List<Integer> skills) {
        this.name = name;
        this.isLastUsed = isLastUsed;
        this.skills = skills;
    }

    public String getName() {
        return name;
    }

    public Boolean getLastUsed() {
        return isLastUsed;
    }

    public void setLastUsed(Boolean lastUsed) {
        isLastUsed = lastUsed;
    }

    public List<Integer> getSkills() {
        return skills;
    }
}
