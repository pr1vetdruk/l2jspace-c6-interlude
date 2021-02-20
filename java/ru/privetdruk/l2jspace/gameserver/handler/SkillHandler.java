
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

import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.BalanceLife;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.BeastFeed;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Blow;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Charge;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.ClanGate;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.CombatPointHeal;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Continuous;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.CpDam;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Craft;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.DeluxeKey;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Disablers;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.DrainSoul;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Fishing;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.FishingSkill;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.GetPlayer;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Harvest;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Heal;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.ManaHeal;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Manadam;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Mdam;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Pdam;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Recall;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Resurrect;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.SiegeFlag;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Sow;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Spoil;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.StrSiegeAssault;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.SummonFriend;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.SummonTreasureKey;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Sweep;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.TakeCastle;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.Unlock;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.ZakenPlayer;
import ru.privetdruk.l2jspace.gameserver.handler.skillhandlers.ZakenSelf;
import ru.privetdruk.l2jspace.gameserver.model.Skill;

public class SkillHandler {
    private static final Logger LOGGER = Logger.getLogger(SkillHandler.class.getName());

    private final Map<Skill.SkillType, ISkillHandler> _datatable;

    private SkillHandler() {
        _datatable = new HashMap<>();
        registerSkillHandler(new BalanceLife());
        registerSkillHandler(new BeastFeed());
        registerSkillHandler(new Blow());
        registerSkillHandler(new Charge());
        registerSkillHandler(new ClanGate());
        registerSkillHandler(new CombatPointHeal());
        registerSkillHandler(new Continuous());
        registerSkillHandler(new CpDam());
        registerSkillHandler(new Craft());
        registerSkillHandler(new DeluxeKey());
        registerSkillHandler(new Disablers());
        registerSkillHandler(new DrainSoul());
        registerSkillHandler(new Fishing());
        registerSkillHandler(new FishingSkill());
        registerSkillHandler(new GetPlayer());
        registerSkillHandler(new Harvest());
        registerSkillHandler(new Heal());
        registerSkillHandler(new Manadam());
        registerSkillHandler(new ManaHeal());
        registerSkillHandler(new Mdam());
        registerSkillHandler(new Pdam());
        registerSkillHandler(new Recall());
        registerSkillHandler(new Resurrect());
        registerSkillHandler(new SiegeFlag());
        registerSkillHandler(new Sow());
        registerSkillHandler(new Spoil());
        registerSkillHandler(new StrSiegeAssault());
        registerSkillHandler(new SummonFriend());
        registerSkillHandler(new SummonTreasureKey());
        registerSkillHandler(new Sweep());
        registerSkillHandler(new TakeCastle());
        registerSkillHandler(new Unlock());
        registerSkillHandler(new ZakenPlayer());
        registerSkillHandler(new ZakenSelf());

        LOGGER.info("SkillHandler: Loaded " + _datatable.size() + " handlers.");
    }

    public void registerSkillHandler(ISkillHandler handler) {
        final Skill.SkillType[] types = handler.getSkillIds();
        for (Skill.SkillType t : types) {
            _datatable.put(t, handler);
        }
    }

    public ISkillHandler getSkillHandler(Skill.SkillType skillType) {
        return _datatable.get(skillType);
    }

    public int size() {
        return _datatable.size();
    }

    public static SkillHandler getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        protected static final SkillHandler INSTANCE = new SkillHandler();
    }
}