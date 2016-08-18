package impulusecontrol.lend;

import java.util.List;

/**
 * Created by kerrk on 8/7/16.
 */
public class Category {

    private String id;

    private String name;

    private List<String> examples;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getExamples() {
        return examples;
    }

    public void setExamples(List<String> examples) {
        this.examples = examples;
    }
}
