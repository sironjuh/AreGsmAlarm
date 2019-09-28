/**
 * <CODE>UserEditor</CODE>
 * <p>
 * Opens a frame with possibility edit userdata defined in User-class.
 * <p>
 * ChangeLog:
 * <p>
 * v0.10 - Initial version, just opens a empty frame, now it needs just some content (26.09.2009)
 * v0.20 - Basic layout is here, buttons, textfields. (28.09.2009)
 * v0.30 - Now it's possible to modify userList-data from GsmGUI-class (01.10.2009)
 * v0.40 - Small bugfixes (ArrayIndexOutOfBounds etc). (06.10.2009)
 * v0.41 - Added the possibility to change active state from UserEditor (22.10.2009)
 * <p>
 * TODO:
 * - code cleanup
 * - visual improvments
 *
 * @author Juha-Matti Sironen
 * @version 0.41
 * @date 22.10.2009
 */

package AreGsmAlarm.editors;

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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import AreGsmAlarm.GsmGUI;
import AreGsmAlarm.TimeTable;
import AreGsmAlarm.User;

import java.util.ArrayList;

public class UserEditor extends JFrame implements ActionListener, WindowListener {
    private String otsikko;
    private int numOfUsers;
    private int selectedUser = 0;
    private int selectedSchedule = 0;
    private int selectedState = 0;

    private ImageIcon logo;

    private String[] nimet;
    private String[] ajat;
    private String[] tila;
    private ArrayList<User> userList;
    private ArrayList<TimeTable> ttList;

    private JButton addUser;
    private JButton removeUser;
    private JButton saveData;

    private JPanel logoPanel;
    private JPanel dataPanel;
    private JLabel logoLabel;

    private JComboBox nimiValikko;
    private JComboBox aikaValikko;
    private JComboBox tilaValikko;

    private JTextField nimiData;
    private JTextField puhelinData;
    private JTextField kohdeData;

    private GsmGUI gui;

    public UserEditor(String otsikko, ArrayList<User> list, ArrayList<TimeTable> tt, GsmGUI gui) {

        // basic setup
        this.otsikko = otsikko;
        this.userList = list;
        this.ttList = tt;
        this.gui = gui;

        setTitle(this.otsikko);
        setSize(640, 480);
        setBackground(Color.white);
        setResizable(false);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

        // logo on top
        this.logoPanel = new JPanel();
        this.logoPanel.setLayout(new GridBagLayout());
        this.logo = gui.createImageIcon("images/bg.png");
        this.logoLabel = new JLabel();
        this.logoLabel.setIcon(this.logo);
        this.logoPanel.add(this.logoLabel);

        // input-datahandling
        this.numOfUsers = this.userList.size();
        this.nimet = new String[this.numOfUsers];

        this.ajat = new String[this.ttList.size()];

        for (int i = 0; i < this.numOfUsers; i++) {
            this.nimet[i] = this.userList.get(i).getName();
        }

        for (int i = 0; i < this.ttList.size(); i++) {
            this.ajat[i] = this.ttList.get(i).getName();
        }

        this.tila = new String[2];
        this.tila[0] = "Ei";
        this.tila[1] = "Kyllä";

        // comboboxes and anction listeners
        this.nimiValikko = new JComboBox(this.nimet);
        this.nimiValikko.addActionListener(this);

        this.aikaValikko = new JComboBox(this.ajat);
        this.aikaValikko.addActionListener(this);

        this.tilaValikko = new JComboBox(this.tila);
        this.tilaValikko.addActionListener(this);

        // userdata and buttons
        dataPanel = new JPanel();
        dataPanel.setLayout(new GridBagLayout());
        dataPanel.setBackground(Color.white);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        JLabel nimet = new JLabel(" Valitse käyttäjä ");
        JLabel ajat = new JLabel(" Valitse aikataulu ");

        JLabel nimi = new JLabel(" Nimi ");
        JLabel puhelin = new JLabel(" Puhelinnumero ");
        JLabel tunnus = new JLabel(" Kohdetunnus ");
        JLabel aika = new JLabel(" Aikaohjelma ");
        JLabel akti = new JLabel(" Aktiivinen ");

        if (numOfUsers > 0) {
            nimiData = new JTextField(this.nimet[0], 15);
            this.nimiData.setBackground(Color.getHSBColor(0.0f, 0.0f, 0.95f));
            puhelinData = new JTextField(this.userList.get(selectedUser).getNumber(), 15);
            this.puhelinData.setBackground(Color.getHSBColor(0.0f, 0.0f, 0.95f));
            kohdeData = new JTextField(this.userList.get(selectedUser).getId(), 15);
            this.kohdeData.setBackground(Color.getHSBColor(0.0f, 0.0f, 0.95f));

            //oletuksen tyhjä valinta
            this.aikaValikko.setSelectedIndex(-1);

            //valitaan oikea aikaohjelma, jos on listassa
            for (int i = 0; i < aikaValikko.getItemCount(); i++) {
                if (this.userList.get(selectedUser).getTimeTableId().equals(tt.get(i).getId())) {
                    this.aikaValikko.setSelectedIndex(i);
                    this.selectedSchedule = i;
                }
            }

            if (this.userList.get(selectedUser).getState()) {
                this.tilaValikko.setSelectedIndex(1);
                this.selectedState = 1;
            } else {
                this.tilaValikko.setSelectedIndex(0);
                this.selectedState = 0;
            }
        } else {
            this.nimiData = new JTextField(" ", 15);
            this.nimiData.setBackground(Color.getHSBColor(0.0f, 0.0f, 0.95f));
            this.nimiData.setEditable(false);
            this.puhelinData = new JTextField(" ", 15);
            this.puhelinData.setBackground(Color.getHSBColor(0.0f, 0.0f, 0.95f));
            this.puhelinData.setEditable(false);
            this.kohdeData = new JTextField(" ", 15);
            this.kohdeData.setBackground(Color.getHSBColor(0.0f, 0.0f, 0.95f));
            this.kohdeData.setEditable(false);
        }

        this.addUser = new JButton("Lisää käyttäjä");
        this.removeUser = new JButton("Poista käyttäjä");
        this.saveData = new JButton("Tallenna muutokset");

        if (numOfUsers == 0) {
            this.removeUser.setEnabled(false);
            this.saveData.setEnabled(false);
        }

        this.addUser.addActionListener(this);
        this.removeUser.addActionListener(this);
        this.saveData.addActionListener(this);

        this.addUser.setActionCommand("add");
        this.removeUser.setActionCommand("remove");
        this.saveData.setActionCommand("save");
        this.nimiValikko.setActionCommand("userSelect");
        this.aikaValikko.setActionCommand("scheduleSelect");
        this.tilaValikko.setActionCommand("stateSelect");

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
        dataPanel.add(nimet, c);

        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;

        dataPanel.add(nimiValikko, c);

        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        dataPanel.add(new JLabel(" "), c);

        c.gridx = 3;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(addUser, c);

        c.gridx = 4;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(removeUser, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 5;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.insets = new Insets(22, 2, 22, 2);
        dataPanel.add(new JSeparator(), c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.insets = new Insets(2, 2, 2, 2);
        dataPanel.add(nimi, c);

        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(nimiData, c);

        c.gridx = 3;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(aika, c);

        c.gridx = 4;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(aikaValikko, c);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(puhelin, c);

        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(puhelinData, c);

        c.gridx = 3;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(akti, c);

        c.gridx = 4;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(tilaValikko, c);

        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(tunnus, c);

        c.gridx = 1;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(kohdeData, c);

        c.gridx = 1;
        c.gridy = 6;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        dataPanel.add(new JLabel(" "), c);

        c.gridx = 4;
        c.gridy = 7;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(saveData, c);

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
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("userSelect")) {
            JComboBox cb = (JComboBox) e.getSource();
            this.selectedUser = cb.getSelectedIndex();
            this.updateFieldData();
        }

        if (e.getActionCommand().equals("scheduleSelect")) {
            JComboBox cb = (JComboBox) e.getSource();
            this.selectedSchedule = cb.getSelectedIndex();
        }

        if (e.getActionCommand().equals("stateSelect")) {
            JComboBox cb = (JComboBox) e.getSource();
            this.selectedState = cb.getSelectedIndex();
        }

        if (e.getActionCommand().equals("add")) {
            if (this.userList.size() == 0) {
                this.removeUser.setEnabled(true);
                this.saveData.setEnabled(true);
            }

            this.userList.add(new User("Käyttäjä #" + (userList.size() + 1), "+358401234567", ttList.get(0), "Kohteen tunnus", true));
            this.nimiValikko.addItem("Käyttäjä #" + userList.size());
            this.nimiValikko.setSelectedIndex(userList.size() - 1);
            this.tilaValikko.setSelectedIndex(1);
            this.updateFieldData();
        }

        if (e.getActionCommand().equals("save")) {
            this.userList.get(selectedUser).changeName(this.nimiData.getText());
            this.userList.get(selectedUser).changeNumber(this.puhelinData.getText());
            this.userList.get(selectedUser).changeId(this.kohdeData.getText());

            for (int i = 0; i < ttList.size(); i++) {
                if (this.aikaValikko.getItemAt(this.selectedSchedule).equals(this.ttList.get(i).getName()))
                    this.userList.get(this.selectedUser).changeSchedule(this.ttList.get(i));
            }
            //this.userList.get(selectedUser).changeSchedule(ttList.get(selectedSchedule));

            if (this.selectedState == 0)
                this.userList.get(selectedUser).changeState(false);
            else
                this.userList.get(selectedUser).changeState(true);

            if (this.userList.size() > 0) {
                numOfUsers = this.userList.size();
                this.nimet = new String[numOfUsers];
                this.nimiValikko.removeAllItems();

                for (int i = 0; i < numOfUsers; i++) {
                    this.nimet[i] = this.userList.get(i).getName();
                    this.nimiValikko.addItem(this.nimet[i]);
                }
                this.updateFieldData();
            }
            gui.fileIO.saveUserList(this.userList);

        }

        if (e.getActionCommand().equals("remove")) {
            int temp;
            int n;

            Object[] options = {"Kyllä", "Ei"};

            if (this.userList.size() > 0) {
                // varmistetaan ettei tule valinta mene negatiiviseksi kun poistetaan
                // listan ensimmäinen käyttäjä
                if (selectedUser > 0) {
                    temp = selectedUser - 1;
                } else {
                    temp = selectedUser;
                }

                n = JOptionPane.showOptionDialog(this,
                        "Halutako poistaa k�ytt�j�n " + this.userList.get(selectedUser).getName()
                                + " tiedot?",
                        "Varoitus!",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,        //do not use a custom Icon
                        options,     //the titles of buttons
                        options[0]); //default button title

                if (n == 0) {
                    this.userList.remove(selectedUser);
                    //this.nimiValikko.removeItemAt(selectedUser);

                    numOfUsers = this.userList.size();
                    this.nimet = new String[numOfUsers];
                    this.nimiValikko.removeAllItems();

                    for (int i = 0; i < numOfUsers; i++) {
                        this.nimet[i] = this.userList.get(i).getName();
                        this.nimiValikko.addItem(this.nimet[i]);
                    }

                    // jos lista tyhjä, otetaan arvolla -1 kenttä tyhjäksi
                    if (this.userList.size() > 0) {
                        this.nimiValikko.setSelectedIndex(temp);
                    } else {
                        this.nimiValikko.setSelectedIndex(-1);
                    }

                    this.updateFieldData();
                }
            }

            if (this.userList.size() == 0) {
                this.saveData.setEnabled(false);
                this.removeUser.setEnabled(false);
                this.updateFieldData();
            }
        }
    }

    public void updateFieldData() {
        String temp;
        if (this.userList.size() > 0 && selectedUser < this.userList.size() && selectedUser >= 0) {
            this.nimiData.setText(this.userList.get(selectedUser).getName());
            this.nimiData.setEditable(true);

            this.puhelinData.setText(this.userList.get(selectedUser).getNumber());
            this.puhelinData.setEditable(true);

            this.kohdeData.setText(this.userList.get(selectedUser).getId());
            this.kohdeData.setEditable(true);

            if (this.userList.get(selectedUser).getTimeTable() == null)
                temp = "";
            else
                temp = this.userList.get(selectedUser).getTimeTable().getName();

            selectedSchedule = -1;

            for (int i = 0; i < this.ttList.size(); i++) {
                if (temp.equals(ttList.get(i).getName())) {
                    selectedSchedule = i;
                }
            }

            this.aikaValikko.setSelectedIndex(selectedSchedule);

            if (this.userList.get(selectedUser).getState())
                this.tilaValikko.setSelectedIndex(1);
            else
                this.tilaValikko.setSelectedIndex(0);
        } else {
            this.nimiData.setText("");
            this.nimiData.setEditable(false);

            this.puhelinData.setText("");
            this.puhelinData.setEditable(false);

            this.kohdeData.setText("");
            this.kohdeData.setEditable(false);

            this.aikaValikko.setSelectedIndex(-1);
            this.tilaValikko.setSelectedIndex(-1);
        }

        gui.updateUserTable();
    }

    public void windowActivated(WindowEvent arg0) {
    }

    public void windowClosed(WindowEvent arg0) {
    }

    public void windowClosing(WindowEvent arg0) {
        gui.userEdit.setEnabled(true);
        this.setVisible(false);
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
}
