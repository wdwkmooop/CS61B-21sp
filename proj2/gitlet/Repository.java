package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author WDW
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    public static final File INDEX = join(GITLET_DIR, "staged_index");
    public static final File CACHE_DIR = join(GITLET_DIR, "cache");
    public static final File BRANCH_DIR = join(GITLET_DIR, "ref");


    /* TODO: fill in the rest of this class. */

    static void init() throws IOException {
        if(GITLET_DIR.exists()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
        INDEX.createNewFile();
        HEAD.createNewFile();
        CACHE_DIR.mkdir();
        BRANCH_DIR.mkdir();

        File master = join(BRANCH_DIR, "master");
        master.createNewFile();
        writeContents(HEAD, "master");
        Commit init_commit = new Commit("initial commit", null);
        String name = sha1(serialize((init_commit)));
        File f = join(COMMIT_DIR, name);
        f.createNewFile();
        writeObject(f, init_commit);
        writeContents(master, name);
        writeObject(INDEX, new StageArea());
    }

    static void add(String fileName) throws IOException{
        File addFile = join(CWD, fileName);
        if(!addFile.exists()){
            System.out.println("File does not exist.");
            System.exit(0);
        }
        StageArea stageArea = readObject(INDEX, StageArea.class);
        Blob blob = new Blob(fileName);
        String blobName = sha1(serialize(blob));

        // 若在暂存区标记为删除了，就恢复
        if(stageArea.removel.contains(fileName)) {
            stageArea.removel.remove(fileName);
        }

        Commit curCommit = readObject(join(COMMIT_DIR, readContentsAsString(headOfCurBranch())), Commit.class);
        // 在commit中保存了相同的版本
        if(blobName.equals(curCommit.tracking.getOrDefault(fileName, null))){
            if(stageArea.addition.containsKey(fileName)){
                stageArea.addition.remove(fileName);
            }
            join(CACHE_DIR, blobName).delete();
        }else{ // 否则，在暂存区加上该文件
            File savePath = join(CACHE_DIR, blobName);
            savePath.createNewFile();
            writeObject(savePath, blob);
            stageArea.addition.put(fileName, blobName);
        }

        writeObject(INDEX, stageArea);
    }

    static void commit(String message) throws IOException {
        // 若stagedindex 为空
        StageArea stageArea = readObject(INDEX, StageArea.class);
        if(stageArea.empty()){
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        // 新commit
        String parentId = readContentsAsString(headOfCurBranch());
        Commit commit = new Commit(message, parentId);
        // 在commit 中tracking中新增暂存区的部分
        for(String filePath : stageArea.addition.keySet()) {
            String blobName = stageArea.addition.get(filePath);
            commit.tracking.put(filePath, blobName);
            File blob = join(BLOB_DIR, blobName);
            if(!blob.exists()){
                Blob cachedBlob = readObject(join(CACHE_DIR, blobName), Blob.class);
                blob.createNewFile();
                writeObject(blob, cachedBlob);
            }
            join(CACHE_DIR, blobName).delete();
        }
        for(String filePath : stageArea.removel){
            commit.tracking.remove(filePath);
        }
        // 保存commit
        String commitName = sha1(serialize(commit));
        File savePath = Utils.join(COMMIT_DIR,
                commitName);
        savePath.createNewFile();
        writeObject(savePath, commit);
        writeContents(headOfCurBranch(), commitName);
        // 清空缓存区
        writeObject(INDEX, new StageArea());
    }

    public static void rm(String fileName) {
        Commit curCommit = readObject(join(COMMIT_DIR, readContentsAsString(headOfCurBranch())), Commit.class);
        StageArea stageArea = readObject(INDEX, StageArea.class);
        boolean isStaged = stageArea.addition.containsKey(fileName);
        boolean isTracing = curCommit.tracking.containsKey(fileName);
        if(!isStaged && !isTracing){
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        // 若在commit中，则暂存区记录为删除,且在工作目录删除，否则只在暂存区删除
        if(isStaged){
            stageArea.addition.remove(fileName);
            Blob blob = new Blob(fileName);
            String blobName = sha1(serialize(blob));
            join(CACHE_DIR, blobName).delete();
        }
        if(isTracing){
            stageArea.removel.add(fileName);
            File workFile = join(CWD, fileName);
            restrictedDelete(workFile);
        }
        writeObject(INDEX, stageArea);
    }

    public static void checkout(String fileName) throws IOException {
        String commitID = readContentsAsString(headOfCurBranch());
        checkout(commitID, fileName);
    }
    public static void checkout(String commitID, String fileName) throws IOException {
        if(!join(COMMIT_DIR, commitID).exists()){
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = readObject(join(COMMIT_DIR, commitID), Commit.class);
        if(!commit.tracking.containsKey(fileName)){
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String blobName = commit.tracking.get(fileName);
        Blob blob = readObject(join(BLOB_DIR, blobName), Blob.class);
        File f = join(CWD, fileName);
        if(!f.exists()){
            f.createNewFile();
        }
        writeContents(f, blob.getFileContent());
    }

    public static void checkoutBranch(String branchName) throws IOException {
        File branch = join(BRANCH_DIR, branchName);
        if(!branch.exists()){
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if(branchName.equals(readContentsAsString(HEAD))){
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        String commitID = readContentsAsString(headByBranchName(branchName));
        if(!checkSwitchCommit(commitID)){
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        String otherCommitID = readContentsAsString(headByBranchName(branchName));
        switchCommit(otherCommitID);
        writeContents(HEAD, branchName);
    }

    public static void branch(String branchName) throws IOException {
        File newBranch = join(BRANCH_DIR, branchName);
        if(newBranch.exists()){
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        newBranch.createNewFile();
        String curCommit = readContentsAsString(headOfCurBranch());
        writeContents(newBranch, curCommit);
    }

    public static void rmBranch(String branchName) {
        if(branchName.equals(readContentsAsString(HEAD))){
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        File branch = join(BRANCH_DIR, branchName);
        if(!branch.exists()){
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        branch.delete();
    }

    public static void merge(String otherBranch) throws IOException {
        // 出现冲突，当且仅当其他分支和当前分支都修改/新增了同一文件，且修改方式不同。
        // 其他情况：
        //
        // 当前文件在其他分支修改了，当前分支未修改，则以其他分支为准。
        // 反之，以当前分支为准
        // 都修改了，则
        // 相同修改，保持不变
        //
        // 总结起来，变化有三种：新增，删除，修改
        // 都有变化，则变化相同的无冲突，不同的有冲突
        // 一个变一个不变，则以变化为准。
        // 变化应当暂存
        if(!join(BRANCH_DIR, otherBranch).exists()){
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if(readContentsAsString(HEAD).equals(otherBranch)){
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        // 如果当前提交中未跟踪的文件将被合并覆盖或删除，
        // 则打印
        // There is an untracked file in the way; delete it, or add and commit it first.

        String curBranch = readContentsAsString(HEAD);
        ArrayList<String> l1 = allCommitInBranch(curBranch), l2 = allCommitInBranch(otherBranch);
        String splitCommitID = leastCommonParent(l1, l2);
        if(splitCommitID.equals(readContentsAsString(headByBranchName(otherBranch)))){
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if(splitCommitID.equals(readContentsAsString(headOfCurBranch()))){
            checkout(otherBranch);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        StageArea stageArea = readObject(INDEX, StageArea.class);
        if(stageArea.empty()){
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        Map<String, String> changedInOther = new HashMap<>();
        Map<String, String> changedInCur = new HashMap<>();
        Map<String, String> deletedInOther = new HashMap<>();
        Set<String> deletedInOtherAndCur = new HashSet<>();
        Commit sliptCommit = readObject(join(COMMIT_DIR, splitCommitID), Commit.class);
        Commit curCommit = readObject(headOfCurBranch(), Commit.class);
        Commit otherCommit = readObject(join(COMMIT_DIR, splitCommitID), Commit.class);
    }

    public static void reset(String commitID) throws IOException {
        if(!join(COMMIT_DIR, commitID).exists()){
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        if(!checkSwitchCommit(commitID)){
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
        switchCommit(commitID);
        writeContents(headOfCurBranch(), commitID);
    }

    public static void log() {
        String curCommitID = readContentsAsString(headOfCurBranch());
        while(curCommitID != null){
            Commit curCommit = readObject(join(COMMIT_DIR, curCommitID), Commit.class);
            printCommit(curCommitID, curCommit);
            curCommitID = curCommit.getParentID();
        }
    }

    public static void globalLog(){
        for(String file : plainFilenamesIn(COMMIT_DIR)){
            String commitID = file;
            Commit commit = readObject(join(COMMIT_DIR, commitID), Commit.class);
            printCommit(commitID, commit);
        }
    }

    public static void find(String message) {
        boolean found = false;
        for(String commitID : plainFilenamesIn(COMMIT_DIR)){
            Commit commit = readObject(join(COMMIT_DIR, commitID), Commit.class);
            if(commit.getMessage().equals(message)){
                found = true;
                System.out.println(commitID);
            }
        }
        if(!found){
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void status() {
        System.out.println("=== Branches ===");
        String curBranch = readContentsAsString(HEAD);
        System.out.println("*" + curBranch);
        for(String branch : plainFilenamesIn(BRANCH_DIR)){
            if(!curBranch.equals(branch)){
                System.out.println(branch);
            }
        }
        System.out.println();

        StageArea stageArea = readObject(INDEX, StageArea.class);
        System.out.println("=== Staged Files ===");
        for(String file : stageArea.addition.keySet()){
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for(String file : stageArea.removel){
            System.out.println(file);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }


    private static File headOfCurBranch(){
        return headByBranchName(readContentsAsString(HEAD));
    }

    private static File headByBranchName(String branchName) {
        return join(BRANCH_DIR, branchName);
    }

    private static boolean checkSwitchBranch(String branchName) {
        // 如果有一文件未被tracking, 而又要被checkout覆写，则返回false
        File otherFile = join(COMMIT_DIR, readContentsAsString(headByBranchName(branchName)));
        Commit other = readObject(otherFile, Commit.class);
        File curFile = join(COMMIT_DIR, readContentsAsString(headOfCurBranch()));
        Commit cur = readObject(curFile, Commit.class);
        for(String file:plainFilenamesIn(CWD)){ // todo 这里对子文件夹内文件的处理。
            if(!cur.tracking.containsKey(file) && other.tracking.containsKey(file)){
                return false;
            }
        }
        return true;
    }

    private static boolean checkSwitchCommit(String commitID) {
        // 如果有一文件未被tracking, 而又要被checkout覆写，则返回false
        Commit other = readObject(join(COMMIT_DIR, commitID), Commit.class);
        File curFile = join(COMMIT_DIR, readContentsAsString(headOfCurBranch()));
        Commit cur = readObject(curFile, Commit.class);
        for(String file:plainFilenamesIn(CWD)){ // todo 这里对子文件夹内文件的处理。
            if(!cur.tracking.containsKey(file) && other.tracking.containsKey(file)){
                return false;
            }
        }
        return true;
    }

    private static void switchCommit(String commitID) throws IOException {
        // 清空暂存区
        for(String fileName:CACHE_DIR.list()) {
            join(CACHE_DIR, fileName).delete();
        }
        writeObject(INDEX, new StageArea());
        // 清空当前tracking的文件
        String curCommitName = readContentsAsString(headOfCurBranch());
        Commit curCommit = readObject(join(COMMIT_DIR, curCommitName), Commit.class);
        for(String path : curCommit.tracking.keySet()){
            restrictedDelete(join(CWD, path));
        }
        // 切换出另一个分支的文件
        Commit otherCommit = readObject(join(COMMIT_DIR, commitID), Commit.class);
        for(String path : otherCommit.tracking.keySet()){
            String blobName = otherCommit.tracking.get(path);
            Blob blob = readObject(join(BLOB_DIR, blobName), Blob.class);
            join(CWD, path).createNewFile();
            writeContents(join(CWD, path), blob.getFileContent());
        }
    }

    private static void printCommit(String commitID, Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commitID);
        if(commit.isFromMerge()){
            String mergeID = commit.getMergedID();
            String parentID = commit.getParentID();
            System.out.println("Merge: " + parentID.substring(0, 7) + mergeID.substring(0, 7));
        }

        Formatter formatter = new Formatter(Locale.ENGLISH);
        formatter.format("Date: %1$ta %1$tb %1$te %1$tT %1$tY %1$tz", commit.getTimeStamp());
        System.out.println(formatter);
        System.out.println(commit.getMessage());
        System.out.println();
    }

    private static ArrayList<String> allCommitInBranch (String branch){
        String curCommitID = readContentsAsString(join(BRANCH_DIR, branch));
        ArrayList<String> ret = new ArrayList<>();
        while(curCommitID != null){
            ret.add(curCommitID);
            Commit curCommit = readObject(join(COMMIT_DIR, curCommitID), Commit.class);
            curCommitID = curCommit.getParentID();
        }
        ret = (ArrayList<String>) ret.reversed();
        return ret;
    }

    private static String leastCommonParent(ArrayList<String> l1, ArrayList<String> l2){
        for(int i=0;i<Math.min(l1.size(), l2.size());i++){
            if(!l1.get(i).equals(l2.get(i))){
                return l1.get(i-1);
            }
        }
        return null;
    }

}
