package iuxta.nearby.model;

import com.bignerdranch.expandablerecyclerview.Model.ParentObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kerrk on 9/10/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class History implements ParentObject {

    private Request request;

    private List<Response> responses;

    private Transaction transaction;

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

    @JsonIgnore
    public boolean hasOpenTransaction() {
        Request request = this.getRequest();
        return this.getTransaction() != null && !request.isClosed() &&
                (this.getTransaction().getCanceled() != null && !this.getTransaction().getCanceled());
    }

    @JsonIgnore
    public boolean isTransactionComplete() {
        if (this.getRequest() == null || this.getTransaction() == null) {
            return false;
        }
        return this.isRental() ? transaction.getExchanged() && transaction.getReturned() :
                transaction.getExchanged();
    }

    @JsonIgnore
    public boolean isRental() {
        return this.getRequest().getType().equals(Request.Type.renting) || this.getRequest().getType().equals(Request.Type.loaning);
    }

    @JsonIgnore
    public Response getAcceptedOffer() {
        if (this.transaction == null || this.getResponses() == null || this.getResponses().size() < 1) {
            return null;
        }
        Response resp = null;
        for (Response res : this.getResponses()) {
            if (res.getId().equals(transaction.getResponseId())) {
                resp = res;
                break;
            }
        }
        return resp;
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
