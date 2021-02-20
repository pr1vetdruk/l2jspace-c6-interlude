
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
package ru.privetdruk.l2jspace.gameserver.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.BeastSoulShot;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.BeastSpice;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.BeastSpiritShot;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.BlessedSpiritShot;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.Book;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.BreakingArrow;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.CharChangePotions;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.ChestKey;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.ChristmasTree;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.CrystalCarol;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.Crystals;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.CustomPotions;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.EnchantScrolls;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.EnergyStone;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.ExtractableItems;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.Firework;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.FishShots;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.Harvester;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.HeroCustomItem;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.JackpotSeed;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.MOSKey;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.MapForestOfTheDead;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.Maps;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.MercTicket;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.MysteryPotion;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.Nectar;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.NobleCustomItem;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.PaganKeys;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.Potions;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.Recipes;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.Remedy;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.RollingDice;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.ScrollOfEscape;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.ScrollOfResurrection;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.Scrolls;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.Seed;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.SevenSignsRecord;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.SoulCrystals;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.SoulShots;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.SpecialXMas;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.SpiritShot;
import ru.privetdruk.l2jspace.gameserver.handler.itemhandlers.SummonItems;

public class ItemHandler {
    private static final Logger LOGGER = Logger.getLogger(ItemHandler.class.getName());

    private final Map<Integer, IItemHandler> _datatable;

    /**
     * Returns the number of elements contained in datatable
     *
     * @return int : Size of the datatable
     */
    public int size() {
        return _datatable.size();
    }

    private ItemHandler() {
        _datatable = new HashMap<>();
        registerItemHandler(new BeastSoulShot());
        registerItemHandler(new BeastSpice());
        registerItemHandler(new BeastSpiritShot());
        registerItemHandler(new BlessedSpiritShot());
        registerItemHandler(new Book());
        registerItemHandler(new BreakingArrow());
        registerItemHandler(new CharChangePotions());
        registerItemHandler(new ChestKey());
        registerItemHandler(new ChristmasTree());
        registerItemHandler(new CrystalCarol());
        registerItemHandler(new Crystals());
        registerItemHandler(new CustomPotions());
        registerItemHandler(new EnchantScrolls());
        registerItemHandler(new EnergyStone());
        registerItemHandler(new ExtractableItems());
        registerItemHandler(new Firework());
        registerItemHandler(new FishShots());
        registerItemHandler(new Harvester());
        registerItemHandler(new HeroCustomItem());
        registerItemHandler(new JackpotSeed());
        registerItemHandler(new MapForestOfTheDead());
        registerItemHandler(new Maps());
        registerItemHandler(new MercTicket());
        registerItemHandler(new MOSKey());
        registerItemHandler(new MysteryPotion());
        registerItemHandler(new Nectar());
        registerItemHandler(new NobleCustomItem());
        registerItemHandler(new PaganKeys());
        registerItemHandler(new Potions());
        registerItemHandler(new Recipes());
        registerItemHandler(new Remedy());
        registerItemHandler(new RollingDice());
        registerItemHandler(new ScrollOfEscape());
        registerItemHandler(new ScrollOfResurrection());
        registerItemHandler(new Scrolls());
        registerItemHandler(new Seed());
        registerItemHandler(new SevenSignsRecord());
        registerItemHandler(new SoulCrystals());
        registerItemHandler(new SoulShots());
        registerItemHandler(new SpecialXMas());
        registerItemHandler(new SpiritShot());
        registerItemHandler(new SummonItems());

        LOGGER.info("ItemHandler: Loaded " + _datatable.size() + " handlers.");
    }

    /**
     * Adds handler of item type in <i>datatable</i>.<br>
     * <b><i>Concept :</i></u><br>
     * This handler is put in <i>datatable</i> Map &lt;Integer ; IItemHandler &gt; for each ID corresponding to an item type (existing in classes of package itemhandlers) sets as key of the Map.
     *
     * @param handler (IItemHandler)
     */
    public void registerItemHandler(IItemHandler handler) {
        // Get all ID corresponding to the item type of the handler
        final int[] ids = handler.getItemIds();

        // Add handler for each ID found
        for (int id : ids) {
            _datatable.put(id, handler);
        }
    }

    /**
     * Returns the handler of the item
     *
     * @param itemId : int designating the itemID
     * @return IItemHandler
     */
    public IItemHandler getItemHandler(int itemId) {
        return _datatable.get(itemId);
    }

    public static ItemHandler getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        protected static final ItemHandler INSTANCE = new ItemHandler();
    }
}
