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

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.RecipeData;
import ru.privetdruk.l2jspace.gameserver.handler.IItemHandler;
import ru.privetdruk.l2jspace.gameserver.model.RecipeList;
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

public class Recipes implements IItemHandler {
    private static final int[] ITEM_IDS = RecipeData.getInstance().getAllItemIds();

    @Override
    public void useItem(Playable playable, ItemInstance item) {
        if (!(playable instanceof PlayerInstance)) {
            return;
        }

        final PlayerInstance player = (PlayerInstance) playable;
        if (!Config.IS_CRAFTING_ENABLED) {
            player.sendMessage("Crafting is disabled, you cannot register this recipe.");
            return;
        }

        final RecipeList recipe = RecipeData.getInstance().getRecipeByItemId(item.getItemId());
        if (player.hasRecipeList(recipe.getId())) {
            player.sendPacket(new SystemMessage(SystemMessageId.THAT_RECIPE_IS_ALREADY_REGISTERED));
        } else if (recipe.isDwarvenRecipe()) {
            if (player.hasDwarvenCraft()) {
                if (recipe.getLevel() > player.getDwarvenCraft()) {
                    // Cannot add recipe, because create item level too low.
                    player.sendPacket(new SystemMessage(SystemMessageId.YOUR_CREATE_ITEM_LEVEL_IS_TOO_LOW_TO_REGISTER_THIS_RECIPE));
                } else if (player.getDwarvenRecipeBook().length >= player.getDwarfRecipeLimit()) {
                    final SystemMessage sm = new SystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_BE_REGISTERED);
                    sm.addNumber(player.getDwarfRecipeLimit());
                    player.sendPacket(sm);
                } else {
                    player.registerDwarvenRecipeList(recipe);
                    player.saveRecipeIntoDB(recipe);
                    player.destroyItem("Consume", item.getObjectId(), 1, null, false);
                    final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_ADDED);
                    sm.addString(item.getItemName());
                    player.sendPacket(sm);
                }
            } else {
                player.sendPacket(new SystemMessage(SystemMessageId.THE_RECIPE_CANNOT_BE_REGISTERED_YOU_DO_NOT_HAVE_THE_ABILITY_TO_CREATE_ITEMS));
            }
        } else if (player.hasCommonCraft()) {
            if (recipe.getLevel() > player.getCommonCraft()) {
                // Cannot add recipe, because create item level too low.
                player.sendPacket(new SystemMessage(SystemMessageId.YOUR_CREATE_ITEM_LEVEL_IS_TOO_LOW_TO_REGISTER_THIS_RECIPE));
            } else if (player.getCommonRecipeBook().length >= player.getCommonRecipeLimit()) {
                final SystemMessage sm = new SystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_BE_REGISTERED);
                sm.addNumber(player.getCommonRecipeLimit());
                player.sendPacket(sm);
            } else {
                player.registerCommonRecipeList(recipe);
                player.saveRecipeIntoDB(recipe);
                player.destroyItem("Consume", item.getObjectId(), 1, null, false);
                final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_ADDED);
                sm.addString(item.getItemName());
                player.sendPacket(sm);
            }
        } else {
            player.sendPacket(new SystemMessage(SystemMessageId.THE_RECIPE_CANNOT_BE_REGISTERED_YOU_DO_NOT_HAVE_THE_ABILITY_TO_CREATE_ITEMS));
        }
    }

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }
}
