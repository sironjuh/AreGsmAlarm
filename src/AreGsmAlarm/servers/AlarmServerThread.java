/**
 * <CODE>AlarmServerThread</CODE>
 * <p>
 * Receives alarms packets from port 4445 and forwards them to ComServerThread
 * <p>
 * ChangeLog:
 * <p>
 * v0.1 - Initial version (27.08.2009)
 * v0.2 - Code cleanup (28.08.2009)
 * v0.3 - Separates the alarm class id from the text (31.5.2010)
 * <p>
 * TODO:
 * - cleanup the alarm class handling (make it more error safe)
 *
 * @author Juha-Matti Sironen
 * @version 0.3
 * @date 31.05.2010
 */

package AreGsmAlarm.servers;

import java.io.*;
import java.net.*;
import java.util.*;

import AreGsmAlarm.GsmGUI;

public class AlarmServerThread implements Runnable {

    protected DatagramSocket socket = null;
    protected boolean noError = true;
    int alarmsReceived;
    private GsmGUI gsmGUI;

    public AlarmServerThread(GsmGUI gsmGUI) throws IOException {
        this.gsmGUI = gsmGUI;
        this.alarmsReceived = 0;
        this.socket = new DatagramSocket(4445);
    }

    public int getAlarmNumber() {
        return this.alarmsReceived;
    }

    public void close() {
        this.noError = false; // this will cause socket to close
    }

    public void run() {
        String data;
        String alarm = "";
        String id = "";
        String[] tempBuf;
        String temp = "";

        int val = 0;
        int i = 0;

        while (this.noError) {
            try {
                byte[] buf = new byte[256];

                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                this.socket.receive(packet);

                // build the alarm string
                String dString = null;
                dString = new Date().toString();

                data = new String(buf);

                for (i = 0; i < data.length() - 3; i++) {
                    temp = data.substring(i, i + 3);
                    if (temp.equals("EOF")) {
                        val = i;
                    }
                }

                alarm = data.substring(0, val);
                tempBuf = alarm.split(":", 2);

                System.out.println(alarm);
                System.out.println(tempBuf[0]);
                System.out.println(tempBuf[1]);

                this.alarmsReceived++;
                this.gsmGUI.updatePanel1();
                this.gsmGUI.sendSMS(tempBuf[1], tempBuf[0]);
            } catch (IOException e) {
                e.printStackTrace();
                this.noError = false;
            }
        }
        this.socket.close();
    }
}
