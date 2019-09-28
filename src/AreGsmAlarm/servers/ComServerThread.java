/**
 * <CODE>ComServerThread</CODE>
 * <p>
 * Handles the Serial IO.
 * <p>
 * ChangeLog:
 * <p>
 * v0.1 - Initial version (22.9.2009)
 * v0.2 - Modified the SerialReader class so that it sends the input from
 * serial-port to linkedblockingqueue. This makes it possible to handle
 * errors in message sending, checking the network signal strenght etc.
 * (6.10.2009)
 * v0.3 - Added alarmSend, parseScandics, serviceProvider, signalStrength methods. Now only
 * this class has linkedBlockingQueues, as no need for them in main class.
 * (7.10.2009)
 * v0.4 - Modified parseScandics method to convert the scandics to GSM-charcters instead of replacing
 * those with a and o.
 * Introduced comReserved-flag to block signal-stregth and operator queries while sending
 * alarms. (4.2.2010)
 * <p>
 * TODO:
 * - alot
 *
 * @author Juha-Matti Sironen
 * @version 0.4
 * @date 4.2.2010
 */

package AreGsmAlarm.servers;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ComServerThread implements Runnable {

    private CommPort commPort;
    private SerialPort serialPort;
    private InputStream inStream;
    private OutputStream outStream;
    private boolean comReserve = false;

    private String signal = "Ei signaalia";
    private String operator = "Ei operaattoria";

    LinkedBlockingQueue<String> outLinkedBlockingQueue;
    LinkedBlockingQueue<String> inLinkedBlockingQueue;

    public ComServerThread() throws IOException {
        this.outLinkedBlockingQueue = new LinkedBlockingQueue<String>();
        this.inLinkedBlockingQueue = new LinkedBlockingQueue<String>();
        //this.alive = Boolean.TRUE;
    }

    public void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            this.commPort = portIdentifier.open(this.getClass().getName(), 2000);

            if (this.commPort instanceof SerialPort) {
                this.serialPort = (SerialPort) this.commPort;
                this.serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                this.inStream = this.serialPort.getInputStream();
                this.outStream = this.serialPort.getOutputStream();

                this.serialPort.addEventListener(new SerialReader(this.inStream, this.inLinkedBlockingQueue));
                this.serialPort.notifyOnDataAvailable(true);
            } else {
                System.out.println("Error: Only serial ports are handled.");
            }
        }
    }

    public void close() {
        try {
            this.inStream.close();
        } catch (IOException problem) {
            problem.printStackTrace();
        }
        try {
            this.outStream.close();
        } catch (IOException problem) {
            problem.printStackTrace();
        }
        this.commPort.close();
    }

    /**
     *
     * Plain datasending function. When it receives EOF it will write byte 26 into
     * the datastream.
     *
     * @param data
     * @throws IOException
     */
    private void sendData(String data) throws IOException {
        byte[] buf;

        if (data.equals("EOF")) {
            this.outStream.write(26);
        } else {
            buf = data.getBytes();
            this.outStream.write(buf);
        }
    }

    /**
     * This function will send alarm SMS-message to number it gets as a
     * parameter.
     *
     * @param number
     * @param id
     * @throws IOException
     */
    public void sendAlarm(String number, String id, String message) throws IOException, InterruptedException {
        String command;
        this.comReserve = true;

        this.outLinkedBlockingQueue.put("ATZ\r\n");
        Thread.sleep(250);
        command = this.inLinkedBlockingQueue.poll(250, TimeUnit.MILLISECONDS);
        command = this.inLinkedBlockingQueue.poll(250, TimeUnit.MILLISECONDS);

        this.outLinkedBlockingQueue.put("AT+CMGF=1\r\n");
        Thread.sleep(250);
        command = this.inLinkedBlockingQueue.poll(250, TimeUnit.MILLISECONDS);
        command = this.inLinkedBlockingQueue.poll(250, TimeUnit.MILLISECONDS);

        this.outLinkedBlockingQueue.put("AT+CSCS=GSM\r\n");
        Thread.sleep(250);
        command = this.inLinkedBlockingQueue.poll(250, TimeUnit.MILLISECONDS);
        command = this.inLinkedBlockingQueue.poll(250, TimeUnit.MILLISECONDS);

        //this.outLinkedBlockingQueue.put("AT+CSCA=\"+358405202000\"\r\n");	//sonera
        this.outLinkedBlockingQueue.put("AT+CSCA=\"+358508771010\"\r\n");    //elisa
        Thread.sleep(250);
        command = this.inLinkedBlockingQueue.poll(250, TimeUnit.MILLISECONDS);
        command = this.inLinkedBlockingQueue.poll(250, TimeUnit.MILLISECONDS);

        this.outLinkedBlockingQueue.put("AT+CMGS=\"" + number + "\"\r\n");
        Thread.sleep(250);
        command = this.inLinkedBlockingQueue.poll(250, TimeUnit.MILLISECONDS);
        command = this.inLinkedBlockingQueue.poll(250, TimeUnit.MILLISECONDS);

        this.outLinkedBlockingQueue.put(this.parseScandics(id + ": " + message) + "\r\n");
        Thread.sleep(250);
        this.outLinkedBlockingQueue.put("EOF");
        Thread.sleep(500);
        command = this.inLinkedBlockingQueue.poll(250, TimeUnit.MILLISECONDS);
        command = this.inLinkedBlockingQueue.poll(250, TimeUnit.MILLISECONDS);
        System.out.println("Vastaus: " + command);

        this.inLinkedBlockingQueue.clear(); //throw it away
        Thread.sleep(2000);

        this.comReserve = false;
    }

    /**
     *
     * parseScandics will replace all characters that will not work in
     * GSM-modem
     *
     * @param input
     * @return parsed output string
     */

    public String parseScandics(String input) {
        input = input.replace('_', (char) 0x11);
        input = input.replace('�', (char) 0x0f);
        input = input.replace('�', (char) 0x7b);
        input = input.replace('�', (char) 0x7c);
        input = input.replace('�', (char) 0x5b);
        input = input.replace('�', (char) 0x5c);
        input = input.replace('�', (char) 0x0e);

        return input;
    }

    /**
     * signalStrength returns the gsm-signal strength in percentage value.
     *
     * @return signal strength in percentage value
     */
    public String signalStrength() {
        int length;

        if (!this.comReserve) {
            try {
                this.inLinkedBlockingQueue.clear();
                this.outLinkedBlockingQueue.put("AT+CSQ\r\n");
                Thread.sleep(100);
                this.signal = this.inLinkedBlockingQueue.poll(); //echo
                Thread.sleep(100);
                this.signal = this.inLinkedBlockingQueue.poll(); //response

                if (this.signal == null) {
                    this.signal = "Ei signaalia";
                } else {
                    length = this.signal.length();
                    this.signal = this.signal.substring(6, length - 1);
                    this.signal = this.signal.replace(',', '.');
                }

                System.out.println("Signaali: " + this.signal);
                this.inLinkedBlockingQueue.clear();
            } catch (InterruptedException problem) {
                problem.printStackTrace();
            }
        }
        return this.signal;
    }

    public String serviceProvider() {
        String temp = "";
        int length;

        if (!this.comReserve) {
            try {
                this.inLinkedBlockingQueue.clear();    //clear the incoming buffer
                this.outLinkedBlockingQueue.put("AT+COPS?\r\n");
                Thread.sleep(100);
                this.operator = this.inLinkedBlockingQueue.poll(); //echo
                Thread.sleep(100);
                this.operator = this.inLinkedBlockingQueue.poll(); //respose
                this.inLinkedBlockingQueue.clear();

                if (this.operator == null) {
                    this.operator = "Ei operaattoria";
                } else {
                    length = this.operator.length();
                    if (this.operator.equals("+COPS: 0")) {
                        this.operator = "Haetaan verkkoa";
                    } else {
                        temp = this.operator.substring(12, length - 2);
                        this.operator = temp;
                    }
                }

                System.out.println("Operaattori: " + this.operator);
            } catch (InterruptedException problem) {
                problem.printStackTrace();
            }
        }
        return this.operator;
    }

    /**
     *
     * This method will return all Serial-ports found from computer
     *
     * @return ArrayList<String> ports
     */
    public ArrayList<String> listPorts() {
        ArrayList<String> ports = new ArrayList<String>();

        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            if (portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                ports.add(portIdentifier.getName());
            }
        }
        return ports;
    }

    static String getPortTypeName(int portType) {
        switch (portType) {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }

    private void processMessage() throws InterruptedException, IOException {
        String message = this.outLinkedBlockingQueue.take();
        this.sendData(message);
    }

    public void run() {
        while (!Thread.interrupted()) {
            try {
                processMessage();
            } catch (Throwable problem) {
                problem.printStackTrace();
            }
        }
    }


    /**
     * Class SerialReader, reads data from serialport
     *
     * @author sironju
     */
    public class SerialReader implements SerialPortEventListener {
        private InputStream in;
        private byte[] buffer = new byte[1024];

        public SerialReader(InputStream in, LinkedBlockingQueue<String> inLinkedBlockingQueue) {
            this.in = in;
        }

        public void serialEvent(SerialPortEvent arg0) {
            int data;

            try {
                int len = 0;
                while ((data = in.read()) > -1) {
                    if (data == '\n') {
                        break;
                    }
                    buffer[len++] = (byte) data;
                }
                System.out.print(new String(buffer,0,len));
                try {
                    inLinkedBlockingQueue.put(new String(buffer, 0, len));
                } catch (InterruptedException problem) {
                    problem.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }
}
