package gui.elements;

import data_helpers.AppLogger;

import javax.swing.*;

public class AppMenuBar {

    public static void fillFileMenuBar(JFrame frame, JMenuBar menuBar) {
        try {

            menuBar.removeAll();
            JMenu fileMenu = new JMenu("Файл");
            JMenuItem closeMenuItem = new JMenuItem("Закрыть");
            closeMenuItem.addActionListener(ev -> System.exit(0));
            fileMenu.add(closeMenuItem);
            menuBar.add(fileMenu);

            menuBar.validate();
            menuBar.repaint();
        } catch (Exception e) {
            AppLogger.fatal(frame, e);
        }
    }
}
