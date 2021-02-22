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
package ru.privetdruk.l2jspace.gameserver.ui;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.gameserver.Shutdown;
import ru.privetdruk.l2jspace.gameserver.cache.HtmCache;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.MultisellData;
import ru.privetdruk.l2jspace.gameserver.enums.ChatType;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.CreatureSay;
import ru.privetdruk.l2jspace.gameserver.util.Broadcast;
import ru.privetdruk.l2jspace.ui.AbstractGui;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Mobius
 * Modify by privetdruk
 */
public class Gui extends AbstractGui {
    private final String[] CONFIRM_OPTIONS = {"Confirm", "Cancel"};
    private final String[] ABORT_OPTIONS = {"Abort", "Cancel"};

    public Gui() {
        initialize("L2jSpace - GameServer");
    }

    @Override
    protected void initialMenuItems() {
        JMenu menuAction = createMenu("Actions");

        JMenuItem itemShutdown = createMenuItem("Shutdown", menuAction);
        addDialog(itemShutdown, "Shutdown GameServer?", "Shutdown delay in seconds", SHUTDOWN_OPTIONS);

        JMenuItem itemRestart = createMenuItem("Restart", menuAction);
        addDialog(itemRestart, "Restart GameServer?", "Restart delay in seconds", RESTART_OPTIONS);

        JMenuItem itemAbort = createMenuItem("Abort", menuAction);
        itemAbort.addActionListener(arg0 -> {
            if (isShowOptionDialog("Abort server shutdown?", ABORT_OPTIONS)) {
                Shutdown.getInstance().abort(null);
            }
        });

        JMenu menuReload = createMenu("Reload");

        JMenuItem itemConfigs = createMenuItem("Configs", menuReload);
        itemConfigs.addActionListener(arg0 -> {
            if (isShowOptionDialog("Reload configs?", CONFIRM_OPTIONS)) {
                Config.load(Config.SERVER_MODE);
            }
        });

        JMenuItem itemHtml = createMenuItem("HTML", menuReload);
        itemHtml.addActionListener(arg0 -> {
            if (isShowOptionDialog("Reload HTML files?", CONFIRM_OPTIONS)) {
                HtmCache.getInstance().reload();
            }
        });

        JMenuItem itemMultisells = createMenuItem("Multisells", menuReload);
        itemMultisells.addActionListener(arg0 -> {
            if (isShowOptionDialog("Reload multisells?", CONFIRM_OPTIONS)) {
                MultisellData.getInstance().reload();
            }
        });

        JMenu menuAnnounce = createMenu("Announce");

        JMenuItem itemNormal = createMenuItem("Normal", menuAnnounce);
        itemNormal.addActionListener(arg0 -> {
            final Object input = showInputDialog("Announce message", "");
            if (input != null) {
                final String message = ((String) input).trim();
                if (!message.isEmpty()) {
                    Broadcast.toAllOnlinePlayers(new CreatureSay(-1, ChatType.ANNOUNCEMENT, "", message));
                }
            }
        });

        JMenuItem itemCritical = createMenuItem("Critical", menuAnnounce);
        itemCritical.addActionListener(arg0 -> {
            final Object input = showInputDialog("Critical announce message", "");
            if (input != null) {
                final String message = ((String) input).trim();
                if (!message.isEmpty()) {
                    Broadcast.toAllOnlinePlayers(new CreatureSay(-1, ChatType.CRITICAL_ANNOUNCE, "", message));
                }
            }
        });

        initialDefaultMenuItems();
    }

    @Override
    protected void initializeFrame(String windowTitle) {
        JPanel systemPanel = new SystemPanel();
        JScrollPane scrollPanel = new JScrollPane(textPane);
        scrollPanel.setBounds(0, 0, 800, 550);

        JLayeredPane layeredPanel = new JLayeredPane();
        layeredPanel.add(scrollPanel, 0, 0);
        layeredPanel.add(systemPanel, 1, 0);

        jFrame = new JFrame(windowTitle);
        jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                if (JOptionPane.showOptionDialog(null, "Shutdown server immediately?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, SHUTDOWN_OPTIONS, SHUTDOWN_OPTIONS[1]) == 0) {
                    Shutdown.getInstance().startShutdown(null, 1, false);
                }
            }
        });
        jFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent ev) {
                scrollPanel.setSize(jFrame.getContentPane().getSize());
                systemPanel.setLocation(jFrame.getContentPane().getWidth() - systemPanel.getWidth() - 34, systemPanel.getY());
            }
        });

        jFrame.setJMenuBar(menuBar);
        jFrame.setIconImages(ICONS);
        jFrame.add(layeredPanel, BorderLayout.CENTER);
        jFrame.getContentPane().setPreferredSize(new Dimension(800, 550));
        jFrame.pack();
        jFrame.setLocationRelativeTo(null);
    }
}
