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

    private static final byte[] CSC0 = DatatypeConverter.parseHexBinary("AAAAAAAA");
    private static final byte[] CSC1 = DatatypeConverter.parseHexBinary("11111111");
    private static final byte[] CSC2 = DatatypeConverter.parseHexBinary("22222222");

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

    private static ResponseAPDU read(int P2, int length) throws CardException {
        System.out.println("Reading " + length + " words at " + P2);
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x80, 0xBE, 00, P2, length));
        System.out.println("SW : " + Integer.toHexString(r.getSW()));
        String text = toString(r.getData());
        if (!text.isEmpty())
            System.out.println(text);
        return r;
    }

    private static ResponseAPDU verify(int P2, byte[] PIN) throws CardException {
        System.out.println("Verifying PIN " + toString(PIN) + " at " + P2);
        ResponseAPDU r = channel.transmit(new CommandAPDU(0x00, 0x20, 0x00, P2, PIN, 0x04));
        System.out.println("SW : " + Integer.toHexString(r.getSW()));
        String text = toString(r.getData());
        if (!text.isEmpty())
            System.out.println(text);
        return r;
    }

    public static void main(String[] args) throws CardException {
        CardTerminal terminal = SmartCard.getTerminals().get(0);
        System.out.println("READER : " + terminal.toString());
        Card card = terminal.connect("T=0");
        System.out.println("ATR : " + toString(card.getATR().getBytes()));
        channel = card.getBasicChannel();

        read(0x06, 0x04);
        verify(P2_CSC0, CSC0);
        read(0x06, 0x04);

        card.disconnect(true);
    }
}
