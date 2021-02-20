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

import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.templates.NpcTemplate;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;

public class EffectPointInstance extends NpcInstance {
    private final Creature _owner;

    public EffectPointInstance(int objectId, NpcTemplate template, Creature owner) {
        super(objectId, template);
        _owner = owner;
    }

    public Creature getOwner() {
        return _owner;
    }

    /**
     * this is called when a player interacts with this NPC
     *
     * @param player
     */
    @Override
    public void onAction(PlayerInstance player) {
        // Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    @Override
    public void onActionShift(PlayerInstance player) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }
}
