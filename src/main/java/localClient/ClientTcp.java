package localClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientTcp {
    public static final int STARTING_STATE = 0;
    public static final int USERNAME_ENTERED = 1;
    public static final int PASSWORD_INFO_SENT = 2;
    public static final int SENDING_BADGE_ID = 3;
    public static final int BADGE_INFO_SENT = 4;
    public static final int SENDING_RETINA_HASH_KEY = 5;
    public static final int WAITING_FINAL_RESPONSE = 6;


    private Socket socket;
    private int portNumber;
    private String address;
    private Boolean isConnected;
    private Boolean shouldRun;
    private BufferedReader inputStream;
    private PrintWriter outputStream;
    private ListenerRunnable listenerRunnable;
    private ClientUI clientUI;
    private int transactionState;
    private int currentSeed;
    private int selX;
    private int selY;
    private CardInterface cardInterface;
    private String badgeId;
    private String hashingRetina;

    public ClientTcp(ClientUI clientUI) {
        this.portNumber = 5000;
        this.address = "localhost";
        this.isConnected = false;
        this.shouldRun = true;
        this.clientUI = clientUI;
        this.transactionState = STARTING_STATE;
        this.cardInterface = new CardPlaceHolder();

    }

    public Boolean connection() {
        try {
            socket = new Socket(this.address, this.portNumber);
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new PrintWriter(socket.getOutputStream(), true);
            listenerRunnable = new ListenerRunnable(clientUI, this);
            Thread listenerThread = new Thread(listenerRunnable);
            listenerThread.start();


        } catch (UnknownHostException e) {
            System.err.println("Hote inconnu");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Can't connect");
            return false;
        }
        isConnected = true;
        return true;
    }

    public void sendMessage(String message) {
        outputStream.println(message);
    }

    public Boolean getConnected() {
        return isConnected;
    }

    public Boolean getShouldRun() {
        return shouldRun;
    }

    public BufferedReader getInputStream() {
        return inputStream;
    }

    public int getTransactionState() {
        return transactionState;
    }

    public void setTransactionState(int transactionState) {
        this.transactionState = transactionState;
    }

    public int getCurrentSeed() {
        return currentSeed;
    }

    public int getSelX() {
        return selX;
    }

    public int getSelY() {
        return selY;
    }

    public CardInterface getCardInterface() {
        return cardInterface;
    }

    public void setBadgeId(String badgeId) {
        this.badgeId = badgeId;
    }

    public void setHashingRetina(String hashingRetina) {
        this.hashingRetina = hashingRetina;
    }

    public String getHashingRetina() {
        return hashingRetina;
    }

    private class ListenerRunnable implements Runnable {
        private ClientUI clientUI;
        private ClientTcp clientTcp;

        public ListenerRunnable(ClientUI clientUI, ClientTcp clientTcp) {
            this.clientUI = clientUI;
            this.clientTcp = clientTcp;
        }

        @Override
        public void run() {
            while (clientTcp.getShouldRun()) {
                if (clientTcp.getConnected()) {
                    String receivedString;
                    try {
                        receivedString = clientTcp.getInputStream().readLine();
//                        clientUI.getReceivedTextArea().setText("");
                        clientUI.getReceivedTextArea().append(receivedString + "\n");
                        if (transactionState == USERNAME_ENTERED) {
                            String[] result = receivedString.split(",");
                            selX = Integer.parseInt(result[0]);
                            selY = Integer.parseInt(result[1]);
                            currentSeed = Integer.parseInt(result[2]);
                        } else if (transactionState == PASSWORD_INFO_SENT) {
                            if (receivedString.equals("correct")) {
                                transactionState = SENDING_BADGE_ID;
                            } else {
                                transactionState = STARTING_STATE;
                            }
                        } else if (transactionState == BADGE_INFO_SENT) {
                            if (receivedString.equals("correct pin")) {
                                transactionState = SENDING_RETINA_HASH_KEY;
                            } else {
                                transactionState = STARTING_STATE;
                            }

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
