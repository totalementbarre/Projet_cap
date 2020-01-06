import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.awt.image.BufferedImage;

import static org.opencv.videoio.Videoio.CAP_PROP_FPS;
import static org.opencv.videoio.Videoio.CV_CAP_PROP_FRAME_WIDTH;


public class VideoCap {

    static {
        nu.pattern.OpenCV.loadShared();
    }

    VideoCapture cap;
    Mat2Image mat2Img = new Mat2Image();

    VideoCap() {
        cap = new VideoCapture();
        cap.open(0);

        cap.set(CV_CAP_PROP_FRAME_WIDTH,1280); //to get the actual width of the camera
        cap.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT,720);//to get the actual height of the camera
        cap.set(CAP_PROP_FPS,1);
    }

    BufferedImage getOneFrame() {
        cap.read(mat2Img.mat);
        return mat2Img.getImage(mat2Img.mat);
    }
}