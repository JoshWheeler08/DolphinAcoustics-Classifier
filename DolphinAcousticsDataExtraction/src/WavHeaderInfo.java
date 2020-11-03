/**
 * Stores the wav header information for the recording data we're extracting annotations from.
 * We can use this data for creating new waves and ensuring we read in the correct number of bytes.
 */
public class WavHeaderInfo {

    /* See Microsoft RIFF format */
    private int numberOfChannels;
    private int sampleRate;
    private int dataSizeInBytes;
    private int byteRate;
    private int bytesPerSample;

    /**
     * Constructor.
     * @param values The header data to be stored in the instance variables.
     */
    public WavHeaderInfo(int[] values){
        this.numberOfChannels = values[0];
        this.sampleRate = values[1];
        this.byteRate = values[2];
        this.bytesPerSample = values[3] / 8; //converting to bytes
        this.dataSizeInBytes = values[4];
    }

    // Simple getters

    public int getNumberOfChannels(){
        return this.numberOfChannels;
    }

    public int getSampleRate(){
        return this.sampleRate;
    }

    public int getDataSizeInBytes(){
        return this.dataSizeInBytes;
    }

    public int getByteRate(){
        return byteRate;
    }

    public int getBytesPerSample(){
        return bytesPerSample;
    }
}
