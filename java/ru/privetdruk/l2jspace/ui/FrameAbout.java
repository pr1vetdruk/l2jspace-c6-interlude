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
package ru.privetdruk.l2jspace.ui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Window.Type;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

/**
 * @author Mobius
 */
public class FrameAbout {
    public FrameAbout() {
        initialize();
    }

    private void initialize() {
        JFrame jFrame = new JFrame();
        jFrame.setResizable(false);
        jFrame.setTitle("About");
        jFrame.setBounds(100, 100, 297, 197);
        jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        jFrame.setType(Type.UTILITY);
        jFrame.getContentPane().setLayout(null);

        final JLabel label = new JLabel("PrivetDruk");
        label.setFont(new Font("Tahoma", Font.PLAIN, 32));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBounds(10, 11, 271, 39);
        jFrame.getContentPane().add(label);

        final JLabel lblData = new JLabel("2021-" + Calendar.getInstance().get(Calendar.YEAR));
        lblData.setHorizontalAlignment(SwingConstants.CENTER);
        lblData.setBounds(10, 44, 271, 14);
        jFrame.getContentPane().add(lblData);

        final JLabel lblLoginServer = new JLabel("L2J Server");
        lblLoginServer.setHorizontalAlignment(SwingConstants.CENTER);
        lblLoginServer.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblLoginServer.setBounds(10, 86, 271, 23);
        jFrame.getContentPane().add(lblLoginServer);

        String url = "www.privetdruk.ru/l2j";

        final JLabel site = new JLabel(url);
        site.setText("<html><font color=\"#000099\"><u>" + url + "</u></font></html>");
        site.setHorizontalAlignment(SwingConstants.CENTER);
        site.setBounds(76, 128, 140, 14);
        site.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (IOException ignore) {
                    } catch (URISyntaxException e) {
                        throw new IllegalArgumentException(e.getMessage(), e);
                    }
                }
            }
        });
        jFrame.getContentPane().add(site);

        // Center frame to screen.
        jFrame.setLocationRelativeTo(null);

        jFrame.setVisible(true);
    }
}
