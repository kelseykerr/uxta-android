package superstartupteam.nearby.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by kerrk on 7/16/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User extends BaseEntity {

    private String userId;

    @JsonIgnore
    private String facebookId;

    @JsonIgnore
    private String googleId;

    @JsonIgnore
    private String gender;

    private String email;

    @JsonIgnore
    private String name;

    @JsonIgnore
    private String accessToken;

    @JsonIgnore
    private String authMethod;

    @JsonIgnore
    private String googleAccessToken;

    private String firstName;

    private String lastName;

    private String phone;

    private String address;

    private String addressLine2;

    private String city;

    private String state;

    private String zip;

    private String fullName;

    private Double homeLongitude;

    private Double homeLatitude;

    private Boolean newRequestNotificationsEnabled;

    private Double notificationRadius;

    private List<String> notificationKeywords;

    private Boolean currentLocationNotifications;

    private Boolean homeLocationNotifications;

    private String paymentMethodNonce;

    private String merchantId;

    private String merchantStatus;

    private String customerId;

    private String dateOfBirth;

    private String bankAccountNumber;

    private String bankRoutingNumber;

    private String fundDestination;

    private Boolean tosAccepted;

    private String merchantStatusMessage;

    public Boolean isPaymentSetup;

    public String customerStatus;

    private Boolean removedMerchantDestination;

    @JsonIgnore
    private String creditCardNumber;

    @JsonIgnore
    private String ccExpirationDate;

    @JsonIgnore
    private String braintreeClientToken;

    private String pictureUrl;

    public User() {

    }

    public void updateUserFromServer(User fromServer) {
        this.firstName = fromServer.firstName;
        this.lastName = fromServer.lastName;
        this.userId = fromServer.userId;
        this.email = fromServer.email;
        this.name = fromServer.name;
        this.phone = fromServer.phone;
        this.address = fromServer.address;
        this.addressLine2 = fromServer.addressLine2;
        this.city = fromServer.city;
        this.state = fromServer.state;
        this.zip = fromServer.zip;
        this.homeLongitude = fromServer.homeLongitude;
        this.homeLatitude = fromServer.homeLatitude;
        this.newRequestNotificationsEnabled = fromServer.newRequestNotificationsEnabled;
        this.notificationRadius = fromServer.notificationRadius;
        this.notificationKeywords = fromServer.notificationKeywords;
        this.currentLocationNotifications = fromServer.currentLocationNotifications;
        this.homeLocationNotifications = fromServer.homeLocationNotifications;
        this.tosAccepted = fromServer.tosAccepted;
        this.isPaymentSetup = fromServer.isPaymentSetup;
        this.customerStatus = fromServer.customerStatus;
        this.removedMerchantDestination = fromServer.removedMerchantDestination;
        if (fromServer.authMethod != null) {
            this.authMethod = fromServer.authMethod;
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Double getHomeLongitude() {
        return homeLongitude;
    }

    public void setHomeLongitude(Double homeLongitude) {
        this.homeLongitude = homeLongitude;
    }

    public Double getHomeLatitude() {
        return homeLatitude;
    }

    public void setHomeLatitude(Double homeLatitude) {
        this.homeLatitude = homeLatitude;
    }

    public Boolean getNewRequestNotificationsEnabled() {
        return newRequestNotificationsEnabled;
    }

    public void setNewRequestNotificationsEnabled(Boolean newRequestNotificationsEnabled) {
        this.newRequestNotificationsEnabled = newRequestNotificationsEnabled;
    }

    public Double getNotificationRadius() {
        return notificationRadius;
    }

    public void setNotificationRadius(Double notificationRadius) {
        this.notificationRadius = notificationRadius;
    }

    public List<String> getNotificationKeywords() {
        return notificationKeywords;
    }

    public void setNotificationKeywords(List<String> notificationKeywords) {
        this.notificationKeywords = notificationKeywords;
    }

    public Boolean getCurrentLocationNotifications() {
        return currentLocationNotifications;
    }

    public void setCurrentLocationNotifications(Boolean currentLocationNotifications) {
        this.currentLocationNotifications = currentLocationNotifications;
    }

    public Boolean getHomeLocationNotifications() {
        return homeLocationNotifications;
    }

    public void setHomeLocationNotifications(Boolean homeLocationNotifications) {
        this.homeLocationNotifications = homeLocationNotifications;
    }

    public String getBraintreeClientToken() {
        return braintreeClientToken;
    }

    public void setBraintreeClientToken(String braintreeClientToken) {
        this.braintreeClientToken = braintreeClientToken;
    }

    public String getPaymentMethodNonce() {
        return paymentMethodNonce;
    }

    public void setPaymentMethodNonce(String paymentMethodNonce) {
        this.paymentMethodNonce = paymentMethodNonce;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantStatus() {
        return merchantStatus;
    }

    public void setMerchantStatus(String merchantStatus) {
        this.merchantStatus = merchantStatus;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankRoutingNumber() {
        return bankRoutingNumber;
    }

    public void setBankRoutingNumber(String bankRoutingNumber) {
        this.bankRoutingNumber = bankRoutingNumber;
    }

    public String getFundDestination() {
        return fundDestination;
    }

    public void setFundDestination(String fundDestination) {
        this.fundDestination = fundDestination;
    }

    public Boolean getTosAccepted() {
        return tosAccepted;
    }

    public void setTosAccepted(Boolean tosAccepted) {
        this.tosAccepted = tosAccepted;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getCcExpirationDate() {
        return ccExpirationDate;
    }

    public void setCcExpirationDate(String ccExpirationDate) {
        this.ccExpirationDate = ccExpirationDate;
    }

    public String getMerchantStatusMessage() {
        return merchantStatusMessage;
    }

    public void setMerchantStatusMessage(String merchantStatusMessage) {
        this.merchantStatusMessage = merchantStatusMessage;
    }

    public Boolean getIsPaymentSetup() {
        return isPaymentSetup;
    }

    public void setIsPaymentSetup(Boolean isPaymentSetup) {
        this.isPaymentSetup = isPaymentSetup;
    }

    public String getCustomerStatus() {
        return customerStatus;
    }

    public void setCustomerStatus(String customerStatus) {
        this.customerStatus = customerStatus;
    }

    public Boolean getRemovedMerchantDestination() {
        return removedMerchantDestination;
    }

    public void setRemovedMerchantDestination(Boolean removedMerchantDestination) {
        this.removedMerchantDestination = removedMerchantDestination;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getGoogleAccessToken() {
        return googleAccessToken;
    }

    public void setGoogleAccessToken(String googleAccessToken) {
        this.googleAccessToken = googleAccessToken;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getAuthMethod() {
        return this.authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

}

