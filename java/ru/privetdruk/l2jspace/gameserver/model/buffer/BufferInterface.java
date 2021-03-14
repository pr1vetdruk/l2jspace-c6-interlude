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

import ru.privetdruk.l2jspace.gameserver.datatables.BufferTable;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.holders.BuffSkillHolder;
import ru.privetdruk.l2jspace.gameserver.util.Util;

import java.util.List;
import java.util.Map;

import static ru.privetdruk.l2jspace.gameserver.model.skills.SkillEnum.ElementalSummoner.GIFT_OF_SERAPHIM;
import static ru.privetdruk.l2jspace.gameserver.model.skills.SkillEnum.ElementalSummoner.ICON_GIFT_OF_SERAPHIM;
import static ru.privetdruk.l2jspace.gameserver.model.skills.SkillEnum.HotSprings.FLU;
import static ru.privetdruk.l2jspace.gameserver.model.skills.SkillEnum.HotSprings.ICON;
import static ru.privetdruk.l2jspace.gameserver.model.skills.SkillEnum.HotSprings.MALARIA;
import static ru.privetdruk.l2jspace.gameserver.model.skills.SkillEnum.Warlock.BLESSING_OF_QUEEN;
import static ru.privetdruk.l2jspace.gameserver.model.skills.SkillEnum.Warlock.ICON_BLESSING_OF_QUEEN;

public class BufferInterface {
    private static final int PAGE_LIMIT = 16;

    private static final String CREATE_SCHEME = "<td><edit var=\"name\" width=60 height=10></td>" +
            "<td><button value=\"Create\" action=\"bypass -h npc_%objectId%_create $name\" width=64 height=15 back=\"L2UI.SquareWhite\" fore=\"L2UI.SquareGray\"></td>" +
            "<td><button value=\"Cancel\" action=\"bypass -h npc_%objectId%_cancel\" width=64 height=15 back=\"L2UI.SquareWhite\" fore=\"L2UI.SquareGray\"></td>";

    private static final String MANIPULATE_SCHEME = "<td><button value=\"+\" action=\"bypass -h npc_<objectId>_new_profile $profileName\" width=16 height=15 back=\"L2UI.SquareWhite\" fore=\"L2UI.SquareGray\"></td>" +
            "<td><button value=\"-\" action=\"bypass -h npc_<objectId>_delete $profileName\" width=16 height=15 back=\"L2UI.SquareWhite\" fore=\"L2UI.SquareGray\"></td>" +
            "<td><button value=\"*\" action=\"bypass -h npc_<objectId>_edit $profileName %s %s %d\" width=16 height=15 back=\"L2UI.SquareWhite\" fore=\"L2UI.SquareGray\"></td>";

    private static final String BUFF_BUTTONS = "<td><button value=\"Me\" action=\"bypass -h npc_%objectId%_full_buff $profileName\" width=32 height=15 back=\"L2UI.SquareWhite\" fore=\"L2UI.SquareGray\"></td>" +
            "<td><button value=\"Pet\" action=\"bypass -h npc_%objectId%_full_buff $profileName pet\" width=32 height=15 back=\"L2UI.SquareWhite\" fore=\"L2UI.SquareGray\"></td>";

    private static final String HEAL_CANCEL = "<td><button value=\"Heal\" action=\"bypass -h npc_%objectId%_heal\" width=32 height=15 back=\"L2UI.SquareWhite\" fore=\"L2UI.SquareGray\"></td>" +
            "<td><button value=\"Cancel\" action=\"bypass -h npc_%objectId%_cancel\" width=42 height=15 back=\"L2UI.SquareWhite\" fore=\"L2UI.SquareGray\"></td>";

    public static String configureControlPanel(PlayerInstance player,
                                               String groupType,
                                               int page,
                                               boolean displayEditing,
                                               boolean displayCreationScheme) {
        String schemeList = configureProfileList(player);

        if (schemeList.isEmpty()) {
            return CREATE_SCHEME + HEAL_CANCEL;
        } else {
            return (displayCreationScheme ? CREATE_SCHEME : String.format(MANIPULATE_SCHEME, !displayEditing, groupType, page) +
                    "<td><combobox width=60 height=17 var=\"profileName\" list=\"" + schemeList + "\"></td>" +
                    BUFF_BUTTONS) + HEAL_CANCEL;

        }
    }

    private static String configureProfileList(PlayerInstance player) {
        BufferTable buffer = BufferTable.getInstance();

        Map<String, BufferProfileSetting> playerProfiles = buffer.getProfiles(player.getObjectId());

        String defaultProfiles = BufferTable.getDefaultProfiles().keySet().stream()
                .reduce("", (next, current) -> current.concat(";").concat(next));

        if (playerProfiles == null || playerProfiles.isEmpty()) {
            return defaultProfiles;
        }

        String lastUsedProfile = playerProfiles.values().stream()
                .filter(BufferProfileSetting::getLastUsed)
                .map(BufferProfileSetting::getName)
                .findFirst()
                .orElse("");

        return lastUsedProfile
                .concat(";")
                .concat(playerProfiles.keySet().stream()
                        .filter(profileName -> !profileName.equals(lastUsedProfile))
                        .reduce("", (next, current) -> current.concat(";").concat(next)))
                .concat(defaultProfiles);
    }

    private static void configureBuffControl(StringBuilder html, int page, int maxPage, String groupType, boolean displayEditing) {
        html.append("<img src=\"L2UI.SquareGray\" width=277 height=1><table width=298 bgcolor=000000><tr><td align=center width=16>");

        if (page > 1) {
            html.append("<button width=16 height=12 fore=\"l2ui.bbs_prev\" back=\"l2ui.bbs_prev_down\" action=\"bypass -h npc_<objectId>_show $profileName ").append(displayEditing).append(" ").append(groupType).append(" ").append(page - 1).append("\"");
        } else {
            html.append("<button width=16 height=12 fore=\"l2ui.bbs_prev_off\" back=\"l2ui.bbs_prev_off\">");
        }

        html.append("</td>");

        html.append("<td><a action=\"bypass -h npc_%objectId%_help\"><font color=\"00FF00\">?</font></a></td>");

        for (String type : BufferTable.getInstance().getSkillTypes()) {
            if (groupType.equalsIgnoreCase(type)) {
                html.append("<td><font color=\"LEVEL\">").append(type).append("</font></td>");
            } else {
                html.append("<td><a action=\"bypass -h npc_%objectId%_show $profileName ").append(displayEditing).append(" ").append(type).append(" 1\">").append(type).append("</a></td>");
            }
        }

        html.append("<td align=right width=16>");

        if (page < maxPage) {
            html.append("<button width=16 height=12 fore=\"l2ui.bbs_next\" back=\"l2ui.bbs_next_down\" action=\"bypass -h npc_<objectId>_show $profileName ").append(displayEditing).append(" ").append(groupType).append(" ").append(page + 1).append("\"");
        } else {
            html.append("<button width=16 height=12 fore=\"l2ui.bbs_next_off\" back=\"l2ui.bbs_next_off\">");
        }

        html.append("</td></tr></table><img src=\"L2UI.SquareGray\" width=277 height=1><br>");
    }

    public static String configureSkillListFrame(PlayerInstance player,
                                                 String buffType,
                                                 String schemeName,
                                                 int pageValue,
                                                 boolean displayEditing) {
        List<Integer> skills = BufferTable.getInstance().getSkillsIdsByType(buffType);
        if (skills.isEmpty()) {
            return "That group doesn't contain any skills.";
        }

        int max = Util.countPagesNumber(skills.size(), PAGE_LIMIT);
        int page = Math.min(pageValue, max);

        skills = skills.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, skills.size()));

        List<Integer> profileSkills = BufferTable.getInstance().getSkills(player.getObjectId(), schemeName);
        StringBuilder html = new StringBuilder(skills.size() * 150);

        configureBuffControl(html, page, max, buffType, displayEditing);

        html.append("<table width=290>");

        for (int skillIndex = 0, numberBuffsPerLine = 1; skillIndex < skills.size(); skillIndex++, numberBuffsPerLine++) {
            if (numberBuffsPerLine == 1) {
                html.append("<tr>");
            }

            Integer skillId = skills.get(skillIndex);

            String skillSelect = "";
            String add = "+";

            if (profileSkills.contains(skillId)) {
                skillSelect = "un";
                add = "-";
            }

            int iconSkillId = skillId;

            if (skillId == FLU.getId() || skillId == MALARIA.getId()) {
                iconSkillId = ICON.getId();
            } else if (skillId == GIFT_OF_SERAPHIM.getId()) {
                iconSkillId = ICON_GIFT_OF_SERAPHIM.getId();
            } else if (skillId == BLESSING_OF_QUEEN.getId()) {
                iconSkillId = ICON_BLESSING_OF_QUEEN.getId();
            }

            html.append(
                    String.format(
                            "<td align=\"center\" width=82 height=62><button action=\"bypass -h npc_<objectId>_buff $profileName %s %d %s %d\" fore=\"icon.skill%04d\" back=\"icon.etc_l2_i00\" width=32 height=32>",
                            displayEditing, skillId, buffType, page, iconSkillId
                    )
            );

            if (displayEditing) {
                html.append(String.format(
                        "<button value=\"%s\" action=\"bypass -h npc_<objectId>_add_buff %sselect %s $profileName %d %s\" width=32 height=15 back=\"L2UI.SquareWhite\" fore=\"L2UI.SquareGray\">",
                        add, skillSelect, buffType, skillId, page)
                );
            }

            BuffSkillHolder availableBuff = BufferTable.getInstance().getAvailableBuff(skillId);
            html.append(availableBuff.getDescription()).append("</td>");

            if (numberBuffsPerLine == 4) {
                numberBuffsPerLine = 0;
                html.append("</tr>");
            }
        }
        html.append("</table>");

        return html.toString();
    }
}
