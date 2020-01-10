package server;

import database.DBConnection;
import database.DatabaseFiller;
import database.UserInfos;
import org.hibernate.Session;
import org.hibernate.Transaction;
import videotreatment.ImageProcessingOpti;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class TCPServer {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    public static final int STARTING_STATE = 0;
    public static final int USERNAME_ENTERED = 1;
    public static final int PASSWORD_INFO_SENT = 2;
    public static final int RECEIVING_BADGE_ID = 3;
    public static final int RECEIVING_RETINA_HASH_KEY = 4;
    private static final int RECEIVING_RETINA_FEATURES = 5;


    //initialize socket and input stream

    private HashMap<InetAddress, Socket> socketsIP;
    private HashMap<Socket, String> socketsId;
    private Boolean stopSCmdThread = false;
    private ArrayList<Socket> sockets;
    private int transactionState;
    private boolean isUsernameCorrect;
    DBConnection dbConnection = new DBConnection();
    Session session;
//    Lock l = new ReentrantLock();

    /**
     * @param port If port is set to -1 localhost is used
     */
    public TCPServer(int port) {
        Socket socket;
        ServerSocket server;
        Scanner sc = new Scanner(System.in);
        sockets = new ArrayList<>();
        socketsIP = new HashMap<>();
        socketsId = new HashMap<>();
        transactionState = 0;
        isUsernameCorrect = false;


        try {
            if (port == -1)
                server = new ServerSocket(9090, 0, InetAddress.getByName(null));
            else
                server = new ServerSocket(port);
            System.out.println(ANSI_GREEN + "Server started" + ANSI_RESET);
            System.out.println("Waiting for a client ...");

            Thread cmd = new Thread(() -> {
                while (true) {
                    String sCmd = sc.nextLine();
                    System.out.println("[CMD] " + sCmd);
                }
            });
            cmd.start();

            do {
                socket = server.accept();
                System.out.println(ANSI_GREEN + "Client accepted : " + socket.getInetAddress().getHostName() + " on " + socket.getInetAddress().getHostAddress() + ANSI_RESET);
                sockets.add(socket);
                socketsIP.put(socket.getInetAddress(), socket);

                SocketLife sl = new SocketLife(socket);
                Thread t = new Thread(sl);
                t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
                t.setName("SocketLife");
                t.start();
                System.out.println("SOCKETS : " + sockets.size());
            } while (true);

        } catch (IOException i) {
            System.err.println(i);
        }
    }

    public static void main(String[] args) {
        new TCPServer(5000);
    }

    Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (th, ex) -> {
        System.err.println("Error happened in " + th.getName() + " :");
        ex.printStackTrace();
        stopSCmdThread = true;
    };

    public class SocketLife implements Runnable {
        Socket s;
        DataInputStream in;
        DataOutputStream out;
        BufferedReader inputStream;
        PrintWriter outputStream;

        public SocketLife(Socket s) {
            this.s = s;
        }

        @Override
        public void run() {
            try {
                inputStream = new BufferedReader(new InputStreamReader(s.getInputStream()));
                outputStream = new PrintWriter(s.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String line;
            UserInfos userInfos = null;
            String retinaInfos = null;
            int currentSeed = (new Random()).nextInt(1000);

            while (true) {

                try {
                    switch (transactionState) {
                        case STARTING_STATE:
                            userInfos = null;
                            currentSeed = (new Random()).nextInt(1000);
                            line = inputStream.readLine();
                            if (line == null)
                                break;
                            session = dbConnection.getSessionFactory().getCurrentSession();
                            Transaction transaction = session.beginTransaction();
                            List<UserInfos> queryResult = session.createSQLQuery("select * from USERINFOS where username = '" + line + "'").addEntity(UserInfos.class).list();
                            transaction.commit();
                            session.close();

                            if (queryResult.size() > 0) {
                                userInfos = queryResult.get(0);
                                isUsernameCorrect = true;
                                System.out.println(ANSI_CYAN + "User found in database" + ANSI_RESET);
                            } else {
                                userInfos = null;
                                System.out.println(ANSI_CYAN + "User not found in database" + ANSI_RESET);
                                isUsernameCorrect = false;
                            }
                            transactionState = USERNAME_ENTERED;
                            break;

                        case USERNAME_ENTERED:
                            if (isUsernameCorrect) {
                                outputStream.println(userInfos.getSelX() + "," +
                                        userInfos.getSelY() + "," +
                                        currentSeed);
                                System.out.println(ANSI_CYAN + "Sending : " +
                                        userInfos.getSelX() + "," +
                                        userInfos.getSelY() + "," +
                                        currentSeed + ANSI_RESET);
                            } else {
                                String messageToSend = "";
                                messageToSend += (new Random()).nextInt(1000) + ",";
                                messageToSend += (new Random()).nextInt(1000) + "," + currentSeed;
                                outputStream.println(messageToSend);
                            }
                            transactionState = PASSWORD_INFO_SENT;
                            break;
                        case (PASSWORD_INFO_SENT):
                            line = inputStream.readLine();
                            if (line == null)
                                break;
                            if (userInfos != null) {
                                /*if (DatabaseFiller.hashPasswordWithSeed(currentSeed, line).equals(
                                        DatabaseFiller.hashPasswordWithSeed(currentSeed, userInfos.getHashedPassword()))) {

                                 */
                                if (line.equals(userInfos.getHashedPassword())) {
                                    // TODO ADD CHECK WITH SEED
                                    outputStream.println("correct");
                                    transactionState = RECEIVING_BADGE_ID;
                                    System.out.println(ANSI_GREEN + "Received correct username/password from user" + ANSI_RESET);
                                    break;
                                }
                            }
                            outputStream.println("incorrect");
                            System.out.println(ANSI_YELLOW + "Received incorrect username/password from user" + ANSI_RESET);
                            transactionState = STARTING_STATE;
                            break;
                        case RECEIVING_BADGE_ID:
                            line = inputStream.readLine();
                            if (line == null)
                                break;

                            if (line.equals(userInfos.getBadgeId())) {
                                outputStream.println("correct pin");
                                transactionState = RECEIVING_RETINA_HASH_KEY;
                                System.out.println(ANSI_GREEN + "Received correct badge id from user" + ANSI_RESET);

                            } else {
                                outputStream.println("incorrect pin");
                                transactionState = STARTING_STATE;
                                System.out.println(ANSI_YELLOW + "Received incorrect badge id from user" + ANSI_RESET);
                            }

                            break;
                        case RECEIVING_RETINA_HASH_KEY:
                            // TODO FILL
                            line = inputStream.readLine();
                            System.out.println(ANSI_CYAN + "Received encryption key : " + line + ANSI_RESET);
                            retinaInfos = DatabaseFiller.decrypt(userInfos.getEncryptedRetina(), line);
                            transactionState = RECEIVING_RETINA_FEATURES;
                            break;
                        case RECEIVING_RETINA_FEATURES:
                            line = inputStream.readLine();
                            String[] result = line.split(",");
                            String[] userRetinaStrings = retinaInfos.split(",");
                            if(ImageProcessingOpti.featureComparator(
                                    Integer.parseInt(result[0]),
                                    Integer.parseInt(result[1]),
                                    Integer.parseInt(result[2]),
                                    Integer.parseInt(result[3]),
                                    Integer.parseInt(userRetinaStrings[0]),
                                    Integer.parseInt(userRetinaStrings[1]),
                                    Integer.parseInt(userRetinaStrings[2]),
                                    Integer.parseInt(userRetinaStrings[3]))){
                                outputStream.println("match");
                            }
                            else{
                                outputStream.println("does not match");
                            }
                            break;
                        default:
                            System.err.println("Wrong value for transaction sate");
                            break;
                    }

                } catch (EOFException i) {
                    System.out.println(ANSI_RED + "Client from " + s.getInetAddress().getHostAddress() + " disconnected" + ANSI_RESET);
                    try {
                        closeConnection();
                        return;
                    } catch (IOException e) {
                        System.err.println("IN DIRTY CLOSING");
                        e.printStackTrace();
                    }
                } catch (SocketException e) {
                    try {
                        closeConnection();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    return;
                } catch (IOException e) {
                    System.err.println("IN SOCKET EXCHANGES");
                    e.printStackTrace();
                }
            }
        }

        private void closeConnection() throws IOException {
            if (socketsId.get(s).equals("local")) {
                for (Socket t_s : sockets)
                    if (t_s != s)
                        new PrintWriter(t_s.getOutputStream(), true).println("OTHERS,local," + (new Date()).getTime() / 1000 + ",DISC,7,,0");
            }
            socketsIP.remove(s.getInetAddress());
            socketsId.remove(s);
            sockets.remove(s);
            s.close();
            out.close();
            in.close();
            stopSCmdThread = true;
        }

    }
}
