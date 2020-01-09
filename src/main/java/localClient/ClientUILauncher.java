package localClient;

import javax.swing.*;

public class ClientUILauncher {
    public static void main(String[] args) {
        JFrame jFrame = new JFrame("Client application");
        jFrame.setContentPane(new ClientUI().getRootPanel());
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setBounds(0,0,480,240);
    }
}
