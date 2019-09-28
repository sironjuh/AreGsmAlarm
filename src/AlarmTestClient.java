 /*
  * AlarmClient
  *
  * Forwards simulated Alarm Class 11 Notifications to port 4445.
  *
  * Version History
  *
  * v0.1
  *  - initial release
  *
  * Copyright (c) 2009 Juha-Matti Sironen
  *
  *
  */

 import java.io.*;
 import java.util.*;
 import java.net.*;

 public class AlarmTestClient {

     public static void main(String[] args) {
     	sendAlarm(args[0], args[1]);
     }

     public static void sendAlarm(String id, String data) {
         DatagramSocket socket;
         InetAddress address;
         String datapass;

         try {
             socket = new DatagramSocket();

             datapass = id + ":" + data + " Viesti: " + "Pumppupysähtynyt" + " Tilanne: " + "Hälytys" + " EOF";

             // send buffer
             byte[] buf = new byte[1024];
             buf = datapass.getBytes();

             // using the localhost
             address = InetAddress.getByName("localhost");
             DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
             socket.send(packet);

             socket.close();

         } catch (SocketException problem1) {
             problem1.printStackTrace();
         } catch (UnknownHostException problem) {
             problem.printStackTrace();
         } catch (IOException problem) {
             problem.printStackTrace();
         }
     }
 }
