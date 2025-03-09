package gitlet;

import java.io.Serializable;

import static gitlet.Utils.join;
import static gitlet.Utils.readContents;

/**
 * blob 作为对存储的文件的抽象，由文件名和文件内容组成，以二进制形式存储。
 * 文件名为git目录下的完整相对路径
 */
public class Blob implements Serializable {


    private final String fileName;
    private final byte[] fileContent;

    Blob(String filePath) {
        fileName = filePath;
        fileContent = readContents(join(Repository.CWD, filePath));

    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileContent() {
        return fileContent;
    }
}
