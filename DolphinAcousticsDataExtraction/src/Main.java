import tonals.tonal;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {
        tonals.TonalBinaryInputStream instream = new tonals.TonalBinaryInputStream();
        try{
            instream.tonalBinaryInputStream("/cs/home/jmw37/Documents/SecondYear/DolphinAcoustics_VIP/Annotations/bottlenose/palmyra092007FS192-070924-205305.bin");
            LinkedList<tonal> whistles = instream.getTonals();
            //tonals.TonalHeader headerInfo = instream.getHeader();
            Iterator<tonal> iterator  = whistles.iterator();
            while(iterator.hasNext()){
                tonal whistle = iterator.next();
                double[] minAndMaxFreq = getMinAndMaxValue(whistle.get_freq());
                double[] minAndMaxTime = getMinAndMaxValue(whistle.get_time());
                System.out.println("Whistle time  = " + minAndMaxTime[0] + " to " +minAndMaxTime[1]);
                System.out.println(whistle.get_duration());
            }
        } catch(Exception e) {
            System.out.println("Failed to read from file.");
        }
    }

    private static double[] getMinAndMaxValue(double[] values){
        Arrays.sort(values);
        return new double[]{values[0], values[values.length - 1]};
    }
}
