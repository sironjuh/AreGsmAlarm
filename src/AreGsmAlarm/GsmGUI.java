/**
 * <CODE>GsmGUI</CODE>
 * <p>
 * Graphical User Interface for Are Gsm Alarm system.
 * <p>
 * ChangeLog:
 * <p>
 * v0.10 - Initial version (27.08.2009)
 * v0.20 - Added TabbedPanes for different informations (27.08.2008)
 * v0.30 - Error checking if can't start AlarmServerThread. (28.08.2008)
 * v0.40 - Handles COM-port opening now (22.09.2009)
 * v0.50 - Forwards the alarms from AlarmServerThread to ComServerThread (22.09.2009)
 * v0.60 - Code cleanup, requester added for exit. (01.10.2009)
 * v0.70 - Modified the SMS-sending function and layout changes. (06.10.2009)
 * v0.80 - removed all methods that belong more naturally to ComServer-class (07.10.2009)
 * v0.81 - now loads the images from the package (14.10.2009)
 * v0.85 - possible to load and save userdata (21.10.2009)
 * v0.86 - only some visual changes (30.01.2010 - 02.02.2010)
 * v0.87 - modified the sendSMS method to handle different alarm class id's (31.5.2010)
 * v0.88 - fixed a nullpointer-error when the usertable was cleared (17.6.2010)
 * <p>
 * TODO:
 * - add possibility to create / modify / remove schedules
 *
 * @author Juha-Matti Sironen
 * @version 0.88
 * @date 17.06.2010
 */

package AreGsmAlarm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.io.IOException;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import AreGsmAlarm.editors.ComEditor;
import AreGsmAlarm.editors.UserEditor;
import AreGsmAlarm.editors.ScheduleEditor;

import AreGsmAlarm.servers.AlarmServerThread;
import AreGsmAlarm.servers.ComServerThread;

//import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;


public class GsmGUI extends JFrame implements ActionListener, TableModelListener, WindowListener {

    // jframe size
    private int width = 640;
    private int height = 480;

    // first page datafields
    private JTextField alarmsField;
    private JTextField startTimeField;

    // tab panels
    private JComponent panel1;
    private JComponent panel2;
    private JComponent panel3;

    // buttons (tab 1)
    //private JButton enableAlarms;
    //private JButton disableAlarms;
    private JButton changeStateButton;

    // buttons (tab 2)
    // non-private as need access to these inside package
    public JButton userEdit;
    public JButton scheduleEdit;
    public JButton messageCentral;
    public JButton comCentral;

    // configuration frames
    private JFrame userEditor;
    private JFrame scheduleEditor;
    private JFrame comEditor;

    // booleans
    private boolean alarmsEnabled = false;

    //images
    private ImageIcon logo;
    private ImageIcon hyvin;
    private ImageIcon ok;
    private ImageIcon seis;
    private ImageIcon ok_roll;
    private ImageIcon seis_roll;

    // tables
    private JTable table;
    private TableModelEvent tableEvent = null;

    //table data
    private String[] columnNames = {"Nimi",
            "Puhelinnumero",
            "Aikataulu",
            "Kohdetunnus",
            "Aktiivinen"};

    private Object[][] data = new Object[0][0];

    // threads
    private AlarmServerThread alarmServer;
    private ComServerThread comServer;
    private ExecutorService executorService;

    // arraylists for userdata
    private ArrayList<String> comPorts;
    private ArrayList<User> userList;
    private ArrayList<TimeTable> ttList;
    //private ArrayList<Viestikeskus> vkList;

    private User tempUser;
    public FileIO fileIO;

    GsmGUI() {

        // set jframe size, title, x-button function
        setSize(this.width, this.height);
        setTitle("Are Gsm Alarm v1.0");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        this.setResizable(false);

        this.comPorts = new ArrayList<String>();
        this.ttList = new ArrayList<TimeTable>();
        this.userList = new ArrayList<User>();
        //this.vkList = new ArrayList<Viestikeskus>();

        // luodaan pohjille aikaohjelma, korvataan myöhemmin
        // käyttäjän mahdollisuduella tehd�ämuutoksia, tallentaa ja ladata
        // muutokset tiedostoihin
		/*
		this.ttList.add(new TimeTable("Huolto (7:00-15:30)"));
		this.ttList.add(new TimeTable("P�ivystys (15:30-7:00)"));
		this.ttList.add(new TimeTable("Valvomo (24h)"));
		*/

        this.fileIO = new FileIO();

        try {
            this.ttList = this.fileIO.readTimeTable(this.ttList);
        } catch (ParserConfigurationException problem1) {
            problem1.printStackTrace();
        } catch (SAXException problem1) {
            problem1.printStackTrace();
        } catch (IOException problem1) {
            problem1.printStackTrace();
        }

        try {
            this.userList = this.fileIO.readUserList(this.userList, this.ttList);
            System.out.println("userlist size: " + this.userList.size());
        } catch (ParserConfigurationException problem) {
            JOptionPane.showMessageDialog(this, "Ongelma alustuksessa!\n\nTietoja ei pysty tallentamaan.\n"
                    + "Käytetään oletustietoja.", "Varoitus!", JOptionPane.WARNING_MESSAGE);
            problem.printStackTrace();
        } catch (SAXException problem) {
            JOptionPane.showMessageDialog(this, "Ongelma alustuksessa!\n\nuserList.xml tiedosto ei\n"
                    + "täytä vaatimuksia.", "Varoitus!", JOptionPane.WARNING_MESSAGE);
            problem.printStackTrace();
        } catch (IOException problem) {
            JOptionPane.showMessageDialog(this, "Ongelma alustuksessa!\n\nuserList.xml tiedosto ei\n"
                    + "aukea.", "Varoitus!", JOptionPane.WARNING_MESSAGE);
            problem.printStackTrace();
        }

        this.data = new Object[this.userList.size()][5];

        for (int i = 0; i < this.userList.size(); i++) {
            this.tempUser = this.userList.get(i);
            this.data[i][0] = this.tempUser.getName();
            this.data[i][1] = this.tempUser.getNumber();
            this.data[i][2] = this.tempUser.getTimeTableName();
            this.data[i][3] = this.tempUser.getId();
            this.data[i][4] = this.tempUser.getState();
        }

        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (UnsupportedLookAndFeelException e) {
            System.out.println("Error: this look and feel is not present on your system");
        } catch (ClassNotFoundException problem) {
            problem.printStackTrace();
        } catch (InstantiationException problem) {
            problem.printStackTrace();
        } catch (IllegalAccessException problem) {
            problem.printStackTrace();
        }

        // logo yläreunaan
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new GridBagLayout());
        this.logo = this.createImageIcon("images/bg_21.png");
        JLabel logoLabel = new JLabel();
        logoLabel.setIcon(this.logo);
        logoPanel.add(logoLabel);

        // etusivun tiedot
        this.startTimeField = new JTextField(new Date().toString(), 30);
        this.startTimeField.setBackground(Color.getHSBColor(0.0f, 0.0f, 0.95f));
        this.startTimeField.setEditable(false);
        this.alarmsField = new JTextField("0", 30);
        this.alarmsField.setBackground(Color.getHSBColor(0.0f, 0.0f, 0.95f));
        this.alarmsField.setEditable(false);

        this.ok = this.createImageIcon("images/ok.png");
        this.seis = this.createImageIcon("images/seis.png");

        this.ok_roll = this.createImageIcon("images/ok_roll.png");
        this.seis_roll = this.createImageIcon("images/seis_roll.png");

        this.changeStateButton = new JButton("");
        this.changeStateButton.setContentAreaFilled(false);
        this.changeStateButton.setRolloverIcon(this.ok_roll);
        this.changeStateButton.setRolloverEnabled(true);
        this.changeStateButton.setToolTipText("Paina t�st� est��ksesi jatkoh�lytykset");
	
		/*
		this.enableAlarms = new JButton("Salli GSM-h�lytykset");
		this.disableAlarms = new JButton("Est� GSM-h�lytykset");
		this.enableAlarms.setActionCommand("enableAlarms");
		this.disableAlarms.setActionCommand("disableAlarms");

		this.enableAlarms.setEnabled(false);
		this.disableAlarms.setEnabled(true);
		*/

        this.changeStateButton.setIcon(this.ok);
        this.changeStateButton.setActionCommand("changeState");

        // actionListenerit ekalle sivulle
        this.changeStateButton.addActionListener(this);
        //this.enableAlarms.addActionListener(this);
        //this.disableAlarms.addActionListener(this);

        // asetetaan oletuksena jatkohälytykset ok
        this.alarmsEnabled = true;

        // asetussivun pohjustus
        //JTable table = new JTable(this.data, this.columnNames);
        this.table = new JTable(new UserTableModel());
        this.table.getModel().addTableModelListener(this);
        this.table.setAutoCreateRowSorter(true);

        //this.table.setAutoCreateColumnsFromModel(false);

        this.table.getTableHeader().setReorderingAllowed(false);
        this.table.getTableHeader().setResizingAllowed(false);

        this.userEdit = new JButton("Muokkaa Käyttäjiä");
        this.scheduleEdit = new JButton("Muokkaa Aikatauluja");
        this.messageCentral = new JButton("Muokkaa Viestikeskuksia");
        this.comCentral = new JButton("Yhteysasetukset");

        this.userEdit.setActionCommand("userEdit");
        this.scheduleEdit.setActionCommand("scheduleEdit");
        this.messageCentral.setActionCommand("messageCentral");
        this.comCentral.setActionCommand("comCentral");

        // aktivoidaan / diabloidaan namiskuukkelit
        this.userEdit.setEnabled(true);
        this.scheduleEdit.setEnabled(true);
        this.messageCentral.setEnabled(false);
        this.comCentral.setEnabled(true);

        // actionListeners
        this.userEdit.addActionListener(this);
        this.scheduleEdit.addActionListener(this);
        this.messageCentral.addActionListener(this);
        this.comCentral.addActionListener(this);

        // tietoja sivu
        this.hyvin = this.createImageIcon("images/hyvin.png");

        // v�lilehdet
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(SwingConstants.BOTTOM);

        this.panel1 = makePanel1();
        tabbedPane.addTab("Etusivu", null, this.panel1, "Yleiset tiedot");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        this.panel2 = makePanel2("Asetukset");
        tabbedPane.addTab("Asetukset", null, this.panel2, "Asetukset, viestinumerot");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        this.panel3 = makePanel3("Tietoja");
        tabbedPane.addTab("Tietoja", null, this.panel3, "Tietoja ohjelmasta");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        this.getContentPane().add(BorderLayout.NORTH, logoPanel);
        this.getContentPane().add(BorderLayout.CENTER, tabbedPane);

        setVisible(true);

        this.executorService = Executors.newCachedThreadPool();

        this.initAlarmServer();
        this.initComServer();
    }

    /**
     * initAlarmServer() will initialize AlarmServerThread. Will inform if the port 4445 is reserved
     * and causes program to exit if AlarmServer fails to start.
     */
    protected void initAlarmServer() {
        try {
            this.alarmServer = new AlarmServerThread(this);
        } catch (IOException problem) {
            JOptionPane.showMessageDialog(this, "Portti 4445 on varattu!\n\nVarmista ettei ohjelma\nole"
                    + " jo käynnissä.", "Varoitus!", JOptionPane.WARNING_MESSAGE);
            System.exit(-1);
        }
    }

    /**
     * initComserver() will initialize ComServerThread.
     */

    protected void initComServer() {
        try {
            this.comServer = new ComServerThread();
        } catch (IOException problem) {
            JOptionPane.showMessageDialog(this, "RXTX-ei käynnisty!\n\nVarmista että ohjelma on\n"
                    + "oikein asennettu.", "Varoitus!", JOptionPane.WARNING_MESSAGE);
            System.exit(-1);
        }
        try {
            this.comPorts = this.comServer.listPorts();
            if (this.comPorts.size() > 0) {
                this.comServer.connect(this.comPorts.get(0));
            } else {
                JOptionPane.showMessageDialog(this, "Ei COM-portteja!\n\nVarmista että koneessa on\n"
                        + "ainakin yksi COM-portti.", "Varoitus!", JOptionPane.WARNING_MESSAGE);
                System.exit(-1);
            }

        } catch (Exception problem) {
            JOptionPane.showMessageDialog(this, "COM1-portti ei aukea!\n\nVarmista ettei mikään muu\nohjelma"
                    + " ole varannut sitä.", "Varoitus!", JOptionPane.WARNING_MESSAGE);
            System.exit(-1);
        }
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    public ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    protected JComponent makePanel1() {
        JPanel panel = new JPanel(false);
        panel.setBackground(Color.white);
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(2, 2, 2, 2);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.2;
        c.weighty = 0.2;
        c.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel(" "), c);

        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 3;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.2;
        c.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel(" "), c);

        c.gridx = 4;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.2;
        c.weighty = 0.2;
        c.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel(" "), c);
        //panel.add(kohdeHolder, c);

		/*
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(2, 2, 20, 2);
		panel.add(osoite, c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.insets = new Insets(2, 2, 2, 2);
		panel.add(new JLabel(" "), c);

		c.gridx = 5;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.insets = new Insets(2, 2, 2, 2);
		panel.add(new JLabel(" "), c);
		*/

        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.8;
        c.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel(" "), c);

        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.15;
        c.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel("Hälytyspalvelin käynnistetty "), c);

        c.gridx = 2;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.15;
        c.insets = new Insets(2, 2, 2, 2);
        panel.add(this.startTimeField, c);

        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.15;
        c.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel("Käsiteltyjä hälytyksiä "), c);

        c.gridx = 2;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.5;
        c.weighty = 0.15;
        c.insets = new Insets(2, 2, 2, 2);
        panel.add(this.alarmsField, c);

        c.gridx = 3;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 4;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.insets = new Insets(2, 2, 2, 2);
        panel.add(this.changeStateButton, c);

        c.gridx = 1;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 0.0;
        c.weighty = 0.8;
        c.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel(" "), c);

		/*
		c.gridx = 1;
		c.gridy = 7;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.insets = new Insets(2, 2, 2, 2);
		panel.add(this.enableAlarms, c);

		c.gridx = 1;
		c.gridy = 8;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		panel.add(this.disableAlarms, c);


		c.gridx = 2;
		c.gridy = 6;
		c.gridwidth = 1;
		c.gridheight = 4;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(2, 50, 2, 50);
		panel.add(this.statusLabel, c);
		*/

        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 4;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.6;
        c.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel(" "), c);

        return panel;
    }

    protected JComponent makePanel2(String text) {
        JPanel panel = new JPanel(false);
        JScrollPane scrollpane = new JScrollPane(panel);
        scrollpane.setBackground(Color.white);
        scrollpane.setBorder(null);

        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(2, 2, 2, 2);

        // taulukko (paneeli 2)
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        panel.add(this.table.getTableHeader(), c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 4;
        c.gridheight = 5;
        c.weightx = 1.0;
        c.weighty = 1.0;
        panel.add(this.table, c);

        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        panel.add(new JLabel(" "), c);

        // napit (paneeli 2)
        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.insets = new Insets(0, 2, 2, 2);
        panel.add(this.userEdit, c);

        c.gridx = 1;
        c.gridy = 8;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.insets = new Insets(0, 2, 2, 2);
        panel.add(this.scheduleEdit, c);

        c.gridx = 2;
        c.gridy = 8;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.insets = new Insets(0, 2, 2, 2);
        panel.add(this.messageCentral, c);

        c.gridx = 3;
        c.gridy = 8;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.insets = new Insets(0, 2, 2, 2);
        panel.add(this.comCentral, c);

        return panel;
    }

    protected JComponent makePanel3(String text) {
        JPanel panel = new JPanel(false);
        JScrollPane scrollpane = new JScrollPane(panel);
        scrollpane.setBackground(Color.white);
        scrollpane.setBorder(null);
        JLabel img = new JLabel();
        JLabel title = new JLabel("Are Gsm Alarm");
        JLabel copy = new JLabel("(c) 2009-2010 Juha-Matti Sironen");

        JLabel rx1 = new JLabel("rxtx (c) 1998-2009 Keane Jarvi");
        JLabel rx2 = new JLabel("www.rxtx.org");

        JLabel tyhja = new JLabel(" ");
        JLabel tyhja2 = new JLabel(" ");
        JLabel tyhja3 = new JLabel(" ");

        JLabel yht1 = new JLabel("http://www.are.fi");
        JLabel yht2 = new JLabel("puh 020 530 5500");

        panel.setBackground(Color.white);
        img.setHorizontalAlignment(SwingConstants.CENTER);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        copy.setHorizontalAlignment(SwingConstants.CENTER);
        yht1.setHorizontalAlignment(SwingConstants.CENTER);
        yht2.setHorizontalAlignment(SwingConstants.CENTER);
        rx1.setHorizontalAlignment(SwingConstants.CENTER);
        rx2.setHorizontalAlignment(SwingConstants.CENTER);

        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        img.setIcon(this.hyvin);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        panel.add(img, c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        panel.add(title, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        panel.add(copy, c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        panel.add(tyhja, c);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        panel.add(rx1, c);

        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        panel.add(rx2, c);

        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 1;
        c.gridheight = 1;
        panel.add(tyhja2, c);

        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = 1;
        c.gridheight = 1;
        panel.add(yht1, c);

        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = 1;
        c.gridheight = 1;
        panel.add(yht2, c);

        c.gridx = 0;
        c.gridy = 9;
        c.gridwidth = 1;
        c.gridheight = 1;
        panel.add(tyhja3, c);

        return scrollpane;
    }

    public void updatePanel1() {
        int val = this.alarmServer.getAlarmNumber();
        this.alarmsField.setText(" " + val);
    }

    public void actionPerformed(ActionEvent e) {
        //enableAlarms
		/*
        if (e.getActionCommand().equals("enableAlarms")) {
            this.enableAlarms.setEnabled(false);
            this.disableAlarms.setEnabled(true);
            this.statusLabel.setIcon(this.ok);
            this.alarmsEnabled = true;
        }
        
		//disableAlarms
        if (e.getActionCommand().equals("disableAlarms")) {
            this.enableAlarms.setEnabled(true);
            this.disableAlarms.setEnabled(false);
            this.statusLabel.setIcon(this.seis);
            this.alarmsEnabled = false;
        }     
        */

        // Disable / Enable alarms
        if (e.getActionCommand().equals("changeState")) {
            if (this.alarmsEnabled) {
                this.changeStateButton.setIcon(this.seis);
                this.changeStateButton.setRolloverIcon(this.seis_roll);
                this.changeStateButton.setToolTipText("Paina tästä aktivoidaksesi jatkohälytykset");
                this.alarmsEnabled = false;
            } else {
                this.changeStateButton.setIcon(this.ok);
                this.changeStateButton.setRolloverIcon(this.ok_roll);
                this.changeStateButton.setToolTipText("Paina tästä estääksesi jatkohälytykset");
                this.alarmsEnabled = true;
            }

        }

        // User Editor
        if (e.getActionCommand().equals("userEdit")) {
            this.userEdit.setEnabled(false);
            this.userEditor = new UserEditor("Käyttäjät", this.userList, this.ttList, this);
            this.userEditor.setVisible(true);
        }

        // Schedule Editor
        if (e.getActionCommand().equals("scheduleEdit")) {
            this.scheduleEdit.setEnabled(false);
            this.scheduleEditor = new ScheduleEditor("Aikaohjelma", this.userList, this.ttList, this);
            this.scheduleEditor.setVisible(true);
        }

        // Message Central
        if (e.getActionCommand().equals("messageCentral")) {
            this.updatePanel1();
        }

        //comCentral
        if (e.getActionCommand().equals("comCentral")) {
            this.comCentral.setEnabled(false);
            this.comEditor = new ComEditor("Yhteysasetukset", this.comPorts, this.comServer, this);
            //this.comEditor.setVisible(true);
        }
    }

    public void sendSMS(String data, String id) {
        if (this.alarmsEnabled) {
            for (int i = 0; i < this.userList.size(); i++) {
                if (this.userList.get(i).getState() && this.userList.get(i).getTimeTable().isActive()) {
                    //System.out.println(this.userList.get(i).getName());
                    //System.out.println("id from alarmserver: " +id);

                    if (this.userList.get(i).almIdMatch(id)) {
                        //System.out.println("alarmId: " + id);
                        try {
                            this.comServer.sendAlarm(this.userList.get(i).getNumber(), this.userList.get(i).getId(), data);
                        } catch (IOException problem) {
                            problem.printStackTrace();
                        } catch (InterruptedException problem) {
                            problem.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void start() {
        this.executorService.submit(this.alarmServer);
        this.executorService.submit(this.comServer);
    }

    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        boolean oldValue = false;

        if (column == 4) {
            oldValue = this.userList.get(row).getState();
            if (oldValue) {
                this.userList.get(row).changeState(false);
            }
            if (!oldValue) {
                this.userList.get(row).changeState(true);
            }
        }

    }

    public void updateUserTable() {
        if (this.userList.size() > 0)
            this.data = new Object[this.userList.size()][5];
        else {
            this.data = new Object[1][5];
            this.data[0][0] = "";
            this.data[0][1] = "";
            this.data[0][2] = "";
            this.data[0][3] = "";
            this.data[0][4] = "";
        }

        for (int i = 0; i < this.userList.size(); i++) {
            this.tempUser = this.userList.get(i);
            this.data[i][0] = this.tempUser.getName();
            this.data[i][1] = this.tempUser.getNumber();
            this.data[i][2] = this.tempUser.getTimeTableName();
            this.data[i][3] = this.tempUser.getId();
            this.data[i][4] = this.tempUser.getState();
        }

        this.table.tableChanged(this.tableEvent);
    }

    // UserTableModel 
    class UserTableModel extends AbstractTableModel {
        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            if (data.length > 0)
                return data[row][col];
            else
                return null;
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            if (data.length > 0)
                return getValueAt(0, c).getClass();
            else
                return null;
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 4) {
                return false;
            } else {
                return true;
            }
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            /*if (DEBUG) {
                System.out.println("Setting value at " + row + "," + col
                                   + " to " + value
                                   + " (an instance of "
                                   + value.getClass() + ")");
            }*/

            data[row][col] = value;
            fireTableCellUpdated(row, col);

            /*if (DEBUG) {
                System.out.println("New value of data:");
                printDebugData();
            }*/
        }

        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            for (int i = 0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j = 0; j < numCols; j++) {
                    System.out.print("  " + data[i][j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
    }

    public void windowClosing(WindowEvent arg0) {
        Object[] options = {"Kyllä", "Ei"};

        int n = JOptionPane.showOptionDialog(this,
                "Olet sulkemassa Are Gsm Alarm ohjelmaa.\n\nOletko varma että haluat sulkea\nohjelman?",
                "Varoitus!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,        //do not use a custom Icon
                options,     //the titles of buttons
                options[0]); //default button title

        if (n == 0) {
            this.alarmServer.close();
            this.comServer.close();
            System.exit(-1);
        }

    }

    public void windowActivated(WindowEvent arg0) {
    }

    public void windowClosed(WindowEvent arg0) {
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
