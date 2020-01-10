package localClient;

import videotreatment.ImageProcessingOpti;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

public class ClientTcp {
    public static final int STARTING_STATE = 0;
    public static final int USERNAME_ENTERED = 1;
    public static final int PASSWORD_INFO_SENT = 2;
    public static final int SENDING_BADGE_ID = 3;
    public static final int BADGE_INFO_SENT = 4;
    public static final int SENDING_RETINA_HASH_KEY = 5;
    public static final int SENDING_RETINA = 6;
    public static final int AUTHENTICATED = 7;


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
    private String badgeId;
    private String hashingKeyRetina;
    private boolean isRetinaValidated;
    private boolean counterStarted;

    public ClientTcp(ClientUI clientUI) {
        this.portNumber = 5000;
        this.address = "localhost";
        this.isConnected = false;
        this.shouldRun = true;
        this.clientUI = clientUI;
        this.transactionState = STARTING_STATE;
        this.counterStarted = false;
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


    public void setBadgeId(String badgeId) {
        this.badgeId = badgeId;
    }

    public void setHashingKeyRetina(String hashingKeyRetina) {
        this.hashingKeyRetina = hashingKeyRetina;
    }

    public String getHashingKeyRetina() {
        return hashingKeyRetina;
    }

    public void setRetinaValidated(boolean retinaValidated) {
        isRetinaValidated = retinaValidated;
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
            long startingTime = 0;
            ImageProcessingOpti imageProcessingOpti = null;
            while (clientTcp.getShouldRun()) {
                if (clientTcp.getConnected()) {
                    String receivedString;
                    try {
                        receivedString = clientTcp.getInputStream().readLine();
//                        clientUI.getReceivedTextArea().setText("");
                        clientUI.getReceivedTextArea().append(receivedString + "\n");

                        switch (transactionState) {
                            case USERNAME_ENTERED:
                                String[] result = receivedString.split(",");
                                selX = Integer.parseInt(result[0]);
                                selY = Integer.parseInt(result[1]);
                                currentSeed = Integer.parseInt(result[2]);
                                counterStarted = false;
                                break;
                            case PASSWORD_INFO_SENT:
                                if (receivedString.equals("correct")) {
                                    transactionState = SENDING_BADGE_ID;
                                } else {
                                    transactionState = STARTING_STATE;
                                }
                                break;
                            case BADGE_INFO_SENT:
                                if (receivedString.equals("correct pin")) {
                                    outputStream.println(hashingKeyRetina);
                                    transactionState = SENDING_RETINA;
                                } else {
                                    transactionState = STARTING_STATE;
                                }
                                break;

                            case SENDING_RETINA:
                                if (!counterStarted) {
                                    startingTime = (new Date()).getTime();
                                    counterStarted = true;
                                    imageProcessingOpti = new ImageProcessingOpti();

                                }
                                String retinaFeatures = null;
                                retinaFeatures = imageProcessingOpti.processing();
                                if (retinaFeatures != null)
                                    outputStream.println(retinaFeatures);


                                if ((new Date()).getTime() - startingTime < 20000)
                                    transactionState = STARTING_STATE;

                                if (receivedString.equals("match")) {
                                    transactionState = AUTHENTICATED;
                                }
                                break;
                            case AUTHENTICATED :
                                System.out.println("YOU HAVE BEEN AUTHENTICATED !");
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
