package openthinclientadvisor;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Die Klasse cNetworkAdapters dient dazu die im System installierten Netzwerkadapter zu ermitteln.
 * Dabei werden Informationen wie die MAC Adresse eines im System installierten Netzwerkadapters gespeichert.
 * Um diese für eine spätere Prüfung im Programmablauf bereit zu halten.
 * Da nur ein Netzwerkadapter neben dem Localhost im System installiert sein darf.
 * Wird dieses Kriterium ebenfalls überprüft.
 * 
 * @author Benedikt Diehl
 */
public class cNetworkAdapters {

    /**
     * Die Variable networkOK hält die Information ob das Prüfungsergebnis den
     * Anforderungen entspricht. Die Variable wird bei der Prüfung des Systems
     * auf true gesetzt, wenn im System nicht mehr als ein aktiver Netzwerkadapter
     * neben dem Loopback installiert ist.
     */
    private boolean networkOk = false;
    /**
     * Die Variable MAC dient als Hilfsvariable und wird im Prüfungsablauf benötigt.
     * Sobald im Prüfungsablauf die MAC Adresse eines gültigen
     * Netzwerkadapters gefunden wird.
     * Wird die Variable MAC auf false gesetzt, um diesen Teil der Prüfung
     * für alle weiteren Netzwerkadapter zu überspringen.
     */
    private boolean MAC = true;
    /**
     * Die Variable MACAddress hält eine MAC Adresse welche zur DHCP Ermittlung benötigt wird.
     * MACAddress wird zu Beginn mit einer Virtuellen MAC Adresse initialisiert,
     * um einen Fehler im Programm Ablauf zu verhindern, wenn bei der Prüfung der Netzwerkadapter
     * keine gültige MAC Adresse ermittelt werden kann.
     */
    private String MACAddress = "00-07-E9-37-2D-02";

    /**
     * Die main Methode überprüft die im System installierten Netzwerkadapter.
     * Während dieser Prüfung werden die im System enthaltenen Netzwerkadapter ermittelt.
     * Deren Daten (Adaptername und IP Adresse) werden gespeichert und die Gesamtanzahl
     * der installierten Netzwerkadapter zusammengezählt und ausgewertet.
     * Während der Überprüfung wird auch versucht, die MAC Adresse eines Netzwerkadapters zu ermitteln.
     * Diese wird in die Variable MACAddress geschrieben.
     * Die ermittelte MAC Adresse wird zur Prüfung des DHCP Servers benötigt.
     *
     * @return
     * Der return Wert enthält das Prüfergebnis und die Details der im System
     * installierten Netzwerkadapter.
     *
     * @throws SocketException
     */
    public String main() throws SocketException {
        int i = 0;
        String StrLocalhost = "";
        String Ergebnis = "";
        String StrNetworkDevises = "";


//          Alle Netzwerkadapter werden mit Hilfe der Java API java.net ermittelt
//          und in eine Enumeration vom Typ NetworkInterface geladen.

        Enumeration<NetworkInterface> interfaceNIC = NetworkInterface.getNetworkInterfaces();



//  Die ermittelten Netzwerkgeräte werden in einer while Schleife durchlaufen
//  und die Konfiguration der einzelnen Adapter wird ausgelesen, ausgewertet und gespeichert.

        while (interfaceNIC.hasMoreElements()) {
            boolean Localhost = false;
            boolean IPaddress = false;
            boolean Loopback = false;
            boolean IPv4 = false;

            NetworkInterface n = interfaceNIC.nextElement();


            Enumeration<InetAddress> addresses = n.getInetAddresses();


// Adressen durchlaufen
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address.getClass().equals(Inet4Address.class)) {
                    IPv4 = true;
                    StrNetworkDevises = StrNetworkDevises + (String.format("Network-Interface: %s (%s)", n.getName(), n.getDisplayName())) + "\r\n";
                    StrNetworkDevises = StrNetworkDevises + (String.format("- %s", address.getHostAddress())) + "\r\n";


//                     Sollte MAC noch true enthalten wird zusätzlich die Hardware Adresse
//                     des Adapters ermittelt um einen gültigen Netzwerkadapter zur
//                     Prüfung des DHCP Servers zu ermitteln.

                    if (MAC) {
                        byte[] hardwareAddress = n.getHardwareAddress();

                        if (hardwareAddress != null) {
                            String result = "";
                            for (int b = 0; b < hardwareAddress.length; b++) {
                                result += String.format((b == 0 ? "" : ":") + "%02X", hardwareAddress[b]);
                            }



//                          Die MAC Adresse 00-50-56 wird ausgeschlossen da dieser
//                          MACAdressbereich von VM Ware adaptern genutzt wird
//                          und diese nicht berücksichtigt werden sollen.

                            if ((!result.startsWith("00:50:56")) && (!result.equals(""))) {
                                this.MACAddress = result;
                                MAC = false;
                            }

                        }
                    }
                }


                if (IPv4) {
                    i++;
                    if ((n.getDisplayName().contains("lo")) || n.getDisplayName().contains("Loopback")) {
                        Localhost = true;
                    }

                    for (InetAddress iaddress : Collections.list(n.getInetAddresses())) {
                        if (Localhost && iaddress.getHostAddress().contains("127.0.0.1")) {
                            IPaddress = true;
                        }
                        if (Localhost && IPaddress && (iaddress.isLoopbackAddress())) {
                            Loopback = true;
                        }
                    }
                    if (Localhost && IPaddress && Loopback) {
                        StrLocalhost = " \r\n" + "The system has got a localhost networkdevice called : " + n.getDisplayName() + " \r\n" + "With the IP address: 127.0.0.1" + " \r\n" + "and loopback is enabled" + " \r\n";
                    }
                    StrNetworkDevises = StrNetworkDevises + "\r\n";
                    IPv4 = false;
                }
            }
        }
        if (i > 2) {
            Ergebnis = StrLocalhost;
            Ergebnis = Ergebnis + (" \r\nWe identified one localhostadapter and  " + (i - 1) + " network devices on your system this could cause some trouble by using the openthinclient software suit.\r\n");

        } else {
            Ergebnis = StrLocalhost;
            Ergebnis = Ergebnis + (" \r\nWe identified one localhostadapter and" + (i - 1) + " network device on your system \r\n this configuration should work correctly.\r\n");
            networkOk = true;
        }

        Ergebnis = Ergebnis + "\r\n" + StrNetworkDevises;


        return Ergebnis;
    }

    /**
     * Die Methode getNetworkOK liefert den Wert der Variable NetworkOK zurück.
     * Die Variable NetworkOK wird während der Methode main der Klasse cNetworkAdapters gesetzt.
     * Sie gibt an ob das System den Anforderungen entspricht.
     *
     * @return
     * Der return Parameter enthält den Wert der Variable NetworkOK.
     */
    public boolean getNetworkOk() {
        return this.networkOk;
    }

    /**
     * Die Methode getMAC gibt den Inhalt der Variable MACAddress zurück.
     *
     * @return
     * Der Return Parameter enthält den Wert der Variable MACAddress.
     */
    public String getMAC() {
        return this.MACAddress;
    }
}
