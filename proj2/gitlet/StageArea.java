package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 代表暂存区的类
 */
public class StageArea implements Serializable {
    /**
     * 增加的更改， 文件名到bolb的映射
     */
    Map<String, String> addition;
    /**
     * 要在commit中删除的文件
     */
    Set<String> removel;

    StageArea() {
        addition = new HashMap<>();
        removel = new HashSet<>();
    }

    public boolean empty() {
        return addition.isEmpty() && removel.isEmpty();
    }
}
