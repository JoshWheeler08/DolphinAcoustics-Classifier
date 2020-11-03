import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Handles the extraction of annotations and the creation of new wav files for these annotations.
 */
public class WavHandler {
    private AudioInputStream inputStream;
    private AudioFormat wavFile;
    private FileOutputStream outputStream;
    private double[][] timeRanges;

    /**
     * Constructor.
     * @param filename The name of the sound file we will be extracting the annotations from.
     * @param timeRanges The time range for each annotation
     */
    public WavHandler(String filename, double[][] timeRanges){
        try{
            inputStream = AudioSystem.getAudioInputStream(new File(filename));
            wavFile = inputStream.getFormat();
            if(wavFile.getChannels() > 1){
                System.out.println("System can only handle mono audio signals, be prepared for undefined behaviour");
            }
            this.timeRanges = timeRanges;
        } catch(IOException | UnsupportedAudioFileException e){
            System.out.println("Failed to open audio input stream : " + filename);
        }
    }

    /**
     * Extracts all annotations from a wav file.
     */
    public void extractAnnotationsFromWav(){
        for(int i = 0; i < timeRanges.length; i++){ //for each annotation
            double extraTime;
            if((extraTime = 1.1 - timeRanges[i][2]) > 0){ // Checking whistle duration is longer than 1.1s
                // Adding buffer to either side
                timeRanges[i][0] -= extraTime/2;
                timeRanges[i][1] += extraTime/2;
            }
            byte[] annotationData = extractAnnotation(timeRanges[i][0], timeRanges[i][1], wavFile.getSampleRate(), wavFile.getSampleSizeInBits() / 8);
            storeAnnotationAsWavFile(annotationData);
        }
    }

    /**
     * Stores the extracted annotation recording data as a new wav file.
     * @param data The recording data.
     * @return  Boolean flag indicating success or failure.
     */
    private boolean storeAnnotationAsWavFile(byte[] data){
        //stub, need to create wav file with ALL THE CORRECT HEADER INFO
        return true;
    }

    /**
     * Extracts a single annotation from the wav file.
     * @param startTime The start time of the annotation.
     * @param endtime The end time of the annotation.
     * @param sampleRate The sampling rate of the recording equipment (I.e. the number of samples per second)
     * @param bytesPerSample The number of bytes per sample (bit depth)
     * @return The recording information corresponding to that annotation.
     *
     * When reading from multiple channels we will need to be careful with how the data is organised. Typically : sample1L sample1R sample2L sample2R sample3L sample3R...
     */
    private byte[] extractAnnotation(double startTime, double endtime, double sampleRate, int bytesPerSample){
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
        byte[] audioBytes = new byte[framesToExtract * bytesPerSample];
        try{
            inputStream.read(audioBytes, (numberOfFramesStart * bytesPerSample), (numberOfFramesEnd * bytesPerSample)); //(annotation, byte starting point, byte ending point)
            return audioBytes;
        } catch(IOException e){
            System.out.println("Failed trying to extract an annotation from the wav file");
        }
        return null;
    }

    /* Memory consideration : is it better to load the entire WAV file in or keep jumping into file like this? */

    /**
     * Extracts the header information from the wav file, which can be used for the creation of new smaller wav files.
     * @return A data transfer object containing all of the important header information.
     *
     * NOTE : Not all wav files will fit the canonical form especially when outputted by PAMGuard or Raven, so its unreliable to use byte ranges to extract
     * the necessary header information, therefore this code may FAIL if the wav headers have been tampered with.
     *
     * The canonical form this section is based off is the Microsoft RIFF container format.
     */
    private WavHeaderInfo extractHeaderInfo(){
        try{
            byte[] headerInfo = new byte[44];
            inputStream.read(headerInfo, 0, headerInfo.length);
            ByteBuffer bb;
            int[][] byteRanges = new int[][]{
                    {22,24}, // Number of channels
                    {24,28}, // Sample Rate
                    {28,32}, // Byte Rate
                    {34,36}, // Bits per sample (going to convert to bytes)
                    {40,44}  // Number of bytes in the data chunk
            };
            int[] values = new int[byteRanges.length];
            for(int i = 0; i < values.length; i++){ //extracting the data using byte ranges
                bb = ByteBuffer.wrap(Arrays.copyOfRange(headerInfo, byteRanges[i][0], byteRanges[i][1]));
                bb.order(ByteOrder.LITTLE_ENDIAN);
                values[i] = bb.getInt();
            }
            return new WavHeaderInfo(values);
        } catch(IOException e){
            System.out.println("Error when reading from WAV file");
            System.out.println("Aborting");
        }
        return null;
    }
}

//Trying switching to audio format input stream to abstract reading in the data