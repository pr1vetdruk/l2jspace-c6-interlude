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
package ru.privetdruk.l2jspace.gameserver.model.actor.instance;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.gameserver.datatables.BufferTable;
import ru.privetdruk.l2jspace.gameserver.datatables.SkillTable;
import ru.privetdruk.l2jspace.gameserver.enums.buffer.BufferAction;
import ru.privetdruk.l2jspace.gameserver.model.Effect;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.Summon;
import ru.privetdruk.l2jspace.gameserver.model.actor.templates.NpcTemplate;
import ru.privetdruk.l2jspace.gameserver.model.buffer.BufferInterface;
import ru.privetdruk.l2jspace.gameserver.model.buffer.BufferProfileSetting;
import ru.privetdruk.l2jspace.gameserver.model.holders.BuffSkillHolder;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.privetdruk.l2jspace.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import static ru.privetdruk.l2jspace.gameserver.model.skills.SkillEnum.Bishop.GREATER_BATTLE_HEAL;
import static ru.privetdruk.l2jspace.gameserver.model.skills.SkillEnum.Spellsinger.CANCELLATION;

public class BufferInstance extends FolkInstance {
    private static final String BUFFS_TYPE = "Buffs";

    public BufferInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(PlayerInstance player, String commandValue) {
        StringTokenizer tokenizer = new StringTokenizer(commandValue, " ");
        BufferAction action = BufferAction.valueOf(tokenizer.nextToken().toUpperCase(Locale.ROOT));

        switch (action) {
            case ADD_BUFF -> addBuffToProfile(player, tokenizer);
            case BUFF -> buff(player, tokenizer);
            case CANCEL -> cancel(player);
            case CREATE -> createProfile(player, tokenizer);
            case DELETE -> deleteProfile(player, tokenizer);
            case EDIT, SHOW -> show(player, tokenizer);
            case FULL_BUFF -> fullBuff(player, tokenizer);
            case HEAL -> heal(player);
            case HELP -> showHelp(player);
            case HOME -> show(player);
            case NEW_PROFILE -> showCreateProfileInput(player, tokenizer.nextToken());
        }

        super.onBypassFeedback(player, commandValue);
    }

    @Override
    public String getHtmlPath(int npcId, int value) {
        String filename = value == 0 ? Integer.toString(npcId) : npcId + "-" + value;

        return "data/html/mods/buffer/" + filename + ".htm";
    }

    public static void configurePage(PlayerInstance player,
                                     NpcHtmlMessage html,
                                     String typeBuff,
                                     String profileName,
                                     int page,
                                     boolean displayCreateProfileInput,
                                     boolean displayEditing) {
        if (displayEditing && BufferTable.getDefaultProfiles().containsKey(profileName)) {
            displayEditing = false;
            player.sendMessage("Editing profiles is prohibited by default!");
        }

        html.replace("%controlPanel%", BufferInterface.configureControlPanel(player, typeBuff, page, displayEditing, displayCreateProfileInput));
        html.replace("%skillListFrame%", BufferInterface.configureSkillListFrame(player, typeBuff, profileName, page, displayEditing));
        html.replace("<objectId>", "%objectId%");
    }

    public static void configurePage(PlayerInstance player, NpcHtmlMessage html) {
        configurePage(player, html, BUFFS_TYPE, BufferTable.getInstance().getLastUsedProfile(player.getObjectId()), 1, false, false);
    }

    private void show(PlayerInstance player, StringTokenizer tokenizer) {
        String profileName = tokenizer.nextToken();
        boolean displayEditing = Boolean.parseBoolean(tokenizer.nextToken());
        String typeBuff = tokenizer.nextToken();
        int page = Integer.parseInt(tokenizer.nextToken());

        BufferTable.getInstance().setLastUsedProfile(player.getObjectId(), profileName);

        show(player, typeBuff, profileName, page, false, displayEditing);
    }

    public void show(PlayerInstance player) {
        show(player, BUFFS_TYPE, "", 1, false, false);
    }

    public void show(PlayerInstance player, String groupType, String schemeName, int page, boolean displayCreateProfileInput, boolean displayEditing) {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

        html.setFile(getHtmlPath(getNpcId(), 0));

        configurePage(player, html, groupType, schemeName, page, displayCreateProfileInput, displayEditing);

        html.replaceAll("%objectId%", getObjectId());

        player.sendPacket(html);
    }

    private void showCreateProfileInput(PlayerInstance player, String profileName) {
        show(player, BUFFS_TYPE, profileName, 1, true, false);
    }

    private void showHelp(PlayerInstance player) {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(getHtmlPath(getNpcId(), 1));
        html.replaceAll("%objectId%", getObjectId());

        player.sendPacket(html);
    }

    public void deleteProfile(PlayerInstance player, StringTokenizer tokenizer) {
        try {
            String profileName = tokenizer.nextToken();

            Map<String, BufferProfileSetting> profiles = BufferTable.getInstance().getProfiles(player.getObjectId());

            if (profiles != null) {
                profiles.remove(profileName);
            }
        } catch (Exception e) {
            player.sendMessage("This profile name is invalid.");
        }

        show(player);
    }

    public void cancel(PlayerInstance player) {
        player.stopAllEffects();

        Summon summon = player.getPet();
        if (summon != null) {
            summon.stopAllEffects();
        }

        useSkill(player, CANCELLATION.getId(), 1);
        show(player);
    }

    public void heal(PlayerInstance player) {
        player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
        player.setCurrentCp(player.getMaxCp());

        Summon summon = player.getPet();
        if (summon != null) {
            summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp());
        }

        useSkill(player, GREATER_BATTLE_HEAL.getId(), 1);
        show(player);
    }

    public void createProfile(PlayerInstance player, StringTokenizer tokenizer) {
        try {
            String profileName = tokenizer.nextToken();

            if (profileName.length() > 8) {
                player.sendMessage("Profile name must be up to 8 characters long.");
                return;
            }

            // Simple hack to use spaces, dots, commas, minus, plus, exclamations or question marks.
            if (!Util.isAlphaNumeric(profileName.replace(" ", "").replace(".", "").replace(",", "").replace("-", "").replace("+", "").replace("!", "").replace("?", ""))) {
                player.sendMessage("Please use simple alphanumeric characters.");
                return;
            }

            Map<String, BufferProfileSetting> profiles = BufferTable.getInstance().getProfiles(player.getObjectId());

            if (profiles != null) {
                if (profiles.size() == Config.BUFFER_MAX_SCHEMES) {
                    player.sendMessage("The maximum number of profiles has already been reached.");
                    return;
                }

                if (profiles.containsKey(profileName)) {
                    player.sendMessage("The profile name already exists.");
                    return;
                }
            }

            BufferTable.getInstance().addProfile(player.getObjectId(), profileName.trim(), true, new ArrayList<>());

            show(player);
        } catch (Exception e) {
            player.sendMessage("An error occurred while working with the profile, contact your administrator.");
        }
    }

    public void addBuffToProfile(PlayerInstance player, StringTokenizer tokenizer) {
        String typeAction = tokenizer.nextToken();
        String typeBuff = tokenizer.nextToken();
        String profileName = tokenizer.nextToken();
        int skillId = Integer.parseInt(tokenizer.nextToken());
        int page = Integer.parseInt(tokenizer.nextToken());

        List<Integer> skills = BufferTable.getInstance().getSkills(player.getObjectId(), profileName);

        if (typeAction.startsWith("select") && !profileName.equalsIgnoreCase("none")) {
            if (skills.size() < player.getMaxBuffCount()) {
                skills.add(skillId);
            } else {
                player.sendMessage("The maximum number of buffs has been reached in this profile.");
            }
        } else if (typeAction.startsWith("unselect")) {
            skills.remove(Integer.valueOf(skillId));
        }

        show(player, typeBuff, profileName, page, false, true);
    }

    public void fullBuff(PlayerInstance player, StringTokenizer tokenizer) {
        if (player == null) {
            return;
        }

        String profileName = tokenizer.nextToken();

        BufferTable.getInstance().setLastUsedProfile(player.getObjectId(), profileName);

        Creature target;

        if (tokenizer.hasMoreTokens()) {
            String targetType = tokenizer.nextToken();

            if ("pet".equalsIgnoreCase(targetType) && player.getPet() != null) {
                target = player.getPet();
            } else {
                player.sendMessage("You don't have a pet.");

                show(player);

                return;
            }
        } else {
            target = player;
        }

        for (int skillId : BufferTable.getInstance().getSkills(player.getObjectId(), profileName)) {
            BuffSkillHolder availableBuff = BufferTable.getInstance().getAvailableBuff(skillId);

            int price = availableBuff.getPrice();

            if (!player.reduceAdena("NPC Buffer", price, this, true)) {
                continue;
            }

            SkillTable.getInstance().getSkill(skillId, availableBuff.getLevel()).getEffects(this, target);
        }

        heal(player);
        show(player);
    }

    public void buff(PlayerInstance player, StringTokenizer tokenizer) {
        String profileName = tokenizer.nextToken();

        boolean displayEditing = Boolean.parseBoolean(tokenizer.nextToken());
        int skillId = Integer.parseInt(tokenizer.nextToken());
        String buffType = tokenizer.nextToken();
        int page = Integer.parseInt(tokenizer.nextToken());

        BuffSkillHolder availableBuff = BufferTable.getInstance().getAvailableBuff(skillId);

        Effect playerEffect = Arrays.stream(player.getAllEffects())
                .filter(effect -> effect.getSkill().getId() == skillId)
                .findFirst()
                .orElse(null);

        if (playerEffect != null) {
            playerEffect.exit();
        } else if (availableBuff.getPrice() == 0
                || player.reduceAdena("NPC Buffer", availableBuff.getPrice(), this, true)) {
            useSkill(player, availableBuff.getId(), availableBuff.getLevel()).getEffects(this, player);
        }

        show(player, buffType, profileName, page, false, displayEditing);
    }
}