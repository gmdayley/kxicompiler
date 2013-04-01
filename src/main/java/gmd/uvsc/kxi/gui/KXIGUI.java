package gmd.uvsc.kxi.gui;

import gmd.uvsc.kxi.Parser;
import gmd.uvsc.kxi.StringParser;
import gmd.uvsc.kxi.gui.TextAreaOutputStream;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: gmdayley
 * Date: Feb 28, 2008
 * Time: 12:05:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class KXIGUI {
    private JButton button1;
    private JPanel panel1;
    private JButton button2;
    private JButton button3;
    private JTabbedPane tabbedPane1;
    private JTabbedPane tabbedPane2;
    private JTextArea textArea1;
    private JEditorPane editorPane1;
    private JButton refreshButton;

    public KXIGUI() {
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textArea1.setText("");

                Parser p = new StringParser(editorPane1.getText());
                p.setOutputStream(new PrintStream(new TextAreaOutputStream(textArea1)));
                p.parse();
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("KXI Compiler");
        frame.setContentPane(new KXIGUI().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
