import java.util.ArrayList;
import java.util.Collections;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
//Anele Danisa
//MedianFilterSerial

/**
 * Handles the input generates the input image pixels and gives the output
 */
public class MedianFilterSerial {

    /**
     * main method, handles the input and output process
     * @param args input arguments
     * @throws IOException file not found exception
     */
    public static void main(String[] args) throws IOException {
        String filename = args[0];
        String outputfile = args[1];
        int window = Integer.parseInt(args[2]);
        BufferedImage image = ImageIO.read(new File(filename));//creating the BufferedImage object
        int width = image.getWidth(); //looping through the height of the image
        int height = image.getHeight();//looping through the width of the image
        ArrayList<int[]> RGB_windows = new ArrayList();
        ArrayList<int[]> dest_windows = new ArrayList();


        //initializing picture pixels for each window from the input picture to the array list
        if (window >= 3 && window % 2 != 0) {
            //loop throughout the image with the step of a window to give the getRGB() method the correct start point
            for (int y = 0; y < height; y += window) { //loops through the height(vertical) of the picture
                for (int x = 0; x < width; x += window) { //loops through the width(horizontal) of the picture
                    if ((x == 0 || x % window == 0) && window - (width - x) <= 0 && window - (height - y) <= 0) { //condition for the ignoring the pixels at the edge of the image
                        int[] window_list = image.getRGB(x, y, window, window, (int[]) null, 0, window);//returns the array of a window with pixels inside
                        RGB_windows.add(window_list);//adding all the windows to the arraylist
                    }
                }
            }
        }
        else {
            System.out.println("Window size not correct try an odd and greater than 2 window size");
        }

        //retrieving the destination pixels from the Filter class
        long start = System.currentTimeMillis();
        for(int i = 0; i < RGB_windows.size(); ++i) {
            MedianFilterS Serial_Median = new MedianFilterS(RGB_windows.get(i));
            dest_windows.add(Serial_Median.getDest());
        }
        //System.out.println(System.currentTimeMillis() - start);


        //write the destination pixels to the picture
        Image_Processor image_handler = new Image_Processor(outputfile, RGB_windows.size(), width, height, image, window);
        image_handler.Write_image(dest_windows);
    }
}

/**
 * Get the filtered destination pixels Sequential
 */
 class MedianFilterS {
    public int[] RGB_pixels ;
    public int[] dest;
    public int RGB_size;

    /**
     * Generate the object with unfiltered pixels
     * @param RGB_pixels unfiltered pixels
     */
    public MedianFilterS(int[] RGB_pixels) {
        this.RGB_pixels = RGB_pixels;
        this.RGB_size = RGB_pixels.length;
        this.dest = new int[RGB_size];

    }

     /**
      * Get the median destination pixels
      * @return filtered destination pixels
      */
    public int[] getDest() {
        ArrayList<Integer> red_pixels = new ArrayList();
        ArrayList<Integer> green_pixels = new ArrayList();
        ArrayList<Integer> blue_pixels = new ArrayList();
        for(int i = 0; i < RGB_size; ++i) {
           //get the pixels and add them to alist
            red_pixels.add(RGB_pixels[i] >> 16 & 255);
            green_pixels.add(RGB_pixels[i] >> 8 & 255);
            blue_pixels.add(RGB_pixels[i] & 255);
        }

        //sorting the list
        Collections.sort(red_pixels);
        Collections.sort(green_pixels);
        Collections.sort(blue_pixels);


        int median_red = red_pixels.get((red_pixels.size() + 1) / 2);
        int median_green = green_pixels.get((green_pixels.size() + 1) / 2);
        int median_blue = blue_pixels.get((blue_pixels.size() + 1) / 2);

        for(int i = 0; i < RGB_size; ++i) {
            //initializing the destination array
            int dpixel = -16777216 | median_red << 16 | median_green << 8 | median_blue;
            dest[i] = dpixel;
        }
        return dest;
    }
}

/**
 * Get the destination pixels and them to the image
 */
class Image_Processor {
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
    public Image_Processor(String image, int RGB_size, int w, int h, BufferedImage ima, int window) {
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
                if ((x == 1 || x % window == 0) && window - (width - x) <= 0 && window - (height - y) <= 0) {
                    image.setRGB(x, y, window, window, (int[])windows.get(point), 0, window);//setting back the windows to an image
                    ++point;
                }
            }
        }

        ImageIO.write(image, "jpeg", f);
    }
}