package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

/** Represents a blob.
 * @author Ryan Gomes
 */
public class Blob implements Serializable {

    /** A blob.
     * @param readFile the file that needs to be blobbed. */
    public Blob(File readFile) {
        blobContents = Utils.readContents(readFile);
        shaID = Utils.sha1(blobContents);
    }

    /** isEqual Function that compares two blobs.
     * @param o other blob
     * @return boolean equality of blobs */
    public boolean compare(Blob o) {
        return Arrays.equals(o.blobContents, blobContents);
    }

    /** Returns sha1 ID of the blob. */
    public String getShaID() {
        return shaID;
    }

    /** Return the byte array of the contents of the blob. */
    public byte[] getBlobContents() {
        return blobContents;
    }

    /** The sha1 serialization ID of the blob. */
    private String shaID;

    /** The contents of a blob. */
    private byte[] blobContents;
}
