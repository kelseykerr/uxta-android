package superstartupteam.nearby.model;

import com.bignerdranch.expandablerecyclerview.Model.ParentObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by kerrk on 9/10/16.
 */
public class History implements ParentObject {

    private Request request;

    private List<Response> responses;

    private Transaction transaction;

    private List<Object> children;

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

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public List<Object> getChildObjectList() {
        List<Object> objs = new ArrayList<>();
        for (Response r:responses) {
            objs.add((Object) r);
        }
        return objs;
    }

    @Override
    public void setChildObjectList(List<Object> list) {
        List<Response> responses = new ArrayList<>();
        for (Object o:list) {
            responses.add((Response) o);
        }
    }
}
