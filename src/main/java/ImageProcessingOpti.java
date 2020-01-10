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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.lang.StrictMath.*;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_8UC3;

public class ImageProcessingOpti {
    public static final int IMG_WIDTH = 1280;
    public static final int IMG_HEIGHT = 720;
    public static final double SEUIL_HOUGH_DETECTION = 300;
    public static final double PAS_HOUGH_ANGLE = 1;

    public static final double DELTA_FEATURE_COMPARAISON = 10;

    public static final int REDUCE_FACTOR = 2;
    public static final int IMG_WIDTH_REDUCED = 1280 / REDUCE_FACTOR;
    public static final int IMG_HEIGHT_REDUCED = 720 / REDUCE_FACTOR;

    private static final int NUMBER_OF_FRAME_KEPT = 50;

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

    private ArrayList<Float> ueEye1;
    private ArrayList<Float> saturationEye1;
    private ArrayList<Float> ueEye2;
    private ArrayList<Float> saturationEye2;

    private Mat mat_img;
    private BufferedImage mask;
    //hough
    private ArrayList<HoughOpti.Beta>[] beta;
    private HoughOpti h;
    private float[][] hough_out_reduced;

    //substractor
    BackgroundSubtractor backSub;


    //Circles
    private List<HoughCircles.Eyes> listEyes;

    public ImageProcessingOpti() {

        ueEye1 = new ArrayList<>();
        saturationEye1 = new ArrayList<>();
        ueEye2 = new ArrayList<>();
        saturationEye2 = new ArrayList<>();

        videoCap = new VideoCap();

        imgToPlot = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        mask = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);

        //mask = new Mat(IMG_HEIGHT, IMG_WIDTH, CV_8UC3);

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


        //HoughTemplateCreation();
        videoCapFrame = new DisplayFrame();
        finalResultFrame = new DisplayFrame();

        Thread t1 = new Thread(videoCap);
        t1.start();


        // SUBSTRACTOR
        //backSub = Video.createBackgroundSubtractorKNN();

        //MaskingCreator(mask);
        System.out.println("Fin constructeur");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        h.PatternCreation(temp_grad_norm, temp_grad_angle, beta);
        System.out.println("Centre x:" + h.getCol_centre() + "\ty:" + h.getLigne_centre());


        h.PatternCreation(temp_grad_norm, temp_grad_angle, beta);
        //for (ArrayList<HoughOpti.Beta> i : beta)
        //for (HoughOpti.Beta b : i)
        //b.affichage();


        Mat HoughOutMat = new Mat(IMG_HEIGHT, IMG_WIDTH, CV_32F);


        //exit(0);


    }

    public BufferedImage processing() {
        Date date = new Date();
        //System.out.println("loop");
        long startingTime = date.getTime();
//        sourceImg = videoCap.getOneFrame();
        if (videoCap.isReady()) {
            //capture + conversion + reduction
            sourceImg = videoCap.getCurrentImageCopy();

            mat_img = videoCap.getCurrentImageMatCopy();
            HoughCircles c = new HoughCircles(mat_img);

            listEyes = c.getListEyes();
            //System.out.println("Nombre de detections gardéés : " + listEyes.size());

            extractHistogram(sourceImg, listEyes);
            finalResultFrame.paint(videoCapFrame.getGraphics(), mat_img);

           if (!this.ueEye1.isEmpty()) {
                //System.out.println(eyeFeatureExtractor());
                System.out.println(featureComparator(computeMedian(this.ueEye1),computeMedian(this.saturationEye1),computeMedian(this.ueEye2),computeMedian(this.saturationEye2),(float)0.46875,(float)0.12857144,(float)0.44444445,(float)0.12195122));
            }

            //substractor(sourceImg,mask,1600);
            //videoCapFrame.paint(videoCapFrame.getGraphics(), sourceImg);
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

    public boolean featureComparator(float u1,float s1,float u2,float s2,float DBu1,float DBs1,float DBu2,float DBs2){
        double du1,du2,ds1,ds2;
        du1 = pow(u1-DBu1,2);
        du2 =pow(s1-DBs1,2);
        ds1=pow(u2-DBu2,2);
        ds2 =pow(s2-DBs2,2);

        double ecartFeature = sqrt(du1 +du2 + ds1+ds2);
        //System.out.println(ecartFeature);
        return ecartFeature<DELTA_FEATURE_COMPARAISON;
    }

    String eyeFeatureExtractor() {
        return (computeMedian(this.ueEye1) +
                "," + computeMedian(this.saturationEye1) +
                "," + computeMedian(this.ueEye2) +
                "," + this.computeMedian(this.saturationEye2));
    }

    float computeMedian(ArrayList<Float> datas) {
        List<Float> dataCopy = new ArrayList<>();
        for (Float aFloat :
                datas) {
            dataCopy.add(new Float(aFloat));
        }
        dataCopy.sort(Float::compareTo);
        return (dataCopy.get(dataCopy.size() / 2));
    }

    void extractHistogram(BufferedImage image, List<HoughCircles.Eyes> listEyes) {
        int r1, g1, b1, r2, g2, b2;

        double rayon1, minX1, maxX1, minY1, maxY1;
        double rayon2, minX2, maxX2, minY2, maxY2;
        ArrayList<Float> firstUes = new ArrayList<>();
        ArrayList<Float> firsSaturation = new ArrayList<>();
        ArrayList<Float> firsBrightness = new ArrayList<>();
        ArrayList<Float> secondUes = new ArrayList<>();
        ArrayList<Float> secondSaturation = new ArrayList<>();
        ArrayList<Float> secondBrightness = new ArrayList<>();

        for (HoughCircles.Eyes eyes : listEyes) {
            rayon1 = eyes.rayon1;
            rayon2 = eyes.rayon2;
            minX1 = eyes.x1 - rayon1;
            maxX1 = eyes.x1 + rayon1;
            minY1 = eyes.y1 - rayon1;
            maxY1 = eyes.y1 + rayon1;
            minX2 = eyes.x2 - rayon2;
            maxX2 = eyes.x2 + rayon2;
            minY2 = eyes.y2 - rayon2;
            maxY2 = eyes.y2 + rayon2;


            if (minX1 >= 0 && minY1 >= 0 && maxX1 < IMG_WIDTH && maxY1 < IMG_HEIGHT && minX2 >= 0 && minY2 >= 0 && maxX2 < IMG_WIDTH && maxY2 < IMG_HEIGHT) {
                BufferedImage crop1, cropAltered1;
                BufferedImage crop2, cropAltered2;

                crop1 = image.getSubimage((int) minX1, (int) minY1, (int) (2 * rayon1), (int) (2 * rayon1));
                crop2 = image.getSubimage((int) minX2, (int) minY2, (int) (2 * rayon2), (int) (2 * rayon2));

                cropAltered1 = new BufferedImage(crop1.getWidth(), crop1.getWidth(), BufferedImage.TYPE_3BYTE_BGR);
                cropAltered2 = new BufferedImage(crop2.getWidth(), crop2.getWidth(), BufferedImage.TYPE_3BYTE_BGR);


                for (int c = 0; c < crop1.getWidth(); c++) {
                    for (int l = 0; l < crop1.getWidth(); l++) {
                        Color pix1 = new Color(crop1.getRGB(c, l));
                        r1 = pix1.getRed();
                        g1 = pix1.getGreen();
                        b1 = pix1.getBlue();
                        double c_x, c_y;
                        c_x = rayon1;
                        c_y = rayon1;
                        double x_relative = c - c_x;
                        double y_relative = l - c_y;
                        double mask = sqrt(pow(x_relative, 2) + pow(y_relative, 2));
                        if (mask > rayon1 || mask < 0.3 * rayon1) {
                            r1 = 0;
                            g1 = 0;
                            b1 = 0;
                        } else {
                            float[] hsb = Color.RGBtoHSB(r1, g1, b1, null);
                            firstUes.add(hsb[0]);
                            firsSaturation.add(hsb[1]);
                            firsBrightness.add(hsb[2]);

                        }
                        int a = 255;
                        int p;


                        //set the pixel value
                        p = (a << 24) | (r1 << 16) | (g1 << 8) | b1;

                        cropAltered1.setRGB(c, l, p);


                        //float[] hsb = Color.RGBtoHSB(red, green, blue, null);


                    }

                }
                for (int c = 0; c < crop2.getWidth(); c++) {
                    for (int l = 0; l < crop2.getWidth(); l++) {
                        Color pix2 = new Color(crop2.getRGB(c, l));
                        r2 = pix2.getRed();
                        g2 = pix2.getGreen();
                        b2 = pix2.getBlue();
                        double c_x, c_y;
                        c_x = rayon2;
                        c_y = rayon2;
                        double x_relative = c - c_x;
                        double y_relative = l - c_y;
                        double mask = sqrt(pow(x_relative, 2) + pow(y_relative, 2));
                        if (mask > rayon2 || mask < 0.3 * rayon2) {
                            r2 = 0;
                            g2 = 0;
                            b2 = 0;
                        } else {
                            float[] hsb = Color.RGBtoHSB(r2, g2, b2, null);
                            secondUes.add(hsb[0]);
                            secondSaturation.add(hsb[1]);
                            secondBrightness.add(hsb[2]);
                        }
                        int a = 255;
                        int p;


                        //set the pixel value
                        p = (a << 24) | (r2 << 16) | (g2 << 8) | b2;

                        cropAltered2.setRGB(c, l, p);

                    }
                }
                BufferedImage resized1 = resize(cropAltered1, 500, 500);
                BufferedImage resized2 = resize(cropAltered2, 500, 500);
                BufferedImage joinImage = joinBufferedImage(resized1, resized2);
                videoCapFrame.paint(videoCapFrame.getGraphics(), joinImage);
            }
        }
        if (!firstUes.isEmpty() && !secondUes.isEmpty()) {

            firstUes.sort(Float::compareTo);
            firsSaturation.sort(Float::compareTo);
            firsBrightness.sort(Float::compareTo);

            secondUes.sort(Float::compareTo);
            secondSaturation.sort(Float::compareTo);
            secondBrightness.sort(Float::compareTo);

            // TODO REMOVE USELESS VARIABLES

            float firstUeMedian = firstUes.get(firstUes.size() / 2);
            float firstSaturationMedian = firsSaturation.get(firstUes.size() / 2);
            float firstBrightnessMedian = firsBrightness.get(firstUes.size() / 2);

            float secondUeMedian = secondUes.get(firstUes.size() / 2);
            float secondSaturationMedian = secondSaturation.get(firstUes.size() / 2);
            float secondBrightnessMedian = secondBrightness.get(firstUes.size() / 2);


            if (ueEye1.size() >= NUMBER_OF_FRAME_KEPT) {
                ueEye1.remove(0);
                ueEye2.remove(0);
                saturationEye1.remove(0);
                saturationEye2.remove(0);
            }
            ueEye1.add(firstUeMedian);
            ueEye2.add(secondUeMedian);
            saturationEye1.add(firstSaturationMedian);
            saturationEye2.add(secondSaturationMedian);

        }


    }

    public static BufferedImage joinBufferedImage(BufferedImage img1, BufferedImage img2) {

        //do some calculate first
        int offset = 5;
        int wid = img1.getWidth() + img2.getWidth() + offset;
        int height = Math.max(img1.getHeight(), img2.getHeight()) + offset;
        //create a new buffer and draw two image into the new image
        BufferedImage newImage = new BufferedImage(wid, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();
        Color oldColor = g2.getColor();
        //fill background
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, wid, height);
        //draw image
        g2.setColor(oldColor);
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, img1.getWidth() + offset, 0);
        g2.dispose();
        return newImage;
    }

    private static BufferedImage resize(BufferedImage img, int height, int width) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    private void substractor(BufferedImage image, BufferedImage mask, double seuil) {
        double dr, dg, db;
        int p;
        int a = 255;
        for (int c = 0; c < IMG_WIDTH; c++) {
            for (int l = 0; l < IMG_HEIGHT; l++) {

                Color pix = new Color(image.getRGB(c, l));
                Color pixM = new Color(mask.getRGB(c, l));
                dr = pow(pix.getRed() - pixM.getRed(), 2);
                dg = pow(pix.getGreen() - pixM.getGreen(), 2);
                db = pow(pix.getBlue() - pixM.getBlue(), 2);

                double delta = (dr + dg + db);
                if (delta < seuil) {
                    p = (a << 24) | (0 << 16) | (0 << 8) | 0;

                    image.setRGB(c, l, p);
                }


            }

        }

    }

    private void MaskingCreator(BufferedImage img_mask) {
        final int nb_image_mask = 20;
        BufferedImage[] tab;
        tab = new BufferedImage[nb_image_mask];
        int count = 0;
        while (true) {
            if (count == nb_image_mask) break;
            if (videoCap.isReady()) {
                tab[count] = videoCap.getCurrentImageCopy();
                count++;

            }

        }

        int r, g, b, p;
        int a = 255;
        //BufferedImage img_mask = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
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
        //byte[] pixels = ((DataBufferByte) img_mask.getRaster().getDataBuffer()).getData();
        //out.put(0, 0, pixels);

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

        System.out.println("min:" + min + "\tmax:" + max);

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
