package gui.swing_workers;

import data.constants.Constants;
import data_helpers.Converter;
import data_helpers.Namer;
import data.models.Letter;
import data.services.DataService;
import data_helpers.JDBCHelper;
import gui.FileProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileWorker extends SwingWorker<File, Integer> {

    private final Logger logger = LogManager.getLogger(Constants.DEFAULT_LOGGER);

    private JFrame frame;
    private Letter letter;
    private FileProgressBar fileProgressBar;

    public FileWorker(JFrame frame, Letter letter) {
        this.frame = frame;
        this.letter = letter;
        fileProgressBar = new FileProgressBar(frame, true);
    }

    @Override
    protected File doInBackground() throws Exception {

        try {

            byte[] letterFile;
            try (AbstractApplicationContext context = new AnnotationConfigApplicationContext(JDBCHelper.class)) {
                DataService dataService = (DataService) context.getBean("dataService");
                letterFile = dataService.getLetterFile(letter.getId());
            }

            Date creatingDate = new Date();
            String creatingYear = Converter.toYear(creatingDate);
            String creatingMonth = Namer.getMonthName(creatingDate);

            String letterDirName = "Информационное_письмо_№_" + letter.getRegNo();
            String lettersFileDirName = "Информационные письма";
            DateFormat dirDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String creatingDateDirName = creatingYear + "\\" + creatingMonth + "\\" + dirDateFormat.format(creatingDate);
            File creatingDir = new File(lettersFileDirName + "\\" + creatingDateDirName + "\\" + letterDirName);
            creatingDir.mkdirs();

            Path letterFilePath = Paths.get(lettersFileDirName, creatingDateDirName, letterDirName, letter.getLetterFileFilename());
            return Files.write(letterFilePath, letterFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).toFile();

        } catch (Exception e) {
            throw new Exception(e.getMessage(), e.getCause());
        }
    }

    public void getLetterFile() {
        execute();
        fileProgressBar.setVisible(true);
    }

    @Override
    protected void done() {
        String title = "Информационное письмо № " + letter.getRegNo();
        try {
            get();

            String successMessage = title + " получено!!!";
            logger.info(successMessage);
            JOptionPane.showMessageDialog(fileProgressBar, successMessage, title, JOptionPane.INFORMATION_MESSAGE);
            fileProgressBar.dispose();
            frame.repaint();
        } catch (Exception e) {
            e.printStackTrace();
            logger.fatal(e.getMessage(), e.getCause());
            String errorMessage = title + " не получено!!!";
            logger.info(errorMessage);
            JOptionPane.showMessageDialog(fileProgressBar, errorMessage, title, JOptionPane.ERROR_MESSAGE);

            fileProgressBar.dispose();
        }
    }
}