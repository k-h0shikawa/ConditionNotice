import java.util.HashMap;
import java.util.Map;

public class ConditionNotice {
    public static void main(String[] args){
        TimeTree timeTree = new TimeTree();
        // System.out.println(timeTree.readEvent());
        Map<String, String> event = new HashMap<>();
        timeTree.writeEvent();
    }
}


