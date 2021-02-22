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
package ru.privetdruk.l2jspace.loginserver.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.util.LimitLinesDocumentListener;
import ru.privetdruk.l2jspace.commons.util.SplashScreen;
import ru.privetdruk.l2jspace.loginserver.GameServerTable;
import ru.privetdruk.l2jspace.loginserver.GameServerTable.GameServerInfo;
import ru.privetdruk.l2jspace.loginserver.LoginController;
import ru.privetdruk.l2jspace.loginserver.LoginServer;
import ru.privetdruk.l2jspace.loginserver.network.gameserverpackets.ServerStatus;
import ru.privetdruk.l2jspace.ui.AbstractGui;

/**
 * @author Mobius
 * Modify by privetdruk
 */
public class Gui extends AbstractGui {
    public Gui() {
        initialize("L2jSpace - LoginServer");
    }

    @Override
    protected void initialMenuItems() {
        JMenu menuAction = createMenu("Actions");

        JMenuItem itemShutdown = createMenuItem("Shutdown", menuAction);
        addDialog(itemShutdown, "Shutdown LoginServer?", false, SHUTDOWN_OPTIONS);

        JMenuItem itemRestart = createMenuItem("Restart", menuAction);
        addDialog(itemRestart, "Restart LoginServer?", true, RESTART_OPTIONS);

        JMenu menuReload = createMenu("Reload");

        JMenuItem itemBanned = createMenuItem("Banned IPs", menuReload);
        itemBanned.addActionListener(arg0 -> {
            LoginController.getInstance().getBannedIps().clear();
            Config.loadBanFile();
        });

        JMenu menuStatus = createMenu("Status");

        JCheckBoxMenuItem checkBoxEnabled = createCheckBox("Enabled", menuStatus);
        JCheckBoxMenuItem checkBoxDisabled = createCheckBox("Disabled", menuStatus);
        JCheckBoxMenuItem checkBoxGmOnly = createCheckBox("GM only", menuStatus);

        checkBoxEnabled.addActionListener(arg0 -> {
            checkBoxEnabled.setSelected(true);
            checkBoxDisabled.setSelected(false);
            checkBoxGmOnly.setSelected(false);
            LoginServer.getInstance().setStatus(ServerStatus.STATUS_NORMAL);
            for (GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values()) {
                gsi.setStatus(ServerStatus.STATUS_NORMAL);
            }
            LoginServer.getInstance().LOGGER.info("Status changed to enabled.");
        });
        checkBoxEnabled.setSelected(true);

        checkBoxDisabled.addActionListener(arg0 -> {
            checkBoxEnabled.setSelected(false);
            checkBoxDisabled.setSelected(true);
            checkBoxGmOnly.setSelected(false);
            LoginServer.getInstance().setStatus(ServerStatus.STATUS_DOWN);
            for (GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values()) {
                gsi.setStatus(ServerStatus.STATUS_DOWN);
            }
            LoginServer.getInstance().LOGGER.info("Status changed to disabled.");
        });

        checkBoxGmOnly.addActionListener(arg0 -> {
            checkBoxEnabled.setSelected(false);
            checkBoxDisabled.setSelected(false);
            checkBoxGmOnly.setSelected(true);
            LoginServer.getInstance().setStatus(ServerStatus.STATUS_GM_ONLY);
            for (GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values()) {
                gsi.setStatus(ServerStatus.STATUS_GM_ONLY);
            }
            LoginServer.getInstance().LOGGER.info("Status changed to GM only.");
        });

        initialDefaultMenuItems();
    }

    @Override
    protected void initializeFrame(String windowTitle) {
        final JScrollPane scrollPanel = new JScrollPane(textPane);
        scrollPanel.setBounds(0, 0, 800, 550);

        jFrame = new JFrame(windowTitle);
        jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                if (JOptionPane.showOptionDialog(null, "Shutdown LoginServer?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, SHUTDOWN_OPTIONS, SHUTDOWN_OPTIONS[1]) == 0) {
                    LoginServer.getInstance().shutdown(false);
                }
            }
        });
        jFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent ev) {
                scrollPanel.setSize(jFrame.getContentPane().getSize());
            }
        });
        jFrame.setJMenuBar(menuBar);
        jFrame.setIconImages(ICONS);
        jFrame.add(scrollPanel, BorderLayout.CENTER);
        jFrame.getContentPane().setPreferredSize(new Dimension(800, 550));
        jFrame.pack();
        jFrame.setLocationRelativeTo(null);
    }
}
