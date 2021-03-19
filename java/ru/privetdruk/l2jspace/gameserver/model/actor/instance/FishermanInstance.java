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

import java.util.StringTokenizer;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.gameserver.TradeController;
import ru.privetdruk.l2jspace.gameserver.datatables.SkillTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.SkillTreeTable;
import ru.privetdruk.l2jspace.gameserver.instancemanager.FishingChampionshipManager;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.SkillLearn;
import ru.privetdruk.l2jspace.gameserver.model.StoreTradeList;
import ru.privetdruk.l2jspace.gameserver.model.actor.templates.NpcTemplate;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.AcquireSkillList;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.BuyList;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SellList;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

public class FishermanInstance extends FolkInstance {
    public FishermanInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public String getHtmlPath(int npcId, int value) {
        String pom = "";
        if (value == 0) {
            pom = "" + npcId;
        } else {
            pom = npcId + "-" + value;
        }
        return "data/html/fisherman/" + pom + ".htm";
    }

    private void showBuyWindow(PlayerInstance player, int value) {
        double taxRate = 0;
        if (isInTown()) {
            taxRate = getCastle().getTaxRate();
        }
        player.tempInvetoryDisable();
        final StoreTradeList list = TradeController.getInstance().getBuyList(value);
        if ((list != null) && list.getNpcId().equals(String.valueOf(getNpcId()))) {
            player.sendPacket(new BuyList(list, player.getAdena(), taxRate));
        } else {
            LOGGER.warning("Possible client hacker: " + player.getName() + " attempting to buy from GM shop! (FishermanInstance)");
            LOGGER.warning("buylist id:" + value);
        }

        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    private void showSellWindow(PlayerInstance player) {
        player.sendPacket(new SellList(player));
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    @Override
    public void onBypassFeedback(PlayerInstance player, String command) {
        if (command.startsWith("FishSkillList")) {
            player.setSkillLearningClassId(player.getClassId());
            showSkillList(player);
        } else if (command.startsWith("FishingChampionship")) {
            if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED) {
                FishingChampionshipManager.getInstance().showChampScreen(player, getObjectId());
            } else {
                sendHtml(player, this, "no_fish_event001.htm");
            }
        } else if (command.startsWith("FishingReward")) {
            if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED) {
                if (FishingChampionshipManager.getInstance().isWinner(player.getName())) {
                    FishingChampionshipManager.getInstance().getReward(player);
                } else {
                    sendHtml(player, this, "no_fish_event_reward001.htm");
                }
            } else {
                sendHtml(player, this, "no_fish_event001.htm");
            }
        }

        final StringTokenizer st = new StringTokenizer(command, " ");
        final String command2 = st.nextToken();
        if (command2.equalsIgnoreCase("Buy")) {
            if (st.countTokens() < 1) {
                return;
            }

            final int val = Integer.parseInt(st.nextToken());
            showBuyWindow(player, val);
        } else if (command2.equalsIgnoreCase("Sell")) {
            showSellWindow(player);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public void showSkillList(PlayerInstance player) {
        final SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player);
        final AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.FISHING);
        int counts = 0;
        for (SkillLearn s : skills) {
            final Skill sk = SkillTable.getInstance().getSkill(s.getId(), s.getLevel());
            if (sk == null) {
                continue;
            }

            counts++;
            asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getSpCost(), 1);
        }

        if (counts == 0) {
            final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            final int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player);
            if (minlevel > 0) {
                // No more skills to learn, come back when you level.
                final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1);
                sm.addNumber(minlevel);
                player.sendPacket(sm);
            } else {
                final StringBuilder sb = new StringBuilder();
                sb.append("<html><head><body>");
                sb.append("You've learned all skills.<br>");
                sb.append("</body></html>");
                html.setHtml(sb.toString());
                player.sendPacket(html);
            }
        } else {
            player.sendPacket(asl);
        }

        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    private void sendHtml(PlayerInstance player, FishermanInstance npc, String htmlName) {
        final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
        html.setFile("data/html/fisherman/championship/" + htmlName);
        player.sendPacket(html);
    }
}
