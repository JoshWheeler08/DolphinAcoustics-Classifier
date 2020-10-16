import tonals.tonal;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class Main {
    public static final String WAV_FILES_DIRECTORY_PATH = "/cs/scratch/jmw37/5th_DCL_data_bottlenose/";
    public static final String ANNOTATIONS_DIRECTORY_PATH = "/cs/home/jmw37/Documents/SecondYear/DolphinAcoustics_VIP/Annotations/bottlenose/";

    public static void main(String[] args) {
        String filename = "palmyra092007FS192-070924-205305";
        tonals.TonalBinaryInputStream instream = new tonals.TonalBinaryInputStream();
        try{
            instream.tonalBinaryInputStream(ANNOTATIONS_DIRECTORY_PATH + filename + ".bin");
            LinkedList<tonal> whistles = instream.getTonals();
            double[][] whistleTimesInWav = extractWhistleTimes(whistles);
            WavHandler wavExtractor = new WavHandler(WAV_FILES_DIRECTORY_PATH + filename + ".wav", whistleTimesInWav);
            wavExtractor.extractAnnotationsFromWav();
        } catch(Exception e) {
            System.out.println("Failed to read from file.");
        }
    }

    private static double[][] extractWhistleTimes(LinkedList<tonal> whistles){
        double[][] whistleTimes = new double[whistles.size()][2];
        Iterator<tonal> iterator  = whistles.iterator();
        int counter = 0;
        while(iterator.hasNext()) {
            tonal whistle = iterator.next();
            double[] minAndMaxTime = getMinAndMaxValue(whistle.get_time());
            whistleTimes[counter++] = minAndMaxTime;
        }
        return whistleTimes;
    }

    private static double[] getMinAndMaxValue(double[] values){
        Arrays.sort(values);
        return new double[]{values[0], values[values.length - 1]};
    }
}
