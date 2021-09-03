package gui.gui_elements;

import data.models.IName;
import data.models.Okpo;
import data.models.Okud;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class JListFilterDecorator {

    public static <T> UserJPanel decorate(Font font, int regexLength, JList<T> jList, BiPredicate<T, String> userFilter) {

        if (!(jList.getModel() instanceof DefaultListModel)) {
            throw new IllegalArgumentException("List model must be an instance of DefaultListModel");
        }

        DefaultListModel<T> model = (DefaultListModel<T>) jList.getModel();
        List<T> items = getItems(model);
        JTextField textField = new JTextField();
        textField.setFont(font);

        textField.setHighlighter(null);
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("^\\d{0," + regexLength + "}$") && (regexLength - fb.getDocument().getLength()) >= text.length())
                    super.replace(fb, offset, length, text, attrs);
            }
        });

        model.clear();
        textField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            private void filter() {
                model.clear();
                String str = textField.getText();
                if (str.length() > 4)
                    for (T item : items)
                        if (userFilter.test(item, str))
                            model.addElement(item);
            }
        });

        UserJPanel panel = new UserJPanel(new BorderLayout());
        panel.setTextField(textField);
        panel.add(textField, BorderLayout.NORTH);
        JScrollPane pane = new JScrollPane(jList);

        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!model.isEmpty() && e.getClickCount() == 2) {
                    IName name = (IName) jList.getSelectedValue();
                    if (name != null) {
                        textField.setText("");
                        textField.setText(name.toString());
                    }
                }
            }
        });

        panel.add(pane);
        return panel;
    }

    private static <T> List<T> getItems(DefaultListModel<T> model) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < model.size(); i++)
            list.add(model.elementAt(i));

        return list;
    }

    public static boolean okpoFilter(Okpo okpo, String str) {
        return okpo.getOkpo().contains(str);
    }

    public static boolean okudFilter(Okud okud, String str) {
        return okud.getOkud().contains(str);
    }

}
