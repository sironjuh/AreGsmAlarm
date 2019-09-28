/**
 * <CODE>AreGsmAlarm</CODE>
 * <p>
 * Main class that is used only for opening the graphical user interface.
 *
 * @author Juha-Matti Sironen
 * @version 1.0
 * @date 27.08.2009
 */

package fi.devl.gsmalarm;

import org.apache.log4j.Logger;

public class AreGsmAlarm {
    final static Logger log = Logger.getLogger(AreGsmAlarm.class);

    public static void main(String[] args) {
        log.info("Starting AreGsmAlarm");
        macOsConfig();

        new GsmGUI().start();
    }

    public static void macOsConfig() {
        if (System.getProperty("os.name").contains("Mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "AreGsmAlarm");
        }
    }
}
