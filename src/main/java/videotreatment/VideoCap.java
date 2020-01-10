package videotreatment;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import static org.opencv.videoio.Videoio.*;


public class VideoCap implements Runnable {
    private BufferedImage currentImage;
    private Mat currentImageMat;
    private boolean shouldRun;
    private boolean isReady;

    static {
        nu.pattern.OpenCV.loadShared();
    }

    VideoCapture cap;
    Mat2Image mat2Img = new Mat2Image();
    Mat image ;
    VideoCap() {
        cap = new VideoCapture();
        cap.open(0);

        cap.set(CV_CAP_PROP_FRAME_WIDTH, ImageProcessingOpti.IMG_WIDTH); //to get the actual width of the camera
        cap.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, ImageProcessingOpti.IMG_HEIGHT);//to get the actual height of the camera
//        cap.set(CAP_PROP_FPS, 1);
        //cap.set(CAP_PROP_FPS, 1);
        currentImage = null;
        currentImageMat = null;
        shouldRun = true;
        isReady = false;

        image = new Mat();
    }

    @Deprecated
    BufferedImage getOneFrame() {
        cap.read(mat2Img.mat);
        return mat2Img.getImage(mat2Img.mat);
    }
    Mat getOneFrameMat() {
        cap.read(image);
        return image;
    }

    @Override
//    public void run() {
//        while (shouldRun) {
//            cap.read(mat2Img.mat);
//            this.currentImage = mat2Img.getImage(mat2Img.mat);
//            isReady = true;
//        }
//    }
//
//    public synchronized BufferedImage getCurrentImageCopy() {
//        ColorModel cm = currentImage.getColorModel();
//        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
//        WritableRaster raster = currentImage.copyData(null);
//        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
//    }
//
//    public boolean isReady() {
//        return isReady;
//    }
//}

    public void run() {
        while (shouldRun) {
            cap.read(image);
            this.currentImageMat = image;
            this.currentImage = mat2Img.getImage(image);
            isReady = true;
        }
    }

    public synchronized BufferedImage getCurrentImageCopy() {
        ColorModel cm = currentImage.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = currentImage.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public synchronized Mat getCurrentImageMatCopy() {
        return currentImageMat.clone();
    }

    public boolean isReady() {
        return isReady;
    }
}