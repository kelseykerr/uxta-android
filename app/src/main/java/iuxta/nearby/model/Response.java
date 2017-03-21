package iuxta.nearby.model;

import java.util.Date;
import java.util.List;

/**
 * Created by kerrk on 9/8/16.
 */
public class Response {

    private String id;

    private String requestId;

    private String sellerId;

    private Date responseTime;

    /**
     * should be initially set by the seller
     */
    private Double offerPrice;

    /**
     * should be initially set by the seller
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

    private User seller;

    private String canceledReason;

    public static enum PriceType {
        FLAT, PER_HOUR, PER_DAY
    }

    /**
     * OPEN: the request is still open, the buyer has not accepted any offers
     * CLOSED: the request is closed either because the user accepted an offer from someone else, or withdrew the request
     * ACCEPTED: the user accepted the offer from this seller
     * DECLINED: the user declined the offer from this seller
     */
    public static enum BuyerStatus {
        OPEN, CLOSED, ACCEPTED, DECLINED
    }

    /**
     * OFFERED: seller extended the offer
     * ACCEPTED: the user accepted the offer and proposed a meeting & return time/location & the seller has accepted
     * WITHDRAWN: seller withdrew the offer (couldn't agree on price, item became unavailable...)
     */
    public static enum SellerStatus {
        OFFERED, ACCEPTED, WITHDRAWN
    }


    /**
     * PENDING: the seller status is 'offered' and the buyer status is either 'open' or 'accepted'
     * ACCEPTED: both the buy and seller status is accepted, a transaction should now exist for the request
     * CLOSED: either the buyer declined the request, the buyer closed the request, or the seller withdrew the request
     */
    public static enum Status {
        PENDING, ACCEPTED, CLOSED
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
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

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public String getCanceledReason() {
        return canceledReason;
    }

    public void setCanceledReason(String canceledReason) {
        this.canceledReason = canceledReason;
    }
}
