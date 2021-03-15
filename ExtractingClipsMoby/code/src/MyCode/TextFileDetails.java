package MyCode;

/**
 * Data transfer object used to store information about the text file.
 */
public class TextFileDetails {
    private int column1;
    private int column2;
    private String delimiter;

    /**
     * Constructor.
     * @param column1 Column containing start time.
     * @param column2 Column containing end time.
     * @param delimiter Delimiter between columns.
     */
    public TextFileDetails(int column1, int column2, String delimiter){
        this.column1 = column1;
        this.column2 = column2;
        this.delimiter = delimiter;
    }

    /* Simple getter functions */

    public int getColumn1() {
        return this.column1;
    }

    public int getColumn2() {
        return this.column2;
    }

    public String getDelimiter(){
        return this.delimiter;
    }
}
