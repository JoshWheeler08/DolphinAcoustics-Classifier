public class WavHeaderInfo {

    private int numberOfChannels;
    private int sampleRate;
    private int dataSizeInBytes;
    private int byteRate;
    private int bitsPerSample;

    public WavHeaderInfo(int[] values){
        this.numberOfChannels = values[0];
        this.sampleRate = values[1];
        this.byteRate = values[2];
        this.bitsPerSample = values[3];
        this.dataSizeInBytes = values[4];
    }

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

    public int getBitsPerSample(){
        return bitsPerSample;
    }
}
