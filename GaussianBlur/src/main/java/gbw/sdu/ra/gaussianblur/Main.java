package gbw.sdu.ra.gaussianblur;

import gbw.sdu.ra.gaussianblur.algorithm.GaussianBlurTask;
import gbw.sdu.ra.gaussianblur.logging.SimpleLogger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

public class Main {

    public static void main(String[] args) {
        System.out.println("Running Gaussian Blur Process");
        System.out.println("Cli Args: " +Arrays.asList(args));
        SimpleLogger.log("Process start");
        SimpleLogger.log("CLI Args: " + Arrays.asList(args));
        String inputFileName = null;
        String outputFileName = null;
        int kernelSize = 10;
        int threadCount = 16; //One could use Runtime.getRuntime().availableProcessors(), but we want to use the maximum amount of resources at all times.
        for(String arg : args){
            if(arg.startsWith("--input-file-name")){
                inputFileName = splitAndExtract(arg, "=");
            }
            if(arg.startsWith("--output-file-name")){
                outputFileName = splitAndExtract(arg, "=");
            }
            if(arg.startsWith("--kernel-size")){
                try{
                    kernelSize = Integer.parseInt(splitAndExtract(arg, "="));
                }catch (Exception e){
                    SimpleLogger.log("Error parsing kernel size arg.");
                    SimpleLogger.logAndExit(e.getMessage(),1);
                }
            }
            if(arg.startsWith("--threads")){
                try{
                    threadCount = Integer.parseInt(splitAndExtract(arg, "="));
                }catch (Exception e){
                    SimpleLogger.log("Error parsing thread count");
                    SimpleLogger.logAndExit(e.getMessage(), 1);
                }
            }
        }
        if(inputFileName == null){
            SimpleLogger.logAndExit("--input-file-name arg missing or invalid", 1);
        }
        if(outputFileName == null){
            SimpleLogger.logAndExit("--output-file-name arg missing or invalid", 1);
        }
        SimpleLogger.log("Args successfully parsed running Gaussian Blur with configuration");
        SimpleLogger.log("inputFileName: " + inputFileName);
        SimpleLogger.log("outputFileName: " + outputFileName);
        SimpleLogger.log("kernelSize: " + kernelSize);
        SimpleLogger.log("threadCount: " + threadCount);

        File imageFile = new File("../../" + inputFileName);
        BufferedImage ogImage = null;

        try{
            ogImage = ImageIO.read(imageFile);
        }catch (IOException e){
            e.printStackTrace();
            SimpleLogger.log("Error reading input image");
            SimpleLogger.logAndExit(e.getMessage(),1);
        }

        int width = ogImage.getWidth();
        int height = ogImage.getHeight();

        // Convert the BufferedImage to a pixel array
        int[] pixels = new int[width * height];
        ogImage.getRGB(0, 0, width, height, pixels, 0, width);

        ForkJoinPool pool = new ForkJoinPool(threadCount);
        int[] blurredPixels = pool.invoke(new GaussianBlurTask(pixels, width, height, kernelSize));

        // Create a new BufferedImage with the blurred pixel data
        BufferedImage blurredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        blurredImage.setRGB(0, 0, width, height, blurredPixels, 0, width);

        // Save the blurred image to a file
        assert outputFileName != null;
        File outputImageFile = new File("../../" + outputFileName);
        try{
            ImageIO.write(blurredImage, "png", outputImageFile);
        }catch (IOException e){
            e.printStackTrace();
            SimpleLogger.log("Error saving blurred image");
            SimpleLogger.logAndExit(e.getMessage(), 1);
        }

        System.out.println("Process Complete");
        SimpleLogger.logAndExit("Process Complete", 0);
    }

    /**
     * @return null on invalid format, else the second index
     */
    private static String splitAndExtract(String string, String separator){
        String[] split = string.split(separator);
        if(split.length < 2){
            return null;
        }
        return split[1];
    }



}
