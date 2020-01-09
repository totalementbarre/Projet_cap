import org.opencv.core.Mat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class DisplayFrame extends JFrame {
    private JPanel contentPane;

    public DisplayFrame() throws HeadlessException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(0, 0, ImageProcessingOpti.IMG_WIDTH, ImageProcessingOpti.IMG_HEIGHT);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        setVisible(true);
    }

    public void paint(Graphics graphics, BufferedImage bufferedImage) {
        graphics = contentPane.getGraphics();
        graphics.drawImage(bufferedImage, 0, 0, this);
    }

    public void paint(Graphics graphics, Mat matImage) {
        graphics = contentPane.getGraphics();
        graphics.drawImage(Mat2Image.matToImage(matImage), 0, 0, this);
    }

}
