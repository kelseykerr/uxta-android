package iuxta.nearby.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.List;

/**
 * Created by kerrk on 9/8/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response extends BaseEntity {
    private String requestId;

    private String responderId;

    private Date responseTime;

    /**
     * should be initially set by the responder
     */
    private Double offerPrice;

    private String description;

    private Boolean messagesEnabled;

    /**
     * should be initially set by the responder
     */
    private String priceType;

    /**
     * should be initially set by the buyer
     */
    private String exchangeLocation;

    /**
     * should be initially set by the buyer
     */
    private String returnLocation;

    /**
     * should be initially set by the buyer
     */
    private Date exchangeTime;

    /**
     * should be initially set by the buyer
     */
    private Date returnTime;

    private BuyerStatus buyerStatus;

    private SellerStatus sellerStatus;

    private Status responseStatus;

    private List<Message> messages;

    private User responder;

    private String canceledReason;

    private Boolean inappropriate;

    private Boolean isOfferToBuyOrRent;

    public static enum PriceType {
        FLAT, PER_HOUR, PER_DAY
    }

    /**
     * OPEN: the request is still open, the buyer has not accepted any offers
     * CLOSED: the request is closed either because the user accepted an offer from someone else, or withdrew the request
     * ACCEPTED: the user accepted the offer from this responder
     * DECLINED: the user declined the offer from this responder
     */
    public static enum BuyerStatus {
        OPEN, CLOSED, ACCEPTED, DECLINED
    }

    /**
     * OFFERED: responder extended the offer
     * ACCEPTED: the user accepted the offer and proposed a meeting & return time/location & the responder has accepted
     * WITHDRAWN: responder withdrew the offer (couldn't agree on price, item became unavailable...)
     */
    public static enum SellerStatus {
        OFFERED, ACCEPTED, WITHDRAWN
    }


    /**
     * PENDING: the responder status is 'offered' and the buyer status is either 'open' or 'accepted'
     * ACCEPTED: both the buy and responder status is accepted, a transaction should now exist for the request
     * CLOSED: either the buyer declined the request, the buyer closed the request, or the responder withdrew the request
     */
    public static enum Status {
        PENDING, ACCEPTED, CLOSED
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getResponderId() {
        return responderId;
    }

    public void setResponderId(String responderId) {
        this.responderId = responderId;
    }

    public Date getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }

    public Double getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(Double offerPrice) {
        this.offerPrice = offerPrice;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public String getExchangeLocation() {
        return exchangeLocation;
    }

    public void setExchangeLocation(String exchangeLocation) {
        this.exchangeLocation = exchangeLocation;
    }

    public String getReturnLocation() {
        return returnLocation;
    }

    public void setReturnLocation(String returnLocation) {
        this.returnLocation = returnLocation;
    }

    public Date getExchangeTime() {
        return exchangeTime;
    }

    public void setExchangeTime(Date exchangeTime) {
        this.exchangeTime = exchangeTime;
    }

    public Date getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(Date returnTime) {
        this.returnTime = returnTime;
    }

    public BuyerStatus getBuyerStatus() {
        return buyerStatus;
    }

    public void setBuyerStatus(BuyerStatus buyerStatus) {
        this.buyerStatus = buyerStatus;
    }

    public SellerStatus getSellerStatus() {
        return sellerStatus;
    }

    public void setSellerStatus(SellerStatus sellerStatus) {
        this.sellerStatus = sellerStatus;
    }

    public Status getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Status responseStatus) {
        this.responseStatus = responseStatus;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public User getResponder() {
        return responder;
    }

    public void setResponder(User responder) {
        this.responder = responder;
    }

    public String getCanceledReason() {
        return canceledReason;
    }

    public void setCanceledReason(String canceledReason) {
        this.canceledReason = canceledReason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getMessagesEnabled() {
        return messagesEnabled;
    }

    public void setMessagesEnabled(Boolean messagesEnabled) {
        this.messagesEnabled = messagesEnabled;
    }

    @JsonIgnore
    public boolean isClosed() {
        return this.getResponseStatus() != null && this.getResponseStatus().equals(Status.CLOSED);
    }

    @JsonIgnore
    public boolean isPending() {
        return this.getResponseStatus() != null && this.getResponseStatus().equals(Status.PENDING);
    }

    @JsonIgnore
    public String getResponderName() {
        if (this.getResponder() == null) {
            return null;
        }
        return this.getResponder().getFirstName() != null ?
                this.getResponder().getFirstName() : this.getResponder().getName();
    }

    public Boolean getInappropriate() {
        return inappropriate;
    }

    public void setInappropriate(Boolean inappropriate) {
        this.inappropriate = inappropriate;
    }

    public Boolean getIsOfferToBuyOrRent() {
        return isOfferToBuyOrRent;
    }

    public void setIsOfferToBuyOrRent(Boolean requestToBuyOrRent) {
        isOfferToBuyOrRent = requestToBuyOrRent;
    }
}
