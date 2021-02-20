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

import java.util.concurrent.ScheduledFuture;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.concurrent.ThreadPool;
import ru.privetdruk.l2jspace.gameserver.datatables.SkillTable;
import ru.privetdruk.l2jspace.gameserver.enums.ChatType;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.Summon;
import ru.privetdruk.l2jspace.gameserver.model.actor.templates.NpcTemplate;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.CreatureSay;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.MagicSkillUse;

/**
 * @author Ederik
 */
public class ProtectorInstance extends NpcInstance {
    private ScheduledFuture<?> _aiTask;

    private class ProtectorAI implements Runnable {
        private final ProtectorInstance _caster;

        protected ProtectorAI(ProtectorInstance caster) {
            _caster = caster;
        }

        @Override
        public void run() {
            /**
             * For each known player in range, cast sleep if pvpFlag != 0 or Karma >0 Skill use is just for buff animation
             */
            for (PlayerInstance player : getKnownList().getKnownPlayers().values()) {
                if (((player.getKarma() > 0) && Config.PROTECTOR_PLAYER_PK) || ((player.getPvpFlag() != 0) && Config.PROTECTOR_PLAYER_PVP)) {
                    handleCast(player, Config.PROTECTOR_SKILLID, Config.PROTECTOR_SKILLLEVEL);
                }
                final Summon activePet = player.getPet();
                if (activePet == null) {
                    continue;
                }

                if (((activePet.getKarma() > 0) && Config.PROTECTOR_PLAYER_PK) || ((activePet.getPvpFlag() != 0) && Config.PROTECTOR_PLAYER_PVP)) {
                    handleCastonPet(activePet, Config.PROTECTOR_SKILLID, Config.PROTECTOR_SKILLLEVEL);
                }
            }
        }

        // Cast for Player
        private boolean handleCast(PlayerInstance player, int skillId, int skillLevel) {
            if (player.isGM() || player.isDead() || !player.isSpawned() || !isInsideRadius(player, Config.PROTECTOR_RADIUS_ACTION, false, false)) {
                return false;
            }

            final Skill skill = SkillTable.getInstance().getSkill(skillId, skillLevel);
            if (player.getFirstEffect(skill) == null) {
                final int objId = _caster.getObjectId();
                skill.getEffects(_caster, player, false, false, false);
                broadcastPacket(new MagicSkillUse(_caster, player, skillId, skillLevel, Config.PROTECTOR_SKILLTIME, 0));
                broadcastPacket(new CreatureSay(objId, ChatType.GENERAL, getName(), Config.PROTECTOR_MESSAGE));
                return true;
            }

            return false;
        }

        // Cast for pet
        private boolean handleCastonPet(Summon player, int skillId, int skillLevel) {
            if (player.isDead() || !player.isSpawned() || !isInsideRadius(player, Config.PROTECTOR_RADIUS_ACTION, false, false)) {
                return false;
            }

            final Skill skill = SkillTable.getInstance().getSkill(skillId, skillLevel);
            if (player.getFirstEffect(skill) == null) {
                final int objId = _caster.getObjectId();
                skill.getEffects(_caster, player, false, false, false);
                broadcastPacket(new MagicSkillUse(_caster, player, skillId, skillLevel, Config.PROTECTOR_SKILLTIME, 0));
                broadcastPacket(new CreatureSay(objId, ChatType.GENERAL, getName(), Config.PROTECTOR_MESSAGE));
                return true;
            }

            return false;
        }
    }

    public ProtectorInstance(int objectId, NpcTemplate template) {
        super(objectId, template);

        if (_aiTask != null) {
            _aiTask.cancel(true);
        }

        _aiTask = ThreadPool.scheduleAtFixedRate(new ProtectorAI(this), 3000, 3000);
    }

    @Override
    public void deleteMe() {
        if (_aiTask != null) {
            _aiTask.cancel(true);
            _aiTask = null;
        }

        super.deleteMe();
    }

    @Override
    public boolean isAutoAttackable(Creature attacker) {
        return false;
    }
}
