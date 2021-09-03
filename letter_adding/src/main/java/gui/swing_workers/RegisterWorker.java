package gui.swing_workers;

import data.constants.Constants;
import data.models.Letter;
import data_helpers.JDBCHelper;
import data.services.DataService;
import gui.LetterAdding;
import gui.RegisterProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.swing.*;
import java.util.List;

public class RegisterWorker extends SwingWorker<Void, Integer> {

    private final Logger logger = LogManager.getLogger(Constants.DEFAULT_LOGGER);

    private LetterAdding frame;
    private String regNo;
    private List<Letter> letterRecords;
    private RegisterProgressBar registerProgressBar;

    public RegisterWorker(LetterAdding frame, List<Letter> letterRecords){
        this.frame = frame;
        this.letterRecords = letterRecords;
        registerProgressBar = new RegisterProgressBar(frame, true);
    }

    @Override
    protected Void doInBackground() throws Exception {

        try (AbstractApplicationContext context = new AnnotationConfigApplicationContext(JDBCHelper.class)) {

            DataService dataService = (DataService) context.getBean("dataService");

            DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
            transactionDefinition.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
            transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            transactionDefinition.setTimeout(60);

            PlatformTransactionManager transactionManager = dataService.getPasportTransactionManager();
            TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
            try {

                dataService.lockLetters();
                long regNoId = dataService.getLastRegNoId() + 1L;
                regNo = dataService.getRegNoPreffix() + regNoId;

                letterRecords.forEach(l -> {
                    l.setRegNoId(regNoId);
                    l.setRegNo(regNo);
                } );

                dataService.addOrEditLetters(letterRecords);

                frame.resetFormData();
                frame.repaint();

                transactionManager.commit(transactionStatus);

            } catch (Exception e) {
                transactionManager.rollback(transactionStatus);
                throw new Exception(e.getMessage(), e.getCause());
            }
        }

        return null;
    }

    public void registerLetter() {
        execute();
        registerProgressBar.setVisible(true);
    }

    @Override
    protected void done() {
        String title = "Регистрация информационного письма";
        try {
            get();

            String successMessage = title + " № " + regNo + " произведена!!!";
            logger.info(successMessage);
            JOptionPane.showMessageDialog(registerProgressBar, successMessage, title, JOptionPane.INFORMATION_MESSAGE);
            registerProgressBar.dispose();
            frame.repaint();
        } catch (Exception e) {
            e.printStackTrace();
            logger.fatal(e.getMessage(), e.getCause());
            String errorMessage = title + " не произведена!!!";
            logger.info(errorMessage);
            JOptionPane.showMessageDialog(registerProgressBar, errorMessage, title, JOptionPane.ERROR_MESSAGE);

            registerProgressBar.dispose();
        }
    }
}


