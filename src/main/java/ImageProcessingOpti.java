import org.opencv.core.Mat;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static java.lang.StrictMath.floor;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_8UC3;

public class ImageProcessingOpti {
    public static final int IMG_WIDTH = 1280;
    public static final int IMG_HEIGHT = 720;
    public static final double SEUIL_HOUGH_DETECTION = 300;
    public static final double PAS_HOUGH_ANGLE = 1;

    public static final int REDUCE_FACTOR = 2;
    public static final int IMG_WIDTH_REDUCED = 1280 / REDUCE_FACTOR;
    public static final int IMG_HEIGHT_REDUCED = 720 / REDUCE_FACTOR;

    private DisplayFrame videoCapFrame;
    private DisplayFrame finalResultFrame;


    private VideoCap videoCap;

    private float[][] image;
    private float[][] gradientNorm;
    private float[][] gradientAngles;
    private float[][] image_reduced;
    private float[][] image_out;

    private BufferedImage sourceImg;
    private BufferedImage imgToPlot;


    private Mat mat_img;
    private Mat mask;
    //hough
    private ArrayList<HoughOpti.Beta>[] beta;
    private HoughOpti h;
    private float[][] hough_out_reduced;

    //substractor
    BackgroundSubtractor backSub;

    public ImageProcessingOpti() {
        videoCap = new VideoCap();
        imgToPlot = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        //mask = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);

        mask= new Mat(IMG_HEIGHT, IMG_WIDTH, CV_8UC3);

        gradientNorm = new float[IMG_WIDTH_REDUCED][IMG_HEIGHT_REDUCED];
        gradientAngles = new float[IMG_WIDTH_REDUCED][IMG_HEIGHT_REDUCED];

        image = new float[IMG_WIDTH][IMG_HEIGHT];
        image_reduced = new float[IMG_WIDTH_REDUCED][IMG_HEIGHT_REDUCED];
        image_out = new float[IMG_WIDTH][IMG_HEIGHT];

        beta = new ArrayList[(int) (360 / PAS_HOUGH_ANGLE)];
        for (int i = 0; i < (int) (360 / PAS_HOUGH_ANGLE); i++) {
            beta[i] = new ArrayList<>();
        }
        hough_out_reduced = new float[IMG_WIDTH_REDUCED][IMG_HEIGHT_REDUCED];


        HoughTemplateCreation();
        videoCapFrame = new DisplayFrame();
        finalResultFrame = new DisplayFrame();

        Thread t1 = new Thread(videoCap);
        t1.start();


        // SUBSTRACTOR
        backSub = Video.createBackgroundSubtractorKNN();

        MaskingCreator(mask);


    }


    private void HoughTemplateCreation() {

        //INIT HOUGH GENERALISE
        h = new HoughOpti(SEUIL_HOUGH_DETECTION, PAS_HOUGH_ANGLE);
        float[][] temp, temp_reduced, temp_grad_norm, temp_grad_angle;
        temp = new float[IMG_WIDTH][IMG_HEIGHT];
        temp_reduced = new float[IMG_WIDTH_REDUCED][IMG_HEIGHT_REDUCED];
        temp_grad_angle = new float[IMG_WIDTH_REDUCED][IMG_HEIGHT_REDUCED];
        temp_grad_norm = new float[IMG_WIDTH_REDUCED][IMG_HEIGHT_REDUCED];


        BufferedImage template = PatternImage("/DATA/FAC/M2/carte_a_puce/ellipse2.png");
        BufferedImageToArray(template, temp);
        ArrayReducer(temp, temp_reduced);


        GradientFastBin(temp_reduced, temp_grad_norm, temp_grad_angle, IMG_WIDTH_REDUCED, IMG_HEIGHT_REDUCED, 1000);


        h.Barycentre(temp_reduced);
        h.PatternCreation(temp_grad_norm,temp_grad_angle,beta);
        System.out.println("Centre x:" + h.getCol_centre() + "\ty:" + h.getLigne_centre());


        h.PatternCreation(temp_grad_norm, temp_grad_angle, beta);
        //for (ArrayList<HoughOpti.Beta> i : beta)
        //for (HoughOpti.Beta b : i)
        //b.affichage();


        Mat HoughOutMat= new Mat(IMG_HEIGHT, IMG_WIDTH, CV_32F);


        //exit(0);


    }

    public BufferedImage processing() {
        Date date = new Date();
        long startingTime = date.getTime();
//        sourceImg = videoCap.getOneFrame();
        if (videoCap.isReady()) {
            //capture + conversion + reduction
            sourceImg = videoCap.getCurrentImageCopy();


            mat_img = videoCap.getCurrentImageMatCopy();
            HoughCircles c = new HoughCircles(mat_img);
            videoCapFrame.paint(videoCapFrame.getGraphics(), mat_img);
            //sourceImg = PatternImage("/DATA/FAC/M2/carte_a_puce/ellipse3.png");
            //gradientNormFrame.setCurrentImage(sourceImg);

/*

            //SUBSTRACTOR
            //mat_img = videoCap.getCurrentImageMatCopy();
            //Mat maskfg = mask.clone();
            //substractor(mat_img,maskfg);
            //videoCapFrame.paint(videoCapFrame.getGraphics(), maskfg);
            //sourceImg = Mat2Image.matToImage(maskfg);
//            // FIN DE SUSBSTRACTOR


            videoCapFrame.paint(videoCapFrame.getGraphics(), sourceImg);
            //sourceImg = mask;
            BufferedImageToArray(sourceImg, image);
            ArrayReducer(image, image_reduced);


            GradientFastBin(image_reduced, gradientNorm, gradientAngles, IMG_WIDTH_REDUCED, IMG_HEIGHT_REDUCED, 200);
            //GradientFast(image_reduced, gradientNorm, gradientAngles, IMG_WIDTH_REDUCED, IMG_HEIGHT_REDUCED);

            System.out.println("AVANT HOUGH");
            h.houghVoteLine(gradientNorm, gradientAngles, beta, hough_out_reduced);
            System.out.println("APRES HOUGH");

            // conversion sortie
            ArrayIncreaser(hough_out_reduced, image_out);
            ArrayToBufferedImage(image_out, imgToPlot);
            //ArrayToBufferedImage(image_out, imgToPlot);

        }
        long loopTime = (new Date()).getTime();
//        System.out.println("Loop duration : " + (loopTime - startingTime));

        finalResultFrame.paint(finalResultFrame.getGraphics(), imgToPlot);
        */




        }
        return imgToPlot;
    }

    private void substractor(Mat image, Mat mask){



        backSub.apply(image, mask);

    }

    private void MaskingCreator(Mat out) {
        final int nb_image_mask = 20;
        BufferedImage[] tab;
        tab = new BufferedImage[nb_image_mask];
        int count = 0;
        while (true) {
            if (count==nb_image_mask) break;
            System.out.println(count);
            if (videoCap.isReady()) {
                tab[count] = videoCap.getCurrentImageCopy();
                count++;

            }

        }

        int r, g, b, p;
        int a = 255;
        System.out.println("caca");
        BufferedImage img_mask = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        for (int c = 0; c < IMG_WIDTH; c++) {

            //System.out.println(c);
            for (int l = 0; l < IMG_HEIGHT; l++) {
                r = 0;
                g = 0;
                b = 0;
                for (int i = 0; i < nb_image_mask; i++) {
                    //System.out.println("i:"+i);
                    Color pix = new Color(tab[i].getRGB(c, l));
                    r += pix.getRed();
                    g += pix.getGreen();
                    b += pix.getBlue();
                }

                r = r / nb_image_mask;
                g = g / nb_image_mask;
                b = b / nb_image_mask;

                //System.out.println(out[c][l]);
                p = (a << 24) | (r << 16) | (g << 8) | b;

                img_mask.setRGB(c, l, p);
            }

        }
        byte[] pixels = ((DataBufferByte) img_mask.getRaster().getDataBuffer()).getData();
        out.put(0, 0, pixels);
    }

    private void ArrayReducer(float[][] in, float[][] out) {
        int rf = REDUCE_FACTOR; //reduce factor
        for (int c = 0; c < IMG_WIDTH_REDUCED; c++) {
            for (int l = 1; l < IMG_HEIGHT_REDUCED; l++) {
                out[c][l] = in[c * rf][l * rf];
            }
        }
    }

    private void ArrayIncreaser(float[][] in, float[][] out) {
        int rf = REDUCE_FACTOR; //reduce factor
        for (int c = 0; c < IMG_WIDTH; c++) {
            for (int l = 1; l < IMG_HEIGHT; l++) {

                out[c][l] = in[(int) floor(c / rf)][(int) floor(l / rf)];
            }
        }


    }

    private void BufferedImageToArray(BufferedImage in, float[][] out) {
        int r, g, b;
        for (int c = 0; c < IMG_WIDTH; c++) {
            for (int l = 0; l < IMG_HEIGHT; l++) {

                Color pix = new Color(in.getRGB(c, l));
                r = pix.getRed();
                g = pix.getGreen();
                b = pix.getBlue();
                out[c][l] = (r + g + b) / 3;
                //System.out.println(out[c][l]);


            }

        }
    }

    private void ArrayToBufferedImage(float[][] in, BufferedImage out) {
        double pix;
        int r, g, b, a, p;
        //AUTOSCALLING
        double min = 100, max = 0;
        for (int c = 0; c < IMG_WIDTH; c++) {
            for (int l = 0; l < IMG_HEIGHT; l++) {
                pix = in[c][l];
                if (pix > max) max = pix;
                if (pix < min) min = pix;
            }
        }

        System.out.println("min:"+min +"\tmax:"+max);

        for (int c = 0; c < IMG_WIDTH; c++) {
            for (int l = 0; l < IMG_HEIGHT; l++) {
                pix = in[c][l];

                pix = (((pix - min) / (max - min)) - 0.5) * 128 + 128;
                //System.out.println(pix);


                a = 255;
                r = (int) pix;
                g = (int) pix;
                b = (int) pix;

                //set the pixel value
                p = (a << 24) | (r << 16) | (g << 8) | b;

                out.setRGB(c, l, p);
            }
        }
    }

    public void GradientFast(float[][] in, float[][] norm, float[][] angles, int width, int height) {


        float gradH, gradV;
        for (int c = 1; c < width - 1; c++) {
            for (int l = 1; l < height - 1; l++) {

                gradH = -in[c - 1][l - 1] + in[c + 1][l - 1]
                        - 2 * in[c - 1][l] + 2 * in[c + 1][l]
                        - 1 * in[c - 1][l + 1] + in[c + 1][l + 1];


                gradV = -in[c - 1][l - 1] + in[c - 1][l + 1]
                        - 2 * in[c][l - 1] + 2 * in[c][l + 1]
                        - in[c + 1][l - 1] + in[c + 1][l + 1];
                //gradV = 0;
                norm[c][l] = (float) Math.sqrt(Math.pow(gradH, 2) + Math.pow(gradV, 2));
                angles[c][l] = (float) (Math.atan2(gradH, gradV) * 180 / 3.14);
                //System.out.println(angles [c][l]);

            }
        }
    }

    public void GradientFastBin(float[][] in, float[][] norm, float[][] angles, int width, int height, float s) {


        float gradH, gradV, a, n;
        for (int c = 1; c < width - 1; c++) {
            for (int l = 1; l < height - 1; l++) {

                gradH = -in[c - 1][l - 1] + in[c + 1][l - 1]
                        - 2 * in[c - 1][l] + 2 * in[c + 1][l]
                        - 1 * in[c - 1][l + 1] + in[c + 1][l + 1];


                gradV = -in[c - 1][l - 1] + in[c - 1][l + 1]
                        - 2 * in[c][l - 1] + 2 * in[c][l + 1]
                        - in[c + 1][l - 1] + in[c + 1][l + 1];

                n = (float) Math.sqrt(Math.pow(gradH, 2) + Math.pow(gradV, 2));
                //System.out.println(n);
                a = (float) (Math.atan2(gradH, gradV) * 180 / 3.14);

                if (n > s) {
                    norm[c][l] = 255;
                    angles[c][l] = a;
                    //System.out.println(angles [c][l]);
                } else {
                    norm[c][l] = 0;
                    angles[c][l] = 0;

                }
            }
        }
    }


    static BufferedImage PatternImage(String path) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(path));
        } catch (IOException e) {

        }
        final BufferedImage img1 = img;
        return img;
    }

}
