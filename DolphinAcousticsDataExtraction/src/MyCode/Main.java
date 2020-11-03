package MyCode;

import tonals.tonal;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.exit;

public class Main {
    public static final String WAV_FILES_DIRECTORY_PATH = "/cs/home/jmw37/Documents/SecondYear/DolphinAcoustics_VIP/PracticeCommonSamples/";
    public static final String ANNOTATIONS_DIRECTORY_PATH = "/cs/home/jmw37/Documents/SecondYear/DolphinAcoustics_VIP/Annotations/common/";
    public static final String CREATED_CLIPS_DIRECTORY_PATH = "/cs/home/jmw37/Documents/SecondYear/DolphinAcoustics_VIP/CreatedClips/";

    public static void main(String[] args) {
        String unparsedFilename;
        String[] fileDetails;
        /* Getting the filename and parsing it */
        do{
            unparsedFilename = getFilename();
            fileDetails = parseFilename(unparsedFilename);
        }while (fileDetails == null);
        double[][] annotationTimesForWav = null;
        try {
            /* Determining the type of file */
            switch(fileDetails[1]){
                case "bin":
                    annotationTimesForWav = extractAnnotationTimesFromBinFile(unparsedFilename);
                    break;
                case "txt":
                    annotationTimesForWav = extractAnnotationTimesFromTxtFile(unparsedFilename);
                    break;
                default:
                    System.out.println("We don't have support for that file type");
                    exit(1);
            }
            if(annotationTimesForWav != null){
                /* Extracting the annotations */
                WavHandler wavExtractor = new WavHandler(WAV_FILES_DIRECTORY_PATH + fileDetails[0] + ".wav", annotationTimesForWav);
                wavExtractor.extractAnnotationsFromWav();
            }
        } catch(Exception e) {
            handleErrorMessage("Failed to extract the annotations", e);
        }
        exit(0);
    }

    /**
     * Gets the filename from the user.
     * @return The filename.
     */
    private static String getFilename(){
        Scanner getAnnotationFile = new Scanner(System.in);
        String filename = null;
        boolean gotAnswer = false;
        do{
            try{
                System.out.print("Enter annotation file : ");
                filename = getAnnotationFile.nextLine();
                gotAnswer = true;
            } catch(Exception e){
                handleErrorMessage("Error reading in filename", e);
            }
        } while(!gotAnswer);
        return filename;
    } //String filename = "Qx-Dc-CC0411-TAT11-CH2-041114-154040-s.bin";


    /**
     * Separates the filename from its extension.
     * @param unparsedFilename The filename inputted by the user.
     * @return A string array containing the name and extension of the file (as separate elements).
     */
    private static String[] parseFilename(String unparsedFilename){
        //https://stackoverflow.com/questions/6768779/test-filename-with-regular-expression
        Pattern pattern = Pattern.compile("^(?<filename>[\\w,\\s-]+)\\.(?<extension>[A-Za-z]{3})$");
        Matcher matcher = pattern.matcher(unparsedFilename);
        String[] filenameDetails = null;
        if(matcher.find()){
            String extension = matcher.group("extension");
            String name = matcher.group("filename");
            filenameDetails = new String[]{name, extension};
        } else {
            System.out.println("Invalid filename, please try again");
        }
        return filenameDetails;
    }

    /**
     * Extracts the min and max times for each annotation in a text file.
     * @param unparsedFilename The user-inputted filename.
     * @return The times for each annotation.
     *
     * (Used for parsing annotation data from selection tables.)
     */
    private static double[][] extractAnnotationTimesFromTxtFile(String unparsedFilename) {
        double[][] annotationTimesInWav = null;
        try{
            int[] fileDetails = getTxtFileDetails();
            List<String> fileLines = Files.readAllLines(Paths.get(ANNOTATIONS_DIRECTORY_PATH + unparsedFilename), StandardCharsets.UTF_8);
            annotationTimesInWav = new double[fileLines.size() - 1][2]; // - 1 because we aren't including the heading row
            /* Iterating through each row in selection table */
            Iterator<String> iter = fileLines.listIterator();
            iter.next(); // Skipping the header of table
            int counter = 0;
            while(iter.hasNext()){
                String[] result = iter.next().split("\t");
                /* Getting the start and end times for an annotation */
                annotationTimesInWav[counter++] = new double[]{ Double.parseDouble(result[fileDetails[0]]), Double.parseDouble(result[fileDetails[1]])};
            }
        } catch(Exception e){
            handleErrorMessage("Failed to extract the annotation data from " + unparsedFilename, e);
        }
        return annotationTimesInWav;
    }

    /**
     * Gets the user to enter which columns the start and end times for an annotation are stored,
     * as well as the delimiter between columns.
     * @return The column indexes.
     */
    private static int[] getTxtFileDetails(){
        Scanner getFileDetails = new Scanner(System.in);
        boolean gotAnswers = false;
        int[] details = null;
        do{
            try{
                System.out.println("Which column is the first value : ");
                int firstColumnIndex = getFileDetails.nextInt() - 1; //we start from 0
                System.out.println("Which column is the second value : ");
                int secondColumnIndex = getFileDetails.nextInt() - 1; //we start from 0
                if(firstColumnIndex < 0 || secondColumnIndex < 0){
                    System.out.println("Input can't be negative");
                } else{
                    details = new int[]{firstColumnIndex, secondColumnIndex};
                    gotAnswers = true;
                }
            } catch(Exception e){
                handleErrorMessage("Invalid user input", e);
            }
        } while(!gotAnswers);
        return details;
    }

    /**
     * Extracts the min and max times for each annotation from a binary file.
     * @param unparsedFilename The user-inputted (complete) filename
     * @return The min and max times for each annotation.
     */
    private static double[][] extractAnnotationTimesFromBinFile(String unparsedFilename){
        double[][] annotationTimesInWav = null;
        try{
            tonals.TonalBinaryInputStream instream = new tonals.TonalBinaryInputStream();
            instream.tonalBinaryInputStream(ANNOTATIONS_DIRECTORY_PATH + unparsedFilename);
            LinkedList<tonal> whistles = instream.getTonals();
            annotationTimesInWav = getAnnotationTimes(whistles);
        } catch(Exception e){
            handleErrorMessage("Failed to extract the annotation data from " + unparsedFilename, e);
        }
        return annotationTimesInWav;
    }

    /**
     * Extracts the start and end times for every annotation and stores them in a 2D array.
     * @param annotations All the annotations encapsulated as a list of tonal nodes.
     * @return The start and end time for each annotation.
     */
    private static double[][] getAnnotationTimes(LinkedList<tonal> annotations){
        double[][] whistleTimes = new double[annotations.size()][2];
        Iterator<tonal> iterator  = annotations.iterator();
        int counter = 0;
        while(iterator.hasNext()) {
            tonal whistle = iterator.next();
            double[] minAndMaxTime = getMinAndMaxValue(whistle.get_time(), whistle.get_duration());
            whistleTimes[counter++] = minAndMaxTime;
        }
        return whistleTimes;
    }

    /**
     * Extracts the minimum and max times for an annotation so that we can get the time range for our new clip.
     * @param values All the time-frequency nodes (tfnode) recorded for a single annotation.
     * @return The earliest and latest times for an annotation.
     */
    private static double[] getMinAndMaxValue(double[] values, double whistleDuration){
        Arrays.sort(values); //Just in case we get annotation information that isn't sorted
        return new double[]{values[0], values[values.length - 1], whistleDuration};
    }

    /**
     * Handles the outputting of error messages.
     * @param message The message to be outputted.
     * @param e The exception object containing specific error details.
     */
    public static void handleErrorMessage(String message, Exception e){
        System.out.println(message);
        System.out.println("Error : " + e.getMessage());
    }
}

//get file working then try to generalise for any delimiter