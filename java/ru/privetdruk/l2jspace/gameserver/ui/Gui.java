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
import ru.privetdruk.l2jspace.commons.util.LimitLinesDocumentListener;
import ru.privetdruk.l2jspace.commons.util.SplashScreen;
import ru.privetdruk.l2jspace.gameserver.Shutdown;
import ru.privetdruk.l2jspace.gameserver.cache.HtmCache;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.MultisellData;
import ru.privetdruk.l2jspace.gameserver.enums.ChatType;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.CreatureSay;
import ru.privetdruk.l2jspace.gameserver.util.Broadcast;
import ru.privetdruk.l2jspace.gameserver.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mobius
 */
public class Gui {
    JTextArea txtrConsole;

    static final String[] shutdownOptions =
            {
                    "Shutdown",
                    "Cancel"
            };
    static final String[] restartOptions =
            {
                    "Restart",
                    "Cancel"
            };
    static final String[] abortOptions =
            {
                    "Abort",
                    "Cancel"
            };
    static final String[] confirmOptions =
            {
                    "Confirm",
                    "Cancel"
            };

    public Gui() {
        // Initialize console.
        txtrConsole = new JTextArea();
        txtrConsole.setEditable(false);
        txtrConsole.setLineWrap(true);
        txtrConsole.setWrapStyleWord(true);
        txtrConsole.setDropMode(DropMode.INSERT);
        txtrConsole.setFont(new Font("Monospaced", Font.PLAIN, 16));
        txtrConsole.getDocument().addDocumentListener(new LimitLinesDocumentListener(500));

        // Initialize menu items.
        final JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        final JMenu mnActions = new JMenu("Actions");
        mnActions.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        menuBar.add(mnActions);

        final JMenuItem mntmShutdown = new JMenuItem("Shutdown");
        mntmShutdown.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mntmShutdown.addActionListener(arg0 ->
        {
            if (JOptionPane.showOptionDialog(null, "Shutdown GameServer?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, shutdownOptions, shutdownOptions[1]) == 0) {
                final Object answer = JOptionPane.showInputDialog(null, "Shutdown delay in seconds", "Input", JOptionPane.INFORMATION_MESSAGE, null, null, "600");
                if (answer != null) {
                    final String input = ((String) answer).trim();
                    if (Util.isDigit(input)) {
                        final int delay = Integer.parseInt(input);
                        if (delay > 0) {
                            Shutdown.getInstance().startShutdown(null, delay, false);
                        }
                    }
                }
            }
        });
        mnActions.add(mntmShutdown);

        final JMenuItem mntmRestart = new JMenuItem("Restart");
        mntmRestart.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mntmRestart.addActionListener(arg0 ->
        {
            if (JOptionPane.showOptionDialog(null, "Restart GameServer?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, restartOptions, restartOptions[1]) == 0) {
                final Object answer = JOptionPane.showInputDialog(null, "Restart delay in seconds", "Input", JOptionPane.INFORMATION_MESSAGE, null, null, "600");
                if (answer != null) {
                    final String input = ((String) answer).trim();
                    if (Util.isDigit(input)) {
                        final int delay = Integer.parseInt(input);
                        if (delay > 0) {
                            Shutdown.getInstance().startShutdown(null, delay, true);
                        }
                    }
                }
            }
        });
        mnActions.add(mntmRestart);

        final JMenuItem mntmAbort = new JMenuItem("Abort");
        mntmAbort.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mntmAbort.addActionListener(arg0 ->
        {
            if (JOptionPane.showOptionDialog(null, "Abort server shutdown?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, abortOptions, abortOptions[1]) == 0) {
                Shutdown.getInstance().abort(null);
            }
        });
        mnActions.add(mntmAbort);

        final JMenu mnReload = new JMenu("Reload");
        mnReload.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        menuBar.add(mnReload);

        final JMenuItem mntmConfigs = new JMenuItem("Configs");
        mntmConfigs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mntmConfigs.addActionListener(arg0 ->
        {
            if (JOptionPane.showOptionDialog(null, "Reload configs?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, confirmOptions, confirmOptions[1]) == 0) {
                Config.load(Config.SERVER_MODE);
            }
        });
        mnReload.add(mntmConfigs);

        final JMenuItem mntmHtml = new JMenuItem("HTML");
        mntmHtml.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mntmHtml.addActionListener(arg0 ->
        {
            if (JOptionPane.showOptionDialog(null, "Reload HTML files?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, confirmOptions, confirmOptions[1]) == 0) {
                HtmCache.getInstance().reload();
            }
        });
        mnReload.add(mntmHtml);

        final JMenuItem mntmMultisells = new JMenuItem("Multisells");
        mntmMultisells.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mntmMultisells.addActionListener(arg0 ->
        {
            if (JOptionPane.showOptionDialog(null, "Reload multisells?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, confirmOptions, confirmOptions[1]) == 0) {
                MultisellData.getInstance().reload();
            }
        });
        mnReload.add(mntmMultisells);

        final JMenu mnAnnounce = new JMenu("Announce");
        mnAnnounce.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        menuBar.add(mnAnnounce);

        final JMenuItem mntmNormal = new JMenuItem("Normal");
        mntmNormal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mntmNormal.addActionListener(arg0 ->
        {
            final Object input = JOptionPane.showInputDialog(null, "Announce message", "Input", JOptionPane.INFORMATION_MESSAGE, null, null, "");
            if (input != null) {
                final String message = ((String) input).trim();
                if (!message.isEmpty()) {
                    Broadcast.toAllOnlinePlayers(new CreatureSay(-1, ChatType.ANNOUNCEMENT, "", message));
                }
            }
        });
        mnAnnounce.add(mntmNormal);

        final JMenuItem mntmCritical = new JMenuItem("Critical");
        mntmCritical.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mntmCritical.addActionListener(arg0 ->
        {
            final Object input = JOptionPane.showInputDialog(null, "Critical announce message", "Input", JOptionPane.INFORMATION_MESSAGE, null, null, "");
            if (input != null) {
                final String message = ((String) input).trim();
                if (!message.isEmpty()) {
                    Broadcast.toAllOnlinePlayers(new CreatureSay(-1, ChatType.CRITICAL_ANNOUNCE, "", message));
                }
            }
        });
        mnAnnounce.add(mntmCritical);

        final JMenu mnFont = new JMenu("Font");
        mnFont.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        menuBar.add(mnFont);

        final String[] fonts =
                {
                        "16",
                        "21",
                        "27",
                        "33"
                };
        for (String font : fonts) {
            final JMenuItem mntmFont = new JMenuItem(font);
            mntmFont.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            mntmFont.addActionListener(arg0 -> txtrConsole.setFont(new Font("Monospaced", Font.PLAIN, Integer.parseInt(font))));
            mnFont.add(mntmFont);
        }

        final JMenu mnHelp = new JMenu("Help");
        mnHelp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        menuBar.add(mnHelp);

        final JMenuItem mntmAbout = new JMenuItem("About");
        mntmAbout.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        mntmAbout.addActionListener(arg0 -> new frmAbout());
        mnHelp.add(mntmAbout);

        // Set icons.
        final List<Image> icons = new ArrayList<>();
        icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "l2j_16x16.png").getImage());
        icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "l2j_32x32.png").getImage());
        icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "l2j_64x64.png").getImage());
        icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "l2j_128x128.png").getImage());

        // Set Panels.
        final JPanel systemPanel = new SystemPanel();
        final JScrollPane scrollPanel = new JScrollPane(txtrConsole);
        scrollPanel.setBounds(0, 0, 800, 550);
        final JLayeredPane layeredPanel = new JLayeredPane();
        layeredPanel.add(scrollPanel, 0, 0);
        layeredPanel.add(systemPanel, 1, 0);

        // Set frame.
        final JFrame frame = new JFrame("L2jSpace - GameServer");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent ev) {
                if (JOptionPane.showOptionDialog(null, "Shutdown server immediately?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, shutdownOptions, shutdownOptions[1]) == 0) {
                    Shutdown.getInstance().startShutdown(null, 1, false);
                }
            }
        });
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent ev) {
                scrollPanel.setSize(frame.getContentPane().getSize());
                systemPanel.setLocation(frame.getContentPane().getWidth() - systemPanel.getWidth() - 34, systemPanel.getY());
            }
        });
        frame.setJMenuBar(menuBar);
        frame.setIconImages(icons);
        frame.add(layeredPanel, BorderLayout.CENTER);
        frame.getContentPane().setPreferredSize(new Dimension(800, 550));
        frame.pack();
        frame.setLocationRelativeTo(null);

        // Redirect output to text area.
        redirectSystemStreams();

        // Show SplashScreen.
        new SplashScreen(".." + File.separator + "images" + File.separator + "splash.png", 5000, frame);
    }

    // Set where the text is redirected. In this case, txtrConsole.
    void updateTextArea(String text) {
        SwingUtilities.invokeLater(() ->
        {
            txtrConsole.append(text);
            txtrConsole.setCaretPosition(txtrConsole.getText().length());
        });
    }

    // Method that manages the redirect.
    private void redirectSystemStreams() {
        final OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                updateTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                updateTextArea(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }
}
