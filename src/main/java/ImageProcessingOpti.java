import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static java.lang.StrictMath.floor;

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


    //hough
    private ArrayList<HoughOpti.Beta>[] beta;
    private HoughOpti h;
    private float[][] hough_out_reduced;


    public ImageProcessingOpti() {
        videoCap = new VideoCap();
        imgToPlot = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);


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
    }


    private void HoughTemplateCreation() {

        //INIT HOUGH GENERALISE
        h = new HoughOpti(SEUIL_HOUGH_DETECTION, PAS_HOUGH_ANGLE);
        float[][] temp, temp_reduced, temp_grad_norm, temp_grad_angle;
        temp = new float[IMG_WIDTH][IMG_HEIGHT];
        temp_reduced = new float[IMG_WIDTH_REDUCED][IMG_HEIGHT_REDUCED];
        temp_grad_angle = new float[IMG_WIDTH_REDUCED][IMG_HEIGHT_REDUCED];
        temp_grad_norm = new float[IMG_WIDTH_REDUCED][IMG_HEIGHT_REDUCED];


        BufferedImage template = PatternImage();
        BufferedImageToArray(template, temp);
        ArrayReducer(temp, temp_reduced);


        GradientFastBin(temp_reduced, temp_grad_norm, temp_grad_angle, IMG_WIDTH_REDUCED, IMG_HEIGHT_REDUCED,1000);


        h.Barycentre(temp_reduced);
        //h.PatternCreation(temp_grad_norm,temp_grad_angle);
        System.out.println("Centre x:" + h.getCol_centre() + "\ty:" + h.getLigne_centre());


        h.PatternCreation(temp_grad_norm, temp_grad_angle, beta);
        //for (ArrayList<HoughOpti.Beta> i : beta)
        //for (HoughOpti.Beta b : i)
        //b.affichage();


        //Mat HoughOutMat= new Mat(IMG_HEIGHT, IMG_WIDTH, CV_32F);
        //h.Transformation(gradientNorm,gradientAngles,HoughOutMat);


        //exit(0);


    }

    public BufferedImage processing() {
        Date date = new Date();
        long startingTime = date.getTime();
//        sourceImg = videoCap.getOneFrame();
        if (videoCap.isReady()) {
            //capture + conversion + reduction
            sourceImg = videoCap.getCurrentImageCopy();
            //sourceImg = PatternImage();
            //gradientNormFrame.setCurrentImage(sourceImg);
            videoCapFrame.paint(videoCapFrame.getGraphics(), sourceImg);


            //sourceImg = PatternImage();
            BufferedImageToArray(sourceImg, image);
            ArrayReducer(image, image_reduced);


            GradientFastBin(image_reduced, gradientNorm, gradientAngles, IMG_WIDTH_REDUCED, IMG_HEIGHT_REDUCED, 50);
            //GradientFast(image_reduced, gradientNorm, gradientAngles, IMG_WIDTH_REDUCED, IMG_HEIGHT_REDUCED);

            h.houghVote(gradientNorm, gradientAngles, beta, hough_out_reduced);


            // conversion sortie
            ArrayIncreaser(hough_out_reduced, image_out);
            //ArrayToBufferedImage(gradientAngles, imgToPlot);
            ArrayToBufferedImage(image_out, imgToPlot);

        }
        long loopTime = (new Date()).getTime();
//        System.out.println("Loop duration : " + (loopTime - startingTime));

        finalResultFrame.paint(finalResultFrame.getGraphics(), imgToPlot);
        return imgToPlot;
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

        //System.out.println("min:"+min +"\tmax:"+max);

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


    static BufferedImage PatternImage() {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("/DATA/FAC/M2/carte_a_puce/ellipse2.png"));
        } catch (IOException e) {

        }
        final BufferedImage img1 = img;
        return img;
    }

}
