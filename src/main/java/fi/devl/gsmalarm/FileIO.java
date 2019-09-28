/**
 * <CODE>FileIO</CODE>
 * <p>
 * class with methods that are used for handling the XML-file IO.
 * <p>
 * ChangeLog:
 * <p>
 * v0.1 - Initial version (20.10.2009)
 * v0.2 - Now uses Transformer-classes instead of Xerces (21.10.2009)
 * v0.3 - Added the basics for reading TimeTables from XML-files. (9.3.2010)
 * v0.4 - Now reads fully the schedule.xml file. (10.5.2010)
 * v0.5 - Support for alarmclass ids (26.5.2010)
 * v0.6 - Can now read DaySchedules from schedule.xml (31.5.2010)
 * <p>
 * TODO:
 * - alot
 *
 * @author Juha-Matti Sironen
 * @version 0.6
 * @date 31.05.2010
 */


package fi.devl.gsmalarm;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import fi.devl.gsmalarm.domain.DaySchedule;
import fi.devl.gsmalarm.domain.TimeTable;
import fi.devl.gsmalarm.domain.User;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class FileIO {
    private DocumentBuilder schedbuilder;
    private DocumentBuilder userbuilder;
    private Document userDoc;

    private static final String ver = "1.0";

    private final static Logger log = Logger.getLogger(FileIO.class);

    FileIO() {
    }

    /**
     * reads timetabledata from xml-file
     *
     * @throws ParserConfigurationException fail
     * @throws IOException fail
     * @throws SAXException fail
     */
    ArrayList<TimeTable> readTimeTable() throws ParserConfigurationException, SAXException, IOException {
        ArrayList<TimeTable> ttList = new ArrayList<>();
        Element root;
        NodeList list;

        if (this.schedbuilder == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            this.schedbuilder = factory.newDocumentBuilder();

            Document scheduleDoc = this.schedbuilder.parse("schedule.xml");

            root = scheduleDoc.getDocumentElement();
            list = root.getElementsByTagName("timetable");

            if (list != null && list.getLength() > 0) {
                for (int i = 0; i < list.getLength(); i++) {
                    Element el = (Element) list.item(i);
                    TimeTable t = getTimeTable(el);
                    ttList.add(t);
                }
                log.info("schedule.xml: " + list.getLength());
            }
        }
        return ttList;
    }


    /**
     * reads userdata from xml-file
     *
     * @param userList list of User objects
     * @throws ParserConfigurationException fail
     * @throws IOException fail
     * @throws SAXException fail
     */
    ArrayList<User> readUserList(ArrayList<User> userList, ArrayList<TimeTable> ttList) throws ParserConfigurationException, SAXException, IOException {
        Element root;
        NodeList list;

        if (this.userbuilder == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            this.userbuilder = factory.newDocumentBuilder();

            this.userDoc = this.userbuilder.parse("./userList.xml");

            root = this.userDoc.getDocumentElement();
            list = root.getElementsByTagName("user");

            if (list != null && list.getLength() > 0) {
                for (int i = 0; i < list.getLength(); i++) {
                    Element el = (Element) list.item(i);
                    User u = getUser(el, ttList);
                    userList.add(u);
                }
                log.info("userList.xml: " + list.getLength());
            }
        }
        return userList;
    }

    /**
     * take an user element and read the values in, create
     * an User object and return it
     */
    private User getUser(Element userEl, ArrayList<TimeTable> ttList) {
        NodeList almIds;
        String name = getTextValue(userEl, "name");
        String phone = getTextValue(userEl, "phoneNumber");
        String scheduleId = getTextValue(userEl, "scheduleId");
        String id = getTextValue(userEl, "id");
        String almId;
        boolean state = getBooleanValue(userEl, "state");

        TimeTable sched = null;

        log.info("scheduleId found on userlist: " + scheduleId);

        for (TimeTable timeTable : ttList) {
            if (scheduleId.equals(timeTable.getId())) {
                log.info("scheduleId found on schedulelist: " + timeTable.getId());
                sched = timeTable;
            }
        }

        User u = new User(name, phone, sched, id, state);
        almIds = userEl.getElementsByTagName("alarmId");

        log.info("list size: " + almIds.getLength());

        if (almIds != null && almIds.getLength() > 0) {
            for (int i = 0; i < almIds.getLength(); i++) {
                Element el = (Element) almIds.item(i);
                almId = el.getFirstChild().getNodeValue();
                u.addAlmId(almId);
                log.info("user: " + u.getName() + " almId: " + almId);
            }
        }

        return u;
    }


    /**
     * take an timetable-element and read the values in, create
     * an TimeTable object and return it
     */

    private TimeTable getTimeTable(Element ttEl) {
        NodeList dslist;

        String name = getTextValue(ttEl, "name");
        String id = getTextValue(ttEl, "id");

        TimeTable sched = new TimeTable(name, id);

        dslist = ttEl.getElementsByTagName("day");

        if (dslist != null && dslist.getLength() > 0) {
            for (int i = 0; i < dslist.getLength(); i++) {
                Element el = (Element) dslist.item(i);
                DaySchedule t = getDaySchedule(el);
                sched.addDaySchedule(t);
            }
            log.info("Day schedules found: " + dslist.getLength());
        }

        return sched;
    }

    /**
     * take an day-element and read the values in, create
     * an DaySchedule object and return it
     */

    private DaySchedule getDaySchedule(Element dsEl) {
        NodeList onlist;
        NodeList offlist;
        String name;
        String onTime;
        String offTime;
        int dayNum;

        name = dsEl.getAttribute("name");
        dayNum = Integer.parseInt(dsEl.getAttribute("value"), 10);

        DaySchedule ds = new DaySchedule(name);
        ds.setDay(dayNum);

        onlist = dsEl.getElementsByTagName("on");
        offlist = dsEl.getElementsByTagName("off");

        log.info("Day schedule " + name + " begins.");

        if (onlist != null && onlist.getLength() > 0) {
            for (int i = 0; i < onlist.getLength(); i++) {

                Element el = (Element) onlist.item(i);

                if (el.getFirstChild() != null) {
                    onTime = el.getFirstChild().getNodeValue();

                    el = (Element) offlist.item(i);
                    if (el.getFirstChild() != null) {
                        offTime = el.getFirstChild().getNodeValue();
                        log.info("ontime: " + onTime + " offtime: " + offTime);
                        ds.addSchedule(onTime, offTime, "1");
                    }
                }
            }
            log.info(name + " schedules found: " + onlist.getLength());
        }

        return ds;
    }


    /**
     * take xml-element and the tagname, look for the tag and get
     * the text content
     */

    private String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);

        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }
        return textVal;
    }

    /**
     * take xml-element and the tagname, look for the tag and get
     */

    private boolean getBooleanValue(Element ele, String tagName) {
        boolean boolVal = false;
        NodeList nl = ele.getElementsByTagName(tagName);

        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            if (el.getAttribute("value").equals("true"))
                boolVal = true;
        }
        return boolVal;
    }

    public void saveUserList(ArrayList<User> list) {
        this.createUserDocument();
        this.createDOMTree(list);
        this.saveToFile(userDoc, "userlist.dtd", "./userList.xml");
    }

    private void createUserDocument() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            this.userDoc = db.newDocument();
        } catch (ParserConfigurationException pce) {
            log.error("Error while trying to instantiate DocumentBuilder " + pce);
            System.exit(1);
        }

    }

    private void createDOMTree(ArrayList<User> list) {
        Element rootEle = userDoc.createElement("userList");
        rootEle.setAttribute("version", ver);
        userDoc.appendChild(rootEle);

        for (User u : list) {
            Element userEle = createUserElement(u);
            rootEle.appendChild(userEle);
        }
    }

    /**
     * Helper method which creates a XML element <user>
     *
     * @param u The user for which we need to create an xml representation
     * @return XML element snippet representing a user
     */

    private Element createUserElement(User u) {
        String state;
        Element userEle = this.userDoc.createElement("user");

        Element nameEle = this.userDoc.createElement("name");
        Text nameText = this.userDoc.createTextNode(u.getName());
        nameEle.appendChild(nameText);
        userEle.appendChild(nameEle);

        Element phoneEle = this.userDoc.createElement("phoneNumber");
        Text phoneText = this.userDoc.createTextNode(u.getNumber());
        phoneEle.appendChild(phoneText);
        userEle.appendChild(phoneEle);

        Element scheduleEle = this.userDoc.createElement("scheduleId");
        Text scheduleText = this.userDoc.createTextNode(u.getTimeTableId());
        scheduleEle.appendChild(scheduleText);
        userEle.appendChild(scheduleEle);

        Element idEle = this.userDoc.createElement("id");
        Text idText = this.userDoc.createTextNode(u.getId());
        idEle.appendChild(idText);
        userEle.appendChild(idEle);

        Element AlmClassEle = this.userDoc.createElement("alarmClass");

        for (int i = 0; i < u.getAlmIds().size(); i++) {
            Element AlmIdEle = this.userDoc.createElement("alarmId");
            Text AlmIdText = this.userDoc.createTextNode(u.getAlmIds().get(i));
            AlmIdEle.appendChild(AlmIdText);
            AlmClassEle.appendChild(AlmIdEle);
        }
        userEle.appendChild(AlmClassEle);

        Element stateEle = this.userDoc.createElement("state");

        if (u.getState())
            state = "true";
        else
            state = "false";

        stateEle.setAttribute("value", state);
        userEle.appendChild(stateEle);

        return userEle;
    }

    private void saveToFile(Document doc, String system, String path) {
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();

            t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, system);
            t.setOutputProperty(OutputKeys.INDENT, "yes");

            t.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(path)));
        } catch (TransformerFactoryConfigurationError | FileNotFoundException | TransformerException problem) {
            problem.printStackTrace();
        }
    }
}
