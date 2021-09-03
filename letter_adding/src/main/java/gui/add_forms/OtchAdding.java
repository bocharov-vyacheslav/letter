package gui.add_forms;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import data.models.Okpo;
import data.models.Okud;
import data.services.DataService;
import data_helpers.AppLogger;
import data.models.Otch;
import data.models.PreviousOtch;
import data_helpers.JDBCHelper;
import gui.LetterAdding;
import gui.gui_elements.JListFilterDecorator;
import gui.gui_elements.UserJPanel;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class OtchAdding extends JDialog {
    private JButton addButton;
    private JComboBox<Object> yearComboBox;
    private JPanel okpoPanel;
    private JPanel okudPanel;
    private JComboBox<String> periodComboBox;
    private JPanel otchPanel;
    private JButton closeButton;
    private JComboBox<String> periodicityComboBox;
    private JButton clearPeriodicityButton;
    private JButton getPeriodicityButton;
    private JPanel periodicityPanel;

    private JTextField jOkpoTextFiled;
    private JTextField jShortOkudTextFiled;

    public OtchAdding(LetterAdding frame, Font font, List<Otch> otchRecords, List<Okpo> okpoRecords, JList<Okpo> jOkpoRecords, List<Okud> okudRecords, JList<Okud> jShortOkudRecords, byte letterType) {

        super(frame);

        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        String warning = "Предупреждение";

        setTitle("Добавление отчётности");
        setContentPane(otchPanel);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(frame);

        PreviousOtch previousOtch = frame.getPreviousOtch();

        UserJPanel jOkpoPanel = JListFilterDecorator.decorate(font, 14, jOkpoRecords, JListFilterDecorator::okpoFilter);
        jOkpoTextFiled = jOkpoPanel.getTextField();
        okpoPanel.add(jOkpoPanel);
        jOkpoTextFiled.setText(previousOtch.getOkpo());

        List<Integer> years = new ArrayList<>();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        years.add(year + 1);
        years.add(year);
        years.add(year - 1);

        yearComboBox.setModel(new DefaultComboBoxModel<>(years.toArray()));
        yearComboBox.setSelectedIndex(previousOtch.getYearIndex());

        UserJPanel jShortOkudPanel = JListFilterDecorator.decorate(font, 7, jShortOkudRecords, JListFilterDecorator::okudFilter);
        jShortOkudTextFiled = jShortOkudPanel.getTextField();
        okudPanel.add(jShortOkudPanel);

        boolean isNoActivity = Arrays.asList((byte) 1, (byte) 4, (byte) 5).contains(letterType);

        getPeriodicityButton.addActionListener(e -> {
            try {

                String okud = jShortOkudTextFiled.getText();
                if (okud.isEmpty()) {
                    String message = "Код ОКУД не выбран!!!";
                    JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (okudRecords.parallelStream().noneMatch(o -> o.getOkud().equals(okud))) {
                    String message = "Код ОКУД введён неверно!!!";
                    JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                    return;
                }

                frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                periodicityComboBox.setEnabled(true);

                DefaultComboBoxModel<String> periodicityComboBoxModel = new DefaultComboBoxModel<>();
                DefaultComboBoxModel<String> periodComboBoxModel = new DefaultComboBoxModel<>();
                List<Okud> periodicities = okudRecords.parallelStream().filter(o -> o.getOkud().equals(okud)).collect(Collectors.toList());
                if (periodicities.size() == 1) {

                    Okud periodicity = periodicities.get(0);
                    periodicityComboBoxModel.addElement(periodicity.getPeriodicity());

                    fillModel(periodComboBoxModel, periodicity.getPeriodicityCode());

                } else {

                    periodicityComboBoxModel.addElement("Не выбрано");
                    periodicities = periodicities.parallelStream().sorted(Comparator.comparingInt(Okud::getPeriodicityCode)).collect(Collectors.toList());
                    periodicities.forEach(p -> periodicityComboBoxModel.addElement(p.getPeriodicity()));

                    periodComboBoxModel.addElement("Не выбрано");

                }

                periodicityComboBox.setModel(periodicityComboBoxModel);
                periodComboBox.setModel(periodComboBoxModel);

                clearPeriodicityButton.setEnabled(true);
                jShortOkudTextFiled.setEnabled(false);
                jShortOkudRecords.setEnabled(false);
                getPeriodicityButton.setEnabled(false);

                frame.setCursor(Cursor.getDefaultCursor());

            } catch (Exception ex) {
                frame.setCursor(Cursor.getDefaultCursor());
                AppLogger.fatal(this, ex);
            }
        });

        periodicityComboBox.addActionListener(e -> {
            try {
                DefaultComboBoxModel<String> periodComboBoxModel = new DefaultComboBoxModel<>();
                fillModel(periodComboBoxModel, Objects.requireNonNull(periodicityComboBox.getSelectedItem()).toString());
                periodComboBox.setModel(periodComboBoxModel);
            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        clearPeriodicityButton.setEnabled(false);
        periodComboBox.setEnabled(false);
        periodicityComboBox.setEnabled(false);

        String previousOkud = previousOtch.getOkud();
        if (!isNoActivity && !previousOkud.isEmpty() && !previousOkud.equals("0000000") && !previousOkud.equals("0000001") && !previousOkud.equals("0000002")) {
            jShortOkudTextFiled.setText(previousOkud);
            getPeriodicityButton.doClick();
            periodicityComboBox.setSelectedIndex(previousOtch.getPeriodicityIndex());
            periodComboBox.setSelectedIndex(previousOtch.getPeriodIndex());
        }

        addButton.addActionListener(e -> {
            try {

                String okpo = jOkpoTextFiled.getText().trim();
                if (okpo.isEmpty()) {
                    String message = "Код ОКПО не выбран!!!";
                    JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Okpo originOkpo = okpoRecords.parallelStream().filter(o -> o.getOkpo().equals(okpo)).findFirst().orElse(null);
                if (originOkpo == null) {
                    String message = "Код ОКПО введён неверно!!!";
                    JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String okud;
                String period = "";
                Okud originOkud = null;
                String periodicity = "";
                if (!isNoActivity) {

                    okud = jShortOkudTextFiled.getText().trim();
                    if (okud.isEmpty()) {
                        String message = "Код ОКУД не выбран!!!";
                        JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    originOkud = okudRecords.parallelStream().filter(o -> o.getOkud().equals(okud)).findFirst().orElse(null);
                    if (originOkud == null) {
                        String message = "Код ОКУД введён неверно!!!";
                        JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    periodicity = Objects.requireNonNull(periodicityComboBox.getSelectedItem()).toString();
                    if (periodicity.equals("Не выбрано")) {
                        String message = "Периодичность не выбрана!!!";
                        JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    period = Objects.requireNonNull(periodComboBox.getSelectedItem()).toString();
                    if (period.equals("Не выбрано")) {
                        String message = "Отчётный период не выбран!!!";
                        JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                }

                frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                String periodYear = Objects.requireNonNull(yearComboBox.getSelectedItem()).toString();

                Otch otch = new Otch();
                otch.setId((byte) (otchRecords.size() + 1));
                otch.setOkpo(originOkpo.getName());
                otch.setPeriodYear(periodYear);

                PreviousOtch originPreviosOtch = new PreviousOtch();
                originPreviosOtch.setOkpo(originOkpo.getOkpo());
                originPreviosOtch.setYearIndex(yearComboBox.getSelectedIndex());

                switch (letterType) {

                    case 1:

                        otch.setOkud("0000000 - отсутствие деятельности");
                        otch.setPeriodicity("Отсутствие деятельности");
                        otch.setPeriod("0000 - отсутствие деятельности");

                        originPreviosOtch.setOkud("0000000");
                        originPreviosOtch.setPeriodicityIndex(-1);
                        originPreviosOtch.setPeriodIndex(-1);

                        break;
                    case 4:

                        otch.setOkud("0000001 - в стадии ликвидации");
                        otch.setPeriodicity("В стадии ликвидации");
                        otch.setPeriod("0001 - в стадии ликвидации");

                        originPreviosOtch.setOkud("0000001");
                        originPreviosOtch.setPeriodicityIndex(-1);
                        originPreviosOtch.setPeriodIndex(-1);

                        break;
                    case 5:

                        otch.setOkud("0000002 - произведена ликвидация");
                        otch.setPeriodicity("Произведена ликвидация");
                        otch.setPeriod("0002 - произведена ликвидация");

                        originPreviosOtch.setOkud("0000002");
                        originPreviosOtch.setPeriodicityIndex(-1);
                        originPreviosOtch.setPeriodIndex(-1);

                        break;
                    default:

                        otch.setOkud(Objects.requireNonNull(originOkud).getName());
                        otch.setPeriodicity(periodicity);
                        otch.setPeriod(period);

                        originPreviosOtch.setOkud(originOkud.getOkud());
                        originPreviosOtch.setPeriodicityIndex(periodicityComboBox.getSelectedIndex());
                        originPreviosOtch.setPeriodIndex(periodComboBox.getSelectedIndex());

                        break;
                }

                if (otchRecords.parallelStream().anyMatch(o -> o.equals(otch))) {
                    frame.setCursor(Cursor.getDefaultCursor());
                    String message = "Такая отчётность уже вводилась!!!";
                    JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try (AbstractApplicationContext context = new AnnotationConfigApplicationContext(JDBCHelper.class)) {

                    String okpoTemp = otch.getOkpo();
                    String okpoForCheck = okpoTemp.substring(0, okpoTemp.indexOf('-') - 1);
                    String okudForCheck = otch.getOkud().substring(0, 7);
                    short periodYearForCheck = Short.parseShort(otch.getPeriodYear());
                    String periodNumberForCheck = otch.getPeriod().substring(0, 4);

                    DataService dataService = (DataService) context.getBean("dataService");
                    String message = dataService.checkOtch(okpoForCheck, okudForCheck, periodYearForCheck, periodNumberForCheck);

                    if (!message.isEmpty()) {
                        frame.setCursor(Cursor.getDefaultCursor());
                        JOptionPane.showMessageDialog(this, message, warning, JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                otchRecords.add(otch);
                frame.setPreviousOtch(originPreviosOtch);

                frame.repaint();

                frame.setCursor(Cursor.getDefaultCursor());

                dispose();

            } catch (Exception ex) {
                frame.setCursor(Cursor.getDefaultCursor());
                AppLogger.fatal(this, ex);
            }
        });

        closeButton.addActionListener(e -> {
            try {
                dispose();
            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        clearPeriodicityButton.addActionListener(e -> {
            try {
                DefaultComboBoxModel<String> periodicityComboBoxModel = new DefaultComboBoxModel<>();
                periodicityComboBoxModel.addElement("Не выбрано");
                periodicityComboBox.setModel(periodicityComboBoxModel);

                DefaultComboBoxModel<String> periodComboBoxModel = new DefaultComboBoxModel<>();
                periodComboBoxModel.addElement("Не выбрано");
                periodComboBox.setModel(periodComboBoxModel);

                periodComboBox.setEnabled(false);
                periodicityComboBox.setEnabled(false);
                clearPeriodicityButton.setEnabled(false);
                jShortOkudTextFiled.setEnabled(true);
                jShortOkudRecords.setEnabled(true);
                getPeriodicityButton.setEnabled(true);
            } catch (Exception ex) {
                AppLogger.fatal(this, ex);
            }
        });

        if (isNoActivity) {
            jShortOkudTextFiled.setEnabled(false);
            jShortOkudRecords.setEnabled(false);
            getPeriodicityButton.setEnabled(false);
        }

        setModal(true);
        setVisible(true);

        frame.setCursor(Cursor.getDefaultCursor());
    }

    private void fillModel(DefaultComboBoxModel<String> periodComboBoxModel, byte periodicityCode) {
        switch (periodicityCode) {
            case 0:
                periodComboBoxModel.addElement("Не выбрано");
                periodicityComboBox.setEnabled(true);
                periodComboBox.setEnabled(true);
                break;
            case 1:
                periodComboBoxModel.addElement("0101 - год");
                periodicityComboBox.setEnabled(false);
                periodComboBox.setEnabled(false);
                break;
            case 2:
                periodComboBoxModel.addElement("Не выбрано");
                periodComboBoxModel.addElement("0401 - 1й квартал");
                periodComboBoxModel.addElement("0402 - 2й квартал");
                periodComboBoxModel.addElement("0403 - 3й квартал");
                periodComboBoxModel.addElement("0404 - 4й квартал");
                periodicityComboBox.setEnabled(true);
                periodComboBox.setEnabled(true);
                break;
            case 3:
                periodComboBoxModel.addElement("Не выбрано");
                periodComboBoxModel.addElement("1201 - январь");
                periodComboBoxModel.addElement("1202 - февраль");
                periodComboBoxModel.addElement("1203 - март");
                periodComboBoxModel.addElement("1204 - апрель");
                periodComboBoxModel.addElement("1205 - май");
                periodComboBoxModel.addElement("1206 - июнь");
                periodComboBoxModel.addElement("1207 - июль");
                periodComboBoxModel.addElement("1208 - август");
                periodComboBoxModel.addElement("1209 - сентябрь");
                periodComboBoxModel.addElement("1210 - октябрь");
                periodComboBoxModel.addElement("1211 - ноябрь");
                periodComboBoxModel.addElement("1212 - декабрь");
                periodicityComboBox.setEnabled(true);
                periodComboBox.setEnabled(true);
                break;
            case 4:
                periodComboBoxModel.addElement("Не выбрано");
                periodComboBoxModel.addElement("0201 - 1 полугодие");
                periodComboBoxModel.addElement("0202 - 2 полугодие");
                periodicityComboBox.setEnabled(true);
                periodComboBox.setEnabled(true);
                break;
        }
    }

    private void fillModel(DefaultComboBoxModel<String> periodComboBoxModel, String periodicity) {
        switch (periodicity) {
            case "Не выбрано":
                periodComboBoxModel.addElement("Не выбрано");
                periodComboBox.setEnabled(true);
                break;
            case "Годовая":
                periodComboBoxModel.addElement("0101 - год");
                periodComboBox.setEnabled(false);
                break;
            case "Квартальная":
                periodComboBoxModel.addElement("Не выбрано");
                periodComboBoxModel.addElement("0401 - 1й квартал");
                periodComboBoxModel.addElement("0402 - 2й квартал");
                periodComboBoxModel.addElement("0403 - 3й квартал");
                periodComboBoxModel.addElement("0404 - 4й квартал");
                periodComboBox.setEnabled(true);
                break;
            case "Месячная":
                periodComboBoxModel.addElement("Не выбрано");
                periodComboBoxModel.addElement("1201 - январь");
                periodComboBoxModel.addElement("1202 - февраль");
                periodComboBoxModel.addElement("1203 - март");
                periodComboBoxModel.addElement("1204 - апрель");
                periodComboBoxModel.addElement("1205 - май");
                periodComboBoxModel.addElement("1206 - июнь");
                periodComboBoxModel.addElement("1207 - июль");
                periodComboBoxModel.addElement("1208 - август");
                periodComboBoxModel.addElement("1209 - сентябрь");
                periodComboBoxModel.addElement("1210 - октябрь");
                periodComboBoxModel.addElement("1211 - ноябрь");
                periodComboBoxModel.addElement("1212 - декабрь");
                periodComboBox.setEnabled(true);
                break;
            case "Полугодовая":
                periodComboBoxModel.addElement("Не выбрано");
                periodComboBoxModel.addElement("0201 - 1 полугодие");
                periodComboBoxModel.addElement("0202 - 2 полугодие");
                periodComboBox.setEnabled(true);
                break;
        }
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
        otchPanel = new JPanel();
        otchPanel.setLayout(new GridLayoutManager(11, 2, new Insets(20, 20, 20, 20), -1, -1));
        final Spacer spacer1 = new Spacer();
        otchPanel.add(spacer1, new GridConstraints(7, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        otchPanel.add(spacer2, new GridConstraints(9, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        closeButton = new JButton();
        Font closeButtonFont = this.$$$getFont$$$(null, -1, 20, closeButton.getFont());
        if (closeButtonFont != null) closeButton.setFont(closeButtonFont);
        closeButton.setText("  Закрыть  ");
        otchPanel.add(closeButton, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), null, null, 0, false));
        addButton = new JButton();
        Font addButtonFont = this.$$$getFont$$$(null, -1, 20, addButton.getFont());
        if (addButtonFont != null) addButton.setFont(addButtonFont);
        addButton.setText("  Добавить отчётность  ");
        otchPanel.add(addButton, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), null, null, 0, false));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, -1, 22, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(2);
        label1.setText("  Отчётность  ");
        otchPanel.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        otchPanel.add(spacer3, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        periodicityPanel = new JPanel();
        periodicityPanel.setLayout(new GridLayoutManager(11, 2, new Insets(0, 0, 0, 0), -1, -1));
        Font periodicityPanelFont = this.$$$getFont$$$(null, -1, -1, periodicityPanel.getFont());
        if (periodicityPanelFont != null) periodicityPanel.setFont(periodicityPanelFont);
        otchPanel.add(periodicityPanel, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        periodicityPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), "Периодичность", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, -1, 20, periodicityPanel.getFont()), new Color(-16777216)));
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, Font.BOLD, 18, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setForeground(new Color(-3014656));
        label2.setHorizontalAlignment(0);
        label2.setHorizontalTextPosition(0);
        label2.setText("  ОКУД  ");
        periodicityPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        okudPanel = new JPanel();
        okudPanel.setLayout(new BorderLayout(0, 0));
        periodicityPanel.add(okudPanel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        periodicityPanel.add(spacer4, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        getPeriodicityButton = new JButton();
        Font getPeriodicityButtonFont = this.$$$getFont$$$(null, -1, 20, getPeriodicityButton.getFont());
        if (getPeriodicityButtonFont != null) getPeriodicityButton.setFont(getPeriodicityButtonFont);
        getPeriodicityButton.setText("  Подобрать периодичность  ");
        periodicityPanel.add(getPeriodicityButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        periodicityPanel.add(spacer5, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        clearPeriodicityButton = new JButton();
        Font clearPeriodicityButtonFont = this.$$$getFont$$$(null, -1, 20, clearPeriodicityButton.getFont());
        if (clearPeriodicityButtonFont != null) clearPeriodicityButton.setFont(clearPeriodicityButtonFont);
        clearPeriodicityButton.setText("  Очистить периодичность  ");
        periodicityPanel.add(clearPeriodicityButton, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        periodicityPanel.add(spacer6, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        Font label3Font = this.$$$getFont$$$(null, Font.BOLD, 18, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setForeground(new Color(-3014656));
        label3.setHorizontalAlignment(0);
        label3.setHorizontalTextPosition(0);
        label3.setText("  Перидичность  ");
        periodicityPanel.add(label3, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        periodicityComboBox = new JComboBox();
        Font periodicityComboBoxFont = this.$$$getFont$$$(null, Font.PLAIN, 18, periodicityComboBox.getFont());
        if (periodicityComboBoxFont != null) periodicityComboBox.setFont(periodicityComboBoxFont);
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Не выбрано");
        periodicityComboBox.setModel(defaultComboBoxModel1);
        periodicityPanel.add(periodicityComboBox, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        periodicityPanel.add(spacer7, new GridConstraints(8, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$(null, Font.BOLD, 18, label4.getFont());
        if (label4Font != null) label4.setFont(label4Font);
        label4.setForeground(new Color(-3014656));
        label4.setHorizontalAlignment(0);
        label4.setHorizontalTextPosition(0);
        label4.setText("  Отчётный период  ");
        periodicityPanel.add(label4, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        periodComboBox = new JComboBox();
        periodComboBox.setEditable(false);
        Font periodComboBoxFont = this.$$$getFont$$$(null, Font.PLAIN, 18, periodComboBox.getFont());
        if (periodComboBoxFont != null) periodComboBox.setFont(periodComboBoxFont);
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("Не выбрано");
        periodComboBox.setModel(defaultComboBoxModel2);
        periodicityPanel.add(periodComboBox, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        periodicityPanel.add(spacer8, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        periodicityPanel.add(spacer9, new GridConstraints(10, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        Font label5Font = this.$$$getFont$$$(null, Font.BOLD, 18, label5.getFont());
        if (label5Font != null) label5.setFont(label5Font);
        label5.setForeground(new Color(-3014656));
        label5.setHorizontalAlignment(0);
        label5.setHorizontalTextPosition(0);
        label5.setText("  ОКПО  ");
        otchPanel.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        okpoPanel = new JPanel();
        okpoPanel.setLayout(new BorderLayout(0, 0));
        otchPanel.add(okpoPanel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer10 = new Spacer();
        otchPanel.add(spacer10, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        Font label6Font = this.$$$getFont$$$(null, Font.BOLD, 18, label6.getFont());
        if (label6Font != null) label6.setFont(label6Font);
        label6.setForeground(new Color(-3014656));
        label6.setHorizontalAlignment(0);
        label6.setHorizontalTextPosition(0);
        label6.setText("  Отчётный год  ");
        otchPanel.add(label6, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        yearComboBox = new JComboBox();
        yearComboBox.setEnabled(true);
        Font yearComboBoxFont = this.$$$getFont$$$(null, Font.PLAIN, 18, yearComboBox.getFont());
        if (yearComboBoxFont != null) yearComboBox.setFont(yearComboBoxFont);
        final DefaultComboBoxModel defaultComboBoxModel3 = new DefaultComboBoxModel();
        defaultComboBoxModel3.addElement("Не выбрано");
        yearComboBox.setModel(defaultComboBoxModel3);
        otchPanel.add(yearComboBox, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer11 = new Spacer();
        otchPanel.add(spacer11, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
        return otchPanel;
    }

}
