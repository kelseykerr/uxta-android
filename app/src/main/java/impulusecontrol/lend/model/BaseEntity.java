package impulusecontrol.lend.model;

import java.io.Serializable;

/**
 * Created by kerrk on 8/21/16.
 */
public class BaseEntity implements Serializable {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
