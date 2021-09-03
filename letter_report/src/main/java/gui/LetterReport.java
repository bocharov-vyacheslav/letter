package gui;

import com.github.lgooddatepicker.components.DatePicker;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import data_helpers.Converter;
import data.models.Letter;
import data.models.LetterPagination;
import data.models.Okpo;
import data.models.Okud;
import data.services.DataService;
import data_helpers.AppLogger;
import data_helpers.JDBCHelper;
import data_helpers.Opener;
import data_helpers.ReportCreator;
import gui.elements.AppMenuBar;
import gui.elements.ButtonColumn;
import gui.gui_elements.JDatePicker;
import gui.gui_elements.JListFilterDecorator;
import gui.gui_elements.ListRenderer;
import gui.gui_elements.UserJPanel;
import gui.table_models.LetterTableModel;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.TableColumn;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.io.File;
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

public class LetterReport extends JFrame {
    private JPanel applicationPanel;
    private JToolBar receiptToolBar;
    private JPanel periodFromPanel;
    private JPanel periodToPanel;
    private JComboBox<String> letterTypeComboBox;
    private JToolBar periodToolBar;
    private JComboBox<Object> yearComboBox;
    private JComboBox<String> periodComboBox;
    private JPanel okpoPanel;
    private JPanel okudPanel;
    private JPanel filterPanel;
    private JButton findButton;
    private JButton createReportButton;
    private JScrollPane letterScrollPane;
    private JTable letterTable;
    private JPanel toolBarPanel;
    private JToolBar tableToolBar;
    private JComboBox<String> pageSizeComboBox;
    private JButton firstButton;
    private JButton previousButton;
    private JLabel currentPageLabel;
    private JLabel pagesCountLabel;
    private JButton nextButton;
    private JButton lastButton;
    private JButton refreshButton;
    private JLabel recordsCountLabel;
    private JComboBox<String> periodicityComboBox;
    private JToolBar otchToolBar;
    private JButton getPeriodicityButton;
    private JButton clearPeriodicityButton;
    private JTextField okatoTextField;
    private JTextField oktmoTextField;
    private JTextField okvedTextField;
    private JToolBar okatoToolBar;

    private static ServerSocket serverSocket;

    private long totalPage = 1L;
    private long pageNumber = 1L;
    private final String warning = "Предупреждение";

    private List<Okpo> okpoRecords;
    private List<Okud> okudRecords;
    private List<Okud> shortOkudRecords;

    private JTextField jOkpoTextFiled;
    private JTextField jShortOkudTextFiled;

    private DatePicker periodFromPicker;
    private DatePicker periodToPicker;

    List<Byte> noActivityTypes = Arrays.asList((byte) 1, (byte) 4, (byte) 5);

    public static void main(String[] args) {
        new LetterReport();
    }

    private LetterReport() {

        try {
            serverSocket = new ServerSocket(9991, 1, InetAddress.getLocalHost());
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

        Font font = new Font("sans-serif", Font.PLAIN, 16);
        UIManager.put("Menu.font", font);
        UIManager.put("MenuItem.font", font);
        UIManager.put("OptionPane.messageFont", new FontUIResource(font));

        ResourceBundle b = ResourceBundle.getBundle("i18n");
        for (String s : b.keySet())
            UIManager.put(s, new String(b.getString(s).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));

        setTitle("Информационные письма (анализ)");
        setContentPane(applicationPanel);
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        AppMenuBar.fillFileMenuBar(this, menuBar);

        letterTable.setRowHeight(32);
        letterTable.getTableHeader().setFont(font);
        letterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        letterTable.setModel(new LetterTableModel(this, new ArrayList<>()));

        periodFromPicker = JDatePicker.getDatePicker(font);
        periodFromPicker.clear();
        periodFromPanel.add(periodFromPicker);

        periodToPicker = JDatePicker.getDatePicker(font);
        periodToPicker.clear();
        periodToPanel.add(periodToPicker);

        List<Integer> years = new ArrayList<>();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        years.add(year + 1);
        years.add(year);

        for (int i = 1; i < 4; i++)
            years.add(year - i);

        yearComboBox.setModel(new DefaultComboBoxModel<>(years.toArray()));
        yearComboBox.setSelectedIndex(1);

        JDBCHelper.pasportSecurity.setPasportUsername("letter_user");
        JDBCHelper.pasportSecurity.setPasportPassword("!qwertyQ0");

        fillOkpoAndOkud();

        DefaultListModel<Okpo> okpoModel = new DefaultListModel<>();
        okpoRecords.forEach(okpoModel::addElement);
        JList<Okpo> jOkpoRecords = new JList<>(okpoModel);
        jOkpoRecords.setCellRenderer(ListRenderer.createListRenderer(font));

        DefaultListModel<Okud> shortOkudModel = new DefaultListModel<>();
        shortOkudRecords.forEach(shortOkudModel::addElement);
        JList<Okud> jShortOkudRecords = new JList<>(shortOkudModel);
        jShortOkudRecords.setCellRenderer(ListRenderer.createListRenderer(font));

        UserJPanel jOkpoPanel = JListFilterDecorator.decorate(font, 14, jOkpoRecords, JListFilterDecorator::okpoFilter);
        jOkpoTextFiled = jOkpoPanel.getTextField();
        okpoPanel.add(jOkpoPanel);

        UserJPanel jShortOkudPanel = JListFilterDecorator.decorate(font, 7, jShortOkudRecords, JListFilterDecorator::okudFilter);
        jShortOkudTextFiled = jShortOkudPanel.getTextField();
        okudPanel.add(jShortOkudPanel);

        createReportButton.addActionListener(e -> getReport());

        previousButton.addActionListener(e -> {
            if (pageNumber > 1L) {
                pageNumber -= 1L;
                getLetterData();
            }
        });

        nextButton.addActionListener(e -> {
            if (pageNumber < totalPage) {
                pageNumber += 1L;
                getLetterData();
            }
        });

        lastButton.addActionListener(e -> {
            pageNumber = totalPage;
            getLetterData();
        });

        refreshButton.addActionListener(e -> getLetterData());

        findButton.addActionListener(e -> getLetterData());

        firstButton.addActionListener(e -> {
            resetPageNumber();
            getLetterData();
        });

        okatoTextField.setHighlighter(null);
        ((AbstractDocument) okatoTextField.getDocument()).setDocumentFilter(new DocumentFilter() {

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("^\\d{0," + 11 + "}$") && (11 - fb.getDocument().getLength()) >= text.length())
                    super.replace(fb, offset, length, text, attrs);
            }
        });

        oktmoTextField.setHighlighter(null);
        ((AbstractDocument) oktmoTextField.getDocument()).setDocumentFilter(new DocumentFilter() {

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("^\\d{0," + 11 + "}$") && (11 - fb.getDocument().getLength()) >= text.length())
                    super.replace(fb, offset, length, text, attrs);
            }
        });

        okvedTextField.setHighlighter(null);
        ((AbstractDocument) okvedTextField.getDocument()).setDocumentFilter(new DocumentFilter() {

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("^(([0-9.]+)|$)$") && (8 - fb.getDocument().getLength()) >= text.length())
                    super.replace(fb, offset, length, text, attrs);
            }
        });

        letterTypeComboBox.addActionListener(e -> {
            try {

                int letterType = letterTypeComboBox.getSelectedIndex();
                DefaultComboBoxModel<String> periodComboBoxModel = new DefaultComboBoxModel<>();
                switch (letterType) {
                    case 1:

                        periodComboBoxModel.addElement("Отсутвие деятельности");
                        periodComboBox.setModel(periodComboBoxModel);

                        jShortOkudTextFiled.setText("0000000");
                        jShortOkudTextFiled.setEnabled(false);
                        jShortOkudRecords.setEnabled(false);
                        periodicityComboBox.setEnabled(false);
                        getPeriodicityButton.setEnabled(false);
                        clearPeriodicityButton.setEnabled(false);

                        break;
                    case 4:

                        periodComboBoxModel.addElement("В стадии ликвидации");
                        periodComboBox.setModel(periodComboBoxModel);

                        jShortOkudTextFiled.setText("0000001");
                        jShortOkudTextFiled.setEnabled(false);
                        jShortOkudRecords.setEnabled(false);
                        periodicityComboBox.setEnabled(false);
                        getPeriodicityButton.setEnabled(false);
                        clearPeriodicityButton.setEnabled(false);

                        break;
                    case 5:

                        periodComboBoxModel.addElement("Произведена ликвидация");
                        periodComboBox.setModel(periodComboBoxModel);

                        jShortOkudTextFiled.setText("0000002");
                        jShortOkudTextFiled.setEnabled(false);
                        jShortOkudRecords.setEnabled(false);
                        periodicityComboBox.setEnabled(false);
                        getPeriodicityButton.setEnabled(false);
                        clearPeriodicityButton.setEnabled(false);

                        break;
                    default:

                        periodComboBoxModel.addElement("Все");
                        periodComboBox.setModel(periodComboBoxModel);

                        jShortOkudTextFiled.setEnabled(true);
                        jShortOkudRecords.setEnabled(true);
                        periodicityComboBox.setEnabled(true);
                        getPeriodicityButton.setEnabled(true);

                        break;
                }

                jShortOkudTextFiled.setText("");
                periodComboBox.setEnabled(false);

                DefaultComboBoxModel<String> periodicityComboBoxModel = new DefaultComboBoxModel<>();
                fillPeriodicityModel(periodicityComboBoxModel, letterType);
                periodicityComboBox.setModel(periodicityComboBoxModel);

            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        periodicityComboBox.addActionListener(e -> {
            try {

                DefaultComboBoxModel<String> periodComboBoxModel = new DefaultComboBoxModel<>();
                String periodicity = Objects.requireNonNull(periodicityComboBox.getSelectedItem()).toString();
                String periodYear = Objects.requireNonNull(yearComboBox.getSelectedItem()).toString();
                fillPeriodModel(periodComboBoxModel, periodicity, periodYear);
                periodComboBox.setModel(periodComboBoxModel);
            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        yearComboBox.addActionListener(e -> {
            try {

                byte letterType = (byte) letterTypeComboBox.getSelectedIndex();
                String periodicity = Objects.requireNonNull(periodicityComboBox.getSelectedItem()).toString();
                if (!noActivityTypes.contains(letterType) && periodicity.equals("Годовая")) {
                    DefaultComboBoxModel<String> periodComboBoxModel = new DefaultComboBoxModel<>();
                    String periodYear = Objects.requireNonNull(yearComboBox.getSelectedItem()).toString();
                    fillPeriodModel(periodComboBoxModel, periodicity, periodYear);
                    periodComboBox.setModel(periodComboBoxModel);
                }
            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        getPeriodicityButton.addActionListener(e -> {
            try {

                String shortOkud = jShortOkudTextFiled.getText();
                if (shortOkud.isEmpty()) {
                    String message = "Код ОКУД не выбран!!!";
                    JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (okudRecords.parallelStream().noneMatch(o -> o.getOkud().equals(shortOkud))) {
                    String message = "Код ОКУД введён неверно!!!";
                    JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                    return;
                }

                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                DefaultComboBoxModel<String> periodicityComboBoxModel = new DefaultComboBoxModel<>();
                DefaultComboBoxModel<String> periodComboBoxModel = new DefaultComboBoxModel<>();
                List<Okud> periodicities = okudRecords.parallelStream().filter(o -> o.getOkud().equals(shortOkud)).collect(Collectors.toList());
                if (periodicities.size() == 1) {

                    Okud periodicity = periodicities.get(0);
                    String periodicityString = periodicity.getPeriodicity();
                    String yearString = Objects.requireNonNull(yearComboBox.getSelectedItem()).toString();
                    periodicityComboBoxModel.addElement(periodicityString);

                    fillPeriodModel(periodComboBoxModel, periodicityString, yearString);

                } else {

                    periodicityComboBoxModel.addElement("Все");
                    periodicities = periodicities.parallelStream().sorted(Comparator.comparingInt(Okud::getPeriodicityCode)).collect(Collectors.toList());
                    periodicities.forEach(p -> periodicityComboBoxModel.addElement(p.getPeriodicity()));

                    periodComboBoxModel.addElement("Все");

                }

                periodicityComboBox.setModel(periodicityComboBoxModel);
                periodComboBox.setModel(periodComboBoxModel);

                periodicityComboBox.setEnabled(true);
                clearPeriodicityButton.setEnabled(true);
                jShortOkudTextFiled.setEnabled(false);
                jShortOkudRecords.setEnabled(false);
                getPeriodicityButton.setEnabled(false);

                setCursor(Cursor.getDefaultCursor());

            } catch (Exception ex) {
                setCursor(Cursor.getDefaultCursor());
                AppLogger.fatal(this, ex);
            }
        });

        clearPeriodicityButton.addActionListener(e -> {
            try {

                int letterType = letterTypeComboBox.getSelectedIndex();
                DefaultComboBoxModel<String> periodicityComboBoxModel = new DefaultComboBoxModel<>();
                fillPeriodicityModel(periodicityComboBoxModel, letterType);
                periodicityComboBox.setModel(periodicityComboBoxModel);

                DefaultComboBoxModel<String> periodComboBoxModel = new DefaultComboBoxModel<>();
                periodComboBoxModel.addElement("Все");
                periodComboBox.setModel(periodComboBoxModel);

                periodComboBox.setEnabled(false);
                clearPeriodicityButton.setEnabled(false);
                jShortOkudTextFiled.setEnabled(true);
                jShortOkudRecords.setEnabled(true);
                getPeriodicityButton.setEnabled(true);

            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        periodComboBox.setEnabled(false);
        clearPeriodicityButton.setEnabled(false);

        setAlwaysOnTop(true);
        setVisible(true);

        requestFocus();
        setAlwaysOnTop(false);

    }

    private void getLetterData() {
        try {

            String okato = okatoTextField.getText();
            if (!okato.isEmpty() && !okato.matches("^\\d{5,11}$")) {
                String message = "Код ОКАТО введён неверно (минимум 5 знаков)!!!";
                JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                return;
            }

            String oktmo = oktmoTextField.getText();
            if (!oktmo.isEmpty() && !oktmo.matches("^\\d{5,11}$")) {
                String message = "Код ОКТМО введён неверно (минимум 5 знаков)!!!";
                JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                return;
            }

            String okved = okvedTextField.getText();
            if (!okved.isEmpty() && !okved.matches("^((\\d{2})|(\\d{2}.\\d{2})|(\\d{2}.\\d{2}.\\d{2}))$")) {
                String message = "Код ОКВЭД введён неверно (минимум 2 знака; например, 62.09)!!!";
                JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                return;
            }

            Okpo originOkpo;
            String okpo = jOkpoTextFiled.getText().trim();

            if (!okpo.isEmpty()) {
                originOkpo = okpoRecords.parallelStream().filter(o -> o.getOkpo().equals(okpo)).findFirst().orElse(null);
                if (originOkpo == null) {
                    String message = "Код ОКПО введён неверно!!!";
                    JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            Okud originOkud;
            String okud = jShortOkudTextFiled.getText().trim();
            byte letterType = (byte) letterTypeComboBox.getSelectedIndex();

            if (!noActivityTypes.contains(letterType) && !okud.isEmpty()) {
                originOkud = okudRecords.parallelStream().filter(o -> o.getOkud().equals(okud)).findFirst().orElse(null);
                if (originOkud == null) {
                    String message = "Код ОКУД введён неверно!!!";
                    JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            LocalDate from = periodFromPicker.getDate();
            LocalDate to = periodToPicker.getDate();

            if (isNullPeriods(from, to) || isAfterPeriodFrom(from, to))
                return;

            setWaitCursor();

            Date periodFrom = Converter.toDate(from);
            Date periodTo = Converter.toDate(to);

            String periodicity = Objects.requireNonNull(periodicityComboBox.getSelectedItem()).toString();
            int periodIndex = periodComboBox.getSelectedIndex();
            short periodNumber = getPeriodNumber(periodicity, periodIndex);

            byte periodicityCode = getPeriodicityCode(periodicity);
            short year = Short.parseShort(Objects.requireNonNull(yearComboBox.getSelectedItem()).toString());
            byte rowsPerPage = Byte.parseByte(Objects.requireNonNull(pageSizeComboBox.getSelectedItem()).toString());

            long recordsCount = 0L;
            ButtonColumn buttonColumn = new ButtonColumn();

            List<LetterPagination> letterRecords;
            try (AbstractApplicationContext context = new AnnotationConfigApplicationContext(JDBCHelper.class)) {
                DataService dataService = (DataService) context.getBean("dataService");
                letterRecords = dataService.getLetters(periodFrom, periodTo, okpo, okud, letterType, periodicityCode, year, periodNumber, rowsPerPage, pageNumber, totalPage, okato, oktmo, okved);
            }

            if (!letterRecords.isEmpty()) {
                LetterPagination pagination = letterRecords.get(0);
                recordsCount = pagination.getRecordsCount();
                pageNumber = pagination.getPageNumber();
                totalPage = pagination.getTotalPage();
            }

            letterTable.setModel(new LetterTableModel(this, letterRecords));
            setRendererAndEditor(letterTable.getColumnModel().getColumn(7), buttonColumn);

            if (pageNumber == 1L) {
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

            if (recordsCount > 0L) {
                currentPageLabel.setText(String.valueOf(pageNumber));
                pagesCountLabel.setText(String.valueOf(totalPage));
                recordsCountLabel.setText(String.valueOf(recordsCount));
            } else {
                resetPageNavigation();
            }

            setDefaultCursor();

            if (letterTable.getRowCount() == 0) {
                String message = "Нет данных!!!";
                JOptionPane.showMessageDialog(this, message, warning, JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            AppLogger.fatal(this, e);
        }
    }

    private void getReport() {
        try {

            String okato = okatoTextField.getText();
            if (!okato.isEmpty() && !okato.matches("^\\d{5,11}$")) {
                String message = "Код ОКАТО введён неверно (минимум 5 символов)!!!";
                JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                return;
            }

            String oktmo = oktmoTextField.getText();
            if (!oktmo.isEmpty() && !oktmo.matches("^\\d{5,11}$")) {
                String message = "Код ОКТМО введён неверно (минимум 5 символов)!!!";
                JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                return;
            }

            String okved = okvedTextField.getText();
            if (!oktmo.isEmpty() && !oktmo.matches("^((\\d{2})|(\\d{2}.\\d{2})|(\\d{2}.\\d{2}.\\d{2}))$")) {
                String message = "Код ОКВЭД введён неверно (минимум 2 символа; например, 62.09)!!!";
                JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                return;
            }

            Okpo originOkpo;
            String okpo = jOkpoTextFiled.getText().trim();

            if (!okpo.isEmpty()) {
                originOkpo = okpoRecords.parallelStream().filter(o -> o.getOkpo().equals(okpo)).findFirst().orElse(null);
                if (originOkpo == null) {
                    String message = "Код ОКПО введён неверно!!!";
                    JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            Okud originOkud;
            String okud = jShortOkudTextFiled.getText().trim();
            byte letterType = (byte) letterTypeComboBox.getSelectedIndex();

            if (!noActivityTypes.contains(letterType) && !okud.isEmpty()) {
                originOkud = okudRecords.parallelStream().filter(o -> o.getOkud().equals(okud)).findFirst().orElse(null);
                if (originOkud == null) {
                    String message = "Код ОКУД введён неверно!!!";
                    JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            LocalDate from = periodFromPicker.getDate();
            LocalDate to = periodToPicker.getDate();

            if (isNullPeriods(from, to) || isAfterPeriodFrom(from, to))
                return;

            setWaitCursor();

            Date periodFrom = Converter.toDate(from);
            Date periodTo = Converter.toDate(to);

            String periodicity = Objects.requireNonNull(periodicityComboBox.getSelectedItem()).toString();
            int periodIndex = periodComboBox.getSelectedIndex();
            short periodNumber = getPeriodNumber(periodicity, periodIndex);

            byte periodicityCode = getPeriodicityCode(periodicity);
            short year = Short.parseShort(Objects.requireNonNull(yearComboBox.getSelectedItem()).toString());

            List<Letter> letterRecords;
            try (AbstractApplicationContext context = new AnnotationConfigApplicationContext(JDBCHelper.class)) {
                DataService dataService = (DataService) context.getBean("dataService");
                letterRecords = dataService.getLetterReport(periodFrom, periodTo, okpo, okud, letterType, periodicityCode, year, periodNumber, okato, oktmo, okved);
            }

            if (letterRecords.isEmpty()) {
                setDefaultCursor();
                String message = "Нет данных!!!";
                JOptionPane.showMessageDialog(this, message, warning, JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            File report = ReportCreator.createLetterReport(letterRecords);

            setDefaultCursor();
            Opener.openFile(report);

        } catch (Exception ex) {
            AppLogger.fatal(this, ex);
        }
    }

    private void fillOkpoAndOkud() {
        try {
            try (AbstractApplicationContext context = new AnnotationConfigApplicationContext(JDBCHelper.class)) {
                DataService dataService = (DataService) context.getBean("dataService");
                okpoRecords = dataService.getLetterOkpo();
                okudRecords = dataService.getLetterOkud();
                shortOkudRecords = dataService.getLetterShortOkud();
            }
        } catch (Exception e) {
            AppLogger.fatal(this, e);
        }
    }

    private void setRendererAndEditor(TableColumn tableColumn, ButtonColumn buttonColumn) {
        tableColumn.setCellRenderer(buttonColumn);
        tableColumn.setCellEditor(buttonColumn);
    }

    private void setDefaultCursor() {
        setCursor(Cursor.getDefaultCursor());
    }

    private void setWaitCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    private boolean isNullPeriods(LocalDate from, LocalDate to) {
        if ((from == null && to != null) || (from != null && to == null)) {
            JOptionPane.showMessageDialog(this, "Введите период поступления!!!", warning, JOptionPane.INFORMATION_MESSAGE);
            return true;
        }

        return false;
    }

    private boolean isAfterPeriodFrom(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            JOptionPane.showMessageDialog(this, "Дата начала больше даты окончания периода поступления!!!", warning, JOptionPane.INFORMATION_MESSAGE);
            return true;
        }

        return false;
    }

    private void fillPeriodicityModel(DefaultComboBoxModel<String> comboBoxModel, int letterType) {
        switch (letterType) {
            case 0:
            case 2:
            case 3:
                comboBoxModel.addElement("Все");
                comboBoxModel.addElement("Годовая");
                comboBoxModel.addElement("Полугодовая");
                comboBoxModel.addElement("Квартальная");
                comboBoxModel.addElement("Месячная");
                break;
            case 1:
                comboBoxModel.addElement("Отсутствие деятельности");
                break;
            case 4:
                comboBoxModel.addElement("В стадии ликвидации");
                break;
            case 5:
                comboBoxModel.addElement("Произведена ликвидация");
                break;
        }
    }

    private void fillPeriodModel(DefaultComboBoxModel<String> comboBoxModel, String periodicity, String year) {
        switch (periodicity) {
            case "Все":
                comboBoxModel.addElement("Все");
                periodComboBox.setEnabled(false);
                break;
            case "Годовая":
                comboBoxModel.addElement(year + " год");
                periodComboBox.setEnabled(false);
                break;
            case "Полугодовая":
                comboBoxModel.addElement("Все");
                comboBoxModel.addElement("1 полугодие");
                comboBoxModel.addElement("2 полугодие");
                periodComboBox.setEnabled(true);
                break;
            case "Квартальная":
                comboBoxModel.addElement("Все");
                comboBoxModel.addElement("1й квартал");
                comboBoxModel.addElement("2й квартал");
                comboBoxModel.addElement("3й квартал");
                comboBoxModel.addElement("4й квартал");
                periodComboBox.setEnabled(true);
                break;
            case "Месячная":
                comboBoxModel.addElement("Все");
                comboBoxModel.addElement("Январь");
                comboBoxModel.addElement("Февраль");
                comboBoxModel.addElement("Март");
                comboBoxModel.addElement("Апрель");
                comboBoxModel.addElement("Май");
                comboBoxModel.addElement("Июнь");
                comboBoxModel.addElement("Июль");
                comboBoxModel.addElement("Август");
                comboBoxModel.addElement("Сентябрь");
                comboBoxModel.addElement("Октябрь");
                comboBoxModel.addElement("Ноябрь");
                comboBoxModel.addElement("Декабрь");
                periodComboBox.setEnabled(true);
                break;
        }
    }

    private static short getPeriodNumber(String periodicity, int periodIndex) {

        if (periodicity.equals("Годовая"))
            return 101;

        if (periodicity.equals("Полугодовая")) {
            switch (periodIndex) {
                case 1:
                    return 201;
                case 2:
                    return 202;
            }
        }

        if (periodicity.equals("Квартальная")) {
            switch (periodIndex) {
                case 1:
                    return 401;
                case 2:
                    return 402;
                case 3:
                    return 403;
                case 4:
                    return 404;
            }
        }

        if (periodicity.equals("Месячная")) {
            switch (periodIndex) {
                case 1:
                    return 1201;
                case 2:
                    return 1202;
                case 3:
                    return 1203;
                case 4:
                    return 1204;
                case 5:
                    return 1205;
                case 6:
                    return 1206;
                case 7:
                    return 1207;
                case 8:
                    return 1208;
                case 9:
                    return 1209;
                case 10:
                    return 1210;
                case 11:
                    return 1211;
                case 12:
                    return 1212;
            }
        }

        return 0;
    }

    private static byte getPeriodicityCode(String periodicity) {

        switch (periodicity) {
            case "Все":
                return 0;
            case "Годовая":
            case "В стадии ликвидации":
            case "Произведена ликвидация":
            case "Отсутствие деятельности":
                return 1;
            case "Полугодовая":
                return 4;
            case "Квартальная":
                return 2;
            case "Месячная":
                return 3;
        }

        return 100;
    }

    private void resetPageNavigation() {
        String zero = "0";
        currentPageLabel.setText(zero);
        pagesCountLabel.setText(zero);
        recordsCountLabel.setText(zero);

        resetPageNumber();
        totalPage = 0L;
    }

    private void resetPageNumber() {
        pageNumber = 1L;
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
        applicationPanel.setLayout(new BorderLayout(0, 0));
        filterPanel = new JPanel();
        filterPanel.setLayout(new GridLayoutManager(6, 2, new Insets(10, 10, 10, 10), -1, -1));
        applicationPanel.add(filterPanel, BorderLayout.NORTH);
        receiptToolBar = new JToolBar();
        receiptToolBar.setBorderPainted(false);
        receiptToolBar.setFloatable(false);
        receiptToolBar.setMargin(new Insets(0, 0, 0, 0));
        receiptToolBar.setRollover(false);
        receiptToolBar.putClientProperty("JToolBar.isRollover", Boolean.FALSE);
        filterPanel.add(receiptToolBar, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, -1, 16, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(0);
        label1.setText("Период поступления с  ");
        receiptToolBar.add(label1);
        periodFromPanel = new JPanel();
        periodFromPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        receiptToolBar.add(periodFromPanel);
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, -1, 16, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setText("  по  ");
        receiptToolBar.add(label2);
        periodToPanel = new JPanel();
        periodToPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        receiptToolBar.add(periodToPanel);
        final JLabel label3 = new JLabel();
        Font label3Font = this.$$$getFont$$$(null, -1, 16, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setHorizontalAlignment(0);
        label3.setHorizontalTextPosition(0);
        label3.setText("  Тип информационного письма  ");
        receiptToolBar.add(label3);
        letterTypeComboBox = new JComboBox();
        Font letterTypeComboBoxFont = this.$$$getFont$$$(null, Font.PLAIN, 14, letterTypeComboBox.getFont());
        if (letterTypeComboBoxFont != null) letterTypeComboBox.setFont(letterTypeComboBoxFont);
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Все");
        defaultComboBoxModel1.addElement("Об отсутствии деятельности");
        defaultComboBoxModel1.addElement("Об отсутствии явления");
        defaultComboBoxModel1.addElement("Нулевой отчёт");
        defaultComboBoxModel1.addElement("В стадии ликвидации");
        defaultComboBoxModel1.addElement("Произведена ликвидация");
        letterTypeComboBox.setModel(defaultComboBoxModel1);
        receiptToolBar.add(letterTypeComboBox);
        otchToolBar = new JToolBar();
        otchToolBar.setBorderPainted(false);
        otchToolBar.setFloatable(false);
        otchToolBar.setRollover(false);
        otchToolBar.putClientProperty("JToolBar.isRollover", Boolean.FALSE);
        filterPanel.add(otchToolBar, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 150), new Dimension(-1, 150), new Dimension(-1, 150), 0, false));
        final JLabel label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$(null, -1, 16, label4.getFont());
        if (label4Font != null) label4.setFont(label4Font);
        label4.setHorizontalAlignment(0);
        label4.setHorizontalTextPosition(0);
        label4.setText("ОКПО  ");
        otchToolBar.add(label4);
        okpoPanel = new JPanel();
        okpoPanel.setLayout(new BorderLayout(0, 0));
        otchToolBar.add(okpoPanel);
        final JLabel label5 = new JLabel();
        Font label5Font = this.$$$getFont$$$(null, -1, 16, label5.getFont());
        if (label5Font != null) label5.setFont(label5Font);
        label5.setHorizontalAlignment(0);
        label5.setHorizontalTextPosition(0);
        label5.setText("  ОКУД  ");
        otchToolBar.add(label5);
        okudPanel = new JPanel();
        okudPanel.setLayout(new BorderLayout(0, 0));
        otchToolBar.add(okudPanel);
        periodToolBar = new JToolBar();
        periodToolBar.setBorderPainted(false);
        periodToolBar.setFloatable(false);
        periodToolBar.setRollover(false);
        periodToolBar.putClientProperty("JToolBar.isRollover", Boolean.FALSE);
        filterPanel.add(periodToolBar, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), new Dimension(-1, 50), new Dimension(-1, 50), 0, false));
        final JLabel label6 = new JLabel();
        Font label6Font = this.$$$getFont$$$(null, -1, 16, label6.getFont());
        if (label6Font != null) label6.setFont(label6Font);
        label6.setHorizontalAlignment(0);
        label6.setHorizontalTextPosition(0);
        label6.setText("Отчётный год  ");
        periodToolBar.add(label6);
        yearComboBox = new JComboBox();
        Font yearComboBoxFont = this.$$$getFont$$$(null, Font.PLAIN, 14, yearComboBox.getFont());
        if (yearComboBoxFont != null) yearComboBox.setFont(yearComboBoxFont);
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        yearComboBox.setModel(defaultComboBoxModel2);
        periodToolBar.add(yearComboBox);
        final JLabel label7 = new JLabel();
        Font label7Font = this.$$$getFont$$$(null, -1, 16, label7.getFont());
        if (label7Font != null) label7.setFont(label7Font);
        label7.setHorizontalAlignment(0);
        label7.setHorizontalTextPosition(0);
        label7.setText("  Периодичность  ");
        periodToolBar.add(label7);
        periodicityComboBox = new JComboBox();
        Font periodicityComboBoxFont = this.$$$getFont$$$(null, Font.PLAIN, 14, periodicityComboBox.getFont());
        if (periodicityComboBoxFont != null) periodicityComboBox.setFont(periodicityComboBoxFont);
        final DefaultComboBoxModel defaultComboBoxModel3 = new DefaultComboBoxModel();
        defaultComboBoxModel3.addElement("Все");
        defaultComboBoxModel3.addElement("Годовая");
        defaultComboBoxModel3.addElement("Полугодовая");
        defaultComboBoxModel3.addElement("Квартальная");
        defaultComboBoxModel3.addElement("Месячная");
        periodicityComboBox.setModel(defaultComboBoxModel3);
        periodToolBar.add(periodicityComboBox);
        final JLabel label8 = new JLabel();
        Font label8Font = this.$$$getFont$$$(null, -1, 16, label8.getFont());
        if (label8Font != null) label8.setFont(label8Font);
        label8.setHorizontalAlignment(0);
        label8.setHorizontalTextPosition(0);
        label8.setText("  Отчётный период  ");
        periodToolBar.add(label8);
        periodComboBox = new JComboBox();
        Font periodComboBoxFont = this.$$$getFont$$$(null, Font.PLAIN, 14, periodComboBox.getFont());
        if (periodComboBoxFont != null) periodComboBox.setFont(periodComboBoxFont);
        final DefaultComboBoxModel defaultComboBoxModel4 = new DefaultComboBoxModel();
        defaultComboBoxModel4.addElement("Все");
        periodComboBox.setModel(defaultComboBoxModel4);
        periodToolBar.add(periodComboBox);
        getPeriodicityButton = new JButton();
        Font getPeriodicityButtonFont = this.$$$getFont$$$(null, -1, 18, getPeriodicityButton.getFont());
        if (getPeriodicityButtonFont != null) getPeriodicityButton.setFont(getPeriodicityButtonFont);
        getPeriodicityButton.setText("  Подобрать периодичность  ");
        filterPanel.add(getPeriodicityButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), null, null, 0, false));
        clearPeriodicityButton = new JButton();
        Font clearPeriodicityButtonFont = this.$$$getFont$$$(null, -1, 18, clearPeriodicityButton.getFont());
        if (clearPeriodicityButtonFont != null) clearPeriodicityButton.setFont(clearPeriodicityButtonFont);
        clearPeriodicityButton.setText("  Очистить периодичность  ");
        filterPanel.add(clearPeriodicityButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), null, null, 0, false));
        findButton = new JButton();
        Font findButtonFont = this.$$$getFont$$$(null, -1, 18, findButton.getFont());
        if (findButtonFont != null) findButton.setFont(findButtonFont);
        findButton.setText("  Найти  ");
        filterPanel.add(findButton, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), null, null, 0, false));
        createReportButton = new JButton();
        Font createReportButtonFont = this.$$$getFont$$$(null, -1, 18, createReportButton.getFont());
        if (createReportButtonFont != null) createReportButton.setFont(createReportButtonFont);
        createReportButton.setText("  Экспорт в Excel  ");
        filterPanel.add(createReportButton, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), null, null, 0, false));
        okatoToolBar = new JToolBar();
        okatoToolBar.setBorderPainted(false);
        okatoToolBar.setFloatable(false);
        okatoToolBar.setRollover(false);
        okatoToolBar.putClientProperty("JToolBar.isRollover", Boolean.FALSE);
        filterPanel.add(okatoToolBar, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), new Dimension(-1, 50), new Dimension(-1, 50), 0, false));
        final JLabel label9 = new JLabel();
        Font label9Font = this.$$$getFont$$$(null, -1, 16, label9.getFont());
        if (label9Font != null) label9.setFont(label9Font);
        label9.setHorizontalAlignment(0);
        label9.setHorizontalTextPosition(0);
        label9.setText("ОКАТО  ");
        okatoToolBar.add(label9);
        okatoTextField = new JTextField();
        Font okatoTextFieldFont = this.$$$getFont$$$(null, Font.PLAIN, 14, okatoTextField.getFont());
        if (okatoTextFieldFont != null) okatoTextField.setFont(okatoTextFieldFont);
        okatoToolBar.add(okatoTextField);
        final JLabel label10 = new JLabel();
        Font label10Font = this.$$$getFont$$$(null, -1, 16, label10.getFont());
        if (label10Font != null) label10.setFont(label10Font);
        label10.setHorizontalAlignment(0);
        label10.setHorizontalTextPosition(0);
        label10.setText("  ОКТМО  ");
        okatoToolBar.add(label10);
        oktmoTextField = new JTextField();
        Font oktmoTextFieldFont = this.$$$getFont$$$(null, Font.PLAIN, 14, oktmoTextField.getFont());
        if (oktmoTextFieldFont != null) oktmoTextField.setFont(oktmoTextFieldFont);
        okatoToolBar.add(oktmoTextField);
        final JLabel label11 = new JLabel();
        Font label11Font = this.$$$getFont$$$(null, -1, 16, label11.getFont());
        if (label11Font != null) label11.setFont(label11Font);
        label11.setHorizontalAlignment(0);
        label11.setHorizontalTextPosition(0);
        label11.setText("  ОКВЭД  ");
        okatoToolBar.add(label11);
        okvedTextField = new JTextField();
        Font okvedTextFieldFont = this.$$$getFont$$$(null, Font.PLAIN, 14, okvedTextField.getFont());
        if (okvedTextFieldFont != null) okvedTextField.setFont(okvedTextFieldFont);
        okatoToolBar.add(okvedTextField);
        toolBarPanel = new JPanel();
        toolBarPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        applicationPanel.add(toolBarPanel, BorderLayout.SOUTH);
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
        final DefaultComboBoxModel defaultComboBoxModel5 = new DefaultComboBoxModel();
        defaultComboBoxModel5.addElement("10");
        defaultComboBoxModel5.addElement("20");
        defaultComboBoxModel5.addElement("50");
        defaultComboBoxModel5.addElement("100");
        pageSizeComboBox.setModel(defaultComboBoxModel5);
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
        final JLabel label12 = new JLabel();
        Font label12Font = this.$$$getFont$$$(null, -1, 14, label12.getFont());
        if (label12Font != null) label12.setFont(label12Font);
        label12.setHorizontalAlignment(0);
        label12.setHorizontalTextPosition(0);
        label12.setText("Страница  ");
        tableToolBar.add(label12);
        currentPageLabel = new JLabel();
        Font currentPageLabelFont = this.$$$getFont$$$(null, -1, 14, currentPageLabel.getFont());
        if (currentPageLabelFont != null) currentPageLabel.setFont(currentPageLabelFont);
        currentPageLabel.setHorizontalAlignment(0);
        currentPageLabel.setHorizontalTextPosition(0);
        currentPageLabel.setText("0");
        tableToolBar.add(currentPageLabel);
        final JLabel label13 = new JLabel();
        Font label13Font = this.$$$getFont$$$(null, -1, 14, label13.getFont());
        if (label13Font != null) label13.setFont(label13Font);
        label13.setHorizontalAlignment(0);
        label13.setHorizontalTextPosition(0);
        label13.setText("  из  ");
        tableToolBar.add(label13);
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
        refreshButton = new JButton();
        Font refreshButtonFont = this.$$$getFont$$$(null, -1, 16, refreshButton.getFont());
        if (refreshButtonFont != null) refreshButton.setFont(refreshButtonFont);
        refreshButton.setHideActionText(false);
        refreshButton.setHorizontalTextPosition(0);
        refreshButton.setIcon(new ImageIcon(getClass().getResource("/images/refresh.png")));
        refreshButton.setText("");
        tableToolBar.add(refreshButton);
        final JToolBar.Separator toolBar$Separator5 = new JToolBar.Separator();
        tableToolBar.add(toolBar$Separator5);
        final JLabel label14 = new JLabel();
        Font label14Font = this.$$$getFont$$$(null, -1, 14, label14.getFont());
        if (label14Font != null) label14.setFont(label14Font);
        label14.setHorizontalAlignment(0);
        label14.setHorizontalTextPosition(0);
        label14.setText("Всего записей - ");
        tableToolBar.add(label14);
        recordsCountLabel = new JLabel();
        Font recordsCountLabelFont = this.$$$getFont$$$(null, -1, 14, recordsCountLabel.getFont());
        if (recordsCountLabelFont != null) recordsCountLabel.setFont(recordsCountLabelFont);
        recordsCountLabel.setHorizontalAlignment(0);
        recordsCountLabel.setHorizontalTextPosition(0);
        recordsCountLabel.setText("0");
        tableToolBar.add(recordsCountLabel);
        final JToolBar.Separator toolBar$Separator6 = new JToolBar.Separator();
        tableToolBar.add(toolBar$Separator6);
        letterScrollPane = new JScrollPane();
        applicationPanel.add(letterScrollPane, BorderLayout.CENTER);
        letterTable = new JTable();
        letterTable.setAutoResizeMode(4);
        Font letterTableFont = this.$$$getFont$$$(null, -1, 14, letterTable.getFont());
        if (letterTableFont != null) letterTable.setFont(letterTableFont);
        letterScrollPane.setViewportView(letterTable);
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
