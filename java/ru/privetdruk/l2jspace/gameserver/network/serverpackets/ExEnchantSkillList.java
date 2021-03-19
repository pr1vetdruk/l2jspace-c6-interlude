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
package ru.privetdruk.l2jspace.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

public class ExEnchantSkillList extends GameServerPacket {
    private final List<Skill> skills;

    class Skill {
        public int id;
        public int nextLevel;
        public int sp;
        public int exp;

        Skill(int id, int nextLevel, int sp, int exp) {
            this.id = id;
            this.nextLevel = nextLevel;
            this.sp = sp;
            this.exp = exp;
        }
    }

    public void addSkill(int id, int level, int sp, int exp) {
        skills.add(new Skill(id, level, sp, exp));
    }

    public int size() {
        return skills.size();
    }

    public ExEnchantSkillList() {
        skills = new ArrayList<>();
    }

    /*
     * (non-Javadoc)
     * @see ru.privetdruk.l2jspace.gameserver.serverpackets.ServerBasePacket#writeImpl()
     */
    @Override
    protected void writeImpl() {
        writeC(0xfe);
        writeH(0x17);
        writeD(skills.size());

        for (Skill skill : skills) {
            writeD(skill.id);
            writeD(skill.nextLevel);
            writeD(skill.sp);
            writeQ(skill.exp);
        }
    }
}
