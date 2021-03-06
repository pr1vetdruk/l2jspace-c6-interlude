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
package ru.privetdruk.l2jspace.gameserver.network.clientpackets;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.util.Rnd;
import ru.privetdruk.l2jspace.gameserver.datatables.SkillTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.SkillTreeTable;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.ExperienceData;
import ru.privetdruk.l2jspace.gameserver.model.EnchantSkillLearn;
import ru.privetdruk.l2jspace.gameserver.model.ShortCut;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.FolkInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.NpcInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ShortCutRegister;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.StatusUpdate;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.UserInfo;
import ru.privetdruk.l2jspace.gameserver.util.IllegalPlayerAction;
import ru.privetdruk.l2jspace.gameserver.util.Util;

/**
 * Format chdd c: (id) 0xD0 h: (subid) 0x06 d: skill id d: skill level
 *
 * @author -Wooden-
 */
public class RequestExEnchantSkill extends GameClientPacket {
    private int _skillId;
    private int _skillLevel;

    @Override
    protected void readImpl() {
        _skillId = readD();
        _skillLevel = readD();
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        final FolkInstance trainer = player.getLastFolkNPC();
        if (trainer == null) {
            return;
        }

        final int npcid = trainer.getNpcId();
        if (!player.isInsideRadius(trainer, NpcInstance.INTERACTION_DISTANCE, false, false) && !player.isGM()) {
            return;
        }

        if (player.getSkillLevel(_skillId) >= _skillLevel) {
            return;
        }

        if (player.getClassId().getId() < 88) {
            return;
        }

        if (player.getLevel() < 76) {
            return;
        }

        final Skill skill = SkillTable.getInstance().getSkill(_skillId, _skillLevel);
        int counts = 0;
        int requiredSp = 10000000;
        int requiredExp = 100000;
        byte rate = 0;
        int baseLevel = 1;

        final EnchantSkillLearn[] skills = SkillTreeTable.getInstance().getAvailableEnchantSkills(player);
        for (EnchantSkillLearn s : skills) {
            final Skill sk = SkillTable.getInstance().getSkill(s.getId(), s.getLevel());
            if ((sk == null) || (sk != skill) || !sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcid)) {
                continue;
            }

            counts++;
            requiredSp = s.getSpCost();
            requiredExp = s.getExp();
            rate = s.getRate(player);
            baseLevel = s.getBaseLevel();
        }

        if ((counts == 0) && !Config.ALT_GAME_SKILL_LEARN) {
            player.sendMessage("You are trying to learn skill that u can't..");
            Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
            return;
        }

        if (player.getSp() >= requiredSp) {
            // Like L2OFF you can't delevel during skill enchant
            final long expAfter = player.getExp() - requiredExp;
            if ((player.getExp() >= requiredExp) && (expAfter >= ExperienceData.getInstance().getExpForLevel(player.getLevel()))) {
                if (Config.ES_SP_BOOK_NEEDED && ((_skillLevel == 101) || (_skillLevel == 141))) // only first level requires book
                {
                    final int spbId = 6622;
                    final ItemInstance spb = player.getInventory().getItemByItemId(spbId);
                    if (spb == null)// Haven't spellbook
                    {
                        player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
                        return;
                    }
                    // ok
                    player.destroyItem("Consume", spb, trainer, true);
                }
            } else {
                player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_EXPERIENCE_EXP_TO_ENCHANT_THAT_SKILL));
                return;
            }
        } else {
            player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL));
            return;
        }
        if (Rnd.get(100) <= rate) {
            player.addSkill(skill, true);
            player.getStat().removeExpAndSp(requiredExp, requiredSp);

            final StatusUpdate su = new StatusUpdate(player.getObjectId());
            su.addAttribute(StatusUpdate.SP, player.getSp());
            player.sendPacket(su);

            final SystemMessage ep = new SystemMessage(SystemMessageId.YOUR_EXPERIENCE_HAS_DECREASED_BY_S1);
            ep.addNumber(requiredExp);
            sendPacket(ep);

            final SystemMessage sp = new SystemMessage(SystemMessageId.YOUR_SP_HAS_DECREASED_BY_S1);
            sp.addNumber(requiredSp);
            sendPacket(sp);

            final SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_ENCHANT_WAS_SUCCESSFUL_S1_HAS_BEEN_ENCHANTED);
            sm.addSkillName(_skillId);
            player.sendPacket(sm);
        } else {
            if (skill.getLevel() > 100) {
                _skillLevel = baseLevel;
                player.addSkill(SkillTable.getInstance().getSkill(_skillId, _skillLevel), true);
                player.sendSkillList();
            }
            final SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_ENCHANT_FAILED_THE_SKILL_WILL_BE_INITIALIZED);
            sm.addSkillName(_skillId);
            player.sendPacket(sm);
        }
        trainer.showEnchantSkillList(player, player.getClassId());
        player.sendPacket(new UserInfo(player));
        player.sendSkillList();

        // update all the shortcuts to this skill
        final ShortCut[] allShortCuts = player.getAllShortCuts();
        for (ShortCut sc : allShortCuts) {
            if ((sc.getId() == _skillId) && (sc.getType() == ShortCut.TYPE_SKILL)) {
                final ShortCut newsc = new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _skillLevel);
                player.sendPacket(new ShortCutRegister(newsc));
                player.registerShortCut(newsc);
            }
        }
    }
}
