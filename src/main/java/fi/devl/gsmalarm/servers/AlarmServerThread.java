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

package fi.devl.gsmalarm.servers;

import fi.devl.gsmalarm.GsmGUI;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class AlarmServerThread implements Runnable {

    private DatagramSocket socket;
    private boolean noError = true;
    private int alarmsReceived;
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
        String alarm;
        String[] dataArray;

        int val = 0;

        while (this.noError) {
            try {
                byte[] buf = new byte[256];

                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                this.socket.receive(packet);

                data = new String(buf);

                for (int i = 0; i < data.length() - 3; i++) {
                    if (data.substring(i, i + 3).equals("EOF")) {
                        val = i;
                    }
                }

                alarm = data.substring(0, val);
                dataArray = alarm.split(":", 2);

                System.out.println(alarm);
                System.out.println(dataArray[0]);
                System.out.println(dataArray[1]);

                this.alarmsReceived++;
                this.gsmGUI.updatePanel1();
                this.gsmGUI.sendSMS(dataArray[1], dataArray[0]);
            } catch (IOException e) {
                e.printStackTrace();
                this.noError = false;
            }
        }

        this.socket.close();
    }
}
