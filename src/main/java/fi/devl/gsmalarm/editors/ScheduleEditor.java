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

package fi.devl.gsmalarm.editors;

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
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.WindowConstants;

import fi.devl.gsmalarm.GsmGUI;
import fi.devl.gsmalarm.domain.TimeTable;
import fi.devl.gsmalarm.domain.User;
import fi.devl.gsmalarm.domain.DaySchedule;
import org.apache.log4j.Logger;

public class ScheduleEditor extends JFrame implements ActionListener, WindowListener, MouseListener {
    final static Logger log = Logger.getLogger(ScheduleEditor.class);

    private int selectedSchedule = 0;
    private int selectedDaySchedule = 0;
    private int selectedDay = 2;

    private String[] ohjelmat;

    private Calendar alkuAika;
    private Calendar loppuAika;

    private ArrayList<TimeTable> ttList;

    private JButton removeSchedule;
    private JButton saveData;

    private JTextField aikaKentta;

    private JPanel logoPanel;
    private JPanel timePanel;

    private JComboBox aikaValikko;
    private JComboBox paivaValikko;
    private JComboBox ohjelmaValikko;

    private JSpinner spinnerAlku;
    private JSpinner spinnerLoppu;

    private JTable timeTable;
    private JTable clockTable;

    private GsmGUI gui;

    private DaySchedule ds;

    public ScheduleEditor(String otsikko, ArrayList<User> list, ArrayList<TimeTable> tt, GsmGUI gui) {
        this.ttList = tt;
        this.gui = gui;

        setTitle(otsikko);
        setSize(700, 780);
        setBackground(Color.white);
        setResizable(false);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

        // logo on top
        this.logoPanel = new JPanel();
        this.logoPanel.setLayout(new GridBagLayout());
        ImageIcon logo = gui.createImageIcon("images/bg_700.png");
        JLabel logoLabel = new JLabel();
        logoLabel.setIcon(logo);
        this.logoPanel.add(logoLabel);

        // luodaan kellotaulu
        Object[][] clockData = new Object[24][1];
        for (int i = 0; i < 24; i++) {
            clockData[i][0] = i + ":00";
        }

        Object[][] tableData = new Object[48][7];
        for (int i = 0; i < 48; i++) {
            tableData[i][0] = "";
        }

        String[] clock = {"Kello"};
        this.clockTable = new JTable(clockData, clock);
        this.clockTable.setCellSelectionEnabled(false);
        this.clockTable.setDragEnabled(false);
        this.clockTable.setRowSelectionAllowed(false);
        this.clockTable.setFillsViewportHeight(true);
        this.clockTable.setRowHeight(16);

        this.clockTable.getTableHeader().setReorderingAllowed(false);
        this.clockTable.getTableHeader().setResizingAllowed(false);

        String[] weekDays = {"Maanantai", "Tiistai", "Keskiviikko", "Torstai", "Perjantai", "Lauantai", "Sunnuntai"};

        this.timeTable = new JTable(tableData, weekDays);
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

        String[] ajat = new String[this.ttList.size()];

        for (int i = 0; i < this.ttList.size(); i++) {
            ajat[i] = this.ttList.get(i).getName();
        }

        this.aikaValikko = new JComboBox(ajat);
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

        this.paivaValikko = new JComboBox(weekDays);
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
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new GridBagLayout());
        dataPanel.setBackground(Color.white);

        this.timePanel = new JPanel();

        this.timePanel.setLayout(new GridBagLayout());
        this.timePanel.setBackground(Color.white);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        bottomPanel.setBackground(Color.white);

        GridBagConstraints t = new GridBagConstraints();
        t.fill = GridBagConstraints.BOTH;

        GridBagConstraints bott = new GridBagConstraints();
        bott.fill = GridBagConstraints.BOTH;

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        JLabel valinta = new JLabel(" Valitse aikataulu ");

        JButton addSchedule = new JButton("Luo uusi aikataulu");
        this.removeSchedule = new JButton("Poista aikataulu");
        this.saveData = new JButton("Tallenna muutokset");

        JButton addDaySchedule = new JButton("Luo uusi päiväohjelma");
        JButton removeDaySchedule = new JButton("Poista päiväohjelma");

        if (this.ttList.size() == 0) {
            this.removeSchedule.setEnabled(false);
            this.saveData.setEnabled(false);
        }

        addSchedule.addActionListener(this);
        this.removeSchedule.addActionListener(this);
        this.saveData.addActionListener(this);

        addDaySchedule.addActionListener(this);
        removeDaySchedule.addActionListener(this);

        addSchedule.setActionCommand("addSchedule");
        this.removeSchedule.setActionCommand("removeSchedule");
        this.saveData.setActionCommand("saveSchedule");

        addDaySchedule.setActionCommand("addDaySchedule");
        removeDaySchedule.setActionCommand("removeDaySchedule");

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
        dataPanel.add(valinta, c);

        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.insets = new Insets(12, 2, 14, 2);
        dataPanel.add(this.aikaValikko, c);

        c.gridx = 3;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        dataPanel.add(new JLabel(" "), c);

        c.gridx = 4;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(addSchedule, c);

        c.gridx = 5;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        dataPanel.add(this.removeSchedule, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 6;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.insets = new Insets(2, 2, 2, 2);
        dataPanel.add(new JSeparator(), c);

        // aikapaneeli
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 6;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.insets = new Insets(0, 0, 0, 0);
        dataPanel.add(this.timePanel, c);

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
        dataPanel.add(new JSeparator(), c);

        //pohjapaneeli
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = 6;
        c.gridheight = 5;
        c.weightx = 1.0;
        c.weighty = 1.0;
        dataPanel.add(bottomPanel, c);

        bott.gridx = 0;
        bott.gridy = 1;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        bott.insets = new Insets(12, 2, 2, 2);
        bottomPanel.add(new JLabel(" Aikataulu"), bott);

        bott.gridx = 1;
        bott.gridy = 1;
        bott.gridwidth = 2;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        bottomPanel.add(this.aikaKentta, bott);

        bott.gridx = 0;
        bott.gridy = 2;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        bott.insets = new Insets(2, 2, 2, 2);
        bottomPanel.add(new JLabel(" Päivä"), bott);

        bott.gridx = 1;
        bott.gridy = 2;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        bottomPanel.add(this.paivaValikko, bott);

        bott.gridx = 0;
        bott.gridy = 3;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        bottomPanel.add(new JLabel(" Päiväohjelma"), bott);

        bott.gridx = 1;
        bott.gridy = 3;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        bottomPanel.add(this.ohjelmaValikko, bott);

        bott.gridx = 0;
        bott.gridy = 4;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        bottomPanel.add(new JLabel(" Aloitusaika"), bott);

        bott.gridx = 1;
        bott.gridy = 4;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        bottomPanel.add(this.spinnerAlku, bott);

        bott.gridx = 2;
        bott.gridy = 3;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        bottomPanel.add(addDaySchedule, bott);

        bott.gridx = 0;
        bott.gridy = 5;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        bottomPanel.add(new JLabel(" Lopetusaika"), bott);

        bott.gridx = 1;
        bott.gridy = 5;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        bottomPanel.add(this.spinnerLoppu, bott);

        bott.gridx = 2;
        bott.gridy = 4;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        bottomPanel.add(removeDaySchedule, bott);

        bott.gridx = 2;
        bott.gridy = 5;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        bottomPanel.add(this.saveData, bott);

        bott.gridx = 0;
        bott.gridy = 6;
        bott.gridwidth = 1;
        bott.gridheight = 1;
        bott.weightx = 0.0;
        bott.weighty = 0.0;
        bottomPanel.add(new JLabel(" "), bott);

        this.add(this.logoPanel);
        this.add(dataPanel);

        this.getContentPane().add(BorderLayout.NORTH, this.logoPanel);
        this.getContentPane().add(BorderLayout.CENTER, dataPanel);

        this.updateFieldData();
        this.timeTable.addMouseListener(this);

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("scheduleSelect")) {
            JComboBox cb = (JComboBox) e.getSource();
            this.selectedSchedule = cb.getSelectedIndex();
            log.debug("[scheduleSelect action]");
            this.paivaValikko.setSelectedIndex(0);
            this.updateFieldData();
            this.repaint();
        }

        if (e.getActionCommand().equals("daySelect")) {
            JComboBox cb = (JComboBox) e.getSource();
            this.selectedDay = cb.getSelectedIndex() + 2;

            log.debug("[daySelect action]");

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
            log.debug("daySchedSelect " + this.selectedDaySchedule);
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

            this.paivaValikko.setSelectedIndex(0);
            this.updateFieldData();
        }

        if (e.getActionCommand().equals("addDaySchedule")) {
            DaySchedule newSched;
            String day = null;
            int daynum = 0;

            this.ds = this.ttList.get(this.selectedSchedule).getDaySchedule(this.selectedDay);

            if (this.ds != null) {
                this.ttList.get(this.selectedSchedule).getDaySchedule(this.selectedDay).addSchedule("0:00", "1:00", "1");
            } else {
                log.debug("Valittu päivä: " + this.selectedDay);
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
                log.debug("Lisätään ohjelmaan: " + this.ttList.get(this.selectedSchedule).getName()
                        + " uusi päiväohjelma päivälle " + day);
            }

            this.updateDaySchedData();
            this.repaint();
        }

        if (e.getActionCommand().equals("removeDaySchedule")) {
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
            //gui.fileIO.saveTimeTableList(this.ttList);
        }

        if (e.getActionCommand().equals("remove")) {
            Object[] options = {"Kyllä", "Ei"};
            // TODO
        }
    }

    private void updateFieldData() {
        log.debug("[updateFieldData]");
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

    private void updateDaySchedData() {
        int count;

        this.ds = this.ttList.get(this.selectedSchedule).getDaySchedule(this.selectedDay);

        if (this.ds != null) {
            log.debug("[updateDaySchedData]");
            log.debug("Number of schedules in " + this.ds.getName() + ": " + this.ds.numberOfSchedules());

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

                log.debug("Ei päiväohjelmia");
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

            log.debug("Ei päiväohjelmia (ds = null)");
            this.ohjelmaValikko.removeAllItems();
            this.selectedDaySchedule = -1;
        }
    }

    public void windowActivated(WindowEvent e) {
        this.repaint();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        this.gui.scheduleEdit.setEnabled(true);
        this.setVisible(false);
        this.dispose();
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void paint(Graphics g) {
        int scheds = 0;
        int num = 0;
        int starth, endh, startm, endm, day;
        double width;

        ArrayList<DaySchedule> dsList = null;
        DaySchedule ds;

        Color c, s;

        super.paint(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        log.debug("Aikapaneelin y: " + this.timePanel.getY());

        double y = this.logoPanel.getHeight() + this.timePanel.getY() + this.timeTable.getRowHeight() + 43.0;
        double x = this.clockTable.getWidth() + 3.0;

        width = ((this.timeTable.getWidth() / 7.0));

        RoundRectangle2D[] rect = null;

        if (this.ttList.size() > 0) {
            dsList = this.ttList.get(this.selectedSchedule).getDaySchedules();

            if (dsList != null) {
                num = dsList.size();

                for (int i = 0; i < num; i++) {
                    scheds = scheds + dsList.get(i).numberOfSchedules();
                }
                log.debug("Total scheds for: " + this.ttList.get(this.selectedSchedule).getName() + " is: " + scheds);
                rect = new RoundRectangle2D[scheds];
            }
        }

        if (scheds > 0) {
            int count = 0;
            for (int i = 0; i < num; i++) {
                ds = dsList.get(i);
                day = ds.getDay();

                if (day == 1)
                    day = 6;
                else
                    day = day - 2;

                for (int j = 0; j < dsList.get(i).numberOfSchedules(); j++) {
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

        AlphaComposite myAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
        g2.setComposite(myAlpha);

        g2.setPaint(s);
        g2.setStroke(new BasicStroke(2));
        g2.setColor(c);

        for (int i = 0; i < scheds; i++) {
            g2.fill(rect[i]);
            g2.draw(rect[i]);
        }

    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
}
