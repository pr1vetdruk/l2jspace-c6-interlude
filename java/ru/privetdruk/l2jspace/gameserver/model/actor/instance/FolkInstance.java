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

import java.util.List;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.gameserver.datatables.SkillTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.SkillTreeTable;
import ru.privetdruk.l2jspace.gameserver.model.EnchantSkillLearn;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.SkillLearn;
import ru.privetdruk.l2jspace.gameserver.model.actor.templates.NpcTemplate;
import ru.privetdruk.l2jspace.gameserver.model.base.ClassId;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.AcquireSkillList;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ExEnchantSkillList;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

import static ru.privetdruk.l2jspace.gameserver.network.SystemMessageId.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1;

/**
 * The Class FolkInstance.
 */
public abstract class FolkInstance extends NpcInstance {
    private final List<ClassId> classesToStudyList;
    protected String pathToFolderHtml;

    /**
     * Instantiates a new folk instance.
     *
     * @param objectId the object id
     * @param template the template
     */
    public FolkInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
        classesToStudyList = template.getTeachInfo();
        pathToFolderHtml = "default/";
    }

    public FolkInstance(int objectId, NpcTemplate template, String pathToFolderHtml) {
        super(objectId, template);
        classesToStudyList = template.getTeachInfo();
        this.pathToFolderHtml = pathToFolderHtml;
    }

    @Override
    public void onAction(PlayerInstance player) {
        player.setLastFolkNPC(this);
        super.onAction(player);
    }

    @Override
    public String getHtmlPath(int npcId, int value) {
        String filename = value == 0 ? Integer.toString(npcId) : npcId + "-" + value;

        return "data/html/" + pathToFolderHtml + filename + ".htm";
    }

    /**
     * this displays SkillList to the player.
     *
     * @param player  the player
     * @param classId the class id
     */
    public void showSkillList(PlayerInstance player, ClassId classId) {
        if (checkClassAvailability(player, classId)) {
            return;
        }

        SkillLearn[] skillsLearn = SkillTreeTable.getInstance().getAvailableSkills(player, classId);
        AcquireSkillList acquireSkillList = new AcquireSkillList(AcquireSkillList.SkillType.USUAL);

        for (SkillLearn skillLearn : skillsLearn) {
            Skill skill = SkillTable.getInstance().getSkill(skillLearn.getId(), skillLearn.getLevel());

            if (skill == null || !skill.getCanLearn(player.getClassId()) || !skill.canTeachBy(getTemplate().getNpcId())) {
                continue;
            }

            int cost = SkillTreeTable.getInstance().getSkillCost(player, skill);
            acquireSkillList.addSkill(skillLearn.getId(), skillLearn.getLevel(), skillLearn.getLevel(), cost, 0);
        }

        if (acquireSkillList.size() == 0) {
            int minLevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player, classId);
            
            if (minLevel > 0) {
                SystemMessage systemMessage = new SystemMessage(YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
                systemMessage.addNumber(minLevel);
                player.sendPacket(systemMessage);
            } else {
                player.sendPacket(new SystemMessage(SystemMessageId.THERE_ARE_NO_OTHER_SKILLS_TO_LEARN));
            }
        } else {
            player.sendPacket(acquireSkillList);
        }

        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    /**
     * this displays EnchantSkillList to the player.
     *
     * @param player  the player
     * @param classId the class id
     */
    public void showEnchantSkillList(PlayerInstance player, ClassId classId) {
        if (checkClassAvailability(player, classId)) {
            return;
        }

        if (player.getClassId().getId() < 88) {
            showHtml(player, "You must have 3rd class change quest completed.");
            return;
        }

        EnchantSkillLearn[] enchantSkillLearns = SkillTreeTable.getInstance().getAvailableEnchantSkills(player);
        ExEnchantSkillList exEnchantSkillList = new ExEnchantSkillList();

        for (EnchantSkillLearn enchantSkillLearn : enchantSkillLearns) {
            Skill skill = SkillTable.getInstance().getSkill(enchantSkillLearn.getId(), enchantSkillLearn.getLevel());

            if (skill == null) {
                continue;
            }

            exEnchantSkillList.addSkill(
                    enchantSkillLearn.getId(),
                    enchantSkillLearn.getLevel(),
                    enchantSkillLearn.getSpCost(),
                    enchantSkillLearn.getExp()
            );
        }

        if (exEnchantSkillList.size() == 0) {
            player.sendPacket(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);

            int level = player.getLevel();

            if (level < 74) {
                SystemMessage sm = new SystemMessage(YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
                sm.addNumber(level);
                player.sendPacket(sm);
            } else {
                showHtml(player, "You've learned all skills for your class.<br>");
            }
        } else {
            player.sendPacket(exEnchantSkillList);
        }

        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    private boolean checkClassAvailability(PlayerInstance player, ClassId classId) {
        if (classesToStudyList == null) {
            if (player.isGM()) {
                showHtml(player, "I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:" + getTemplate().getNpcId() + ", Your classId:" + player.getClassId().getId() + "<br>");
            } else {
                player.sendMessage("Error learning skills, contact the admin.");
            }
            
            return true;
        }

        if (!getTemplate().canTeach(classId)) {
            showHtml(player, "I cannot teach you any skills.<br> You must find your current class teachers.");
            return true;
        }
        
        return false;
    }

    private void showHtml(PlayerInstance player, String message) {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        String htmlText = "<html><body>" + message + "</body></html>";
        
        html.setHtml(htmlText);
        player.sendPacket(html);
    }

    @Override
    public void onBypassFeedback(PlayerInstance player, String command) {
        if (command.startsWith("SkillList")) {
            if (Config.ALT_GAME_SKILL_LEARN) {
                String id = command.substring(9).trim();

                if (id.length() != 0) {
                    player.setSkillLearningClassId(ClassId.getClassId(Integer.parseInt(id)));
                    showSkillList(player, ClassId.getClassId(Integer.parseInt(id)));
                } else {
                    boolean ownClass = false;

                    for (ClassId classId : classesToStudyList) {
                        if (classId.equalsOrChildOf(player.getClassId())) {
                            ownClass = true;
                            break;
                        }
                    }

                    StringBuilder text = new StringBuilder("<html><body><center>Skill learning:</center><br>");
                    if (!ownClass) {
                        String mages = player.getClassId().isMage() ? "fighters" : "mages";
                        text.append("Skills of your class are the easiest to learn.<br>Skills of another class are harder.<br>Skills for another race are even more hard to learn.<br>You can also learn skills of ").append(mages).append(", and they are the hardest to learn!<br><br>");
                    }

                    // make a list of classes
                    if (!classesToStudyList.isEmpty()) {
                        int count = 0;
                        ClassId classCheck = player.getClassId();

                        while (count == 0 && classCheck != null) {
                            for (ClassId classId : classesToStudyList) {
                                if (classId.level() != classCheck.level()) {
                                    continue;
                                }

                                if (SkillTreeTable.getInstance().getAvailableSkills(player, classId).length == 0) {
                                    continue;
                                }

                                text.append("<a action=\"bypass -h npc_%objectId%_SkillList ").append(classId.getId()).append("\">Learn ").append(classId).append("'s class Skills</a><br>\n");
                                count++;
                            }

                            classCheck = classCheck.getParent();
                        }
                    } else {
                        text.append("No Skills.<br>");
                    }

                    text.append("</body></html>");
                    insertObjectIdAndShowChatWindow(player, text.toString());
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                }
            } else {
                player.setSkillLearningClassId(player.getClassId());
                showSkillList(player, player.getClassId());
            }
        } else if (command.startsWith("EnchantSkillList")) {
            showEnchantSkillList(player, player.getClassId());
        } else {
            // this class dont know any other commands, let forward the command to the parent class
            super.onBypassFeedback(player, command);
        }
    }
}