package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Ryan Gomes
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        Gitlet newGitletObj = new Gitlet();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        if (args[0].equals("init")) {
            newGitletObj.init();
            System.exit(0);
        }
        if (args[0].equals("add")) {
            newGitletObj.add(args[1]);
            System.exit(0);
        }
        if (args[0].equals("commit")) {
            newGitletObj.commit(args[1]);
            System.exit(0);
        }
        if (args[0].equals("rm")) {
            newGitletObj.rm(args[1]);
            System.exit(0);
        }
        if (args[0].equals("log")) {
            newGitletObj.log();
            System.exit(0);
        }
        if (args[0].equals("global-log")) {
            newGitletObj.globalLog();
            System.exit(0);
        }
        if (args[0].equals("find")) {
            newGitletObj.find(args[1]);
            System.exit(0);
        }
        if (args[0].equals("status")) {
            newGitletObj.status();
            System.exit(0);
        }
        checkout(newGitletObj, args);
        if (args[0].equals("branch")) {
            newGitletObj.branch(args[1]);
            System.exit(0);
        }
        if (args[0].equals("rm-branch")) {
            newGitletObj.rmBranch(args[1]);
            System.exit(0);
        }
        if (args[0].equals("reset")) {
            newGitletObj.reset(args[1]);
            System.exit(0);
        }
        if (args[0].equals("merge")) {
            newGitletObj.merge(args[1]);
            System.exit(0);
        }
        System.out.println("No command with that name exists.");
    }

    /** Helper method for checkout.
     * @param t gitlet object
     * @param args arguments */
    private static void checkout(Gitlet t, String... args) throws IOException {
        if (args[0].equals("checkout")) {
            if (args.length == 2) {
                t.checkout3(args[1]);
            } else if (args[1].equals("--")) {
                t.checkout1(args[2]);
            } else if (args[2].equals("--")) {
                t.checkout2(args[1], args[3]);
            } else {
                System.out.println("Incorrect operands.");
            }
            System.exit(0);
        }
    }
}
