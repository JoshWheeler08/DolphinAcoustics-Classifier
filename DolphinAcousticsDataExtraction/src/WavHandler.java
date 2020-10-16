import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class WavHandler {
    private FileInputStream inputStream;
    private FileOutputStream outputStream;
    private double[][] timeRanges;

    public WavHandler(String filename, double[][] timeRanges){
        try{
            inputStream = new FileInputStream(filename);
            this.timeRanges = timeRanges;
        } catch(FileNotFoundException e){
            System.out.println("Failed to find file : " + filename);
        }
    }

    public void extractAnnotationsFromWav(){
        WavHeaderInfo dto = extractHeaderInfo();
        if(dto == null) {
            System.out.println("Wav file header format was not recognised");
            return;
        } else{
            for(int i = 0; i < timeRanges.length; i++){ //for each annotation
                byte[] annotationData = extractAnnotation(timeRanges[i][0], timeRanges[i][1], dto.getSampleRate(), dto.getBytesPerSample());
                storeAnnotationAsWavFile(annotationData);
            }
        }
    }

    private boolean storeAnnotationAsWavFile(byte[] data){
        //stub, need to create wav file with ALL THE CORRECT HEADER INFO
        return true;
    }

    private byte[] extractAnnotation(double startTime, double endtime, double sampleRate, int bytesPerSample){
        double timePeriod = 1/sampleRate;
        int numberOfFramesBeforeStart = (int)(startTime / timePeriod);
        int numberOfFramesBeforeEnd = (int)(endtime / timePeriod);
        int framesToExtract = numberOfFramesBeforeEnd - numberOfFramesBeforeStart;
        byte[] annotation = new byte[framesToExtract * bytesPerSample];
        try{
            inputStream.read(annotation, (numberOfFramesBeforeStart * bytesPerSample), (numberOfFramesBeforeEnd * bytesPerSample));
            return annotation;
        } catch(IOException e){
            System.out.println("Failed trying to extract an annotation from the wav file");
        }
        return null;
    }

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
            for(int i = 0; i < values.length; i++){
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

//WAV files are based off of the Microsoft RIFF container format.
//Rounding to the nearest frame based off the time period is going to be tricky. F = 1 / T.
//Mantissa and Exponent