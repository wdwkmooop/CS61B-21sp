package gitlet;

import java.io.IOException;
import java.util.Objects;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author WDW
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if(args.length == 0){
            System.out.println("Please enter a command.\n");
            return ;
        }
        String firstArg = args[0];
        if(!firstArg.equals("init") && !Repository.GITLET_DIR.exists()){
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        try {
            switch (firstArg) {
                case "init":
                    if(args.length != 1){
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.init();
                    break;
                case "add":
                    // 添加到暂存区，如已暂存则更新，如果和当前commit的版本一致，则从暂存区删除
                    if(args.length != 2){
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    String fileName = args[1];
                    Repository.add(fileName);
                    break;
                case "commit":
                    // 参数 messege
                    // 只管staged文件，若无文件staged，退出
                    if(args.length != 2){
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    String message = args[1];
                    if(message.isEmpty()){
                        System.out.println("Please enter a commit message.");
                        System.exit(0);
                    }
                    Repository.commit(message);
                    break;
                case "rm":
                    if(args.length != 2){
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    fileName = args[1];
                    Repository.rm(fileName);
                    break;
                case "log":
                    if(args.length != 1){
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.log();
                    break;
                case "global-log":
                    if(args.length != 1){
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.globalLog();
                    break;
                case "find":
                    if(args.length != 2){
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    message = args[1];
                    Repository.find(message);
                    break;
                case "status":
                    if(args.length != 1){
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.status();
                    break;
                case "checkout":
                    if(args.length == 2){
                        String branchName = args[1];
                        Repository.checkoutBranch(branchName);
                    }else if(args.length == 3){
                        if(!Objects.equals(args[1], "--")){
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        fileName = args[2];
                        Repository.checkout(fileName);
                    }else if(args.length == 4){
                        String comiitID = args[1];
                        if(!Objects.equals(args[2], "--")){
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        fileName = args[3];
                        Repository.checkout(comiitID, fileName);
                    }else{
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    break;
                case "branch":
                    if(args.length != 2){
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    String branchName = args[1];
                    Repository.branch(branchName);
                    break;
                case "rm-branch":
                    if(args.length != 2){
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    branchName = args[1];
                    Repository.rmBranch(branchName);
                    break;
                case "reset":
                    if(args.length != 2){
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    String commitID = args[1];
                    Repository.reset(commitID);
                    break;
                case "merge":
                    if(args.length != 2){
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    branchName = args[1];
                    Repository.merge(branchName);
                    break;
                default:
                    System.out.println("No command with that name exists.");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }


}
