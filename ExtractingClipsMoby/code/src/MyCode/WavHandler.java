package MyCode;

import WavFile.WavFile;
import WavFile.WavFileException;

import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;

/**
 * Handles the extraction and storage of each annotation.
 */
public class WavHandler {
    private static final String DEFAULT_ANNOTATION_FILENAME = "Annotation"; // Stem of name for new wav files
    private WavFile wavFile;
    private double[] fileAsFrames;
    private int numberOfFramesInFile;
    private double[][] timeRanges;
    private double endTimeOfFileInSeconds;

    /**
     * Constructor.
     * @param filename The name of the sound file we will be extracting the annotations from.
     * @param timeRanges The time range for each annotation.
     */
    public WavHandler(String filename, double[][] timeRanges){
        try{
            wavFile = WavFile.openWavFile(new File(filename));
            numberOfFramesInFile = (int)wavFile.getNumFrames();
            fileAsFrames = new double[numberOfFramesInFile];
            System.out.println("Here is the information about the wav file : ");
            wavFile.display();
            if((endTimeOfFileInSeconds = numberOfFramesInFile / wavFile.getSampleRate()) < 1.1) { //Assuming one channel
                System.out.println("This WAV file is too short, it must be  >= 1.1s.");
                exit(1);
            }
            if(wavFile.getNumChannels() > 1){
                System.out.println("System can only handle mono audio signals, be prepared for undefined behaviour (WavHandler)");
            }
            this.timeRanges = timeRanges;
        } catch(Exception e){
            Main.handleErrorMessage("Failed to open audio input stream : " + filename, e);
        }
    }

    /**
     * Extracts all the annotations from a WAV file.
     */
    public void extractAnnotationsFromWav(){
        try{
            int framesReturned;
            /* Reading in file as frames */
            if((framesReturned = wavFile.readFrames(fileAsFrames, 0, numberOfFramesInFile)) != numberOfFramesInFile){
                System.out.println("Failed to read in all of file, got " + framesReturned + " / " + numberOfFramesInFile + " frames");
                return;
            }
            wavFile.close();
            //clearClipsDirectory(); //where we will store the new WAV files
            for(int i = 0; i < timeRanges.length; i++){ //for each annotation
                double extraTime;
                boolean annotationIsValid = true;
                if((extraTime = 1.1 - timeRanges[i][2]) > 0){ // Checking if annotation is less than 1.1s long
                    annotationIsValid = addBufferToTimeRange(extraTime, i);
                }
                if(annotationIsValid){
                    /* Extracting annotation */
                    double[] annotationData = extractAnnotation(timeRanges[i][0], timeRanges[i][1], wavFile.getSampleRate());
                    /* Storing annotation */
                    if(!storeAnnotationAsWavFile(annotationData, DEFAULT_ANNOTATION_FILENAME + i + ".wav", annotationData.length)){
                        System.out.println("Failed to make a new annotation clip");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Main.handleErrorMessage("Failed to read in file as frames", e);
        }
    }

    /**
     * Handles the process of adding a buffer to the annotation if its less than 1.1 seconds.
     * @param extraTime The amount of time that needs to be added to the annotation to make it 1.1 seconds.
     * @param i The index into the array of annotation times.
     * @return A boolean flag stating whether the issue could be resolved.
     */
    private boolean addBufferToTimeRange(double extraTime, int i){
        if(timeRanges[i][0] - extraTime/2 < 0) { // Checking if the annotation is at the start of the file
            if(timeRanges[i][1] + extraTime < endTimeOfFileInSeconds){  // Seeing if the recording is long enough to add the time buffer to the end time
                timeRanges[i][1] += extraTime;
            } else{ //Failure
                System.out.println("Failed to extend annotation " + i + " to 1.1s");
                return false;
            }
        } else if(timeRanges[i][1] + extraTime/2 > endTimeOfFileInSeconds){ // Checking if the annotation is at the end of the file
            if(timeRanges[i][0] - extraTime > 0){ // Seeing if we can add the buffer to the LHS.
                timeRanges[i][0] -= extraTime;
            } else{ //Failure
                System.out.println("Failed to extend annotation " + i + " to 1.1s");
                return false;
            }
        } else{
            // Adding buffer to either side
            timeRanges[i][0] -= extraTime/2;
            timeRanges[i][1] += extraTime/2;
        }
        return true;
    }

    /**
     * Clears the created clips directory.
     */
    private void clearClipsDirectory(){
        /* Citation : http://helpdesk.objects.com.au/java/how-to-delete-all-files-in-a-directory#:~:text=Use%20the%20listFiles()%20method,used%20to%20delete%20each%20file. */
        File directory = new File(Main.CREATED_CLIPS_DIRECTORY_PATH);
        File[] files = directory.listFiles();
        if(files != null){
            for(File file : files){
                if(!file.delete()) System.out.println("Failed to remove file " + file.getName() + " from " + Main.CREATED_CLIPS_DIRECTORY_PATH);
            }
        }
    }

    /**
     * Stores the extracted annotation data as a new wav file.
     * @param data The annotation data.
     * @param newFilename The name of the new file.
     * @param numberOfFrames How long the annotation is in terms of frames.
     * @return  Boolean flag indicating success or failure.
     */
    private boolean storeAnnotationAsWavFile(double[] data, String newFilename, int numberOfFrames){
        try{
            WavFile newWavFile = WavFile.newWavFile(new File(Main.CREATED_CLIPS_DIRECTORY_PATH + newFilename), wavFile.getNumChannels(), numberOfFrames, wavFile.getValidBits(), wavFile.getSampleRate());
            newWavFile.writeFrames(data, numberOfFrames);
            newWavFile.close();
            return true;
        } catch (IOException | WavFileException wfe){
            Main.handleErrorMessage("Problem creating new wav file", wfe);
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
     * When reading from recordings with multiple channels, we need to be careful since the data is typically organised as
     * sample1L sample1R sample2L sample2R sample3L sample3R...
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
            Main.handleErrorMessage("Failed trying to extract frames from buffer", e);
        }
        return audioFrames;
    }
}