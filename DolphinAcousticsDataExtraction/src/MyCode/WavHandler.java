package MyCode;

import WavFile.WavFile;
import WavFile.WavFileException;

import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;

/**
 * Handles the extraction of annotations and the creation of new wav files for these annotations.
 */
public class WavHandler {
    private WavFile wavFile;
    private double[] fileAsFrames;
    private int numberOfFramesInFile;
    private double[][] timeRanges;

    /**
     * Constructor.
     * @param filename The name of the sound file we will be extracting the annotations from.
     * @param timeRanges The time range for each annotation
     */
    public WavHandler(String filename, double[][] timeRanges){
        try{
            wavFile = WavFile.openWavFile(new File(filename));
            numberOfFramesInFile = (int)wavFile.getNumFrames();
            fileAsFrames = new double[numberOfFramesInFile];
            System.out.println("Here is the information about the wav file : ");
            wavFile.display();
            if(wavFile.getNumChannels() > 1){
                System.out.println("System can only handle mono audio signals, be prepared for undefined behaviour (WavHandler)");
            }
            this.timeRanges = timeRanges;
        } catch(Exception e){
            System.out.println("Failed to open audio input stream : " + filename);
            exit(1);
        }
    }

    /**
     * Extracts all annotations from a wav file.
     */
    public void extractAnnotationsFromWav(){
        try{
            int framesReturned;
            if((framesReturned = wavFile.readFrames(fileAsFrames, 0, numberOfFramesInFile)) != numberOfFramesInFile){
                System.out.println("Failed to read in all of file, got " + framesReturned + " / " + numberOfFramesInFile + " frames");
                return;
            }
            wavFile.close();
            for(int i = 0; i < timeRanges.length; i++){ //for each annotation
                double extraTime;
                if((extraTime = 1.1 - timeRanges[i][2]) > 0){ // Checking whistle duration is longer than 1.1s
                    // Adding buffer to either side
                    timeRanges[i][0] -= extraTime/2;
                    timeRanges[i][1] += extraTime/2;
                }
                double[] annotationData = extractAnnotation(timeRanges[i][0], timeRanges[i][1], wavFile.getSampleRate());
                if(!storeAnnotationAsWavFile(annotationData, "Annotation" + Integer.toString(i) + ".wav", annotationData.length)){
                   System.out.println("Failed to make a new annotation clip");
                   return;
               }
            }
        } catch (Exception e) {
            System.out.println("Failed to read in file as frames");
        }
    }

    /**
     * Stores the extracted annotation recording data as a new wav file.
     * @param data The recording data.
     * @return  Boolean flag indicating success or failure.
     */
    private boolean storeAnnotationAsWavFile(double[] data, String newFilename, int numberOfFrames){
        try{
            WavFile newWavFile = WavFile.newWavFile(new File("CreatedClips/" + newFilename), wavFile.getNumChannels(), numberOfFrames, wavFile.getValidBits(), wavFile.getSampleRate());
            newWavFile.writeFrames(data, numberOfFrames);
            newWavFile.close();
            return true;
        } catch (IOException | WavFileException wfe){
            System.out.println("Problem creating new wav file");
            return false;
        }
    }

    /**
     * Extracts a single annotation from the wav file.
     * @param startTime The start time of the annotation.
     * @param endtime The end time of the annotation.
     * @param sampleRate The sampling rate of the recording equipment (I.e. the number of samples per second)
     * @return The recording information corresponding to that annotation.
     *
     * When reading from multiple channels we will need to be careful with how the data is organised. Typically : sample1L sample1R sample2L sample2R sample3L sample3R...
     */
    private double[] extractAnnotation(double startTime, double endtime, double sampleRate){
        double timePeriod = 1/sampleRate;
        /*
            This is the sound data represented as a series of samples/frames (each frame is 1 timePeriod long and consists of {bytesPerSample} bytes)
            We calculate which data to read in by rounding down the lower bound to the nearest frame and rounding the higher bound up (can be seen below).
            We want this rounding effect to get a buffer around the annotation, so that we don't lose any of the important data.
            ---------------------------------------
            | 0  | 1 | 2  | 3 | 4 | 5 | 6 | 7 | 8 |
            ---------------------------------------
            Example:
                Sampling rate : 6Hz
                Time period : 1/6
                Start time : 0.2s
                End time : 0.9s
                Start Frame : roundDown(0.2 / (1/6)) = Frame 1
                End Frame : roundUp(0.9 / (1/6)) = Frame 6 (So we read up but NOT including frame 6).
            Note that the minimum size of an annotation wav file must be 1.1s for ROCCA, so this will be a special case we need to handle.
         */
        int numberOfFramesStart = (int)(startTime / timePeriod);
        int numberOfFramesEnd = (int)Math.ceil(endtime / timePeriod);
        int framesToExtract = numberOfFramesEnd - numberOfFramesStart;
        double[] audioFrames = null;
        try{
            audioFrames = new double[framesToExtract];
            System.arraycopy(fileAsFrames, numberOfFramesStart, audioFrames, 0, framesToExtract);
            return audioFrames;
        } catch(Exception e){
            System.out.println("Failed trying to extract frames from buffer");
            System.out.println("Error : " + e.getMessage());
        }
        return audioFrames;
    }
}
/*
 * Memory consideration : Is it better to load the entire WAV file in or keep jumping into the file? ->
 * Currently it loads the recording into an array which will make the system faster for smaller recordings but could make it impossible to use really large files due to
 * main memory exhaustion.
 *
 */