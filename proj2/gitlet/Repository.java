package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;


/**
 * Represents a gitlet repository.
 * does at a high level.
 *
 * @author WDW
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    public static final File INDEX = join(GITLET_DIR, "staged_index");
    public static final File CACHE_DIR = join(GITLET_DIR, "cache");
    public static final File BRANCH_DIR = join(GITLET_DIR, "ref");


    static void init() throws IOException {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
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
        Commit initCommit = new Commit("initial commit", null);
        String name = sha1(serialize((initCommit)));
        File f = join(COMMIT_DIR, name);
        f.createNewFile();
        writeObject(f, initCommit);
        writeContents(master, name);
        writeObject(INDEX, new StageArea());
    }

    static void add(String fileName) throws IOException {
        File addFile = join(CWD, fileName);
        if (!addFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        StageArea stageArea = readObject(INDEX, StageArea.class);
        Blob blob = new Blob(fileName);
        String blobName = sha1(serialize(blob));

        // 若在暂存区标记为删除了，就恢复
        stageArea.removel.remove(fileName);

        Commit curCommit = readObject(join(COMMIT_DIR,
                readContentsAsString(headOfCurBranch())), Commit.class);
        // 在commit中保存了相同的版本
        if (blobName.equals(curCommit.tracking.getOrDefault(fileName, null))) {
            stageArea.addition.remove(fileName);
            join(CACHE_DIR, blobName).delete();
        } else { // 否则，在暂存区加上该文件
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
        if (stageArea.empty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        // 新commit
        String parentId = readContentsAsString(headOfCurBranch());
        Commit commit = new Commit(message, parentId);
        commit.saveCommit();
    }

    public static void rm(String fileName) {
        Commit curCommit = readObject(
                join(COMMIT_DIR, readContentsAsString(headOfCurBranch())), Commit.class);
        StageArea stageArea = readObject(INDEX, StageArea.class);
        boolean isStaged = stageArea.addition.containsKey(fileName);
        boolean isTracing = curCommit.tracking.containsKey(fileName);
        if (!isStaged && !isTracing) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        // 若在commit中，则暂存区记录为删除,且在工作目录删除，否则只在暂存区删除
        if (isStaged) {
            stageArea.addition.remove(fileName);
            Blob blob = new Blob(fileName);
            String blobName = sha1(serialize(blob));
            join(CACHE_DIR, blobName).delete();
        }
        if (isTracing) {
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
        if (commitID.length() < 40) {
            // to.do 更高效检索的方法可能是建立一个字典树，那在每一次增加commit时都要更新一下
            for (String fullCommitID : plainFilenamesIn(COMMIT_DIR)) {
                if (fullCommitID.startsWith(commitID)) {
                    commitID = fullCommitID;
                    break;
                }
            }
        }

        if (!join(COMMIT_DIR, commitID).exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = readObject(join(COMMIT_DIR, commitID), Commit.class);
        if (!commit.tracking.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String blobName = commit.tracking.get(fileName);
        Blob blob = readObject(join(BLOB_DIR, blobName), Blob.class);
        File f = join(CWD, fileName);
        if (!f.exists()) {
            f.createNewFile();
        }
        writeContents(f, blob.getFileContent());
    }

    public static void checkoutBranch(String branchName) throws IOException {
        File branch = join(BRANCH_DIR, branchName);
        if (!branch.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (branchName.equals(readContentsAsString(HEAD))) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        String commitID = readContentsAsString(headByBranchName(branchName));
        if (!checkSwitchCommit(commitID)) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            System.exit(0);
        }

        String otherCommitID = readContentsAsString(headByBranchName(branchName));
        switchCommit(otherCommitID);
        writeContents(HEAD, branchName);
    }

    public static void branch(String branchName) throws IOException {
        File newBranch = join(BRANCH_DIR, branchName);
        if (newBranch.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        newBranch.createNewFile();
        String curCommit = readContentsAsString(headOfCurBranch());
        writeContents(newBranch, curCommit);
    }

    public static void rmBranch(String branchName) {
        if (branchName.equals(readContentsAsString(HEAD))) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        File branch = join(BRANCH_DIR, branchName);
        if (!branch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        branch.delete();
    }
    
    /**
     * 简而言之就是，以祖先提交为基准，考虑两个分支中的变化
     * 一个变一个不变，以不变为准
     * 都变，若变化内容一致，当然一致保留。
     * 若变化内容不一致，无论是修改还是删除，内容合并为同一个文件。
     *
     *
     * 文件在祖先存在：修改和删除
     *      isChangedInCur
     *      isDeletedInCur
     *
     *
     * 文件在祖先不存在：新增
     *      判断是否都新增，内容是否一致
     * */
    public static void merge(String otherBranch) throws IOException {
        if (!join(BRANCH_DIR, otherBranch).exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (readContentsAsString(HEAD).equals(otherBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        String curBranch = readContentsAsString(HEAD);
        String sliptCommitID = leastCommonParent(curBranch, otherBranch);
        String curCommitID = readContentsAsString(headOfCurBranch());
        String otherCommitID = readContentsAsString(headByBranchName(otherBranch));

        if (sliptCommitID.equals(otherCommitID)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (sliptCommitID.equals(curCommitID)) {
            checkoutBranch(otherBranch);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        StageArea stageArea = readObject(INDEX, StageArea.class);
        if (!stageArea.empty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        Set<String> files = new HashSet<>();
        Commit sliptCommit = readObject(join(COMMIT_DIR, sliptCommitID), Commit.class);
        Commit curCommit = readObject(join(COMMIT_DIR, curCommitID), Commit.class);
        Commit otherCommit = readObject(join(COMMIT_DIR, otherCommitID), Commit.class);
        files.addAll(sliptCommit.tracking.keySet());
        files.addAll(curCommit.tracking.keySet());
        files.addAll(otherCommit.tracking.keySet());
        boolean conflict;
        conflict = mergeFiles(files, sliptCommit, curCommit, otherCommit, otherCommitID);

        String mergeMessage = "Merged " + otherBranch + " into " + curBranch + ".";
        Commit mergedCommit = new Commit(mergeMessage, curCommitID, otherCommitID);
        mergedCommit.saveCommit();

        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    public static void reset(String commitID) throws IOException {
        if (!join(COMMIT_DIR, commitID).exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        if (!checkSwitchCommit(commitID)) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            System.exit(0);
        }
        switchCommit(commitID);
        writeContents(headOfCurBranch(), commitID);
    }

    public static void log() {
        String curCommitID = readContentsAsString(headOfCurBranch());
        while (curCommitID != null) {
            Commit curCommit = readObject(join(COMMIT_DIR, curCommitID), Commit.class);
            printCommit(curCommitID, curCommit);
            curCommitID = curCommit.getParentID();
        }
    }

    public static void globalLog() {
        for (String file : plainFilenamesIn(COMMIT_DIR)) {
            String commitID = file;
            Commit commit = readObject(join(COMMIT_DIR, commitID), Commit.class);
            printCommit(commitID, commit);
        }
    }

    public static void find(String message) {
        boolean found = false;
        for (String commitID : plainFilenamesIn(COMMIT_DIR)) {
            Commit commit = readObject(join(COMMIT_DIR, commitID), Commit.class);
            if (commit.getMessage().equals(message)) {
                found = true;
                System.out.println(commitID);
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void status() {
        System.out.println("=== Branches ===");
        String curBranch = readContentsAsString(HEAD);
        System.out.println("*" + curBranch);
        for (String branch : plainFilenamesIn(BRANCH_DIR)) {
            if (!curBranch.equals(branch)) {
                System.out.println(branch);
            }
        }
        System.out.println();

        StageArea stageArea = readObject(INDEX, StageArea.class);
        System.out.println("=== Staged Files ===");
        for (String file : stageArea.addition.keySet()) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String file : stageArea.removel) {
            System.out.println(file);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }


    static File headOfCurBranch() {
        return headByBranchName(readContentsAsString(HEAD));
    }

    static File headByBranchName(String branchName) {
        return join(BRANCH_DIR, branchName);
    }

    private static boolean checkSwitchCommit(String commitID) {
        // 如果有一文件未被tracking, 而又要被checkout覆写，则返回false
        Commit other = readObject(join(COMMIT_DIR, commitID), Commit.class);
        File curFile = join(COMMIT_DIR, readContentsAsString(headOfCurBranch()));
        Commit cur = readObject(curFile, Commit.class);
        for (String file : plainFilenamesIn(CWD)) { // to.do 这里对子文件夹内文件的处理。
            if (!cur.tracking.containsKey(file) && other.tracking.containsKey(file)) {
                return false;
            }
        }
        return true;
    }

    private static void switchCommit(String commitID) throws IOException {
        // 清空暂存区
        for (String fileName : CACHE_DIR.list()) {
            join(CACHE_DIR, fileName).delete();
        }
        writeObject(INDEX, new StageArea());
        // 清空当前tracking的文件
        String curCommitName = readContentsAsString(headOfCurBranch());
        Commit curCommit = readObject(join(COMMIT_DIR, curCommitName), Commit.class);
        for (String path : curCommit.tracking.keySet()) {
            restrictedDelete(join(CWD, path));
        }
        // 切换出另一个分支的文件
        Commit otherCommit = readObject(join(COMMIT_DIR, commitID), Commit.class);
        for (String path : otherCommit.tracking.keySet()) {
            String blobName = otherCommit.tracking.get(path);
            Blob blob = readObject(join(BLOB_DIR, blobName), Blob.class);
            join(CWD, path).createNewFile();
            writeContents(join(CWD, path), blob.getFileContent());
        }
    }

    private static void printCommit(String commitID, Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commitID);
        if (commit.isFromMerge()) {
            String mergeID = commit.getMergedID();
            String parentID = commit.getParentID();
            System.out.println("Merge: " + parentID.substring(0, 7)
                    + " " + mergeID.substring(0, 7));
        }

        Formatter formatter = new Formatter(Locale.ENGLISH);
        formatter.format("Date: %1$ta %1$tb %1$te %1$tT %1$tY %1$tz", commit.getTimeStamp());
        System.out.println(formatter);
        System.out.println(commit.getMessage());
        System.out.println();
    }

    // 求最近的祖先提交实际上是求DAG中最近公共祖先。
    // 返回此提交的commitID
    private static String leastCommonParent(String curBranch, String otherBranch) {
        String curCommitID = readContentsAsString(headOfCurBranch());
        String otherCommitID = readContentsAsString(headByBranchName(otherBranch));

        // 此算法应该是，求出离当前提交最近的那个最近公共祖先.若有多个就随便一个
        String resultID = null;
        int minDis = Integer.MAX_VALUE;
        HashMap<String, Integer> distance = new HashMap<>();
        Queue<String> queue = new ArrayDeque<>();
        distance.put(curCommitID, 0);
        queue.add(curCommitID);

        while (!queue.isEmpty()) {
            String id = queue.poll();
            Commit commit = readObject(join(COMMIT_DIR, id), Commit.class);
            if (commit.getParentID() != null && !distance.containsKey(commit.getParentID())) {
                queue.add(commit.getParentID());
                distance.put(commit.getParentID(), distance.get(id) + 1);
            }
            if (commit.getMergedID() != null && !distance.containsKey(commit.getMergedID())) {
                queue.add(commit.getMergedID());
                distance.put(commit.getMergedID(), distance.get(id) + 1);
            }
        }

        Queue<String> queue2 = new ArrayDeque<>();
        queue2.add(otherCommitID);

        while (!queue2.isEmpty()) {
            String id = queue2.poll();
            if (distance.getOrDefault(id, Integer.MAX_VALUE) < minDis) {
                minDis = distance.get(id);
                resultID = id;
            }
            Commit commit = readObject(join(COMMIT_DIR, id), Commit.class);
            if (commit.getParentID() != null) {
                queue2.add(commit.getParentID());
            }
            if (commit.getMergedID() != null) {
                queue2.add(commit.getMergedID());
            }
        }

        return resultID;
    }

    private static boolean mergeFiles(Set<String> files, Commit sliptCommit, Commit curCommit,
                                      Commit otherCommit, String otherCommitID) throws IOException {
        boolean conflict = false;
        for (String file : files) {
            // 先把条件取出来
            boolean inSliptCommit = false;    // 是否在祖先提交
            boolean inCurCommit = false;      // 是否在当前分支
            boolean isChangedInCur = false;   // 当前分支中是否有改变
            boolean inOtherCommit = false;    // 是否在待合并分支
            boolean isChangedInOther = false; // 待合并分支是否有改变
            boolean isIdentical = false;      // 两个分支内此文件（若存在）是否相同

            inSliptCommit = sliptCommit.tracking.containsKey(file);
            inCurCommit = curCommit.tracking.containsKey(file);
            inOtherCommit = otherCommit.tracking.containsKey(file);

            if (inSliptCommit && inCurCommit) {
                if (!curCommit.tracking.get(file).equals(sliptCommit.tracking.get(file))) {
                    isChangedInCur = true;
                }
            }
            if (inSliptCommit && inOtherCommit) {
                if (!otherCommit.tracking.get(file).equals(sliptCommit.tracking.get(file))) {
                    isChangedInOther = true;
                }
            }
            if (inCurCommit && inOtherCommit) {
                if (curCommit.tracking.get(file).equals(otherCommit.tracking.get(file))) {
                    isIdentical = true;
                }
            }

            if (isIdentical || (!inCurCommit && !inOtherCommit)) {
                continue;
            }

            if (inSliptCommit) {
                if ((isChangedInCur && isChangedInOther)
                        || (!inCurCommit && isChangedInOther)
                        || (isChangedInCur && !inOtherCommit)
                ) { // 冲突
                    conflict = true;
                    mergeFile(curCommit.tracking.get(file), otherCommit.tracking.get(file));
                }

                if (inCurCommit && !isChangedInCur && isChangedInOther) {  // 只在待合并分支修改
                    checkout(otherCommitID, file);
                    add(file);
                }

                if (inCurCommit && !isChangedInCur && !inOtherCommit) {  // 删除
                    rm(file);
                }
            } else {
                if (!inCurCommit && inOtherCommit) {  // 只在待合并分支存在
                    // 如果这个文件恰好在工作目录中，但是没有被track
                    if (plainFilenamesIn(CWD).contains(file)) {
                        System.out.println("There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
                        System.exit(0);
                    }
                    checkout(otherCommitID, file);
                    add(file);
                }
                if (inCurCommit && inOtherCommit && !isIdentical) { // 新增，两分支不一致
                    conflict = true;
                    mergeFile(curCommit.tracking.get(file), otherCommit.tracking.get(file));
                }
            }
        }
        return conflict;
    }


    // 处理conflict的文件
    private static void mergeFile(String blobNameInCur, String blobNameInOther) {
        Blob cur = null;
        Blob other = null;
        if (blobNameInCur != null) {
            cur = readObject(join(BLOB_DIR, blobNameInCur), Blob.class);
        }
        if (blobNameInOther != null) {
            other = readObject(join(BLOB_DIR, blobNameInOther), Blob.class);
        }
        File f = join(CWD, cur.getFileName());
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeContents(f, "<<<<<<< HEAD\n",
                cur != null ? cur.getFileContent() : "",
                "=======\n",
                other != null ? other.getFileContent() : "",
                ">>>>>>>\n");
        try {
            add(cur.getFileName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
