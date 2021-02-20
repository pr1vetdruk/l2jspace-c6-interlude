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
package quests.Q622_SpecialtyLiquorDelivery;

import ru.privetdruk.l2jspace.commons.util.Rnd;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.NpcInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.quest.Quest;
import ru.privetdruk.l2jspace.gameserver.model.quest.QuestState;
import ru.privetdruk.l2jspace.gameserver.model.quest.State;

public class Q622_SpecialtyLiquorDelivery extends Quest {
    // Items
    private static final int SPECIAL_DRINK = 7197;
    private static final int FEE_OF_SPECIAL_DRINK = 7198;

    // NPCs
    private static final int JEREMY = 31521;
    private static final int PULIN = 31543;
    private static final int NAFF = 31544;
    private static final int CROCUS = 31545;
    private static final int KUBER = 31546;
    private static final int BEOLIN = 31547;
    private static final int LIETTA = 31267;

    // Rewards
    private static final int ADENA = 57;
    private static final int HASTE_POTION = 1062;
    private static final int[] RECIPES =
            {
                    6847,
                    6849,
                    6851
            };

    public Q622_SpecialtyLiquorDelivery() {
        super(622, "Specialty Liquor Delivery");

        registerQuestItems(SPECIAL_DRINK, FEE_OF_SPECIAL_DRINK);

        addStartNpc(JEREMY);
        addTalkId(JEREMY, PULIN, NAFF, CROCUS, KUBER, BEOLIN, LIETTA);
    }

    @Override
    public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player) {
        final String htmltext = event;
        final QuestState st = player.getQuestState(getName());
        if (st == null) {
            return htmltext;
        }

        if (event.equals("31521-02.htm")) {
            st.setState(State.STARTED);
            st.set("cond", "1");
            st.playSound(QuestState.SOUND_ACCEPT);
            st.giveItems(SPECIAL_DRINK, 5);
        } else if (event.equals("31547-02.htm")) {
            st.set("cond", "2");
            st.playSound(QuestState.SOUND_MIDDLE);
            st.takeItems(SPECIAL_DRINK, 1);
            st.giveItems(FEE_OF_SPECIAL_DRINK, 1);
        } else if (event.equals("31546-02.htm")) {
            st.set("cond", "3");
            st.playSound(QuestState.SOUND_MIDDLE);
            st.takeItems(SPECIAL_DRINK, 1);
            st.giveItems(FEE_OF_SPECIAL_DRINK, 1);
        } else if (event.equals("31545-02.htm")) {
            st.set("cond", "4");
            st.playSound(QuestState.SOUND_MIDDLE);
            st.takeItems(SPECIAL_DRINK, 1);
            st.giveItems(FEE_OF_SPECIAL_DRINK, 1);
        } else if (event.equals("31544-02.htm")) {
            st.set("cond", "5");
            st.playSound(QuestState.SOUND_MIDDLE);
            st.takeItems(SPECIAL_DRINK, 1);
            st.giveItems(FEE_OF_SPECIAL_DRINK, 1);
        } else if (event.equals("31543-02.htm")) {
            st.set("cond", "6");
            st.playSound(QuestState.SOUND_MIDDLE);
            st.takeItems(SPECIAL_DRINK, 1);
            st.giveItems(FEE_OF_SPECIAL_DRINK, 1);
        } else if (event.equals("31521-06.htm")) {
            st.set("cond", "7");
            st.playSound(QuestState.SOUND_MIDDLE);
            st.takeItems(FEE_OF_SPECIAL_DRINK, 5);
        } else if (event.equals("31267-02.htm")) {
            if (Rnd.get(5) < 1) {
                st.giveItems(RECIPES[Rnd.get(RECIPES.length)], 1);
            } else {
                st.rewardItems(ADENA, 18800);
                st.rewardItems(HASTE_POTION, 1);
            }
            st.playSound(QuestState.SOUND_FINISH);
            st.exitQuest(true);
        }

        return htmltext;
    }

    @Override
    public String onTalk(NpcInstance npc, PlayerInstance player) {
        String htmltext = getNoQuestMsg();
        final QuestState st = player.getQuestState(getName());
        if (st == null) {
            return htmltext;
        }

        switch (st.getState()) {
            case State.CREATED:
                htmltext = (player.getLevel() < 68) ? "31521-03.htm" : "31521-01.htm";
                break;

            case State.STARTED:
                final int cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case JEREMY:
                        if (cond < 6) {
                            htmltext = "31521-04.htm";
                        } else if (cond == 6) {
                            htmltext = "31521-05.htm";
                        } else if (cond == 7) {
                            htmltext = "31521-06.htm";
                        }
                        break;

                    case BEOLIN:
                        if ((cond == 1) && (st.getQuestItemsCount(SPECIAL_DRINK) == 5)) {
                            htmltext = "31547-01.htm";
                        } else if (cond > 1) {
                            htmltext = "31547-03.htm";
                        }
                        break;

                    case KUBER:
                        if ((cond == 2) && (st.getQuestItemsCount(SPECIAL_DRINK) == 4)) {
                            htmltext = "31546-01.htm";
                        } else if (cond > 2) {
                            htmltext = "31546-03.htm";
                        }
                        break;

                    case CROCUS:
                        if ((cond == 3) && (st.getQuestItemsCount(SPECIAL_DRINK) == 3)) {
                            htmltext = "31545-01.htm";
                        } else if (cond > 3) {
                            htmltext = "31545-03.htm";
                        }
                        break;

                    case NAFF:
                        if ((cond == 4) && (st.getQuestItemsCount(SPECIAL_DRINK) == 2)) {
                            htmltext = "31544-01.htm";
                        } else if (cond > 4) {
                            htmltext = "31544-03.htm";
                        }
                        break;

                    case PULIN:
                        if ((cond == 5) && (st.getQuestItemsCount(SPECIAL_DRINK) == 1)) {
                            htmltext = "31543-01.htm";
                        } else if (cond > 5) {
                            htmltext = "31543-03.htm";
                        }
                        break;

                    case LIETTA:
                        if (cond == 7) {
                            htmltext = "31267-01.htm";
                        }
                        break;
                }
        }

        return htmltext;
    }
}