package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                break;
            // TODO: FILL THE REST IN
        }
    }

    private static void init(){
        String cwd = System.getProperty("user.dir");
        File work_dir = Utils.join(cwd, ".gitlet");
        if(work_dir.exists()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return ;
        }
        work_dir.mkdir();
        File commits_dir = Utils.join(work_dir, "/commits");
        commits_dir.mkdir();

        Commit init_commit = new Commit("initial commit", null);
        String name = Utils.sha1(init_commit);
        File f = Utils.join(commits_dir, name);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(f, init_commit);

    }
}
