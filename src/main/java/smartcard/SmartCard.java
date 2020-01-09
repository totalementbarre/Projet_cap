package smartcard;

import javax.smartcardio.*;
import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

public class SmartCard {

    /*
     *  READ : 80 BE 00 P2 Le
     *  VERIFY : 00 20 00 P2 04 PIN=(AAAAAAAA / 11111111 / 22222222) 04
     */

    private static final boolean DEBUG = false;

    private static final int P2_CSC0 = 0x07;
    private static final int P2_CSC1 = 0x39;
    private static final int P2_CSC2 = 0x3B;

    private static final byte[] CSC0 = hexFromString("AAAAAAAA");
    private static final byte[] CSC1 = hexFromString("11111111");
    private static final byte[] CSC2 = hexFromString("22222222");

    private static CardChannel channel;

    private static List<CardTerminal> getTerminals() throws CardException {
        return TerminalFactory.getDefault().terminals().list();
    }

    private static String toString(byte[] byteTab) {
        String text = "";
        String hexNumber;
        int i;
        for (i = 0; i < byteTab.length; i++) {
            hexNumber = Integer.toHexString(0xFF & byteTab[i]);
            if (hexNumber.length() == 1)
                text += " 0" + hexNumber;
            else
                text += " " + hexNumber;
        }
        return text;
    }

    private static String toStringPSK(byte[] byteTab) {
        String text = "";
        String hexNumber;
        int i;
        for (i = 0; i < byteTab.length; i++) {
            hexNumber = Integer.toHexString(0xFF & byteTab[i]);
            if (hexNumber.length() == 1)
                text += "0" + hexNumber;
            else
                text += "" + hexNumber;
        }
        return text;
    }

    private static String toBString(byte[] byteTab) {
        String text = "";
        String hexNumber;
        int i, j;
        for (i = 0; i < byteTab.length; i++) {
            hexNumber = Integer.toBinaryString(0xFF & byteTab[i]);
            for (j = 8 - hexNumber.length(); j > 0; j--)
                text += "0";
            text += hexNumber;
            text += " ";
        }
        return text;
    }

    private static void read(int P2, int length) throws CardException {
        String text;
        if (DEBUG)
            System.out.println("Reading " + length + " words at 0x" + Integer.toHexString(P2));
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x80, 0xBE, 00, P2, length));
        if (DEBUG)
            System.out.println("SW : " + Integer.toHexString(r.getSW()));
        if (P2 == 0x04 || P2 == 0x05)
            text = toBString(r.getData());
        else
            text = toString(r.getData());
        if (!text.isEmpty() && DEBUG)
            System.out.println(text);
    }

    private static void read(int P2, int length, boolean binary) throws CardException {
        String text;
        if (DEBUG)
            System.out.println("Reading " + length + " words at 0x" + Integer.toHexString(P2));
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x80, 0xBE, 00, P2, length));
        if (DEBUG)
            System.out.println("SW : " + Integer.toHexString(r.getSW()));
        if (binary)
            text = toBString(r.getData());
        else
            text = toString(r.getData());
        if (!text.isEmpty() && DEBUG)
            System.out.println(text);
    }

    private static void getMode() throws CardException {
        String text;
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x80, 0xBE, 00, 0x04, 0x04));
        if (DEBUG)
            System.out.println("SW : " + Integer.toHexString(r.getSW()));
        text = toBString(r.getData()).substring(27, 29);
        switch (text) {
            case "01":
                if (DEBUG)
                    System.out.println("Issuer mode.");
                break;
            case "10":
                if (DEBUG)
                    System.out.println("User mode.");
                break;
            default:
                if (DEBUG)
                    System.out.println("Bricked card.");
                break;
        }
    }

    private static void getACA() throws CardException {
        if (DEBUG)
            System.out.println("Getting ACA.");
        String text;
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x80, 0xBE, 00, 0x05, 0x04));
        if (DEBUG)
            System.out.println("SW : " + Integer.toHexString(r.getSW()));
        text = toBString(r.getData()).substring(27, 35);
        if (DEBUG)
            System.out.println("ACA : " + text);
    }

    private static void verify(int P2, byte[] PIN) throws CardException {
        if (DEBUG)
            System.out.println("Verifying PIN " + toString(PIN) + " at 0x" + Integer.toHexString(P2));
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x00, 0x20, 0x00, P2, PIN, 0x04));
        if (DEBUG)
            System.out.println("SW : " + Integer.toHexString(r.getSW()));
        String text = toString(r.getData());
        if (!text.isEmpty() && DEBUG)
            System.out.println(text);

        /*r = channel.transmit(new CommandAPDU(0x80, 0xBE, 0x00, 0x07, 0x04));
        switch (r.getData()[3]) {
            case -128:
                System.out.println("3 tries left.");
                break;
            case -64:
                System.out.println("2 tries left.");
                break;
            case -32:
                System.out.println("1 try left.");
                break;
            case 0:
                System.out.println("PIN accepted.");
                break;
            default:
                System.out.println("Card blocked.");
                break;
        }*/
    }

    private static void verify(int PIN) throws CardException {
        switch (PIN) {
            case 0:
                verify(P2_CSC0, CSC0);
                break;
            case 1:
                verify(P2_CSC1, CSC1);
                break;
            case 2:
                verify(P2_CSC2, CSC2);
                break;
        }
    }

    private static void update(int P2, byte[] data, int length) throws CardException {
        if (DEBUG)
            System.out.println("Writing " + toString(data) + " at 0x" + Integer.toHexString(P2));
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x80, 0xDE, 0x00, P2, data, 0x00, length));
        if (DEBUG)
            System.out.println("SW : " + Integer.toHexString(r.getSW()));
        String text = toString(r.getData());
        if (!text.isEmpty() && DEBUG)
            System.out.println(text);
    }

    private static byte[] hexFromString(String s) {
        return DatatypeConverter.parseHexBinary(s);
    }

    private static void emulateUserMode() throws CardException {
        if (DEBUG)
            System.out.println("Emulating User Mode");
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x00, 0x20, 0x00, 0x3A, hexFromString("AAAAAAAA"), 0x04));
        if (DEBUG)
            System.out.println("SW : " + Integer.toHexString(r.getSW()));
        String text = toString(r.getData());
        if (!text.isEmpty() && DEBUG)
            System.out.println(text);
    }

    private static void userMode() throws CardException {
        if (DEBUG)
            System.out.println("Changing to User Mode");
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x80, 0xDE, 0x10, 0x04, hexFromString("00000080"), 0x04));
        if (DEBUG)
            System.out.println("SW : " + Integer.toHexString(r.getSW()));
        String text = toString(r.getData());
        if (!text.isEmpty() && DEBUG)
            System.out.println(text);
    }

    private static byte[] generatePSK() {
        Random r = new Random();
        byte[] tab = new byte[32];
        r.nextBytes(tab);
        return tab;
    }

    private static void writePSK(int P2, byte[] psk) throws CardException {
        update(P2, psk, 32);
    }

    private static void getDataWithPIN(String PIN) throws CardException {
        String hPIN = "", data;
        for (char c : PIN.toCharArray())
            hPIN += "0" + c;
        verify(P2_CSC1, hexFromString(hPIN));
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x80, 0xBE, 0x00, 0x10, 32));
        if (r.getSW() == 0x9000)
            data = toStringPSK(getCardID()) + "," + toStringPSK(r.getData());
        else
            data = "-1,-1";

        System.out.println(data);
    }

    private static byte[] getCardID() throws CardException {
        return channel.transmit(new CommandAPDU(0x80, 0xBE, 0x00, 0x01, 4)).getData();
    }

    private static void writeID(int ID) throws CardException {
        update(0x01, ByteBuffer.allocate(4).putInt(ID).array(), 4);
    }

    private static Card resetCard(Card card) throws CardException {
        card.disconnect(true);
        CardTerminal terminal = SmartCard.getTerminals().get(0);
        return terminal.connect("T=0");
    }

    private static void writeACA() throws CardException {
        update(0x05, hexFromString("00000022"), 0x04);
    }

    private static void setPIN(String PIN) throws CardException {
        String hPIN = "";
        for (char c : PIN.toCharArray())
            hPIN += "0" + c;

        update(0x38, hexFromString(hPIN), 4);
    }

    private static boolean checkPINFormat(String PIN) {
        if (PIN.length() != 4)
            return false;
        for (char c : PIN.toCharArray())
            if (!Character.isDigit(c))
                return false;
        return true;
    }

    public static void getIDAndDataWithPIN(String PIN) throws CardException {
        if (!checkPINFormat(PIN)) {
            System.out.println("-1,-1");
            return;
        }
        CardTerminal terminal = SmartCard.getTerminals().get(0);
        if (DEBUG)
            System.out.println("READER : " + terminal.toString());
        Card card = terminal.connect("T=0");
        if (DEBUG)
            System.out.println("ATR : " + toString(card.getATR().getBytes()));
        channel = card.getBasicChannel();

        getDataWithPIN(PIN);

        card.disconnect(true);
    }
}
