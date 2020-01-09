package smartcard;

import javax.smartcardio.*;
import javax.xml.bind.DatatypeConverter;
import java.util.List;

/**
 * @author Guillaume
 */
public class SmartCard {
    private static final int P2_CSC0 = 0x07;
    private static final int P2_CSC1 = 0x39;
    private static final int P2_CSC2 = 0x3B;

    private static final byte[] CSC0 = hexFromString("AAAAAAAA");
    private static final byte[] CSC1 = hexFromString("11111111");
    private static final byte[] CSC2 = hexFromString("22222222");

    private static CardChannel channel;

    static public List<CardTerminal> getTerminals() throws CardException {
        return TerminalFactory.getDefault().terminals().list();
    }

    static public String toString(byte[] byteTab) {
        String text = "";
        String hexNumber;
        int i;
        for (i = 0; i < byteTab.length; i++) {
            hexNumber = Integer.toHexString(byteTab[i]);
            if (hexNumber.length() == 1)
                text += " 0" + hexNumber;
            else
                text += " " + hexNumber;
        }
        return text;
    }

    static public String toBString(byte[] byteTab) {
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
        System.out.println("Reading " + length + " words at 0x" + Integer.toHexString(P2));
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x80, 0xBE, 00, P2, length));
        System.out.println("SW : " + Integer.toHexString(r.getSW()));
        if (P2 == 0x04 || P2 == 0x05)
            text = toBString(r.getData());
        else
            text = toString(r.getData());
        if (!text.isEmpty())
            System.out.println(text);
    }

    private static void read(int P2, int length, boolean binary) throws CardException {
        String text;
        System.out.println("Reading " + length + " words at 0x" + Integer.toHexString(P2));
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x80, 0xBE, 00, P2, length));
        System.out.println("SW : " + Integer.toHexString(r.getSW()));
        if (binary)
            text = toBString(r.getData());
        else
            text = toString(r.getData());
        if (!text.isEmpty())
            System.out.println(text);
    }

    private static void getMode() throws CardException {
        String text;
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x80, 0xBE, 00, 0x04, 0x04));
        System.out.println("SW : " + Integer.toHexString(r.getSW()));
        text = toBString(r.getData()).substring(27, 29);
        switch (text) {
            case "01":
                System.out.println("Issuer mode.");
                break;
            case "10":
                System.out.println("User mode.");
                break;
            default:
                System.out.println("Bricked card.");
                break;
        }
    }

    private static void getACA() throws CardException {
        System.out.println("Getting ACA.");
        String text;
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x80, 0xBE, 00, 0x05, 0x04));
        System.out.println("SW : " + Integer.toHexString(r.getSW()));
        text = toBString(r.getData()).substring(27, 35);
        System.out.println("ACA : " + text);
    }

    private static void verify(int P2, byte[] PIN) throws CardException {
        System.out.println("Verifying PIN " + toString(PIN) + " at 0x" + Integer.toHexString(P2));
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x00, 0x20, 0x00, P2, PIN, 0x04));
        System.out.println("SW : " + Integer.toHexString(r.getSW()));
        String text = toString(r.getData());
        if (!text.isEmpty())
            System.out.println(text);

        r = channel.transmit(new CommandAPDU(0x80, 0xBE, 00, 0x07, 0x04));
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
        }
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
        System.out.println("Writing " + toString(data) + " at 0x" + Integer.toHexString(P2));
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x80, 0xDE, 0x00, P2, data, 0x00, length));
        System.out.println("SW : " + Integer.toHexString(r.getSW()));
        String text = toString(r.getData());
        if (!text.isEmpty())
            System.out.println(text);
    }

    private static byte[] hexFromString(String s) {
        return DatatypeConverter.parseHexBinary(s);
    }

    private static void emulateUserMode() throws CardException {
        System.out.println("Emulating User Mode");
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x00, 0x20, 0x00, 0x3A, hexFromString("AAAAAAAA"), 0x04));
        System.out.println("SW : " + Integer.toHexString(r.getSW()));
        String text = toString(r.getData());
        if (!text.isEmpty())
            System.out.println(text);
    }

    private static void userMode() throws CardException {
        System.out.println("Changing to User Mode");
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x80, 0xDE, 0x10, 0x04, hexFromString("00000080"), 0x04));
        System.out.println("SW : " + Integer.toHexString(r.getSW()));
        String text = toString(r.getData());
        if (!text.isEmpty())
            System.out.println(text);
    }

    private static Card resetCard(Card card) throws CardException {
        card.disconnect(true);
        CardTerminal terminal = SmartCard.getTerminals().get(0);
        return terminal.connect("T=0");
    }

    public static void main(String[] args) throws CardException {
        CardTerminal terminal = SmartCard.getTerminals().get(0);
        System.out.println("READER : " + terminal.toString());
        Card card = terminal.connect("T=0");
        System.out.println("ATR : " + toString(card.getATR().getBytes()));
        channel = card.getBasicChannel();

//        verify(0);
//        read(0x04, 04, true);
        getACA();
        verify(0);
        update(0x05, hexFromString("00000022"), 0x04);

        card = resetCard(card);
        channel = card.getBasicChannel();

        getACA();

        read(0x10, 0x04);
        read(0x28, 0x04);
        card.disconnect(true);
    }
}
