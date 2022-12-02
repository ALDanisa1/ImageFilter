import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;

//Anele Danisa
//MeanFilterSerial
/**
 *  Handles the input generates the input image pixels and gives the output
 */
public class MeanFilterSerial {
    /**
     * main method, handles the input and output process
     * @param args input arguments
     * @throws IOException file not found exception
     */
    public static void main(String[] args) throws IOException {
        //taking input from the user
        String inputfile = args[0];
        String outputfile = args[1];
        int window = Integer.parseInt(args[2]);
        BufferedImage image = ImageIO.read(new File(inputfile));
        int width = image.getWidth();
        int height = image.getHeight();
        ArrayList<int[]> RGB_windows = new ArrayList();
        ArrayList<int[]> dest_windows = new ArrayList();

        //initializing picture pixels for each window from the input picture to the array list
        if (window >= 3 && window % 2 != 0) {
            //loop throughout the image with the step of a window to give the getRGB() method the correct start point
            for (int y = 0; y < height; y += window) {  //loops through the height(vertical) of the picture
                for (int x = 0; x < width; x += window) { //loops through the width(horizontal) of the picture
                    //check if the window size is within the picture width or height
                    if ((x == 0 || x % window == 0) && window - (width - x) <= 0 && window - (height - y) <= 0) {
                        int[] window_list = image.getRGB(x, y, window, window, (int[]) null, 0, window);//get the array of window pixels
                        RGB_windows.add(window_list);
                    }
                }
            }
        }
        else {
            System.out.println("Window size not correct try an odd and greater than 2 window size");
        }

        //retrieving the destination pixels from the Filter class
        long start = System.currentTimeMillis();//timing starts
        for(int i = 0; i < RGB_windows.size(); ++i) {
            MeanFilterS medianFilterSerial = new MeanFilterS(RGB_windows.get(i));
            dest_windows.add(medianFilterSerial.getAverage());
        }
        //System.out.println((System.currentTimeMillis() - start)); //timing ends


        //write the destination pixels to the picture
        Image image_handler = new Image(outputfile, RGB_windows.size(), width, height, image, window);
        image_handler.Write_image(dest_windows);
    }
}

/**
 * Get the filtered destination pixels Sequential
 */
 class MeanFilterS {
    public int[] RGB;
    public int[] dest;
    int sum_red = 0;
    int sum_green = 0;
    int sum_blue = 0;

    public MeanFilterS(int[] RGB) {
        this.RGB = RGB;
        this.dest = new int[RGB.length];
    }

    /**
     * Get the average destination pixels
     * @return filtered destination pixels
     */
    public int[] getAverage() {
        for(int i = 0; i < RGB.length; i++) {
            //get the sum for each component of rgb
            sum_red += RGB[i] >> 16 & 255;
            sum_green += RGB[i] >> 8 & 255 ;
            sum_blue += RGB[i] & 255 ;
        }
        //calculate the average for each
        int ave_red = sum_red / RGB.length;
        int ave_green = sum_green / RGB.length;
        int ave_blue = sum_blue / RGB.length;

        for(int i = 0; i < RGB.length; ++i) {
            int dpixel = -16777216 | ave_red << 16 | ave_green << 8 | ave_blue; //get the new filtered pixel(destination pixel)
            dest[i] = dpixel;
        }
        return dest;
    }
}

/**
 * Get the filtered destination pixels Sequential
 */
class Image {
    public ArrayList<Integer> pixels;
    String image_name;
    int rgbsize;
    int width;
    BufferedImage image;
    int height;
    int window;

    /**
     *Creates an Image Processor object that will handle the process of writing the pixels to the new image
     * @param image input image
     * @param RGB_size size of the array
     * @param w width of the image
     * @param h height of the image
     * @param ima BufferedImage object
     * @param window window size
     */
    public Image(String image, int RGB_size, int w, int h, BufferedImage ima, int window) {
        this.image_name = image;
        this.rgbsize = RGB_size;
        this.pixels = new ArrayList();
        this.width = w;
        this.image = ima;
        this.height = h;
        this.window = window;
    }

    /**
     * Goes through the picture and allocate new filtered pixels
     * @param windows Array list of windows
     * @throws IOException exception error if file name is not found
     */
    public void Write_image(ArrayList<int[]> windows) throws IOException {
        File f = new File(image_name);
        int point = 0;

        for(int y = 0; y < height; y += window) { //looping with the step of a window
            for(int x = 0; x < width; x += window) { //looping with the step of a window
                if ((x == 0 || x % window == 0) && window - (width - x) <= 0 && window - (height - y) <= 0) {
                    image.setRGB(x, y, window, window, (int[])windows.get(point), 0, window);//set the pixels to the image
                    ++point;
                }
            }
        }
        ImageIO.write(image, "jpeg", f);
    }
}