package gitlet;

import java.util.LinkedList;

/** Represents a branch.
 * @author Ryan Gomes
 */
public class Branch extends LinkedList<Commit> {
    /** A branch.
     * @param name name of the branch */
    public Branch(String name) {
        this.branchName = name;
    }

    /** Set the commit object for the head pointer to commit.
     * @param commit a commit */
    public void setHead(Commit commit) {
        head = commit;
    }

    /** Return the commit object for the head pointer. */
    public Commit getHead() {
        return head;
    }

    /** Return the name of the branch. */
    public String getBranchName() {
        return branchName;
    }

    /** The commit object for the head pointer. */
    private Commit head;

    /** The name of the branch. */
    private String branchName;
}
