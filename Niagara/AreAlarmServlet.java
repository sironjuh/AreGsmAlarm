/**
 * Are Alarm Servlet v0.2
 * <p>
 * Forwards Alarm Notifications to port 4445.
 * <p>
 * Requires an ApiRecipient that exist with the swid:
 * "/{station}/services/NotificationService/api"
 * <p>
 * Version History
 * <p>
 * v0.1 - initial release (27.08.2009)
 * v0.2 - now forwards all alarm classes, and the alarm class
 * handling is done in alarm server thread instead (31.05.2010)
 * <p>
 * TODO:
 * - add securitas messagin with separate alarm state codes.
 *
 * @author Juha-Matti Sironen
 * @version 0.2
 * @date 31.05.2010
 */

import java.io.*;
import java.util.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

import tridium.api.*;
import tridium.notification.*;
import tridium.notification.api.*;

public class AreAlarmServlet extends tridium.api.ServletNode
        implements tridium.notification.api.NotificationListener {

    public void doServletInit() {
        NodeDatabase db = getNodeDatabase();
        this.apiSwid = "/" + db.lookupStation().getName() + "/services/NotificationService/api";

        ApiRecipientApi api = (ApiRecipientApi) db.lookup(this.apiSwid);
        if (api == null)
            System.out.println("Missing ApiRecipient: " + this.apiSwid);
        else
            api.addNotificationListener(this);
    }

    public void doServletCleanup() {
        ApiRecipientApi api = (ApiRecipientApi) getNodeDatabase().lookup(this.apiSwid);
        if (api != null)
            api.removeNotificationListener(this);
    }

    public void processAlarm(AlarmEvent e) {
        System.out.println("*** processAlarm: " + e);
        System.out.println("swid       = " + e.getSwid());
        System.out.println("timestamp  = " + new Date(e.getTimestamp()));
        System.out.println("classId    = " + e.getNotificationClassId());
        System.out.println("msgText    = " + e.getMessageText());
        System.out.println("notifyType = " + e.getNotifyType());
        System.out.println("fromState  = " + e.getFromState());
        System.out.println("toState    = " + e.getToState());

        if (e.getNotifyType().equals("event")) {
            DatagramSocket socket;
            InetAddress address;
            String datapass;
            String tilanne = "";

            if (e.getToState().equals("offnormal")) {
                tilanne = "Hälytys";
            }

            if (e.getToState().equals("normal")) {
                tilanne = "Paluu normaaliin";
            }

            try {
                socket = new DatagramSocket();

                datapass = e.getNotificationClassId() + ":" + e.getSwid() + " Viesti: " + e.getMessageText() + " Tilanne: " + tilanne + " EOF";

                byte[] buf = new byte[256];
                buf = datapass.getBytes();

                address = InetAddress.getByName("localhost");
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
                socket.send(packet);

                socket.close();

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Process an alert event.
     */
    public void processAlert(AlertEvent e) {
        System.out.println("*** processAlert: " + e);
    }

    private String apiSwid;
}
