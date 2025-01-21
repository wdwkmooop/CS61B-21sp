package gitlet;


import java.io.Serializable;
import java.util.Date;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date timeStamp;
    private String parentID;


    /* TODO: fill in the rest of this class. */

    Commit(String message, String parentID){
        this.message = message;
        this.parentID = parentID;
        if(parentID == null){
            timeStamp = new Date(0);
        }else {
            timeStamp = new Date();
        }
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
}
