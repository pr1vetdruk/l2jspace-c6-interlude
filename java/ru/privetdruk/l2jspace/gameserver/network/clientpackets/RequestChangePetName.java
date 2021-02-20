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
package ru.privetdruk.l2jspace.gameserver.network.clientpackets;

import ru.privetdruk.l2jspace.gameserver.datatables.sql.PetNameTable;
import ru.privetdruk.l2jspace.gameserver.model.actor.Summon;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PetInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.InventoryUpdate;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.NpcInfo;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.PetInfo;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

public class RequestChangePetName extends GameClientPacket {
    private String _name;

    @Override
    protected void readImpl() {
        _name = readS();
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        final Summon pet = player.getPet();
        if (pet == null) {
            return;
        }

        if (pet.getName() != null) {
            player.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SET_THE_NAME_OF_THE_PET));
            return;
        } else if (PetNameTable.getInstance().doesPetNameExist(_name, pet.getTemplate().getNpcId())) {
            player.sendPacket(new SystemMessage(SystemMessageId.THIS_IS_ALREADY_IN_USE_BY_ANOTHER_PET));
            return;
        } else if ((_name.length() < 3) || (_name.length() > 16)) {
            player.sendMessage("Your pet's name can be up to 16 characters.");
            return;
        } else if (!PetNameTable.getInstance().isValidPetName(_name)) {
            player.sendPacket(new SystemMessage(SystemMessageId.AN_INVALID_CHARACTER_IS_INCLUDED_IN_THE_PET_S_NAME));
            return;
        }

        pet.setName(_name);
        pet.broadcastPacket(new NpcInfo(pet, player));
        player.sendPacket(new PetInfo(pet));
        // The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
        pet.updateEffectIcons(true);

        // set the flag on the control item to say that the pet has a name
        if (pet instanceof PetInstance) {
            final ItemInstance controlItem = pet.getOwner().getInventory().getItemByObjectId(pet.getControlItemId());
            if (controlItem != null) {
                controlItem.setCustomType2(1);
                controlItem.updateDatabase();
                final InventoryUpdate iu = new InventoryUpdate();
                iu.addModifiedItem(controlItem);
                player.sendPacket(iu);
            }
        }
    }
}
