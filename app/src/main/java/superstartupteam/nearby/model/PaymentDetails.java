package superstartupteam.nearby.model;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDetails {

    private String ccMaskedNumber;

    private String ccExpDate;

    private String destination;

    private String bankAccountLast4;

    private String routingNumber;

    private String email;

    private String phone;

    private String ccType;

    public PaymentDetails() {

    }

    public String getCcMaskedNumber() {
        return ccMaskedNumber;
    }

    public void setCcMaskedNumber(String ccMaskedNumber) {
        this.ccMaskedNumber = ccMaskedNumber;
    }

    public String getCcExpDate() {
        return ccExpDate;
    }

    public void setCcExpDate(String ccExpDate) {
        this.ccExpDate = ccExpDate;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getBankAccountLast4() {
        return bankAccountLast4;
    }

    public void setBankAccountLast4(String bankAccountLast4) {
        this.bankAccountLast4 = bankAccountLast4;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCcType() {
        return ccType;
    }

    public void setCcType(String ccType) {
        this.ccType = ccType;
    }
}
