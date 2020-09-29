package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/** Represents a Gitlet object.
 * @author Ryan Gomes
 */
public class Gitlet {

    /** Current Working Directory. */
    private File cwd = new File(System.getProperty("user.dir"));

    /** Gitlet Directory. */
    private File gitletDir = Utils.join(cwd, ".gitlet");

    /** Staging Area File. */
    private File stagingArea = Utils.join(gitletDir, "stagingArea");

    /** Removing Area File. */
    private File removingArea = Utils.join(gitletDir, "removingArea");

    /** Branch File. */
    private File branchFile = Utils.join(gitletDir, "branch");

    /** Head Pointer File. */
    private File curBranch = Utils.join(gitletDir, "curBranch");

    /** Blob Directory. */
    private File blobDir = Utils.join(gitletDir, ".blob");

    /** Commits Directory. */
    private File commitsDir = Utils.join(gitletDir, ".commits");

    /** The commit object for the head pointer. */
    private Commit head;

    /** Creates a new Gitlet version-control system in the current directory.
     * This system will automatically start with one commit: a commit that
     * contains no files and has the commit message initial commit (just
     * like that, with no punctuation). It will have a single branch: master,
     * which initially points to this initial commit, and master will be the
     * current branch. The timestamp for this initial commit will be
     * 00:00:00 UTC, Thursday, 1 January 1970 in whatever format you choose
     * for dates (this is called "The (Unix) Epoch", represented internally
     * by the time 0.) Since the initial commit in all repositories created
     * by Gitlet will have exactly the same content, it follows that all
     * repositories will automatically share this commit (they will all
     * have the same UID) and all commits in all repositories will trace
     * back to it.
     * @throws IOException */
    public void init() throws IOException {
        if (!gitletDir.exists()) {
            gitletDir.mkdir();
            stagingArea.mkdir();
            removingArea.mkdir();
            branchFile.mkdir();
            blobDir.mkdir();
            curBranch.createNewFile();
            commitsDir.mkdir();
        } else {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }

        Commit initial = new Commit("initial commit", null, new HashMap<>());
        File initialCommit = Utils.join(commitsDir, initial.compID());
        initialCommit.createNewFile();
        Utils.writeObject(initialCommit, initial);

        File masterFile = Utils.join(branchFile, "master");
        masterFile.createNewFile();
        Branch masterBranch = new Branch("master");
        masterBranch.add(initial);
        masterBranch.setHead(initial);
        Utils.writeObject(masterFile, masterBranch);

        Utils.writeObject(curBranch, masterBranch);
    }

    /** Adds a copy of the file as it currently exists to the staging
     * area (see the description of the commit command). For this reason,
     * adding a file is also called staging the file for addition. Staging
     * an already-staged file overwrites the previous entry in the staging
     * area with the new contents. The staging area should be somewhere
     * in .gitlet. If the current working version of the file is identical
     * to the version in the current commit, do not stage it to be added,
     * and remove it from the staging area if it is already there (as can
     * happen when a file is changed, added, and then changed back). The
     * file will no longer be staged for removal (see gitlet rm), if it
     * was at the time of the command.
     * @param newAdd name of the file
     * @throws IOException */
    public void add(String newAdd) throws IOException {
        File addCommit = Utils.join(cwd, newAdd);

        if (!addCommit.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        Branch branchHolder = Utils.readObject(curBranch, Branch.class);
        head = branchHolder.getHead();
        Blob temp = new Blob(addCommit);

        for (File file: removingArea.listFiles()) {
            if (file.getName().equals(newAdd)) {
                File remove = Utils.join(removingArea, addCommit.getName());
                remove.delete();
                System.exit(0);
            }
        }

        if (head.getParent() == null) {
            File add = Utils.join(stagingArea, addCommit.getName());
            add.createNewFile();
            Utils.writeContents(add, Utils.readContentsAsString(addCommit));
        } else {
            if (head.getBlobs().containsKey(addCommit.getName())) {
                File hold = Utils.join(blobDir, head.getBlobs().get(newAdd));
                Blob blobHolder = Utils.readObject(hold, Blob.class);
                boolean addIf = true;
                File add = Utils.join(stagingArea, addCommit.getName());
                if (blobHolder.compare(temp)
                        && addCommit.getName().equals(newAdd)) {
                    addIf = false;
                }
                if (addIf) {
                    add.createNewFile();
                    String contents = Utils.readContentsAsString((addCommit));
                    Utils.writeContents(add, contents);
                }
            } else {
                File add = Utils.join(stagingArea, addCommit.getName());
                add.createNewFile();
                Utils.writeContents(add, Utils.readContentsAsString(addCommit));
            }
        }
    }

    /** Saves a snapshot of certain files in the current commit and
     * staging area so they can be restored at a later time, creating
     * a new commit. The commit is said to be tracking the saved files.
     * By default, each commit's snapshot of files will be exactly the
     * same as its parent commit's snapshot of files; it will keep
     * versions of files exactly as they are, and not update them.
     * A commit will only update the contents of files it is tracking
     * that have been staged for addition at the time of commit, in
     * which case the commit will now include the version of the file
     * that was staged instead of the version it got from its parent.
     * A commit will save and start tracking any files that were staged
     * for addition but weren't tracked by its parent. Finally, files
     * tracked in the current commit may be untracked in the new commit
     * as a result being staged for removal by the rm command.
     * @param msg message
     * @throws IOException */
    public void commit(String msg) throws IOException {
        if (stagingArea.listFiles().length == 0
                && removingArea.listFiles().length == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        if (msg.length() == 0) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        Branch branch = Utils.readObject(curBranch, Branch.class);
        head = branch.getHead();
        Commit com = new Commit(msg, head.compID(), head.getBlobs());
        for (File file: stagingArea.listFiles()) {
            boolean isDuplicate = true;
            Blob blob = new Blob(file);
            for (Map.Entry<String, String> obj: com.getBlobs().entrySet()) {
                if (file.getName().equals(obj.getKey())) {
                    com.getBlobs().replace(file.getName(), blob.getShaID());
                    isDuplicate = false;
                }
            }
            if (isDuplicate) {
                com.getBlobs().put(file.getName(), blob.getShaID());
            }
            File holder = Utils.join(blobDir, blob.getShaID());
            holder.createNewFile();
            Utils.writeObject(holder, blob);
        }
        for (File file: removingArea.listFiles()) {
            if (com.getBlobs().containsKey(file.getName())) {
                com.getBlobs().remove(file.getName());
            }
        }

        File nextCommit = Utils.join(commitsDir, com.compID());
        nextCommit.createNewFile();
        Utils.writeObject(nextCommit, com);

        Branch branch2 = Utils.readObject(curBranch, Branch.class);
        File hold = Utils.join(branchFile, branch2.getBranchName());
        Branch hold2 = Utils.readObject(hold, Branch.class);
        hold2.add(com);
        hold2.setHead(com);
        Utils.writeObject(curBranch, hold2);
        Utils.writeObject(hold, hold2);

        for (File file: stagingArea.listFiles()) {
            file.delete();
        }
        for (File file: removingArea.listFiles()) {
            file.delete();
        }
    }

    /** Unstage the file if it is currently staged for addition. If the
     * file is tracked in the current commit, stage it for removal and
     * remove the file from the working directory if the user has not
     * already done so (do not remove it unless it is tracked in the
     * current commit).
     * @param fileName name of file */
    public void rm(String fileName) throws IOException {
        Branch branchHolder = Utils.readObject(curBranch, Branch.class);
        head = branchHolder.getHead();
        boolean stage = false;
        for (File file: stagingArea.listFiles()) {
            if (file.getName().equals(fileName)) {
                File staging = Utils.join(stagingArea, file.getName());
                staging.delete();
                stage = true;
            }
        }
        for (Map.Entry<String, String> entry: head.getBlobs().entrySet()) {
            if (fileName.equals(entry.getKey())) {
                File removing = Utils.join(removingArea, fileName);
                removing.createNewFile();
            }
        }
        for (File file: removingArea.listFiles()) {
            File remove = Utils.join(cwd, fileName);
            if (file.getName().equals(remove.getName())) {
                remove.delete();
                stage = true;
            }
        }
        if (!stage) {
            System.out.println("No reason to remove the file.");
        }
    }

    /** Starting at the current head commit, display information about
     * each commit backwards along the commit tree until the initial
     * commit, following the first parent commit links, ignoring any
     * second parents found in merge commits. (In regular Git, this
     * is what you get with git log --first-parent). This set of commit
     * nodes is called the commit's history. For every node in this
     * history, the information it should display is the commit id,
     * the time the commit was made, and the commit message. */
    public void log() {
        Branch branchHolder = Utils.readObject(curBranch, Branch.class);
        head = branchHolder.getHead();
        while (head.getParent() != null) {
            System.out.println("===");
            System.out.println("commit " + head.compID());
            System.out.println("Date: " + head.getTimestamp());
            System.out.println(head.getMessage());
            System.out.println();
            File recentCommit = Utils.join(commitsDir, head.getParent());
            head = Utils.readObject(recentCommit, Commit.class);
        }
        System.out.println("===");
        System.out.println("commit " + head.compID());
        System.out.println("Date: " + head.getTimestamp());
        System.out.println(head.getMessage());
        System.out.println();
    }

    /** Like log, except displays information about all commits
     * ever made. The order of the commits does not matter. */
    public void globalLog() {
        for (File file: commitsDir.listFiles()) {
            Commit commit = Utils.readObject(file, Commit.class);
            System.out.println("===");
            System.out.println("commit " + commit.compID());
            System.out.println("Date: " + commit.getTimestamp());
            System.out.println(commit.getMessage());
            System.out.println();
        }
    }

    /** Prints out the ids of all commits that have the given commit
     * message, one per line. If there are multiple such commits, it
     * prints the ids out on separate lines. The commit message is a
     * single operand; to indicate a multiword message, put the operand
     * in quotation marks, as for the commit command below.
     * @param message commit message */
    public void find(String message) {
        boolean failureCase = true;
        for (File file: commitsDir.listFiles()) {
            Commit commit = Utils.readObject(file, Commit.class);
            if (commit.getMessage().equals(message)) {
                System.out.println(commit.compID());
                failureCase = false;
            }
        }
        if (failureCase) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** Displays what branches currently exist, and marks the current branch
     * with a *. Also displays what files have been staged for addition or
     * removal. */
    public void status() {
        if (!gitletDir.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        System.out.println("=== Branches ===");
        ArrayList<String> branchList = new ArrayList<String>();
        for (File file: branchFile.listFiles()) {
            Branch branch = Utils.readObject(curBranch, Branch.class);
            if (file.getName().equals(branch.getBranchName())) {
                branchList.add("*" + file.getName());
            } else {
                branchList.add(file.getName());
            }
        }
        branchList.sort(String.CASE_INSENSITIVE_ORDER);
        for (String item: branchList) {
            System.out.println(item);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        for (File file: stagingArea.listFiles()) {
            System.out.println(file.getName());
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (File file: removingArea.listFiles()) {
            System.out.println(file.getName());
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();
    }
    /** Takes the version of the file as it exists in the head commit,
     * the front of the current branch, and puts it in the working
     * directory, overwriting the version of the file that's already
     * there if there is one. The new version of the file is not staged.
     * @param fileName name of the file */
    public void checkout1(String fileName) throws IOException {
        Branch branch = Utils.readObject(curBranch, Branch.class);
        head = branch.getHead();
        String blob = "";
        boolean doesExist = false;
        for (Map.Entry<String, String> obj: head.getBlobs().entrySet()) {
            if (obj.getKey().equals(fileName)) {
                doesExist = true;
                blob = obj.getValue();
            }
        }
        if (doesExist) {
            File holder = Utils.join(cwd, fileName);
            if (!holder.exists()) {
                holder.createNewFile();
            }
            File temp = Utils.join(blobDir, blob);
            Blob holderBlob = Utils.readObject(temp, Blob.class);
            Utils.writeContents(holder, holderBlob.getBlobContents());
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    /** Takes the version of the file as it exists in the commit with
     * the given id, and puts it in the working directory, overwriting
     * the version of the file that's already there if there is one.
     * The new version of the file is not staged.
     * @param commitID sha1ID of the commit
     * @param fileName name of the file */
    public void checkout2(String commitID, String fileName) throws IOException {
        String tempID = commitID;
        for (File file: commitsDir.listFiles()) {
            String id = file.getName();
            String subString = id.substring(0, commitID.length());
            if (commitID.equals(subString)) {
                tempID = id;
            }
        }

        File commitFile = Utils.join(commitsDir, tempID);
        if (!commitFile.exists()) {
            System.out.println("No commit with that ID exists.");
            System.exit(0);
        }

        String blob = "";
        Commit commit = Utils.readObject(commitFile, Commit.class);
        boolean doesExist = false;
        for (Map.Entry<String, String> obj: commit.getBlobs().entrySet()) {
            if (obj.getKey().equals(fileName)) {
                doesExist = true;
                blob = obj.getValue();
            }
        }
        if (doesExist) {
            File holder = Utils.join(cwd, fileName);
            if (!holder.exists()) {
                holder.createNewFile();
            }
            File temp = Utils.join(blobDir, blob);
            Blob holderBlob = Utils.readObject(temp, Blob.class);
            Utils.writeContents(holder, holderBlob.getBlobContents());
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    /** Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory, overwriting the versions
     * of the files that are already there if they exist. Also, at the
     * end of this command, the given branch will now be considered the
     * current branch (HEAD). Any files that are tracked in the current
     * branch but are not present in the checked-out branch are deleted.
     * The staging area is cleared, unless the checked-out branch is
     * the current branch.
     * @param branchName name of the branch */
    public void checkout3(String branchName) throws IOException {
        File givenBranch = Utils.join(branchFile, branchName);
        if (!givenBranch.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        Branch branch = Utils.readObject(curBranch, Branch.class);
        if (branch.getBranchName().equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        head = branch.getHead();
        Branch b = Utils.readObject(givenBranch, Branch.class);

        untracked(b.getHead(), head);

        for (Map.Entry<String, String> obj: b.getHead().getBlobs().entrySet()) {
            File cwdPoint = Utils.join(cwd, obj.getKey());
            if (cwdPoint.exists()) {
                File blob = Utils.join(blobDir, obj.getValue());
                Blob blobHolder = Utils.readObject(blob, Blob.class);
                Utils.writeContents(cwdPoint, blobHolder.getBlobContents());
            }
        }

        boolean containsCase = false;
        for (Map.Entry<String, String> obj: b.getHead().getBlobs().entrySet()) {
            for (Map.Entry<String, String> obj2: head.getBlobs().entrySet()) {
                if (obj.getKey().equals(obj2.getKey())) {
                    containsCase = true;
                    break;
                }
            }
            if (!containsCase) {
                File holder = Utils.join(cwd, obj.getKey());
                holder.delete();
            }
        }

        Utils.writeObject(curBranch, b);

        for (File file: cwd.listFiles()) {
            file.delete();
        }

        for (String obj: b.getHead().getBlobs().keySet()) {
            File temp = Utils.join(cwd, obj);
            temp.createNewFile();
            File blob = Utils.join(blobDir, b.getHead().getBlobs().get(obj));
            Blob blobHolder = Utils.readObject(blob, Blob.class);
            Utils.writeContents(temp, blobHolder.getBlobContents());
        }

        if (!branch.getBranchName().equals(branchName)) {
            for (File file: stagingArea.listFiles()) {
                file.delete();
            }
        }
    }

    /** Handles the error case when a file is untracked.
     * @param commit a commit
     * @param commit2 a commit */
    public void untracked(Commit commit, Commit commit2) {
        for (Map.Entry<String, String> obj: commit.getBlobs().entrySet()) {
            File cwdPoint = Utils.join(cwd, obj.getKey());
            boolean failureCase = false;
            for (Map.Entry<String, String> ent: commit2.getBlobs().entrySet()) {
                if (obj.getKey().equals(ent.getKey())) {
                    failureCase = true;
                    break;
                }
            }
            if (!failureCase && cwdPoint.exists()) {
                System.out.println("There is an untracked file in the "
                        + "way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }
    /** Creates a new branch with the given name, and points it at the
     * current head node. A branch is nothing more than a name for a
     * reference (a SHA-1 identifier) to a commit node. This command does
     * NOT immediately switch to the newly created branch (just as in real
     * Git). Before you ever call branch, your code should be running
     * with a default branch called "master".
     * @param branchName name of the branch */
    public void branch(String branchName) throws IOException {
        for (File file: branchFile.listFiles()) {
            if (file.getName().equals(branchName)) {
                System.out.println("A branch with that name already exists.");
                System.exit(0);
            }
        }
        Branch branch = Utils.readObject(curBranch, Branch.class);
        Branch newBranch = new Branch(branchName);
        newBranch.setHead(branch.getHead());
        File newFile = Utils.join(branchFile, branchName);
        newFile.createNewFile();
        Utils.writeObject(newFile, newBranch);
    }

    /** Deletes the branch with the given name. This only means to delete the
     * pointer associated with the branch; it does not mean to delete all
     * commits that were created under the branch, or anything like that.
     * @param branchName name of the branch */
    public void rmBranch(String branchName) {
        File givenBranch = Utils.join(branchFile, branchName);
        if (!givenBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        Branch branchHold = Utils.readObject(curBranch, Branch.class);
        if (branchName.equals(branchHold.getBranchName())) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        givenBranch.delete();
    }

    /** Checks out all the files tracked by the given commit. Removes tracked
     * files that are not present in that commit. Also moves the current
     * branch's head to that commit node. See the intro for an example of
     * what happens to the head pointer after using reset. The [commit id]
     * may be abbreviated as for checkout. The staging area is cleared. The
     * command is essentially checkout of an arbitrary commit that also
     * changes the current branch head.
     * @param commitID ID of the commit */
    public void reset(String commitID) throws IOException {
        File commitPoint = Utils.join(commitsDir, commitID);
        if (!commitPoint.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Branch branchHolder = Utils.readObject(curBranch, Branch.class);
        head = branchHolder.getHead();
        Commit current = Utils.readObject(commitPoint, Commit.class);
        for (String file: current.getBlobs().keySet()) {
            File cwdPoint = Utils.join(cwd, file);
            boolean inside = false;
            for (String file2: head.getBlobs().keySet()) {
                if (file.equals(file2)) {
                    inside = true;
                    break;
                }
            }
            if (cwdPoint.exists() && !inside) {
                System.out.println("There is an untracked file in the "
                        + "way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        for (File file: cwd.listFiles()) {
            file.delete();
        }
        for (String obj: current.getBlobs().keySet()) {
            File temp = Utils.join(cwd, obj);
            temp.createNewFile();
            File blob = Utils.join(blobDir, current.getBlobs().get(obj));
            Blob blobHolder = Utils.readObject(blob, Blob.class);
            Utils.writeContents(temp, blobHolder.getBlobContents());
        }
        for (File file: stagingArea.listFiles()) {
            file.delete();
        }
        for (File file: removingArea.listFiles()) {
            file.delete();
        }

        branchHolder.setHead(current);
        Utils.writeObject(curBranch, branchHolder);

        for (File file: branchFile.listFiles()) {
            if (file.getName().equals(branchHolder.getBranchName())) {
                Utils.writeObject(file, branchHolder);
            }
        }
    }

    /** Handles the error case when there are uncommitted changes. */
    public void uncommitted() {
        if (stagingArea.listFiles().length != 0
                || removingArea.listFiles().length != 0) {
            System.out.println("You have uncommitted changes");
            System.exit(0);
        }
    }

    /** Handles the error case when a branch does not exist.
     * @param file a file */
    public void doesNotExist(File file) {
        if (!file.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }

    /** Merges files from the given branch into the current branch.
     * @param branchName name of the branch */
    public void merge(String branchName) throws IOException {
        uncommitted();
        File branch = Utils.join(branchFile, branchName);
        doesNotExist(branch);
        Branch currentBranch = Utils.readObject(curBranch, Branch.class);
        if (currentBranch.getBranchName().equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        head = currentBranch.getHead();
        Branch holding = Utils.readObject(branch, Branch.class);
        File pointer = Utils.join(commitsDir, holding.getHead().compID());
        Commit newC = Utils.readObject(pointer, Commit.class);
        untracked(newC, head);
        Commit splitCommit = getSplitPoint(head, holding.getHead());
        if (holding.getHead().compID().equals(splitCommit.compID())) {
            System.out.println("Given branch is an ancestor"
                    + "of the current branch.");
            System.exit(0);
        }
        if (head.compID().equals(splitCommit.compID())) {
            checkout3(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        for (Map.Entry<String, String> obj: splitCommit.getBlobs().entrySet()) {
            File split = Utils.join(blobDir, obj.getValue());
            Blob splitBlob = Utils.readObject(split, Blob.class);
            if (newC.getBlobs().containsKey(obj.getKey())
                    && head.getBlobs().containsKey(obj.getKey())) {
                File h = Utils.join(blobDir, head.getBlobs().get(obj.getKey()));
                Blob hBlob = Utils.readObject(h, Blob.class);
                File b = Utils.join(blobDir, newC.getBlobs().get(obj.getKey()));
                Blob bBlob = Utils.readObject(b, Blob.class);
                if (!bBlob.compare(splitBlob) && hBlob.compare(splitBlob)) {
                    File stage = Utils.join(stagingArea, obj.getKey());
                    stage.createNewFile();
                    checkout2(newC.compID(), obj.getKey());
                }
            } else if (!newC.getBlobs().containsKey(obj.getKey())
                    && head.getBlobs().containsKey(obj.getKey())) {
                File h = Utils.join(blobDir, head.getBlobs().get(obj.getKey()));
                Blob hBlob = Utils.readObject(h, Blob.class);
                if (hBlob.compare(splitBlob)) {
                    rm(obj.getKey());
                }
            }
        }
        for (Map.Entry<String, String> obj: newC.getBlobs().entrySet()) {
            if (!splitCommit.getBlobs().containsKey(obj.getKey())
                    && !head.getBlobs().containsKey(obj.getKey())) {
                File stage = Utils.join(stagingArea, obj.getKey());
                stage.createNewFile();
                checkout2(newC.compID(), obj.getKey());
            }
        }
        commit("Merged " + branchName + " into "
                + currentBranch.getBranchName() + ".");
    }

    /** Returns the split point commit object between two commits.
     * @param other a commit
     * @param current a commit */
    public Commit getSplitPoint(Commit other, Commit current) {
        Commit splitPoint;
        while (true) {
            if (other.compID().equals(current.compID())) {
                splitPoint = other;
                break;
            } else {
                File otherNext = Utils.join(commitsDir, other.getParent());
                other = Utils.readObject(otherNext, Commit.class);
                File currentNext = Utils.join(commitsDir, current.getParent());
                current = Utils.readObject(currentNext, Commit.class);
            }
        }
        return splitPoint;
    }
}
