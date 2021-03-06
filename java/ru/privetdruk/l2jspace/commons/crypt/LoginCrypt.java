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
package ru.privetdruk.l2jspace.commons.crypt;

import ru.privetdruk.l2jspace.commons.util.Rnd;

import java.io.IOException;

/**
 * @author KenM
 */
public class LoginCrypt {
    private static final byte[] STATIC_BLOWFISH_KEY =
            {
                    (byte) 0x6b,
                    (byte) 0x60,
                    (byte) 0xcb,
                    (byte) 0x5b,
                    (byte) 0x82,
                    (byte) 0xce,
                    (byte) 0x90,
                    (byte) 0xb1,
                    (byte) 0xcc,
                    (byte) 0x2b,
                    (byte) 0x6c,
                    (byte) 0x55,
                    (byte) 0x6c,
                    (byte) 0x6c,
                    (byte) 0x6c,
                    (byte) 0x6c
            };

    private final NewCrypt _staticCrypt = new NewCrypt(STATIC_BLOWFISH_KEY);
    private NewCrypt _crypt;
    private boolean _static = true;

    public void setKey(byte[] key) {
        _crypt = new NewCrypt(key);
    }

    public boolean decrypt(byte[] raw, int offset, int size) throws IOException {
        _crypt.decrypt(raw, offset, size);
        return NewCrypt.verifyChecksum(raw, offset, size);
    }

    public int encrypt(byte[] raw, int offset, int size) throws IOException {
        int newSize = size;
        // reserve checksum
        newSize += 4;

        if (_static) {
            // reserve for XOR "key"
            newSize += 4;

            // padding
            newSize += 8 - (newSize % 8);
            NewCrypt.encXORPass(raw, offset, newSize, Rnd.nextInt());
            _staticCrypt.crypt(raw, offset, newSize);

            _static = false;
        } else {
            // padding
            newSize += 8 - (newSize % 8);
            NewCrypt.appendChecksum(raw, offset, newSize);
            _crypt.crypt(raw, offset, newSize);
        }

        return newSize;
    }
}
