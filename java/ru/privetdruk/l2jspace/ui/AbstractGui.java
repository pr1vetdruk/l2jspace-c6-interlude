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

import ru.privetdruk.l2jspace.commons.util.LimitLinesDocumentListener;
import ru.privetdruk.l2jspace.commons.util.SplashScreen;
import ru.privetdruk.l2jspace.gameserver.Shutdown;
import ru.privetdruk.l2jspace.ui.FrameAbout;
import ru.privetdruk.l2jspace.gameserver.util.Util;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGui {
    protected JTextPane textPane;
    protected JMenuBar menuBar;
    protected JFrame jFrame;

    protected static final String[] SHUTDOWN_OPTIONS = {"Shutdown", "Cancel"};
    protected static final String[] RESTART_OPTIONS = {"Restart", "Cancel"};

    protected final List<Image> ICONS = new ArrayList<>();
    {
        ICONS.add(new ImageIcon(".." + File.separator + "images" + File.separator + "l2j_16x16.png").getImage());
        ICONS.add(new ImageIcon(".." + File.separator + "images" + File.separator + "l2j_32x32.png").getImage());
        ICONS.add(new ImageIcon(".." + File.separator + "images" + File.separator + "l2j_64x64.png").getImage());
        ICONS.add(new ImageIcon(".." + File.separator + "images" + File.separator + "l2j_128x128.png").getImage());
    }

    protected void initialize(String windowTitle) {
        initialConsole();
        initializeMenuBar();
        initialMenuItems();
        initializeFrame(windowTitle);
        redirectSystemStreams();
        new SplashScreen(".." + File.separator + "images" + File.separator + "splash.png", 5000, jFrame);
    }

    protected void initialConsole() {
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setDropMode(DropMode.INSERT);
        textPane.setFont(new Font("Consolas", Font.PLAIN, 13));
        textPane.setBackground(Color.BLACK);
        textPane.setForeground(Color.LIGHT_GRAY);
        textPane.getDocument().addDocumentListener(new LimitLinesDocumentListener(500));
    }

    protected void initializeMenuBar() {
        menuBar = new JMenuBar();
        menuBar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    protected void initialDefaultMenuItems() {
        JMenu menuFont = createMenu("Font");

        final String[] fonts = {"10", "11", "12", "13", "14", "15", "16", "21", "27", "33"};

        for (String font : fonts) {
            JMenuItem menuItem = createMenuItem(font, menuFont);
            menuItem.addActionListener(arg0 -> textPane.setFont(new Font("Monospaced", Font.PLAIN, Integer.parseInt(font))));
        }

        final JMenu menuHelp = createMenu("Help");

        JMenuItem itemAbout = createMenuItem("About", menuHelp);
        itemAbout.addActionListener(arg0 -> new FrameAbout());
    }

    protected abstract void initialMenuItems();

    protected abstract void initializeFrame(String windowTitle);

    protected JMenu createMenu(String name) {
        JMenu menu = new JMenu(name);

        menu.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        menuBar.add(menu);

        return menu;
    }

    protected JMenuItem createMenuItem(String name, JMenu menu) {
        JMenuItem item = new JMenuItem(name);

        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        menu.add(item);

        return item;
    }

    protected void addDialog(JMenuItem item, String message, String delayMessage, String[] options) {
        item.addActionListener(arg0 -> {
            if (isShowOptionDialog(message, options)) {
                Object answer = showInputDialog(delayMessage, "600");

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
    }

    protected Object showInputDialog(String delayMessage, String initialSelectionValue) {
        return JOptionPane.showInputDialog(
                null,
                delayMessage,
                "Input",
                JOptionPane.INFORMATION_MESSAGE,
                null,
                null,
                initialSelectionValue
        );
    }

    protected boolean isShowOptionDialog(String message, String[] options) {
        return JOptionPane.showOptionDialog(
                null,
                message,
                "Select an option",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
        ) == 0;
    }

    // Method that manages the redirect.
    protected void redirectSystemStreams() {
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

    // Set where the text is redirected. In this case, txtrConsole.
    protected void updateTextArea(String text) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument styledDocument = textPane.getStyledDocument();

            SimpleAttributeSet keyWord = new SimpleAttributeSet();


            String level = text.substring(0, 4);

            boolean isWarning = "WARN".equals(level);
            boolean isException = !"INFO".equals(level) && !isWarning;

            if (isException) {
                StyleConstants.setForeground(keyWord, Color.RED);
            } else if (isWarning) {
                StyleConstants.setForeground(keyWord, Color.YELLOW);
            }

            try {
                styledDocument.insertString(styledDocument.getLength(), text, keyWord);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
}
