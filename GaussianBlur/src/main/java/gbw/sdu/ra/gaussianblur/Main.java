package gbw.sdu.ra.gaussianblur;

import gbw.sdu.ra.gaussianblur.algorithm.GaussianBlurTask;
import gbw.sdu.ra.gaussianblur.algorithm.GuassianBlurManual;
import gbw.sdu.ra.gaussianblur.logging.SimpleLogger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

import static gbw.sdu.ra.gaussianblur.logging.SimpleLogger.log;
import static gbw.sdu.ra.gaussianblur.logging.SimpleLogger.logAndExit;

public class Main {

    record ArgData(String inputFileName, String outputFileName, int kernelSize, int threadCount ){}

    public static void main(String[] args) {
        System.out.println("Running Gaussian Blur Process");
        System.out.println("Cli Args: " +Arrays.asList(args));
        log("Process start");
        log("CLI Args: " + Arrays.asList(args));

        ArgData parsedArgs = extractArgData(args); //Exits if any is missing or invalid

        File imageFile = new File("../../" + parsedArgs.inputFileName());
        BufferedImage ogImage = null;

        try{
            ogImage = ImageIO.read(imageFile);
        }catch (IOException e){
            e.printStackTrace();
            log("Error reading input image");
            SimpleLogger.logAndExit(e.getMessage(),1);
        }

        int width = ogImage.getWidth();
        int height = ogImage.getHeight();
        log("Image resolution: " + width + "x" + height);
        System.out.println("Image resolution: " + width + "x" + height);

        // Convert the BufferedImage to a pixel array
        int[] pixels = new int[width * height];
        ogImage.getRGB(0, 0, width, height, pixels, 0, width);

        int[] blurredPixels = null;
        long timeA = -1, timeB = -1;
        try{
            timeA = System.currentTimeMillis();
            log("Time start: " + timeA);
            blurredPixels = GuassianBlurManual.run(
                    new ImageData(width, height, pixels),
                    parsedArgs.kernelSize(),
                    parsedArgs.threadCount()
            );
            timeB = System.currentTimeMillis();
        }catch (InterruptedException e){
            e.printStackTrace();
            log("Error running algorithm");
            logAndExit(e.getMessage(), 1);
        }
        log("Time end: " + timeB);
        long deltaMs = timeB - timeA;
        log("Delta ms: " + deltaMs + " in seconds: " + (deltaMs / 1000));

        // Create a new BufferedImage with the blurred pixel data
        BufferedImage blurredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        blurredImage.setRGB(0, 0, width, height, blurredPixels, 0, width);

        // Save the blurred image to a file
        assert parsedArgs.outputFileName() != null;
        File outputImageFile = new File("../../" + parsedArgs.outputFileName());
        try{
            ImageIO.write(blurredImage, "png", outputImageFile);
        }catch (IOException e){
            e.printStackTrace();
            log("Error saving blurred image");
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

    private static ArgData extractArgData(String[] args){
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
                    log("Error parsing kernel size arg.");
                    SimpleLogger.logAndExit(e.getMessage(),1);
                }
            }
            if(arg.startsWith("--threads")){
                try{
                    threadCount = Integer.parseInt(splitAndExtract(arg, "="));
                }catch (Exception e){
                    log("Error parsing thread count");
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
        log("Args successfully parsed running Gaussian Blur with configuration");
        log("inputFileName: " + inputFileName);
        log("outputFileName: " + outputFileName);
        log("kernelSize: " + kernelSize);
        log("threadCount: " + threadCount);
        return new ArgData(inputFileName, outputFileName, kernelSize, threadCount);
    }


}
