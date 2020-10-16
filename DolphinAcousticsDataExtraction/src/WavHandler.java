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
                createWavFile(timeRanges[i][0], timeRanges[i][1]);
            }
        }
    }

    private void createWavFile(double startTime, double endtime, WavHeaderInfo headerInfo){
        double timePeriod = 1/headerInfo.getSampleRate();
        double startTimeRounded =
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
                    {34,36}, // Bits per sample
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
