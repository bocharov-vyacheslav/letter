package gui;

import com.github.lgooddatepicker.components.DatePicker;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import data.models.*;
import data.services.DataService;
import data_helpers.AppLogger;
import data_helpers.JDBCHelper;
import data_helpers.Namer;
import gui.add_forms.EmailAdding;
import gui.add_forms.OtchAdding;
import gui.add_forms.PhoneAdding;
import gui.gui_elements.JDatePicker;
import gui.gui_elements.LabelColumn;
import gui.gui_elements.ListRenderer;
import gui.swing_workers.RegisterWorker;
import gui.table_models.EmailTableModel;
import gui.table_models.OtchTableModel;
import gui.table_models.PhoneTableModel;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class LetterAdding extends JFrame {

    private JPanel applicationPanel;

    private JButton registerButton;
    private JPanel receiptDatePanel;
    private JTextField leaderFioTextField;
    private JComboBox<String> receiptTypeComboBox;
    private JTabbedPane letterTabbedPane;
    private JPanel toolBarPanel;
    private JToolBar tableToolBar;
    private JComboBox<String> pageSizeComboBox;
    private JButton firstButton;
    private JButton previousButton;
    private JLabel currentPageLabel;
    private JLabel pagesCountLabel;
    private JButton nextButton;
    private JButton lastButton;
    private JButton deleteButton;
    private JLabel recordsCountLabel;
    private JScrollPane phoneScrollPane;
    private JScrollPane otchScrollPane;
    private JScrollPane emailScrollPane;
    private JTable otchTable;
    private JTable phoneTable;
    private JTable emailTable;
    private JComboBox<String> letterTypeComboBox;
    private JLabel letterPath;
    private JButton chooseLetterButton;
    private JTextField responsibleFioTextField;
    private JTextField responsiblePostTextField;
    private JButton addButton;
    private JButton closeButton;
    private JComboBox<String> responsiblePostPrefixComboBox;

    private File letterFile;
    private final String warning = "Предупреждение";

    private JFileChooser letterChooser;
    private DatePicker receiptDatePicker;
    private static ServerSocket serverSocket;

    private byte totalPage = 1;
    private byte pageNumber = 1;

    private int previousSelectedIndex = 0;

    private List<Okpo> okpoRecords;
    private List<Okud> okudRecords;
    private List<Okud> shortOkudRecords;

    private List<Otch> otchRecords;
    private List<Phone> phoneRecords;
    private List<Email> emailRecords;
    private PreviousOtch previousOtch;
    private String letterDir;
    private File letterDirectory;

    public static void main(String[] args) {
        new LetterAdding();
    }

    private LetterAdding() {

        try {
            serverSocket = new ServerSocket(9990, 1, InetAddress.getLocalHost());
        } catch (BindException b) {
            System.exit(0);
        } catch (Exception e) {
            AppLogger.fatal(this, e);
        }

        File homeDirectory = new File(".");

        Path homePath = homeDirectory.toPath();
        String homeParent = homeDirectory.getAbsolutePath();
        homeParent = homeParent.substring(0, homeParent.length() - 2);
        if (!Files.isReadable(homePath)) {
            String message = "Нет прав на чтение \nв директории " + homeParent + "!!!";
            JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }

        if (!Files.isWritable(homePath)) {
            String message = "Нет прав на запись \nв директории " + homeParent + "!!!";
            JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }

        if (!Files.isExecutable(homePath)) {
            String message = "Нет прав на выполнение \nв директории " + homeParent + "!!!";
            JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }

        previousOtch = new PreviousOtch();

        Font font = new Font("sans-serif", Font.PLAIN, 18);
        UIManager.put("Menu.font", font);
        UIManager.put("MenuItem.font", font);
        UIManager.put("OptionPane.messageFont", new FontUIResource(font));

        ResourceBundle b = ResourceBundle.getBundle("i18n");
        for (String s : b.keySet())
            UIManager.put(s, new String(b.getString(s).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));

        setTitle("Информационные письма (регистрация)");
        setContentPane(applicationPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        letterTabbedPane.setForegroundAt(0, Color.RED);

        //  File homeDirectory = new File(".");
        //
        //  JFileChooser dataChooser = new JFileChooser(homeDirectory);
        //  dataChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        //  dataChooser.setMultiSelectionEnabled(true);
        //  dataChooser.setPreferredSize(new Dimension(600, 600));
        //  disableTextField(dataChooser);

        otchTable.setRowHeight(32);
        otchTable.getTableHeader().setFont(font);
        otchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        phoneTable.setRowHeight(32);
        phoneTable.getTableHeader().setFont(font);
        phoneTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        emailTable.setRowHeight(32);
        emailTable.getTableHeader().setFont(font);
        emailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        receiptDatePicker = JDatePicker.getDatePicker(font);
        receiptDatePanel.add(receiptDatePicker);

        JDBCHelper.pasportSecurity.setPasportUsername("letter_user");
        JDBCHelper.pasportSecurity.setPasportPassword("!qwertyQ0");

        fillOkpoAndOkud();

        FileNameExtensionFilter letterArchiveFilter = new FileNameExtensionFilter("Архивы (*.rar; *.zip; *.7z)", "rar", "zip", "7z");
        FileNameExtensionFilter letterGraphFilter = new FileNameExtensionFilter("Графические файлы (*.png; *.jpg; *.tiff; *.gif; *.bmp)", "png", "jpg", "tiff", "gif", "bmp");
        FileNameExtensionFilter letterTxtFilter = new FileNameExtensionFilter("Текстовые файлы (*.rtf; *.txt)", "rtf", "txt");
        FileNameExtensionFilter letterExcelFilter = new FileNameExtensionFilter("Файлы Excel (*.xls; *.xlsx; *.xlsm)", "xls", "xlsx", "xlsm");
        FileNameExtensionFilter letterPdfFilter = new FileNameExtensionFilter("Файлы PDF (*.pdf)", "pdf");
        FileNameExtensionFilter letterWordFilter = new FileNameExtensionFilter("Файлы Word (*.doc; *.docx; *.docm)", "doc", "docx", "docm");

        // File desktopDirectory = new File(System.getProperty("user.home") + "\\Desktop");

        letterDirectory = new File(letterDir);
        letterChooser = new JFileChooser(letterDirectory);

        letterChooser.setPreferredSize(new Dimension(900, 650));
        letterChooser.addChoosableFileFilter(letterArchiveFilter);
        letterChooser.addChoosableFileFilter(letterGraphFilter);
        letterChooser.addChoosableFileFilter(letterTxtFilter);
        letterChooser.addChoosableFileFilter(letterExcelFilter);
        letterChooser.addChoosableFileFilter(letterPdfFilter);
        letterChooser.addChoosableFileFilter(letterWordFilter);
        letterChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        disableTextField(letterChooser);

        letterPath.setToolTipText(letterPath.getText());

        resetFormData();

        previousButton.addActionListener(e -> {
            try {
                if (pageNumber > 1) {
                    pageNumber -= 1;
                    getLetterData();
                }
            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        nextButton.addActionListener(e -> {
            try {
                if (pageNumber < totalPage) {
                    pageNumber += 1;
                    getLetterData();
                }
            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        lastButton.addActionListener(e -> {
            try {
                pageNumber = totalPage;
                getLetterData();
            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        letterTabbedPane.addChangeListener(e -> {
            try {
                resetPageNumber();
                getLetterData();
            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        firstButton.addActionListener(e -> {
            try {
                resetPageNumber();
                getLetterData();
            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        leaderFioTextField.setHighlighter(null);
        ((AbstractDocument) leaderFioTextField.getDocument()).setDocumentFilter(new DocumentFilter() {

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("^(([АЁ-яё.\\-\\s]+)|$)$") && (500 - fb.getDocument().getLength()) >= text.length())
                    super.replace(fb, offset, length, text, attrs);
            }
        });

        responsibleFioTextField.setHighlighter(null);
        ((AbstractDocument) responsibleFioTextField.getDocument()).setDocumentFilter(new DocumentFilter() {

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("^(([АЁ-яё.\\-\\s]+)|$)$") && (500 - fb.getDocument().getLength()) >= text.length())
                    super.replace(fb, offset, length, text, attrs);
            }
        });

        responsiblePostTextField.setHighlighter(null);
        ((AbstractDocument) responsiblePostTextField.getDocument()).setDocumentFilter(new DocumentFilter() {

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if ((500 - fb.getDocument().getLength()) >= text.length())
                    super.replace(fb, offset, length, text, attrs);
            }
        });

        responsiblePostPrefixComboBox.addActionListener(e -> {
            try {
                int selectedIndex = responsiblePostPrefixComboBox.getSelectedIndex();
                if (selectedIndex == 8) {
                    responsiblePostTextField.setEnabled(true);
                } else {
                    responsiblePostTextField.setText("");
                    responsiblePostTextField.setEnabled(false);
                }
            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        letterTypeComboBox.addActionListener(e -> {
            try {

                int selectedIndex = letterTypeComboBox.getSelectedIndex();
                if (!otchRecords.isEmpty()) {

                    switch (selectedIndex) {

                        case 1:

                            if (otchRecords.parallelStream().anyMatch(o -> !o.getPeriod().contains("0000"))) {
                                letterTypeComboBox.setSelectedIndex(previousSelectedIndex);
                                JOptionPane.showMessageDialog(this, "Ранее вводилась отчётность не в связи с отсутствием деятельности!!!", warning, JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                            break;
                        case 4:

                            if (otchRecords.parallelStream().anyMatch(o -> !o.getPeriod().contains("0001"))) {
                                letterTypeComboBox.setSelectedIndex(previousSelectedIndex);
                                JOptionPane.showMessageDialog(this, "Ранее вводилась отчётность не в связи с нахождением в стадии ликвидации!!!", warning, JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                            break;
                        case 5:

                            if (otchRecords.parallelStream().anyMatch(o -> !o.getPeriod().contains("0002"))) {
                                letterTypeComboBox.setSelectedIndex(previousSelectedIndex);
                                JOptionPane.showMessageDialog(this, "Ранее вводилась отчётность не в связи с произведённой ликвидацией!!!", warning, JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                            break;
                        default:

                            if (otchRecords.parallelStream().anyMatch(o -> o.getPeriod().contains("0000"))) {
                                letterTypeComboBox.setSelectedIndex(previousSelectedIndex);
                                JOptionPane.showMessageDialog(this, "Ранее вводилась отчётность в связи с отсутствием деятельности!!!", warning, JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                            if (otchRecords.parallelStream().anyMatch(o -> o.getPeriod().contains("0001"))) {
                                letterTypeComboBox.setSelectedIndex(previousSelectedIndex);
                                JOptionPane.showMessageDialog(this, "Ранее вводилась отчётность в связи с нахождением в стадии ликвидации!!!", warning, JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                            if (otchRecords.parallelStream().anyMatch(o -> o.getPeriod().contains("0002"))) {
                                letterTypeComboBox.setSelectedIndex(previousSelectedIndex);
                                JOptionPane.showMessageDialog(this, "Ранее вводилась отчётность в связи с произведённой ликвидацией!!!", warning, JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                            break;
                    }
                }

                previousSelectedIndex = selectedIndex;
                letterTypeComboBox.setSelectedIndex(selectedIndex);

            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        registerButton.addActionListener(e -> {
            try {

                LocalDate receiptLocalDate = receiptDatePicker.getDate();
                if (isNullDate(receiptLocalDate) || isMoreNow(receiptLocalDate))
                    return;

                byte receiptType = (byte) receiptTypeComboBox.getSelectedIndex();
                if (receiptType == 0) {
                    JOptionPane.showMessageDialog(this, "Способ поступления не выбран!!!", warning, JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                byte letterType = (byte) letterTypeComboBox.getSelectedIndex();
                if (letterType == 0) {
                    JOptionPane.showMessageDialog(this, "Тип информационного письма не выбран!!!", warning, JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                int responsiblePostPrefix = responsiblePostPrefixComboBox.getSelectedIndex();
                String responsiblePostText = responsiblePostTextField.getText().trim();
                if (responsiblePostPrefix == 8 && responsiblePostText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Должность ответственного не введена!!!", warning, JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String noFileMessage = "Файл не выбран";
                if (letterPath.getText().equals(noFileMessage)) {
                    JOptionPane.showMessageDialog(this, "Выберите файл!!!", warning, JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (otchRecords.size() == 0) {
                    JOptionPane.showMessageDialog(this, "Отчётность не введена!!!", warning, JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                try {
                    try (FileOutputStream fis = new FileOutputStream(letterFile, true)) {
                    }
                } catch (IOException ex) {
                    String message = "Файл информационного письма занят другим процессом!!!";
                    JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Object[] options = {"Да", "Нет"};
                int reply = JOptionPane.showOptionDialog(this, "Вы действительно хотите зарегистрировать информационное письмо?", "Регистрация информационного письма", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[1]);
                if (reply != JOptionPane.YES_OPTION)
                    return;

                long letterFileSize;
                String letterFilename;
                String letterFileExtension;
                byte[] letterFileBytes;
                try (FileInputStream fis = new FileInputStream(letterFile)) {
                    letterFileSize = letterFile.length();
                    letterFilename = letterFile.getName();
                    letterFileExtension = getFileExtension(letterFilename);

                    letterFileBytes = new byte[(int) letterFile.length()];
                    fis.read(letterFileBytes);
                }

                StringBuilder emailStr = new StringBuilder();
                for (Email email : emailRecords) {
                    emailStr.append(email.getEmail()).append(";  ");
                }

                StringBuilder phoneStr = new StringBuilder();

                for (Phone phone : phoneRecords) {
                    phoneStr.append(phone.getOsnPhone());
                    if (phone.isFax()) {
                        phoneStr.append(" факс;  ");
                    } else {
                        String dopPhone = phone.getDopPhone();
                        if (!dopPhone.isEmpty())
                            phoneStr.append(" вн. ").append(dopPhone);

                        phoneStr.append(";  ");
                    }
                }

                Calendar calendar = Calendar.getInstance();
                Date addingDate = calendar.getTime();
                short addingYear = (short) calendar.get(Calendar.YEAR);

                String username = System.getProperty("user.name");
                String phone = phoneStr.toString();
                String email = emailStr.toString();
                String leaderFio = leaderFioTextField.getText().trim();
                String responsibleFio = responsibleFioTextField.getText().trim();
                String responsiblePost = Namer.getResponsiblePostPrefixName(responsiblePostPrefix, responsiblePostText);
                Date receiptDate = JDatePicker.toDate(receiptLocalDate);

                List<Letter> letterRecords = new ArrayList<>();
                for (Otch otch : otchRecords) {

                    Letter letter = new Letter();
                    letter.setPhone(phone);
                    letter.setEmail(email);
                    letter.setAddingDate(addingDate);
                    letter.setAddingYear(addingYear);
                    letter.setLeaderFio(leaderFio);
                    letter.setResponsibleFio(responsibleFio);
                    letter.setResponsiblePost(responsiblePost);
                    letter.setLetterType(letterType);
                    letter.setUsername(username);
                    letter.setReceiptType(receiptType);
                    letter.setReceiptDate(receiptDate);

                    letter.setLetterFile(letterFileBytes);
                    letter.setLetterFileSize(letterFileSize);
                    letter.setLetterFileFilename(letterFilename);
                    letter.setLetterFileExtension(letterFileExtension);

                    String okpoTemp = otch.getOkpo();
                    letter.setOkpo(okpoTemp.substring(0, okpoTemp.indexOf('-') - 1));
                    letter.setOkud(otch.getOkud().substring(0, 7));
                    letter.setPeriodicity(otch.getPeriodicity());
                    letter.setPeriodNumber(otch.getPeriod().substring(0, 4));
                    letter.setPeriodYear(Short.parseShort(otch.getPeriodYear()));

                    letterRecords.add(letter);
                }

                RegisterWorker worker = new RegisterWorker(this, letterRecords);
                worker.registerLetter();
            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        chooseLetterButton.addActionListener(e -> {

            try {

                int result = letterChooser.showOpenDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {

                    File letter = letterChooser.getSelectedFile();
                    String dataPath = letter.getAbsolutePath();
                    if (!letter.exists()) {
                        String message = "Файла " + letter.getName() + " по пути \n" + dataPath + " не существует!!!";
                        JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    String dataDir = letter.getParent();
                    if (dataDir == null || dataDir.equals("")) {
                        String message = "Некорректный выбор файла, выберите заново!!!";
                        JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    long letterFileSize = letter.length();
                    if (letterFileSize == 0) {
                        String message = "Файл информационного письма не должен весить 0 байт!!!";
                        JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    if (letterFileSize > 22000000) {
                        String message = "Файл информационного письма должен весить не более 20 МБ!!!";
                        JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    letterFile = letter;

                    this.letterPath.setText(dataPath);
                    this.letterPath.setToolTipText(dataPath);
                }

            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }

        });

        leaderFioTextField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                leaderFioTextField.setCaretPosition(0);
            }
        });

        responsibleFioTextField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                responsibleFioTextField.setCaretPosition(0);
            }
        });

        responsiblePostTextField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                responsiblePostTextField.setCaretPosition(0);
            }
        });

        addButton.addActionListener(e -> {
            try {

                int selectedTab = letterTabbedPane.getSelectedIndex();
                switch (selectedTab) {
                    case 0:

                        byte letterType = (byte) letterTypeComboBox.getSelectedIndex();
                        if (letterType == 0) {
                            JOptionPane.showMessageDialog(this, "Тип информационного письма не выбран!!!", warning, JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }

                        DefaultListModel<Okpo> okpoModel = new DefaultListModel<>();
                        okpoRecords.forEach(okpoModel::addElement);
                        JList<Okpo> jOkpoRecords = new JList<>(okpoModel);
                        jOkpoRecords.setCellRenderer(ListRenderer.createListRenderer(font));

                        DefaultListModel<Okud> shortOkudModel = new DefaultListModel<>();
                        shortOkudRecords.forEach(shortOkudModel::addElement);
                        JList<Okud> jShortOkudRecords = new JList<>(shortOkudModel);
                        jShortOkudRecords.setCellRenderer(ListRenderer.createListRenderer(font));

                        new OtchAdding(this, font, otchRecords, okpoRecords, jOkpoRecords, okudRecords, jShortOkudRecords, letterType);

                        break;
                    case 1:

                        new PhoneAdding(this, phoneRecords);

                        break;
                    case 2:

                        new EmailAdding(this, emailRecords);

                        break;
                }
            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        deleteButton.addActionListener(e -> {
            try {

                byte id;
                int reply;
                int rowIndex;
                Object[] options = {"Да", "Нет"};
                int selectedTab = letterTabbedPane.getSelectedIndex();
                switch (selectedTab) {
                    case 0:
                        rowIndex = otchTable.getSelectedRow();
                        if (rowIndex > -1) {
                            reply = JOptionPane.showOptionDialog(this, "Вы действительно хотите удалить запись?", "Удаление записи", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                                    null, options, options[1]);
                            if (reply != JOptionPane.YES_OPTION)
                                return;

                            id = ((OtchTableModel) otchTable.getModel()).getObject(rowIndex).getId();
                            otchRecords.removeIf(o -> o.getId() == id);
                        }

                        break;
                    case 1:
                        rowIndex = phoneTable.getSelectedRow();
                        if (rowIndex > -1) {
                            reply = JOptionPane.showOptionDialog(this, "Вы действительно хотите удалить запись?", "Удаление записи", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                                    null, options, options[1]);
                            if (reply != JOptionPane.YES_OPTION)
                                return;

                            id = ((PhoneTableModel) phoneTable.getModel()).getObject(rowIndex).getId();
                            phoneRecords.removeIf(o -> o.getId() == id);
                        }

                        break;
                    case 2:
                        rowIndex = emailTable.getSelectedRow();
                        if (rowIndex > -1) {
                            reply = JOptionPane.showOptionDialog(this, "Вы действительно хотите удалить запись?", "Удаление записи", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                                    null, options, options[1]);
                            if (reply != JOptionPane.YES_OPTION)
                                return;

                            id = ((EmailTableModel) emailTable.getModel()).getObject(rowIndex).getId();
                            emailRecords.removeIf(o -> o.getId() == id);
                        }

                        break;
                }

                getLetterData();
            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        pageSizeComboBox.addActionListener(e -> getLetterData());

        closeButton.addActionListener(e -> {
            try {
                dispose();
            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        firstButton.setEnabled(false);
        lastButton.setEnabled(false);
        previousButton.setEnabled(false);
        nextButton.setEnabled(false);

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        setSize(1024, 768);
        setLocationRelativeTo(null);
        setResizable(false);

        setAlwaysOnTop(true);
        setVisible(true);

        requestFocus();
        setAlwaysOnTop(false);

        Path letterDirectoryPath = letterDirectory.toPath();
        if (!Files.isReadable(letterDirectoryPath)) {
            String message = "Нет прав на чтение \nв директории " + letterDir + "!!!";
            JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }

        if (!Files.isWritable(letterDirectoryPath)) {
            String message = "Нет прав на запись \nв директории " + letterDir + "!!!";
            JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }

        if (!Files.isExecutable(letterDirectoryPath)) {
            String message = "Нет прав на выполнение \nв директории " + letterDir + "!!!";
            JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }
    }

    private void resetPageNavigation() {
        String zero = "0";
        currentPageLabel.setText(zero);
        pagesCountLabel.setText(zero);
        recordsCountLabel.setText(zero);

        resetPageNumber();
        totalPage = 0;
    }

    private void resetPageNumber() {
        pageNumber = 1;
    }

    private void setDefaultCursor() {
        setCursor(Cursor.getDefaultCursor());
    }

    private void setWaitCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    private boolean disableTextField(Container container) {

        Component[] comps = container.getComponents();

        for (Component comp : comps) {
            if (comp instanceof JTextField) {
                comp.setEnabled(false);
                return true;
            }
            if (comp instanceof Container) {
                if (disableTextField((Container) comp))
                    return true;
            }
        }

        return false;

    }

    private boolean isNullDate(LocalDate date) {
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Введите дату поступления!!!", warning, JOptionPane.INFORMATION_MESSAGE);
            return true;
        }

        return false;
    }

    private boolean isMoreNow(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Дата поступления больше текущей даты!!!", warning, JOptionPane.INFORMATION_MESSAGE);
            return true;
        }

        return false;
    }

    private String getFileExtension(String filename) {
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1)
            return "";

        return filename.substring(lastIndexOf + 1);
    }

    public void resetFormData() {

        otchRecords = new ArrayList<>();
        phoneRecords = new ArrayList<>();
        emailRecords = new ArrayList<>();

        otchTable.setModel(new OtchTableModel(otchRecords));
        emailTable.setModel(new EmailTableModel(emailRecords));
        phoneTable.setModel(new PhoneTableModel(phoneRecords));

        deleteButton.setEnabled(false);

        receiptDatePicker.setDateToToday();
        receiptTypeComboBox.setSelectedIndex(0);
        letterTypeComboBox.setSelectedIndex(0);
        responsiblePostPrefixComboBox.setSelectedIndex(0);

        letterChooser.setSelectedFile(new File(""));
        letterChooser.setCurrentDirectory(letterDirectory);
        letterPath.setText("Файл не выбран");
        letterPath.setToolTipText(letterPath.getText());
        letterFile = null;

        leaderFioTextField.setText("");
        responsibleFioTextField.setText("");
        responsiblePostTextField.setText("");
        responsiblePostTextField.setEnabled(false);

        previousOtch = new PreviousOtch();
    }

    private void fillOkpoAndOkud() {
        try {
            try (AbstractApplicationContext context = new AnnotationConfigApplicationContext(JDBCHelper.class)) {
                DataService dataService = (DataService) context.getBean("dataService");
                okpoRecords = dataService.getLetterOkpo();
                okudRecords = dataService.getLetterOkud();
                shortOkudRecords = dataService.getLetterShortOkud();
                letterDir = dataService.getLetterDir();
            }
        } catch (Exception e) {
            AppLogger.fatal(this, e);
        }
    }

    @Override
    public void repaint() {
        getLetterData();
        super.repaint();
    }

    private long getSkippedRecordsCount(byte pageNumber, byte rowsPerPage) {
        return ((pageNumber - 1) * rowsPerPage);
    }

    private byte getTotalPage(int recordsCount, byte rowsPerPage) {
        return (byte) Math.ceil(((double) recordsCount / (double) rowsPerPage));
    }

    private void getLetterData() {
        try {

            setWaitCursor();

            int recordsCount = 0;
            byte newTotalPage;
            long skippedRecordsCount;
            int selectedTab = letterTabbedPane.getSelectedIndex();
            byte rowsPerPage = Byte.parseByte(Objects.requireNonNull(pageSizeComboBox.getSelectedItem()).toString());

            switch (selectedTab) {
                case 0:

                    if (otchRecords.isEmpty()) {
                        resetPageNumber();
                        resetPageNavigation();
                        otchTable.setModel(new OtchTableModel(otchRecords));
                        deleteButton.setEnabled(false);
                        setDefaultCursor();
                        return;
                    }

                    recordsCount = otchRecords.size();

                    newTotalPage = getTotalPage(recordsCount, rowsPerPage);
                    if (pageNumber > newTotalPage || newTotalPage > totalPage)
                        pageNumber = 1;

                    skippedRecordsCount = getSkippedRecordsCount(pageNumber, rowsPerPage);
                    totalPage = newTotalPage;

                    otchRecords = otchRecords.parallelStream().sorted(Comparator.comparing(Otch::getOkpo).thenComparing(Otch::getOkud).thenComparing(Otch::getPeriodYear).thenComparing(Otch::getPeriodicity).thenComparing(Otch::getPeriod)).collect(Collectors.toList());
                    otchTable.setModel(new OtchTableModel(otchRecords.parallelStream().skip(skippedRecordsCount).limit(rowsPerPage).collect(Collectors.toList())));

                    break;
                case 1:

                    if (phoneRecords.isEmpty()) {
                        resetPageNumber();
                        resetPageNavigation();
                        phoneTable.setModel(new PhoneTableModel(phoneRecords));
                        deleteButton.setEnabled(false);
                        setDefaultCursor();
                        return;
                    }

                    recordsCount = phoneRecords.size();

                    newTotalPage = getTotalPage(recordsCount, rowsPerPage);
                    if (pageNumber > newTotalPage || newTotalPage > totalPage)
                        pageNumber = 1;

                    skippedRecordsCount = getSkippedRecordsCount(pageNumber, rowsPerPage);
                    totalPage = newTotalPage;

                    phoneRecords = phoneRecords.parallelStream().sorted(Comparator.comparing(Phone::getOsnPhone).thenComparing(Phone::isFax).thenComparing(Phone::getDopPhone)).collect(Collectors.toList());
                    phoneTable.setModel(new PhoneTableModel(phoneRecords.parallelStream().skip(skippedRecordsCount).limit(rowsPerPage).collect(Collectors.toList())));
                    TableColumnModel phoneTableModel = phoneTable.getColumnModel();
                    phoneTableModel.getColumn(2).setCellRenderer(new LabelColumn());

                    break;
                case 2:

                    if (emailRecords.isEmpty()) {
                        resetPageNumber();
                        resetPageNavigation();
                        emailTable.setModel(new EmailTableModel(emailRecords));
                        deleteButton.setEnabled(false);
                        setDefaultCursor();
                        return;
                    }

                    recordsCount = emailRecords.size();

                    newTotalPage = getTotalPage(recordsCount, rowsPerPage);
                    if (pageNumber > newTotalPage || newTotalPage > totalPage)
                        pageNumber = 1;

                    skippedRecordsCount = getSkippedRecordsCount(pageNumber, rowsPerPage);
                    totalPage = newTotalPage;

                    emailRecords = emailRecords.parallelStream().sorted(Comparator.comparing(Email::getEmail)).collect(Collectors.toList());
                    emailTable.setModel(new EmailTableModel(emailRecords.parallelStream().skip(skippedRecordsCount).limit(rowsPerPage).collect(Collectors.toList())));

                    break;
            }

            if (pageNumber == 1) {
                firstButton.setEnabled(false);
                previousButton.setEnabled(false);
            } else {
                firstButton.setEnabled(true);
                previousButton.setEnabled(true);
            }

            if (pageNumber == totalPage) {
                nextButton.setEnabled(false);
                lastButton.setEnabled(false);
            } else {
                nextButton.setEnabled(true);
                lastButton.setEnabled(true);
            }

            if (recordsCount > 0) {
                currentPageLabel.setText(String.valueOf(pageNumber));
                pagesCountLabel.setText(String.valueOf(totalPage));
                recordsCountLabel.setText(String.valueOf(recordsCount));
            } else {
                resetPageNavigation();
            }

            deleteButton.setEnabled(true);
            setDefaultCursor();
        } catch (Exception e) {
            setDefaultCursor();
            AppLogger.fatal(this, e);
        }
    }

    @Override
    public void dispose() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            AppLogger.fatal(this, e);
        }

        super.dispose();
    }

    public PreviousOtch getPreviousOtch() {
        return previousOtch;
    }

    public void setPreviousOtch(PreviousOtch previousOtch) {
        this.previousOtch = previousOtch;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        applicationPanel = new JPanel();
        applicationPanel.setLayout(new GridLayoutManager(22, 3, new Insets(20, 20, 20, 20), -1, -1));
        applicationPanel.setEnabled(true);
        applicationPanel.setBorder(BorderFactory.createTitledBorder(""));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, -1, 20, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(2);
        label1.setText("  Информационные письма (регистрация)  ");
        applicationPanel.add(label1, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        registerButton = new JButton();
        Font registerButtonFont = this.$$$getFont$$$(null, -1, 18, registerButton.getFont());
        if (registerButtonFont != null) registerButton.setFont(registerButtonFont);
        registerButton.setText("  Зарегистрировать письмо  ");
        applicationPanel.add(registerButton, new GridConstraints(19, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        applicationPanel.add(spacer1, new GridConstraints(18, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        applicationPanel.add(spacer2, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        applicationPanel.add(spacer3, new GridConstraints(15, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, Font.BOLD, 16, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setForeground(new Color(-16777216));
        label2.setHorizontalAlignment(0);
        label2.setHorizontalTextPosition(0);
        label2.setText("  ФИО руководителя  ");
        applicationPanel.add(label2, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        Font label3Font = this.$$$getFont$$$(null, Font.BOLD, 16, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setForeground(new Color(-16777216));
        label3.setHorizontalAlignment(0);
        label3.setHorizontalTextPosition(0);
        label3.setText("  ФИО ответственного  ");
        label3.setVerticalAlignment(0);
        label3.setVerticalTextPosition(0);
        applicationPanel.add(label3, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$(null, Font.BOLD, 16, label4.getFont());
        if (label4Font != null) label4.setFont(label4Font);
        label4.setForeground(new Color(-16777216));
        label4.setHorizontalAlignment(0);
        label4.setHorizontalTextPosition(0);
        label4.setText("  Должность ответственного  ");
        applicationPanel.add(label4, new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        leaderFioTextField = new JTextField();
        Font leaderFioTextFieldFont = this.$$$getFont$$$(null, Font.PLAIN, 16, leaderFioTextField.getFont());
        if (leaderFioTextFieldFont != null) leaderFioTextField.setFont(leaderFioTextFieldFont);
        applicationPanel.add(leaderFioTextField, new GridConstraints(10, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        responsibleFioTextField = new JTextField();
        Font responsibleFioTextFieldFont = this.$$$getFont$$$(null, Font.PLAIN, 16, responsibleFioTextField.getFont());
        if (responsibleFioTextFieldFont != null) responsibleFioTextField.setFont(responsibleFioTextFieldFont);
        applicationPanel.add(responsibleFioTextField, new GridConstraints(12, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        responsiblePostTextField = new JTextField();
        responsiblePostTextField.setEnabled(false);
        Font responsiblePostTextFieldFont = this.$$$getFont$$$(null, Font.PLAIN, 16, responsiblePostTextField.getFont());
        if (responsiblePostTextFieldFont != null) responsiblePostTextField.setFont(responsiblePostTextFieldFont);
        applicationPanel.add(responsiblePostTextField, new GridConstraints(14, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        Font label5Font = this.$$$getFont$$$(null, Font.BOLD, 16, label5.getFont());
        if (label5Font != null) label5.setFont(label5Font);
        label5.setForeground(new Color(-3014656));
        label5.setHorizontalAlignment(0);
        label5.setHorizontalTextPosition(0);
        label5.setText("  Дата поступления  ");
        applicationPanel.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        receiptDatePanel = new JPanel();
        receiptDatePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        applicationPanel.add(receiptDatePanel, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        Font label6Font = this.$$$getFont$$$(null, Font.BOLD, 16, label6.getFont());
        if (label6Font != null) label6.setFont(label6Font);
        label6.setForeground(new Color(-3014656));
        label6.setHorizontalAlignment(0);
        label6.setHorizontalTextPosition(0);
        label6.setText("  Способ поступления  ");
        applicationPanel.add(label6, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        receiptTypeComboBox = new JComboBox();
        Font receiptTypeComboBoxFont = this.$$$getFont$$$(null, Font.PLAIN, 16, receiptTypeComboBox.getFont());
        if (receiptTypeComboBoxFont != null) receiptTypeComboBox.setFont(receiptTypeComboBoxFont);
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Не выбрано");
        defaultComboBoxModel1.addElement("Спецоператор связи");
        defaultComboBoxModel1.addElement("Бумажный носитель");
        defaultComboBoxModel1.addElement("Электронная почта");
        receiptTypeComboBox.setModel(defaultComboBoxModel1);
        applicationPanel.add(receiptTypeComboBox, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        letterTabbedPane = new JTabbedPane();
        Font letterTabbedPaneFont = this.$$$getFont$$$(null, Font.BOLD, 16, letterTabbedPane.getFont());
        if (letterTabbedPaneFont != null) letterTabbedPane.setFont(letterTabbedPaneFont);
        letterTabbedPane.setForeground(new Color(-16777216));
        applicationPanel.add(letterTabbedPane, new GridConstraints(16, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        otchScrollPane = new JScrollPane();
        otchScrollPane.setForeground(new Color(-16777216));
        letterTabbedPane.addTab("Отчётность", otchScrollPane);
        otchTable = new JTable();
        otchTable.setAutoResizeMode(4);
        Font otchTableFont = this.$$$getFont$$$(null, -1, 14, otchTable.getFont());
        if (otchTableFont != null) otchTable.setFont(otchTableFont);
        otchScrollPane.setViewportView(otchTable);
        phoneScrollPane = new JScrollPane();
        letterTabbedPane.addTab("Телефоны", phoneScrollPane);
        phoneTable = new JTable();
        phoneTable.setAutoResizeMode(4);
        Font phoneTableFont = this.$$$getFont$$$(null, -1, 14, phoneTable.getFont());
        if (phoneTableFont != null) phoneTable.setFont(phoneTableFont);
        phoneScrollPane.setViewportView(phoneTable);
        emailScrollPane = new JScrollPane();
        letterTabbedPane.addTab("Электронная почта", emailScrollPane);
        emailTable = new JTable();
        emailTable.setAutoResizeMode(4);
        Font emailTableFont = this.$$$getFont$$$(null, -1, 14, emailTable.getFont());
        if (emailTableFont != null) emailTable.setFont(emailTableFont);
        emailScrollPane.setViewportView(emailTable);
        toolBarPanel = new JPanel();
        toolBarPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        applicationPanel.add(toolBarPanel, new GridConstraints(17, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        tableToolBar = new JToolBar();
        tableToolBar.setBorderPainted(false);
        tableToolBar.setFloatable(false);
        tableToolBar.setForeground(new Color(-855310));
        tableToolBar.setRollover(false);
        tableToolBar.putClientProperty("JToolBar.isRollover", Boolean.FALSE);
        toolBarPanel.add(tableToolBar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        pageSizeComboBox = new JComboBox();
        pageSizeComboBox.setEditable(false);
        Font pageSizeComboBoxFont = this.$$$getFont$$$(null, -1, 14, pageSizeComboBox.getFont());
        if (pageSizeComboBoxFont != null) pageSizeComboBox.setFont(pageSizeComboBoxFont);
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("10");
        defaultComboBoxModel2.addElement("20");
        defaultComboBoxModel2.addElement("50");
        defaultComboBoxModel2.addElement("100");
        pageSizeComboBox.setModel(defaultComboBoxModel2);
        tableToolBar.add(pageSizeComboBox);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        tableToolBar.add(toolBar$Separator1);
        firstButton = new JButton();
        Font firstButtonFont = this.$$$getFont$$$(null, -1, 16, firstButton.getFont());
        if (firstButtonFont != null) firstButton.setFont(firstButtonFont);
        firstButton.setHideActionText(false);
        firstButton.setHorizontalTextPosition(0);
        firstButton.setIcon(new ImageIcon(getClass().getResource("/images/first.png")));
        firstButton.setText("");
        tableToolBar.add(firstButton);
        previousButton = new JButton();
        Font previousButtonFont = this.$$$getFont$$$(null, -1, 16, previousButton.getFont());
        if (previousButtonFont != null) previousButton.setFont(previousButtonFont);
        previousButton.setHideActionText(false);
        previousButton.setHorizontalTextPosition(0);
        previousButton.setIcon(new ImageIcon(getClass().getResource("/images/previous.png")));
        previousButton.setText("");
        tableToolBar.add(previousButton);
        final JToolBar.Separator toolBar$Separator2 = new JToolBar.Separator();
        tableToolBar.add(toolBar$Separator2);
        final JLabel label7 = new JLabel();
        Font label7Font = this.$$$getFont$$$(null, -1, 14, label7.getFont());
        if (label7Font != null) label7.setFont(label7Font);
        label7.setHorizontalAlignment(0);
        label7.setHorizontalTextPosition(0);
        label7.setText("Страница  ");
        tableToolBar.add(label7);
        currentPageLabel = new JLabel();
        Font currentPageLabelFont = this.$$$getFont$$$(null, -1, 14, currentPageLabel.getFont());
        if (currentPageLabelFont != null) currentPageLabel.setFont(currentPageLabelFont);
        currentPageLabel.setHorizontalAlignment(0);
        currentPageLabel.setHorizontalTextPosition(0);
        currentPageLabel.setText("0");
        tableToolBar.add(currentPageLabel);
        final JLabel label8 = new JLabel();
        Font label8Font = this.$$$getFont$$$(null, -1, 14, label8.getFont());
        if (label8Font != null) label8.setFont(label8Font);
        label8.setHorizontalAlignment(0);
        label8.setHorizontalTextPosition(0);
        label8.setText("  из  ");
        tableToolBar.add(label8);
        pagesCountLabel = new JLabel();
        Font pagesCountLabelFont = this.$$$getFont$$$(null, -1, 14, pagesCountLabel.getFont());
        if (pagesCountLabelFont != null) pagesCountLabel.setFont(pagesCountLabelFont);
        pagesCountLabel.setHorizontalAlignment(0);
        pagesCountLabel.setHorizontalTextPosition(0);
        pagesCountLabel.setText("0");
        tableToolBar.add(pagesCountLabel);
        final JToolBar.Separator toolBar$Separator3 = new JToolBar.Separator();
        tableToolBar.add(toolBar$Separator3);
        nextButton = new JButton();
        Font nextButtonFont = this.$$$getFont$$$(null, -1, 16, nextButton.getFont());
        if (nextButtonFont != null) nextButton.setFont(nextButtonFont);
        nextButton.setHorizontalTextPosition(0);
        nextButton.setIcon(new ImageIcon(getClass().getResource("/images/next.png")));
        nextButton.setText("");
        tableToolBar.add(nextButton);
        lastButton = new JButton();
        Font lastButtonFont = this.$$$getFont$$$(null, -1, 16, lastButton.getFont());
        if (lastButtonFont != null) lastButton.setFont(lastButtonFont);
        lastButton.setHorizontalTextPosition(0);
        lastButton.setIcon(new ImageIcon(getClass().getResource("/images/last.png")));
        lastButton.setText("");
        tableToolBar.add(lastButton);
        final JToolBar.Separator toolBar$Separator4 = new JToolBar.Separator();
        tableToolBar.add(toolBar$Separator4);
        deleteButton = new JButton();
        Font deleteButtonFont = this.$$$getFont$$$(null, -1, 16, deleteButton.getFont());
        if (deleteButtonFont != null) deleteButton.setFont(deleteButtonFont);
        deleteButton.setHideActionText(false);
        deleteButton.setHorizontalTextPosition(0);
        deleteButton.setIcon(new ImageIcon(getClass().getResource("/images/delete.png")));
        deleteButton.setText("");
        tableToolBar.add(deleteButton);
        addButton = new JButton();
        Font addButtonFont = this.$$$getFont$$$(null, -1, 16, addButton.getFont());
        if (addButtonFont != null) addButton.setFont(addButtonFont);
        addButton.setHideActionText(false);
        addButton.setHorizontalTextPosition(0);
        addButton.setIcon(new ImageIcon(getClass().getResource("/images/add.png")));
        addButton.setText("");
        tableToolBar.add(addButton);
        final JToolBar.Separator toolBar$Separator5 = new JToolBar.Separator();
        tableToolBar.add(toolBar$Separator5);
        final JLabel label9 = new JLabel();
        Font label9Font = this.$$$getFont$$$(null, -1, 14, label9.getFont());
        if (label9Font != null) label9.setFont(label9Font);
        label9.setHorizontalAlignment(0);
        label9.setHorizontalTextPosition(0);
        label9.setText("Всего записей - ");
        tableToolBar.add(label9);
        recordsCountLabel = new JLabel();
        Font recordsCountLabelFont = this.$$$getFont$$$(null, -1, 14, recordsCountLabel.getFont());
        if (recordsCountLabelFont != null) recordsCountLabel.setFont(recordsCountLabelFont);
        recordsCountLabel.setHorizontalAlignment(0);
        recordsCountLabel.setHorizontalTextPosition(0);
        recordsCountLabel.setText("0");
        tableToolBar.add(recordsCountLabel);
        final JToolBar.Separator toolBar$Separator6 = new JToolBar.Separator();
        tableToolBar.add(toolBar$Separator6);
        final JLabel label10 = new JLabel();
        Font label10Font = this.$$$getFont$$$(null, Font.BOLD, 16, label10.getFont());
        if (label10Font != null) label10.setFont(label10Font);
        label10.setForeground(new Color(-3014656));
        label10.setHorizontalAlignment(0);
        label10.setHorizontalTextPosition(0);
        label10.setText("  Тип информационного письма  ");
        applicationPanel.add(label10, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        letterTypeComboBox = new JComboBox();
        Font letterTypeComboBoxFont = this.$$$getFont$$$(null, Font.PLAIN, 16, letterTypeComboBox.getFont());
        if (letterTypeComboBoxFont != null) letterTypeComboBox.setFont(letterTypeComboBoxFont);
        final DefaultComboBoxModel defaultComboBoxModel3 = new DefaultComboBoxModel();
        defaultComboBoxModel3.addElement("Не выбрано");
        defaultComboBoxModel3.addElement("Об отсутствии деятельности");
        defaultComboBoxModel3.addElement("Об отсутствии явления");
        defaultComboBoxModel3.addElement("Нулевой отчёт");
        defaultComboBoxModel3.addElement("В стадии ликвидации");
        defaultComboBoxModel3.addElement("Произведена ликвидация");
        letterTypeComboBox.setModel(defaultComboBoxModel3);
        applicationPanel.add(letterTypeComboBox, new GridConstraints(6, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        Font label11Font = this.$$$getFont$$$(null, Font.BOLD, 16, label11.getFont());
        if (label11Font != null) label11.setFont(label11Font);
        label11.setForeground(new Color(-3014656));
        label11.setHorizontalAlignment(0);
        label11.setHorizontalTextPosition(0);
        label11.setText("  Информационное письмо  ");
        applicationPanel.add(label11, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        letterPath = new JLabel();
        Font letterPathFont = this.$$$getFont$$$(null, Font.PLAIN, 14, letterPath.getFont());
        if (letterPathFont != null) letterPath.setFont(letterPathFont);
        letterPath.setText("Файл не выбран");
        applicationPanel.add(letterPath, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(300, -1), new Dimension(300, -1), new Dimension(300, -1), 0, false));
        chooseLetterButton = new JButton();
        Font chooseLetterButtonFont = this.$$$getFont$$$(null, -1, 16, chooseLetterButton.getFont());
        if (chooseLetterButtonFont != null) chooseLetterButton.setFont(chooseLetterButtonFont);
        chooseLetterButton.setText("  Выбрать файл  ");
        applicationPanel.add(chooseLetterButton, new GridConstraints(8, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 40), null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        applicationPanel.add(spacer4, new GridConstraints(13, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        applicationPanel.add(spacer5, new GridConstraints(11, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        applicationPanel.add(spacer6, new GridConstraints(9, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        applicationPanel.add(spacer7, new GridConstraints(7, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        applicationPanel.add(spacer8, new GridConstraints(5, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        applicationPanel.add(spacer9, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        closeButton = new JButton();
        Font closeButtonFont = this.$$$getFont$$$(null, -1, 18, closeButton.getFont());
        if (closeButtonFont != null) closeButton.setFont(closeButtonFont);
        closeButton.setText("  Закрыть  ");
        applicationPanel.add(closeButton, new GridConstraints(21, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), null, null, 0, false));
        final Spacer spacer10 = new Spacer();
        applicationPanel.add(spacer10, new GridConstraints(20, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        responsiblePostPrefixComboBox = new JComboBox();
        Font responsiblePostPrefixComboBoxFont = this.$$$getFont$$$(null, Font.PLAIN, 16, responsiblePostPrefixComboBox.getFont());
        if (responsiblePostPrefixComboBoxFont != null)
            responsiblePostPrefixComboBox.setFont(responsiblePostPrefixComboBoxFont);
        final DefaultComboBoxModel defaultComboBoxModel4 = new DefaultComboBoxModel();
        defaultComboBoxModel4.addElement("Не выбрано");
        defaultComboBoxModel4.addElement("Руководитель");
        defaultComboBoxModel4.addElement("Главный бухгалтер");
        defaultComboBoxModel4.addElement("Заместитель главного бухгалтера");
        defaultComboBoxModel4.addElement("Бухгалтер");
        defaultComboBoxModel4.addElement("Исполнитель");
        defaultComboBoxModel4.addElement("Конкурсный управляющий");
        defaultComboBoxModel4.addElement("Ликвидатор");
        defaultComboBoxModel4.addElement("Иное");
        responsiblePostPrefixComboBox.setModel(defaultComboBoxModel4);
        applicationPanel.add(responsiblePostPrefixComboBox, new GridConstraints(14, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return applicationPanel;
    }

}
