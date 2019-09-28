/**
 * <CODE>ScheduleEditor</CODE>
 * <p>
 * Opens a frame with possibility edit schedules defined in TimeTable class.
 * <p>
 * ChangeLog:
 * <p>
 * v0.10 - Initial version (30.09.2009)
 * v0.20 - Basic layout, still unable to modify data (3.2.2010)
 * v0.21 - Minor modifications to visuals (5.2.2010)
 * v0.30 - finally shows dayschedules, possibility to browse timetables
 * and visually more appealing look (3.6.2010)
 * v0.40 - shows schedules on timetable (rectangles) (10.6.2010)
 * v0.45 - fixed table selection bug, now the rectangles won't disappear when table
 * is clicked (16.6.2010)
 * v0.50 - it is now possible to add dayschedules to timetable (17.6.2010)
 * v0.51 - it is now possible to remove dayschedules from timetable (19.6.2010)
 * <p>
 * TODO:
 * - add functionality
 *
 * @author Juha-Matti Sironen
 * @version 0.51
 * @date 19.6.2010
 */

package AreGsmAlarm.editors;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.RoundRectangle2D;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.WindowConstants;
//import javax.swing.event.TableModelEvent;

import AreGsmAlarm.DaySchedule;
import AreGsmAlarm.GsmGUI;
import AreGsmAlarm.TimeTable;
import AreGsmAlarm.User;

public class ScheduleEditor extends JFrame implements ActionListener, WindowListener, MouseListener {

    private String otsikko;

    private int selectedSchedule = 0;
    private int selectedDaySchedule = 0;
    private int selectedDay = 2;

    private ImageIcon logo;

    private String[] ajat;
    private String[] ohjelmat;

    // spinnerit
    private Calendar alkuAika;
    private Calendar loppuAika;

    private ArrayList<TimeTable> ttList;
    //private ArrayList<DaySchedule> dsList;

    private JButton addSchedule;
    private JButton removeSchedule;
    private JButton saveData;
    private JButton addDaySchedule;
    private JButton removeDaySchedule;

    private JTextField aikaKentta;

    private JPanel logoPanel;
    private JPanel dataPanel;
    private JPanel timePanel;
    private JPanel bottomPanel;

    private JLabel logoLabel;

    private JComboBox aikaValikko;
    private JComboBox paivaValikko;
    private JComboBox ohjelmaValikko;

    private JSpinner spinnerAlku;
    private JSpinner spinnerLoppu;

    // tables
    private JTable timeTable;
    //private TableModelEvent timeEvent = null;

    private JTable clockTable;
    //private TableModelEvent Event = null;

    //table data
    private String[] weekDays = {"Maanantai",
            "Tiistai",
            "Keskiviikko",
            "Torstai",
            "Perjantai",
            "Lauantai",
            "Sunnuntai"};

    private String[] clock = {"Kello"};

    private Object[][] tableData = new Object[0][0];
    private Object[][] clockData = new Object[0][0];

    private GsmGUI gui;

    private DaySchedule ds;

    public ScheduleEditor(String otsikko, ArrayList<User> list, ArrayList<TimeTable> tt, GsmGUI gui) {
        this.otsikko = otsikko;
        this.ttList = tt;
        this.gui = gui;

        setTitle(this.otsikko);
        setSize(700, 780);
        setBackground(Color.white);
        setResizable(false);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

        // logo on top
        this.logoPanel = new JPanel();
        this.logoPanel.setLayout(new GridBagLayout());
        this.logo = gui.createImageIcon("images/bg_700.png");
        this.logoLabel = new JLabel();
        this.logoLabel.setIcon(this.logo);
        this.logoPanel.add(this.logoLabel);

        // luodaan kellotaulu
        this.clockData = new Object[24][1];
        for (int i = 0; i < 24; i++) {
            this.clockData[i][0] = i + ":00";
        }

        this.tableData = new Object[48][7];
        for (int i = 0; i < 48; i++) {
            this.tableData[i][0] = "";
        }

        this.clockTable = new JTable(this.clockData, this.clock);
        this.clockTable.setCellSelectionEnabled(false);
        this.clockTable.setDragEnabled(false);
        this.clockTable.setRowSelectionAllowed(false);
        this.clockTable.setFillsViewportHeight(true);
        this.clockTable.setRowHeight(16);

        this.clockTable.getTableHeader().setReorderingAllowed(false);
        this.clockTable.getTableHeader().setResizingAllowed(false);

        this.timeTable = new JTable(this.tableData, this.weekDays);
        this.timeTable.setColumnSelectionAllowed(false);
        this.timeTable.setRowSelectionAllowed(false);
        this.timeTable.setCellSelectionEnabled(false);
        this.timeTable.setDragEnabled(false);
        this.timeTable.setEnabled(false);
        this.timeTable.setFillsViewportHeight(true);
        this.timeTable.setRowHeight(8);
        this.timeTable.setFont(new Font("Helvetica", Font.PLAIN, 8));

        this.timeTable.setFocusable(false);

        this.timeTable.getTableHeader().setReorderingAllowed(false);
        this.timeTable.getTableHeader().setResizingAllowed(false);

        // input-datahandling
        this.ajat = new String[this.ttList.size()];

        for (int i = 0; i < this.ttList.size(); i++) {
            this.ajat[i] = this.ttList.get(i).getName();
        }

        // comboboxes and action listeners
        this.aikaValikko = new JComboBox(this.ajat);
        this.aikaValikko.addActionListener(this);

        if (this.ttList.size() > 0) {
            this.selectedSchedule = 0;
            this.aikaValikko.setSelectedIndex(0);

            this.ds = this.ttList.get(0).getDaySchedule("monday");

            int temp;
            temp = this.ds.numberOfSchedules();

            if (temp > 0) {
                this.ohjelmat = new String[temp];
                for (int i = 0; i < temp; i++) {
                    this.ohjelmat[i] = "Ohjelma #" + (i + 1);
                }
            }
        }

        this.paivaValikko = new JComboBox(this.weekDays);
        this.paivaValikko.addActionListener(this);

        this.ohjelmaValikko = new JComboBox(this.ohjelmat);
        this.ohjelmaValikko.addActionListener(this);

        // aikaspinnerit
        this.alkuAika = new GregorianCalendar(2000, Calendar.JANUARY, 1);
        this.loppuAika = new GregorianCalendar(2000, Calendar.JANUARY, 1);

        this.alkuAika.set(Calendar.HOUR_OF_DAY, this.ds.getOnHour(0));
        this.alkuAika.set(Calendar.MINUTE, this.ds.getOnMinute(0));

        this.loppuAika.set(Calendar.HOUR_OF_DAY, this.ds.getOffHour(0));
        this.loppuAika.set(Calendar.MINUTE, this.ds.getOffMinute(0));

        SpinnerModel modelAlku = new SpinnerDateModel(this.alkuAika.getTime(), null, null, 0);
        this.spinnerAlku = new JSpinner(modelAlku);

        SpinnerModel modelLoppu = new SpinnerDateModel(this.loppuAika.getTime(), null, null, 0);
        this.spinnerLoppu = new JSpinner(modelLoppu);

        JComponent editorAlku = new JSpinner.DateEditor(this.spinnerAlku, "HH:mm");
        JComponent editorLoppu = new JSpinner.DateEditor(this.spinnerLoppu, "HH:mm");

        this.spinnerAlku.setEditor(editorAlku);
        this.spinnerLoppu.setEditor(editorLoppu);

        this.spinnerAlku.setValue(this.alkuAika.getTime());
        this.spinnerLoppu.setValue(this.loppuAika.getTime());

        // userdata and buttons
        this.dataPanel = new JPanel();
        this.dataPanel.setLayout(new GridBagLayout());
        this.dataPanel.setBackground(Color.white);

        this.timePanel = new JPanel();

        this.timePanel.setLayout(new GridBagLayout());
        this.timePanel.setBackground(Color.white);

        this.bottomPanel = new JPanel();
        this.bottomPanel.setLayout(new GridBagLayout());
        this.bottomPanel.setBackground(Color.white);

        GridBagConstraints t = new GridBagConstraints();
        t.fill = GridBagConstraints.BOTH;

        GridBagConstraints bott = new GridBagConstraints();
        bott.fill = GridBagConstraints.BOTH;

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        JLabel valinta = new JLabel(" Valitse aikataulu ");

        this.addSchedule = new JButton("Luo uusi aikataulu");
        this.removeSchedule = new JButton("Poista aikataulu");
        this.saveData = new JButton("Tallenna muutokset");

        this.addDaySchedule = new JButton("Luo uusi päiväohjelma");
        this.removeDaySchedule = new JButton("Poista päiväohjelma");

        if (this.ttList.size() == 0) {
            this.removeSchedule.setEnabled(false);
            this.saveData.setEnabled(false);
        }

        this.addSchedule.addActionListener(this);
        this.removeSchedule.addActionListener(this);
        this.saveData.addActionListener(this);

        this.addDaySchedule.addActionListener(this);
        this.removeDaySchedule.addActionListener(this);

        this.addSchedule.setActionCommand("addSchedule");
        this.removeSchedule.setActionCommand("removeSchedule");
        this.saveData.setActionCommand("saveSchedule");

        this.addDaySchedule.setActionCommand("addDaySchedule");
        this.removeDaySchedule.setActionCommand("removeDaySchedule");

        this.aikaValikko.setActionCommand("scheduleSelect");
        this.paivaValikko.setActionCommand("daySelect");
        this.ohjelmaValikko.setActionCommand("daySchedSelect");

        this.aikaKentta = new JTextField();

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.insets = new Insets(12, 2, 14, 2);
        this.dataPanel.add(valinta, c);

        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.insets = new Insets(12, 2, 14, 2);
        this.dataPanel.add(this.aikaValikko, c);

        c.gridx = 3;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        this.dataPanel.add(new JLabel(" "), c);

        c.gridx = 4;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        this.dataPanel.add(this.addSchedule, c);

        c.gridx = 5;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        this.dataPanel.add(this.removeSchedule, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 6;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.insets = new Insets(2, 2, 2, 2);
        this.dataPanel.add(new JSeparator(), c);

        // aikapaneeli
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 6;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.insets = new Insets(0, 0, 0, 0);
        this.dataPanel.add(this.timePanel, c);

        // kello
        t.gridx = 0;
        t.gridy = 0;
        t.gridwidth = 1;
        t.gridheight = 1;
        t.weightx = 0.0;
        t.weighty = 0.0;
        t.insets = new Insets(0, 0, 0, 0);
        this.timePanel.add(this.clockTable.getTableHeader(), t);

        t.gridx = 0;
        t.gridy = 1;
        t.gridwidth = 1;
        t.gridheight = 1;
        t.weightx = 0.0;
        t.weighty = 0.0;
        this.timePanel.add(this.clockTable, t);

        // aikataulu
        t.gridx = 1;
        t.gridy = 0;
        t.gridwidth = 9;
        t.gridheight = 1;
        t.weightx = 1.0;
        t.weighty = 0.0;
        this.timePanel.add(this.timeTable.getTableHeader(), t);

        t.gridx = 1;
        t.gridy = 1;
        t.gridwidth = 9;
        t.gridheight = 1;
        t.weightx = 1.0;
        t.weighty = 0.0;
        this.timePanel.add(this.timeTable, t);

        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 6;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.insets = new Insets(2, 2, 2, 2);
        this.dataPanel.add(new JSeparator(), c);

        //pohjapaneeli
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = 6;
        c.gridheight = 5;
        c.weightx = 1.0;
        c.weighty = 1.0;
        this.dataPanel.add(this.bottomPanel, c);

        bott.gridx = 0;
        bott.gridy = 1;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        bott.insets = new Insets(12, 2, 2, 2);
        this.bottomPanel.add(new JLabel(" Aikataulu"), bott);

        bott.gridx = 1;
        bott.gridy = 1;
        bott.gridwidth = 2;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        this.bottomPanel.add(this.aikaKentta, bott);

        bott.gridx = 0;
        bott.gridy = 2;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        bott.insets = new Insets(2, 2, 2, 2);
        this.bottomPanel.add(new JLabel(" Päivä"), bott);

        bott.gridx = 1;
        bott.gridy = 2;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        this.bottomPanel.add(this.paivaValikko, bott);

        bott.gridx = 0;
        bott.gridy = 3;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        this.bottomPanel.add(new JLabel(" Päiväohjelma"), bott);

        bott.gridx = 1;
        bott.gridy = 3;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        this.bottomPanel.add(this.ohjelmaValikko, bott);

        bott.gridx = 0;
        bott.gridy = 4;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        this.bottomPanel.add(new JLabel(" Aloitusaika"), bott);

        bott.gridx = 1;
        bott.gridy = 4;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        this.bottomPanel.add(this.spinnerAlku, bott);

        bott.gridx = 2;
        bott.gridy = 3;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        this.bottomPanel.add(this.addDaySchedule, bott);

        bott.gridx = 0;
        bott.gridy = 5;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        this.bottomPanel.add(new JLabel(" Lopetusaika"), bott);

        bott.gridx = 1;
        bott.gridy = 5;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        this.bottomPanel.add(this.spinnerLoppu, bott);

        bott.gridx = 2;
        bott.gridy = 4;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        this.bottomPanel.add(this.removeDaySchedule, bott);

        bott.gridx = 2;
        bott.gridy = 5;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        this.bottomPanel.add(this.saveData, bott);

        bott.gridx = 0;
        bott.gridy = 6;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        this.bottomPanel.add(new JLabel(" "), bott);

        this.add(this.logoPanel);
        this.add(this.dataPanel);

        this.getContentPane().add(BorderLayout.NORTH, this.logoPanel);
        this.getContentPane().add(BorderLayout.CENTER, this.dataPanel);

        this.updateFieldData();
        this.timeTable.addMouseListener(this);

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("scheduleSelect")) {
            JComboBox cb = (JComboBox) e.getSource();
            this.selectedSchedule = cb.getSelectedIndex();
            System.out.println("[scheduleSelect action]");
            this.paivaValikko.setSelectedIndex(0);
            this.updateFieldData();
            this.repaint();
        }

        if (e.getActionCommand().equals("daySelect")) {
            JComboBox cb = (JComboBox) e.getSource();
            this.selectedDay = cb.getSelectedIndex() + 2;

            System.out.println("[daySelect action]");

            if (this.selectedDay == 8)
                this.selectedDay = 1;

            if (this.ohjelmaValikko.getItemCount() > 0)
                this.ohjelmaValikko.setSelectedIndex(0);
            else
                this.ohjelmaValikko.setSelectedIndex(-1);
        }

        if (e.getActionCommand().equals("daySchedSelect")) {
            JComboBox cb = (JComboBox) e.getSource();
            this.selectedDaySchedule = cb.getSelectedIndex();
            System.out.println("[daySchedSelect action]");
            System.out.println("Selected dayschedule: " + this.selectedDaySchedule);
            this.updateDaySchedData();
        }

        if (e.getActionCommand().equals("addSchedule")) {
            if (this.ttList.size() == 0) {
                this.removeSchedule.setEnabled(false);
                this.saveData.setEnabled(true);
            }

            if (this.ttList.size() > 0) {
                this.removeSchedule.setEnabled(true);
            }

            this.ttList.add(new TimeTable("Aikataulu #" + (this.ttList.size() + 1), String.valueOf(Calendar.getInstance().getTimeInMillis())));

            this.aikaValikko.addItem("Aikataulu #" + this.ttList.size());
            this.aikaValikko.setSelectedIndex(this.ttList.size() - 1);
            this.selectedSchedule = this.aikaValikko.getSelectedIndex();
            //this.selectedDay = 2; //monday
            this.paivaValikko.setSelectedIndex(0);
            this.updateFieldData();
        }

        if (e.getActionCommand().equals("addDaySchedule")) {
            DaySchedule newSched;
            String day = null;
            int daynum = 0;

            this.ds = this.ttList.get(this.selectedSchedule).getDaySchedule(this.selectedDay);
            //System.out.println("Päivässä " + this.ds.getName() + " on " + this.ds.numberOfSchedules() + "päiväohjelmaa");

            if (this.ds != null) {
                this.ttList.get(this.selectedSchedule).getDaySchedule(this.selectedDay).addSchedule("0:00", "1:00", "1");
            } else {
                System.out.println("Valittu päivä: " + this.selectedDay);
                switch (this.selectedDay) {
                    case 1:
                        day = "sunday";
                        daynum = 1;
                        break;
                    case 2:
                        day = "monday";
                        daynum = 2;
                        break;
                    case 3:
                        day = "tuesday";
                        daynum = 3;
                        break;
                    case 4:
                        day = "wednesday";
                        daynum = 4;
                        break;
                    case 5:
                        day = "thursday";
                        daynum = 5;
                        break;
                    case 6:
                        day = "friday";
                        daynum = 6;
                        break;
                    case 7:
                        day = "saturday";
                        daynum = 7;
                        break;
                }

                newSched = new DaySchedule(day);
                newSched.addSchedule("0:00", "1:00", "1");
                newSched.setDay(daynum);

                this.ttList.get(this.selectedSchedule).addDaySchedule(newSched);
                System.out.println("Lisätään ohjelmaan: " + this.ttList.get(this.selectedSchedule).getName()
                        + " uusi päiväohjelma päivälle " + day);
            }

            this.updateDaySchedData();
            this.repaint();
        }

        if (e.getActionCommand().equals("removeDaySchedule")) {
            String day = null;
            int daynum = 0;

            this.ds = this.ttList.get(this.selectedSchedule).getDaySchedule(this.selectedDay);

            if (this.ds != null) {
                this.ds.removeSchedule(this.selectedDaySchedule);
            }

            this.updateDaySchedData();
            this.repaint();
        }

        if (e.getActionCommand().equals("saveSchedule")) {
            String onh;
            String offh;
            String onm;
            String offm;

            Date alkuAika;
            Date loppuAika;

            this.ttList.get(this.selectedSchedule).setName(this.aikaKentta.getText());


            if (this.ttList.get(this.selectedSchedule).getDaySchedule(this.selectedDay) != null) {
                alkuAika = (Date) this.spinnerAlku.getValue();
                loppuAika = (Date) this.spinnerLoppu.getValue();

                this.alkuAika.setTimeInMillis(alkuAika.getTime());
                this.loppuAika.setTimeInMillis(loppuAika.getTime());

                onh = Integer.toString(this.alkuAika.get(Calendar.HOUR_OF_DAY));
                onm = Integer.toString(this.alkuAika.get(Calendar.MINUTE));
                offh = Integer.toString(this.loppuAika.get(Calendar.HOUR_OF_DAY));
                offm = Integer.toString(this.loppuAika.get(Calendar.MINUTE));

                this.ttList.get(this.selectedSchedule).getDaySchedule(this.selectedDay).setOnTime(this.selectedDaySchedule, (onh + ":" + onm));
                this.ttList.get(this.selectedSchedule).getDaySchedule(this.selectedDay).setOffTime(this.selectedDaySchedule, (offh + ":" + offm));

            }

            this.repaint();
            //gui.fileIO.saveUserList(this.userList);
        }

        if (e.getActionCommand().equals("remove")) {
            /*
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
                        "Halutako poistaa käyttäjän " + this.userList.get(selectedUser).getName()
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


                    // jos lista tyhj�, otetaan arvolla -1 kentt� tyhj�ksi
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
        */
    }

    public void updateFieldData() {
        System.out.println("[updateFieldData]");
        if (this.ttList.size() > 0 && this.selectedSchedule < this.ttList.size() && this.selectedSchedule >= 0) {
            this.aikaKentta.setText(this.ttList.get(this.selectedSchedule).getName());
            this.aikaKentta.setEditable(true);
            this.repaint();
        } else {
            this.aikaKentta.setText("Ei aikaohjelmaa");
            this.aikaKentta.setEditable(false);
            this.repaint();
        }
    }

    public void updateDaySchedData() {
        int count;

        this.ds = this.ttList.get(this.selectedSchedule).getDaySchedule(this.selectedDay);

        if (this.ds != null) {
            System.out.println("[updateDaySchedData]");
            System.out.println("Number of schedules in " + this.ds.getName() + ": " + this.ds.numberOfSchedules());

            count = this.ohjelmaValikko.getItemCount();

            if (this.ds.numberOfSchedules() > count) {
                while (count < this.ds.numberOfSchedules()) {
                    this.ohjelmaValikko.addItem("Ohjelma #" + (count + 1));
                    count++;
                }
                this.ohjelmaValikko.setSelectedIndex(0);
                this.selectedDaySchedule = 0;
            }

            if (this.ds.numberOfSchedules() < count && this.ds.numberOfSchedules() > 0) {
                this.ohjelmaValikko.removeAllItems();
                count = 0;

                while (count < this.ds.numberOfSchedules()) {
                    this.ohjelmaValikko.addItem("Ohjelma #" + (count + 1));
                    count++;
                }
                this.ohjelmaValikko.setSelectedIndex(0);
                this.selectedDaySchedule = 0;
            }

            if (this.ds.numberOfSchedules() > 0) {
                this.alkuAika.set(Calendar.HOUR_OF_DAY, this.ds.getOnHour(this.selectedDaySchedule));
                this.alkuAika.set(Calendar.MINUTE, this.ds.getOnMinute(this.selectedDaySchedule));

                this.loppuAika.set(Calendar.HOUR_OF_DAY, this.ds.getOffHour(this.selectedDaySchedule));
                this.loppuAika.set(Calendar.MINUTE, this.ds.getOffMinute(this.selectedDaySchedule));

                this.spinnerAlku.setValue(this.alkuAika.getTime());
                this.spinnerLoppu.setValue(this.loppuAika.getTime());
            } else {
                this.alkuAika.set(Calendar.HOUR_OF_DAY, 0);
                this.alkuAika.set(Calendar.MINUTE, 0);

                this.loppuAika.set(Calendar.HOUR_OF_DAY, 0);
                this.loppuAika.set(Calendar.MINUTE, 0);

                this.spinnerAlku.setValue(this.alkuAika.getTime());
                this.spinnerLoppu.setValue(this.loppuAika.getTime());

                System.out.println("Ei päiväohjelmia");
                this.ohjelmaValikko.removeAllItems();
                this.selectedDaySchedule = -1;
            }

        } else {
            this.alkuAika.set(Calendar.HOUR_OF_DAY, 0);
            this.alkuAika.set(Calendar.MINUTE, 0);

            this.loppuAika.set(Calendar.HOUR_OF_DAY, 0);
            this.loppuAika.set(Calendar.MINUTE, 0);

            this.spinnerAlku.setValue(this.alkuAika.getTime());
            this.spinnerLoppu.setValue(this.loppuAika.getTime());

            System.out.println("Ei päiväohjelmia (ds = null)");
            this.ohjelmaValikko.removeAllItems();
            this.selectedDaySchedule = -1;
        }
    }

    public void windowActivated(WindowEvent arg0) {
        this.repaint();
    }

    public void windowClosed(WindowEvent arg0) {
    }

    public void windowClosing(WindowEvent arg0) {
        this.gui.scheduleEdit.setEnabled(true);
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

    public void paint(Graphics g) {
        int scheds = 0;
        int i, j, num = 0, count = 0;
        int starth, endh, startm, endm, day;
        double width;

        ArrayList<DaySchedule> dsList = null;
        DaySchedule ds = null;

        Color c, s;
        RoundRectangle2D[] rect = null;

        super.paint(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        System.out.println("Aikapaneelin y: " + this.timePanel.getY());

        double y = this.logoPanel.getHeight() + this.timePanel.getY() + this.timeTable.getRowHeight() + 43.0;
        double x = this.clockTable.getWidth() + 3.0;

        width = ((this.timeTable.getWidth() / 7.0));

        if (this.ttList.size() > 0) {
            dsList = this.ttList.get(this.selectedSchedule).getDaySchedules();

            if (dsList != null) {
                num = dsList.size();

                for (i = 0; i < num; i++) {
                    scheds = scheds + dsList.get(i).numberOfSchedules();
                }
                System.out.println("Total scheds for: " + this.ttList.get(this.selectedSchedule).getName() + " is: " + scheds);
                rect = new RoundRectangle2D[scheds];
            }
        }

        if (scheds > 0) {
            count = 0;
            for (i = 0; i < num; i++) {
                ds = dsList.get(i);
                day = ds.getDay();

                if (day == 1)
                    day = 6;
                else
                    day = day - 2;

                for (j = 0; j < dsList.get(i).numberOfSchedules(); j++) {
                    starth = ds.getOnHour(j);
                    endh = ds.getOffHour(j);
                    startm = ds.getOnMinute(j);
                    endm = ds.getOffMinute(j);
                    if (startm >= 0 && startm < 15)
                        startm = 0;
                    if (startm >= 15 && startm < 45)
                        startm = 8;
                    if (startm >= 45 && startm <= 59)
                        startm = 16;

                    if (endm >= 0 && endm < 15)
                        endm = 0;
                    if (endm >= 15 && endm < 45)
                        endm = 8;
                    if (endm >= 45 && endm <= 59)
                        endm = 16;

                    rect[count] = new RoundRectangle2D.Double((x + (day * width)), ((starth * 16.0) + y + startm), width, ((endh - starth) * 16.0 + endm - startm), 10.0, 10.0);

                    g.drawString("Ohjelma #" + (j + 1), (int) (x + (day * width) + 3), (int) ((starth * 16.0) + y + startm + 13));
                    count++;
                }
            }
        }

        c = Color.getHSBColor(0.58f, 1.0f, 0.75f);
        s = Color.getHSBColor(0.58f, 1.0f, 0.15f);

        AlphaComposite myAlpha = AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 0.6f);
        g2.setComposite(myAlpha);

        g2.setPaint(s);
        g2.setStroke(new BasicStroke(2));
        g2.setColor(c);

        for (i = 0; i < scheds; i++) {
            g2.fill(rect[i]);
            g2.draw(rect[i]);
        }

    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent arg0) {
    }

    public void mouseReleased(MouseEvent arg0) {
    }
}
