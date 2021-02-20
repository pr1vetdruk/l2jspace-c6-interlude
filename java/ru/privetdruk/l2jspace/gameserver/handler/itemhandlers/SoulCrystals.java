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

import ru.privetdruk.l2jspace.commons.concurrent.ThreadPool;
import ru.privetdruk.l2jspace.gameserver.datatables.SkillTable;
import ru.privetdruk.l2jspace.gameserver.handler.IItemHandler;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Attackable;
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.MonsterInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

public class SoulCrystals implements IItemHandler {
    // First line is for Red Soul Crystals, second is Green and third is Blue Soul Crystals, ordered by ascending level, from 0 to 13.
    private static final int[] ITEM_IDS =
            {
                    4629,
                    4630,
                    4631,
                    4632,
                    4633,
                    4634,
                    4635,
                    4636,
                    4637,
                    4638,
                    4639,
                    5577,
                    5580,
                    5908,
                    4640,
                    4641,
                    4642,
                    4643,
                    4644,
                    4645,
                    4646,
                    4647,
                    4648,
                    4649,
                    4650,
                    5578,
                    5581,
                    5911,
                    4651,
                    4652,
                    4653,
                    4654,
                    4655,
                    4656,
                    4657,
                    4658,
                    4659,
                    4660,
                    4661,
                    5579,
                    5582,
                    5914
            };

    // Our main method, where everything goes on
    @Override
    public void useItem(Playable playable, ItemInstance item) {
        if (!(playable instanceof PlayerInstance)) {
            return;
        }

        final PlayerInstance player = (PlayerInstance) playable;
        final WorldObject target = player.getTarget();
        if (!(target instanceof MonsterInstance)) {
            // Send a System Message to the caster
            player.sendPacket(new SystemMessage(SystemMessageId.INVALID_TARGET));

            // Send a Server->Client packet ActionFailed to the PlayerInstance
            player.sendPacket(ActionFailed.STATIC_PACKET);

            return;
        }

        if (player.isParalyzed()) {
            player.sendMessage("You Cannot Use This While You Are Paralyzed");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        // u can use soul crystal only when target hp goes below 50%
        if (((MonsterInstance) target).getCurrentHp() > (((MonsterInstance) target).getMaxHp() / 2.0)) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        final int crystalId = item.getItemId();

        // Soul Crystal Casting section
        final Skill skill = SkillTable.getInstance().getSkill(2096, 1);
        player.useMagic(skill, false, true);
        // End Soul Crystal Casting section

        // Continue execution later
        final CrystalFinalizer cf = new CrystalFinalizer(player, target, crystalId);
        ThreadPool.schedule(cf, skill.getHitTime());
    }

    static class CrystalFinalizer implements Runnable {
        private final PlayerInstance _player;
        private final Attackable _target;
        private final int _crystalId;

        CrystalFinalizer(PlayerInstance player, WorldObject target, int crystalId) {
            _player = player;
            _target = (Attackable) target;
            _crystalId = crystalId;
        }

        @Override
        public void run() {
            if (_player.isDead() || _target.isDead()) {
                return;
            }
            _player.enableAllSkills();
            try {
                _target.addAbsorber(_player, _crystalId);
                _player.setTarget(_target);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }
}
