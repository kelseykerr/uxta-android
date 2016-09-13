package superstartupteam.nearby.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by kerrk on 9/10/16.
 */
public class History {

    private Request request;

    private List<Response> responses;

    public History() {

    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public List<Response> getResponses() {
        return responses;
    }

    public void setResponses(List<Response> responses) {
        this.responses = responses;
    }
}
