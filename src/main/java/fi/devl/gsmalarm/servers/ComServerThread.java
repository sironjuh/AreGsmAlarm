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

package fi.devl.gsmalarm.servers;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ComServerThread implements Runnable {
    private final static Logger log = Logger.getLogger(ComServerThread.class);

    private CommPort commPort;
    private InputStream inStream;
    private OutputStream outStream;
    private boolean comReserve = false;

    private String signal = "Ei signaalia";

    private LinkedBlockingQueue<String> outLinkedBlockingQueue;
    private LinkedBlockingQueue<String> inLinkedBlockingQueue;

    public ComServerThread() {
        this.outLinkedBlockingQueue = new LinkedBlockingQueue<>();
        this.inLinkedBlockingQueue = new LinkedBlockingQueue<>();
    }

    public void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            log.error("Error: Port is currently in use");
        } else {
            this.commPort = portIdentifier.open(this.getClass().getName(), 2000);

            if (this.commPort != null) {
                SerialPort serialPort = (SerialPort) this.commPort;
                serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                this.inStream = serialPort.getInputStream();
                this.outStream = serialPort.getOutputStream();

                serialPort.addEventListener(new SerialReader(this.inStream, this.inLinkedBlockingQueue));
                serialPort.notifyOnDataAvailable(true);
            } else {
                log.error("Error: Only serial ports are handled.");
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
     * Plain datas ending function. When it receives EOF it will write byte 26 into
     * the datastream.
     *
     * @param data string to be written to outstream
     * @throws IOException if the write fails we get IOException
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
    public void sendAlarm(String number, String id, String message) throws InterruptedException {
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

        // TODO: add Viestikeskus handling
        //this.outLinkedBlockingQueue.put("AT+CSCA=\"+358405202000\"\r\n");	 //sonera
        this.outLinkedBlockingQueue.put("AT+CSCA=\"+358508771010\"\r\n"); //elisa
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
        log.debug("Vastaus: " + command);

        this.inLinkedBlockingQueue.clear(); //throw it away
        Thread.sleep(2000);

        this.comReserve = false;
    }

    /**
     *
     * parseScandics will replace all characters that will not work in
     * GSM-modem
     *
     * @param input alarm input string
     * @return parsed output string
     */

    private String parseScandics(String input) {
        input = input.replace('_', (char) 0x11);
        input = input.replace('ä', (char) 0x0f);
        input = input.replace('Ä', (char) 0x7b);
        input = input.replace('ö', (char) 0x7c);
        input = input.replace('Ö', (char) 0x5b);
        input = input.replace('å', (char) 0x5c);
        input = input.replace('Å', (char) 0x0e);

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

                log.debug("Signaali: " + this.signal);
                this.inLinkedBlockingQueue.clear();
            } catch (InterruptedException problem) {
                problem.printStackTrace();
            }
        }
        return this.signal;
    }

    public String serviceProvider() {
        String result = "Ei operaattoria";
        String response;
        int length;

        if (!this.comReserve) {
            try {
                this.inLinkedBlockingQueue.clear();
                this.outLinkedBlockingQueue.put("AT+COPS?\r\n");
                Thread.sleep(100);
                Thread.sleep(100);
                response = this.inLinkedBlockingQueue.poll(); //respose
                this.inLinkedBlockingQueue.clear();

                if (response == null) {
                    result = "Ei operaattoria";
                } else {
                    length = response.length();
                    if (response.equals("+COPS: 0")) {
                        result = "Haetaan verkkoa";
                    } else {
                        result = response.substring(12, length - 2);
                    }
                }

                log.debug("Operaattori: " + result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     *
     * This method will return all Serial-ports found from computer
     *
     * @return ArrayList<String> ports
     */
    public ArrayList<String> listPorts() {
        ArrayList<String> ports = new ArrayList<>();

        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier portIdentifier = (CommPortIdentifier) portEnum.nextElement();
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

        SerialReader(InputStream in, LinkedBlockingQueue<String> inLinkedBlockingQueue) {
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
                log.debug(new String(buffer,0,len));
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
