
/**
 * <CODE>ComEditor</CODE>
 * <p>
 * Opens a frame with possibility edit and check communication data.
 * <p>
 * ChangeLog:
 * <p>
 * v0.10 - Initial version, just opens a empty frame, now it needs
 * just some content (6.10.2009)
 * v0.20 - Basic layout is here, buttons, textfields. (6.10.2009)
 * v0.30 - Visual modifications, looks better now. (23.10.2009)
 * v0.35 - Updates Operator and signal data on 10 second intervals (20.01.2010)
 * v0.36 - Little bugfix, didn't update the signal strenght icon if signal was lost.
 * (4.2.2010)
 * <p>
 * TODO:
 * - add possibility to save the changes made to com-port selection
 *
 * @author Juha-Matti Sironen
 * @version 0.36
 * @date 4.2.2010
 */

package fi.devl.gsmalarm.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import fi.devl.gsmalarm.GsmGUI;
import fi.devl.gsmalarm.servers.ComServerThread;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.*;

public class ComEditor extends JFrame implements ActionListener, WindowListener {

    private String otsikko;

    private int selectedPort = 0;
    private int selectedComCentral = 0;
    private double signalStrength;

    private ImageIcon logo;
    private ImageIcon[] verkko;
    private JLabel verkkoHolder;

    private String[] portit;
    private String[] keskukset;

    private JButton saveData;
    private JButton checkSignal;

    private JPanel logoPanel;
    private JPanel dataPanel;
    private JLabel logoLabel;

    private JComboBox porttiValikko;
    private JComboBox vistikeskusValikko;

    private JTextField operatorField;
    private JTextField signalField;

    private ArrayList<String> portList;

    private GsmGUI gui;
    private ComServerThread comServer;

    private ScheduledExecutorService scheduledDataPoll;

    public ComEditor(String otsikko, ArrayList<String> list, ComServerThread comServer, GsmGUI gui) {
        this.otsikko = otsikko;
        this.portList = list;
        this.gui = gui;
        this.comServer = comServer;

        setTitle(this.otsikko);
        setSize(640, 480);
        setBackground(Color.white);
        setResizable(false);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

        // logo on top
        logoPanel = new JPanel();
        logoPanel.setLayout(new GridBagLayout());
        this.logo = this.gui.createImageIcon("images/bg.png");
        logoLabel = new JLabel();
        logoLabel.setIcon(this.logo);
        logoPanel.add(logoLabel);

        this.verkko = new ImageIcon[11];

        for (int i = 0; i < 11; i++) {
            this.verkko[i] = gui.createImageIcon("images/verkko_" + i + ".png");
        }

        this.verkkoHolder = new JLabel();
        verkkoHolder.setIcon(this.verkko[0]);

        // input-datahandling
        this.portit = new String[this.portList.size()];

        for (int i = 0; i < this.portList.size(); i++) {
            this.portit[i] = this.portList.get(i);
        }

        // comboboxes and anction listeners
        porttiValikko = new JComboBox(portit);
        porttiValikko.addActionListener(this);

        // userdata and buttons
        dataPanel = new JPanel();
        dataPanel.setLayout(new GridBagLayout());
        dataPanel.setBackground(Color.white);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        JLabel ports = new JLabel(" GSM-Modeemin portti ");

        this.operatorField = new JTextField("Ei operaattoria", 15);
        this.operatorField.setBackground(Color.getHSBColor(0.0f, 0.0f, 0.95f));

        this.signalField = new JTextField("Ei signaalia", 15);
        this.signalField.setBackground(Color.getHSBColor(0.0f, 0.0f, 0.95f));

        this.saveData = new JButton("Tallenna muutokset");
        this.saveData.addActionListener(this);
        this.saveData.setActionCommand("save");

        this.porttiValikko.setActionCommand("portSelect");

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.insets = new Insets(0, 2, 0, 2);
        dataPanel.add(new JLabel(" "), c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.insets = new Insets(0, 2, 2, 2);
        dataPanel.add(ports, c);

        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.ipadx = 40;
        dataPanel.add(porttiValikko, c);

        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        dataPanel.add(new JLabel(" "), c);

        // save button
        c.gridx = 3;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(saveData, c);

        // separator
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 5;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.insets = new Insets(22, 2, 22, 2);
        dataPanel.add(new JSeparator(), c);

        // operator
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.insets = new Insets(2, 2, 2, 2);
        dataPanel.add(new JLabel(" Operaattori "), c);

        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(operatorField, c);

        c.gridx = 3;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 4;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(verkkoHolder, c);

        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(new JLabel(" Signaalin voimakkuus "), c);

        c.gridx = 1;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(signalField, c);

        c.gridx = 1;
        c.gridy = 6;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        dataPanel.add(new JLabel(" "), c);

        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        dataPanel.add(new JLabel(" "), c);

        // just to push everything else up
        c.gridx = 0;
        c.gridy = 10;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        dataPanel.add(new JLabel(" "), c);

        this.add(logoPanel);
        this.add(dataPanel);

        this.getContentPane().add(BorderLayout.NORTH, logoPanel);
        this.getContentPane().add(BorderLayout.CENTER, dataPanel);

        this.scheduledDataPoll = Executors.newScheduledThreadPool(1);

        this.setVisible(true);
        this.updateFields();
        this.updater(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("portSelect")) {
            JComboBox cb = (JComboBox) e.getSource();
            this.selectedPort = cb.getSelectedIndex();
            this.updateFields();
        }
    }

    public void windowActivated(WindowEvent arg0) {
    }

    public void windowClosed(WindowEvent arg0) {
    }

    public void windowClosing(WindowEvent arg0) {
        this.gui.comCentral.setEnabled(true);
        this.setVisible(false);
        scheduledDataPoll.shutdown();
        this.dispose();
    }

    public void windowDeactivated(WindowEvent arg0) {
    }

    public void windowDeiconified(WindowEvent arg0) {
    }

    public void windowIconified(WindowEvent arg0) {
    }

    public void windowOpened(WindowEvent arg0) {
    }

    public void updater(final ComEditor parent) {
        final Runnable updateData = new Runnable() {
            public void run() {
                parent.updateFields();
            }
        };

        final ScheduledFuture<?> dataHandle = scheduledDataPoll.scheduleAtFixedRate(updateData, 10, 10, SECONDS);
    }

    public void updateFields() {
        Double percent;
        String strength;
        String pattern;
        String result;

        this.operatorField.setText(comServer.serviceProvider());

        strength = comServer.signalStrength();
        if (!strength.equals("Ei signaalia")) {

            pattern = "###.#";

            DecimalFormat myFormatter = new DecimalFormat(pattern);

            percent = Double.parseDouble(strength);
            percent = (percent / 31.0) * 100.0;

            // limiter
            percent = Math.min(percent, 100.0);
            percent = Math.max(percent, 0.0);

            result = myFormatter.format(percent);

            this.signalField.setText(result + " %");

            this.verkkoHolder.setIcon((this.verkko[(int) ((percent / 10))]));
        } else {
            this.signalField.setText(strength);
            this.verkkoHolder.setIcon(this.verkko[0]);
        }
    }
}
