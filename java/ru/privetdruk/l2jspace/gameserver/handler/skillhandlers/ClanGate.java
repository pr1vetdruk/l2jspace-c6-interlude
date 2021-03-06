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
package ru.privetdruk.l2jspace.gameserver.handler.skillhandlers;

import java.util.List;

import ru.privetdruk.l2jspace.commons.concurrent.ThreadPool;
import ru.privetdruk.l2jspace.gameserver.handler.ISkillHandler;
import ru.privetdruk.l2jspace.gameserver.instancemanager.CastleManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.GrandBossManager;
import ru.privetdruk.l2jspace.gameserver.model.Effect;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.clan.Clan;
import ru.privetdruk.l2jspace.gameserver.model.entity.siege.Castle;
import ru.privetdruk.l2jspace.gameserver.model.zone.ZoneId;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

public class ClanGate implements ISkillHandler {
    private static final Skill.SkillType[] SKILL_IDS =
            {
                    Skill.SkillType.CLAN_GATE
            };

    @Override
    public void useSkill(Creature creature, Skill skill, List<Creature> targets) {
        PlayerInstance player = null;
        if (creature instanceof PlayerInstance) {
            player = (PlayerInstance) creature;
        } else {
            return;
        }

        if (player.isInFunEvent() || player.isInsideZone(ZoneId.NO_LANDING) || player.isInOlympiadMode() || player.isInsideZone(ZoneId.PVP) || (GrandBossManager.getInstance().getZone(player) != null)) {
            player.sendMessage("Cannot open the portal here.");
            return;
        }

        final Clan clan = player.getClan();
        if ((clan != null) && (CastleManager.getInstance().getCastleByOwner(clan) != null)) {
            final Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
            if (player.isCastleLord(castle.getCastleId())) {
                // please note clan gate expires in two minutes WHATEVER happens to the clan leader.
                ThreadPool.schedule(new RemoveClanGate(castle.getCastleId(), player), skill.getTotalLifeTime());
                castle.createClanGate(player.getX(), player.getY(), player.getZ() + 20);
                player.getClan().broadcastToOnlineMembers(new SystemMessage(SystemMessageId.COURT_MAGICIAN_THE_PORTAL_HAS_BEEN_CREATED));
                player.setParalyzed(true);
            }
        }

        final Effect effect = player.getFirstEffect(skill.getId());
        if ((effect != null) && effect.isSelfEffect()) {
            effect.exit(false);
        }
        skill.getEffectsSelf(player);
    }

    private class RemoveClanGate implements Runnable {
        private final int castle;
        private final PlayerInstance player;

        protected RemoveClanGate(int castle, PlayerInstance player) {
            this.castle = castle;
            this.player = player;
        }

        @Override
        public void run() {
            if (player != null) {
                player.setParalyzed(false);
            }
            CastleManager.getInstance().getCastleById(castle).destroyClanGate();
        }
    }

    @Override
    public Skill.SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
