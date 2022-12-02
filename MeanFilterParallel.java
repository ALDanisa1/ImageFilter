import java.util.concurrent.RecursiveAction;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import javax.imageio.ImageIO;

//Anele Danisa
//MeanFilterParallel

/**
 *  Handles the input generates the input image pixels and gives the output
 */
public class MeanFilterParallel {

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
        int width = image.getWidth();
        int height = image.getHeight();
        ArrayList<int[]> RGB = new ArrayList();
        if (window >= 3 && window % 2 != 0) {
            //loop throughout the image with the step of a window to give the getRGB() method the correct start point
            for (int y = 0; y < height; y += window) { //looping through the height of the image
                for (int x = 0; x < width; x += window) { //looping through the width of the image
                    if ((x == 0 || x % window == 0) && window - (width - x) <= 0 && window - (height - y) <= 0) { //condition for the ignoring the pixels at the edge of the image
                        int[] window_list = image.getRGB(x, y, window, window, null, 0, window);  //returns the array of a window with pixels inside
                        RGB.add(window_list); //adding all the windows to the arraylist
                    }
                }
            }
        }
        else {
            System.out.println("Window size not correct try an odd and greater than 2 window size");
        }

        ArrayList<int[]> dest_windows = new ArrayList(); //creating an array that with receive the destination/filtered pixels

        long start = System.currentTimeMillis(); //start timing the program
        for (int i = 0; i < RGB.size(); ++i) {
            MeanFilterP parallel_mean = new MeanFilterP(RGB.get(i), 0, ((int[]) RGB.get(i)).length);
            ForkJoinPool.commonPool().invoke(parallel_mean);//invoking the forkjoinpool
            dest_windows.add(parallel_mean.getDest());
        }
        //System.out.println((System.currentTimeMillis() - start)); //end timing the program

        Image_Handler image_handler = new Image_Handler(outputfile, RGB.size(), width, height, image, window);
        image_handler.Write_image(dest_windows); //writing the results to the output image
    }
}

/**
 * Generate the pool of threads that do the task recursively and return the filtered results
 */
class MeanFilterP extends RecursiveAction {
    public int[] RGB;
    public int low;
    public int high;
    int cutoff = 5;
    int RGB_size;
    int ave_red = 0;int ave_blue = 0;int ave_green = 0;
    int[] dest_pixels;

    /**
     *Creates an object that filters an array
     * @param RGB stores the pixels of a window
     * @param low thread start point on the array
     * @param high thread end point on the array
     * @Title: MeanFilterP
     */
    public MeanFilterP(int[] RGB, int low, int high) {
        this.RGB = RGB;
        this.low = low;
        this.high = high;
        this.RGB_size = RGB.length;
       this.dest_pixels = new int[this.RGB_size];
    }

    /**
     *Generating the threads that will handle the filtering work
     */
    protected void compute() {
        if (high - low <= cutoff) {//once done cutting the work to chunks start doing the work
            //calculation the sum of a chunk of pixels in a window
            for(int i = low; i < high; ++i) {
                ave_red += (RGB[i] >> 16 & 255);
                ave_green += (RGB[i] >> 8 & 255);
                ave_blue += (RGB[i] & 255);
            }
        } else {
            //cutting to chunk's of pixels
            MeanFilterP left = new MeanFilterP(RGB, low, (low + high) / 2);
            MeanFilterP right = new MeanFilterP(RGB, (low + high) / 2, high);
            //java forkjoin framework application
            left.fork();
            right.compute();
            left.join();
            //joining all the results together after calculations
            ave_red = left.ave_red + right.ave_red;
            ave_green = left.ave_green + right.ave_green;
            ave_blue = left.ave_blue + right.ave_blue;

        }
    }


    /**
     * Generates the filtered destination pixels
     * @return The Array with filtered pixels of a window
     */
    public int[] getDest() {
        for(int i = 0; i < RGB_size; ++i) {
            int dpixel = -16777216 | ave_red/RGB_size  << 16 | ave_green/RGB_size << 8 | ave_blue/RGB_size ;
            dest_pixels[i] = dpixel;
        }
        return dest_pixels;
    }

}

/**
 * Get the destination pixels and them to the image
 */
class Image_Handler {
    public ArrayList<Integer> pixels;
    String image_name;
    int rgbsize;
    int width;
    BufferedImage image;
    int height;
    int window;

    /**
     *Creates an Image handler object that will handle the process of writing the pixels to the new image
     * @param image input image
     * @param RGB_size size of the array
     * @param w width of the image
     * @param h height of the image
     * @param ima BufferedImage object
     * @param window window size
     */
    public Image_Handler(String image, int RGB_size, int w, int h, BufferedImage ima, int window) {
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

