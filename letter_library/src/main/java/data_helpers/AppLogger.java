package data_helpers;

import data.constants.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;

public class AppLogger {
    private static final Logger logger = LogManager.getLogger(Constants.DEFAULT_LOGGER);

    public static void fatal(Window window, Exception e){
        e.printStackTrace();
        logger.fatal(e.getMessage(), e.getCause());
        JOptionPane.showMessageDialog(window, "Произошла критическая ошибка. Приложение закрывается.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }
}
