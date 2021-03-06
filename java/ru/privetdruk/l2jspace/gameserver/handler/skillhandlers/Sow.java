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
import java.util.logging.Logger;

import ru.privetdruk.l2jspace.commons.util.Rnd;
import ru.privetdruk.l2jspace.gameserver.ai.CtrlIntention;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.ManorSeedData;
import ru.privetdruk.l2jspace.gameserver.handler.ISkillHandler;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.MonsterInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.PlaySound;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

/**
 * @author l3x
 */
public class Sow implements ISkillHandler {
    protected static final Logger LOGGER = Logger.getLogger(Sow.class.getName());
    private static final Skill.SkillType[] SKILL_IDS =
            {
                    Skill.SkillType.SOW
            };

    private PlayerInstance _player;
    private MonsterInstance _target;
    private int _seedId;

    @Override
    public void useSkill(Creature creature, Skill skill, List<Creature> targets) {
        if (!(creature instanceof PlayerInstance)) {
            return;
        }

        _player = (PlayerInstance) creature;

        final List<Creature> targetList = skill.getTargetList(creature);
        if (targetList == null) {
            return;
        }

        for (@SuppressWarnings("unused")
                Creature element : targetList) {
            if (!(targetList.get(0) instanceof MonsterInstance)) {
                continue;
            }

            _target = (MonsterInstance) targetList.get(0);
            if (_target.isSeeded()) {
                _player.sendPacket(ActionFailed.STATIC_PACKET);
                continue;
            }

            if (_target.isDead()) {
                _player.sendPacket(ActionFailed.STATIC_PACKET);
                continue;
            }

            if (_target.getSeeder() != _player) {
                _player.sendPacket(ActionFailed.STATIC_PACKET);
                continue;
            }

            _seedId = _target.getSeedType();
            if (_seedId == 0) {
                _player.sendPacket(ActionFailed.STATIC_PACKET);
                continue;
            }

            final ItemInstance item = _player.getInventory().getItemByItemId(_seedId);
            if (item == null) {
                _player.sendPacket(ActionFailed.STATIC_PACKET);
                break;
            }

            // Consuming used seed
            _player.destroyItem("Consume", item.getObjectId(), 1, null, false);
            SystemMessage sm = null;
            if (calcSuccess()) {
                _player.sendPacket(new PlaySound("Itemsound.quest_itemget"));
                _target.setSeeded();
                sm = new SystemMessage(SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN);
            } else {
                sm = new SystemMessage(SystemMessageId.THE_SEED_WAS_NOT_SOWN);
            }

            if (_player.getParty() == null) {
                _player.sendPacket(sm);
            } else {
                _player.getParty().broadcastToPartyMembers(sm);
            }

            // TODO: Mob should not aggro on player, this way doesn't work really nice
            _target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        }
    }

    private boolean calcSuccess() {
        if ((_player == null) || (_target == null)) {
            return false;
        }

        // TODO: check all the chances
        int basicSuccess = (ManorSeedData.getInstance().isAlternative(_seedId) ? 20 : 90);
        int minlevelSeed = 0;
        int maxlevelSeed = 0;
        minlevelSeed = ManorSeedData.getInstance().getSeedMinLevel(_seedId);
        maxlevelSeed = ManorSeedData.getInstance().getSeedMaxLevel(_seedId);

        final int levelPlayer = _player.getLevel(); // Attacker Level
        final int levelTarget = _target.getLevel(); // taret Level

        // 5% decrease in chance if player level
        // is more then +/- 5 levels to _seed's_ level
        if (levelTarget < minlevelSeed) {
            basicSuccess -= 5;
        }
        if (levelTarget > maxlevelSeed) {
            basicSuccess -= 5;
        }

        // 5% decrease in chance if player level
        // is more than +/- 5 levels to _target's_ level
        int diff = (levelPlayer - levelTarget);
        if (diff < 0) {
            diff = -diff;
        }

        if (diff > 5) {
            basicSuccess -= 5 * (diff - 5);
        }

        // chance can't be less than 1%
        if (basicSuccess < 1) {
            basicSuccess = 1;
        }

        final int rate = Rnd.get(99);
        return (rate < basicSuccess);
    }

    @Override
    public Skill.SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
