package MyCode;

import WavFile.WavFile;
import WavFile.WavFileException;

import java.io.File;
import java.io.IOException;

/**
 * Generates the spectrogram for a clip (Annotation extracted from wav file).
 */
public class ComputeSpectrogram {
    private WavFile wavFile;

    /**
     * Constructor.
     * @param filename The name of the clip to use.
     */
    public ComputeSpectrogram(String filename){
        try{
            wavFile = WavFile.openWavFile(new File(filename));
            System.out.println("Here is the information about the wav file : ");
            wavFile.display();
            if(wavFile.getNumChannels() > 1){
                System.out.println("System can only handle mono audio signals, be prepared for undefined behaviour (Spectrogram)");
            }
        } catch(Exception e){
            System.out.println("Failed to compute spectrogram of : " + filename);
        }
    }

    /**
     * Generates the spectrogram.
     */
    public void execute(){
        double[] frames = readInFileAsFrames();
        if(frames != null){
            //do something with frames to make spectrogram
        } else{
            System.out.println("Not creating spectrogram");
        }
    }

    /**
     * Reads in the clip as a series of frames.
     * @return The frames stored as a double array.
     */
    private double[] readInFileAsFrames(){
        int numOfFrames = (int)wavFile.getNumFrames();
        double[] frames = new double[numOfFrames];
        try{
            int framesRead = wavFile.readFrames(frames, numOfFrames);
            if(framesRead != numOfFrames){
                System.out.println("Unable to read in entire file, got " + framesRead + " / " + numOfFrames);
            }
            wavFile.close();
            return frames;
        } catch (IOException | WavFileException err){
            System.out.println("Failed to read in frames for spectrogram creation");
        }
        return null;
    }
}
