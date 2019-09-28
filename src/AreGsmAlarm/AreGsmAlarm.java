/**
 * <CODE>AreGsmAlarm</CODE>
 * <p>
 * Main class that is used only for opening the graphical user interface.
 *
 * @author Juha-Matti Sironen
 * @version 1.0
 * @date 27.08.2009
 */

package AreGsmAlarm;

import java.io.IOException;

public class AreGsmAlarm {
    public static void main(String[] args) throws IOException {
        new GsmGUI().start();
    }
}
