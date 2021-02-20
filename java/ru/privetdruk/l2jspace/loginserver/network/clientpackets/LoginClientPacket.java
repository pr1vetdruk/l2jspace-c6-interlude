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
package ru.privetdruk.l2jspace.loginserver.network.clientpackets;

import java.util.logging.Logger;

import ru.privetdruk.l2jspace.commons.mmocore.ReceivablePacket;
import ru.privetdruk.l2jspace.loginserver.LoginClient;

/**
 * @author KenM
 */
public abstract class LoginClientPacket extends ReceivablePacket<LoginClient> {
    private static final Logger LOGGER = Logger.getLogger(LoginClientPacket.class.getName());

    @Override
    protected final boolean read() {
        try {
            return readImpl();
        } catch (Exception e) {
            LOGGER.warning("ERROR READING: " + e);
            return false;
        }
    }

    protected abstract boolean readImpl();
}
