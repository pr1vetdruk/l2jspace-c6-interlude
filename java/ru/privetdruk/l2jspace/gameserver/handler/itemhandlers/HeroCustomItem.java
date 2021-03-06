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
package ru.privetdruk.l2jspace.gameserver.handler.itemhandlers;

import java.util.logging.Logger;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.util.Chronos;
import ru.privetdruk.l2jspace.gameserver.handler.IItemHandler;
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SocialAction;

public class HeroCustomItem implements IItemHandler {
    protected static final Logger LOGGER = Logger.getLogger(HeroCustomItem.class.getName());

    private static final int ITEM_IDS[] =
            {
                    Config.HERO_CUSTOM_ITEM_ID
            };

    @Override
    public void useItem(Playable playable, ItemInstance item) {
        if (Config.HERO_CUSTOM_ITEMS) {
            if (!(playable instanceof PlayerInstance)) {
                return;
            }

            final PlayerInstance player = (PlayerInstance) playable;
            if (player.isInOlympiadMode()) {
                player.sendMessage("This item cannot be used in olympiad mode.");
            }

            if (player.isHero()) {
                player.sendMessage("You already are a hero!");
            } else {
                player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
                player.setHero(true);
                player.sendMessage("You are now a hero, you are granted with hero status, skills and aura.");
                player.broadcastUserInfo();
                playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
                player.getInventory().addItem("CustomHeroWings", 6842, 1, player, null);

                final long heroTime = Config.HERO_CUSTOM_DAY * 24 * 60 * 60 * 1000;
                player.getVariables().set("CustomHero", true);
                player.getVariables().set("CustomHeroEnd", heroTime == 0 ? 0 : Chronos.currentTimeMillis() + heroTime);
            }
        }
    }

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }
}
