package gitlet;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Utils.*;

/**
 * Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author WDW
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private final String message;
    private final Date timeStamp;
    private final String parentID;
    /**
     * 文件名->blob名的映射
     */
    Map<String, String> tracking;
    private boolean fromMerge;
    private String mergedID;


    Commit(String message, String parentID) {
        this.message = message;
        this.parentID = parentID;
        this.tracking = new HashMap<>();
        fromMerge = false;
        mergedID = null;
        if (parentID == null) {
            timeStamp = new Date(0);
        } else {
            timeStamp = new Date();
            File parentPath = join(Repository.COMMIT_DIR, parentID);
            if (!parentPath.exists()) {
                throw error("Could not find parent commit.");
            }
            Commit parentCommit = readObject(parentPath, Commit.class);
            tracking = parentCommit.tracking;
        }
    }

    Commit(String message, String parentID, String mergedID) {
        this(message, parentID);
        this.fromMerge = true;
        this.mergedID = mergedID;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public String getParentID() {
        return parentID;
    }

    public boolean isFromMerge() {
        return fromMerge;
    }

    public String getMergedID() {
        return mergedID;
    }

    // 保存此commit
    public void saveCommit() throws IOException {
        StageArea stageArea = readObject(Repository.INDEX, StageArea.class);
        // 在commit 中tracking中新增暂存区的部分
        for (String filePath : stageArea.addition.keySet()) {
            String blobName = stageArea.addition.get(filePath);
            this.tracking.put(filePath, blobName);
            File blob = join(Repository.BLOB_DIR, blobName);
            if (!blob.exists()) {
                Blob cachedBlob = readObject(join(Repository.CACHE_DIR, blobName), Blob.class);
                blob.createNewFile();
                writeObject(blob, cachedBlob);
            }
            join(Repository.CACHE_DIR, blobName).delete();
        }
        for (String filePath : stageArea.removel) {
            this.tracking.remove(filePath);
        }
        // 保存commit
        String commitName = sha1(serialize(this));
        File savePath = Utils.join(Repository.COMMIT_DIR,
                commitName);
        savePath.createNewFile();
        writeObject(savePath, this);
        writeContents(Repository.headOfCurBranch(), commitName);
        // 清空缓存区
        writeObject(Repository.INDEX, new StageArea());
    }
}
