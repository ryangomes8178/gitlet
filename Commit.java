package gitlet;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/** Represents a commit.
 * @author Ryan Gomes
 */
public class Commit implements Serializable {

    /** A commit.
     * @param msg message of the commit
     * @param par parent commit
     * @param map blob HashMap */
    public Commit(String msg, String par, HashMap<String, String> map) {
        this.message = msg;
        this.parent = par;
        if (this.parent == null) {
            this.timestamp = new SimpleDateFormat(time).format(0);
        } else {
            this.timestamp = new SimpleDateFormat(time).format(curTime);
        }
        blobs = map;
    }

    /** Return the message of the commit. */
    public String getMessage() {
        return this.message;
    }

    /** Return the timestamp of the commit. */
    public String getTimestamp() {
        return this.timestamp;
    }

    /** Return the parent of the commit. */
    public String getParent() {
        return this.parent;
    }

    /** Return the hashmap that tracks the blobs and corresponding files. */
    public HashMap<String, String> getBlobs() {
        return this.blobs;
    }

    /** Return the sha1 ID of the commit. */
    public String compID() {
        return Utils.sha1(this.message + this.parent + this.timestamp);
    }

    /** Message of the commit. */
    private String message;

    /** Timestamp of the commit. */
    private String timestamp;

    /** Parent of the commit. */
    private String parent;

    /** Hashmap to track the blobs and corresponding files. */
    private HashMap<String, String> blobs;

    /** String pattern for computing timestamp. */
    private String time = "EEE MMM d HH:mm:ss yyyy Z";

    /** Object for computing current time. */
    private Object curTime = System.currentTimeMillis();
}
