package iuxta.uxta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by kerrk on 7/16/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends BaseEntity {

    private String userId;

    @JsonIgnore
    private String facebookId;

    @JsonIgnore
    private String googleId;

    private String email;

    @JsonIgnore
    private String name;

    @JsonIgnore
    private String accessToken;

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

    private Boolean newRequestNotificationsEnabled;

    private Double notificationRadius;

    private List<String> notificationKeywords;

    private Boolean tosAccepted;

    private String pictureUrl;

    private String tosAcceptIp;

    private String communityId;

    private String requestedCommunityId;

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
        this.newRequestNotificationsEnabled = fromServer.newRequestNotificationsEnabled;
        this.notificationRadius = fromServer.notificationRadius;
        this.notificationKeywords = fromServer.notificationKeywords;
        this.tosAccepted = fromServer.tosAccepted;
        this.communityId = fromServer.communityId;
        this.requestedCommunityId = fromServer.requestedCommunityId;
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

    public Boolean getTosAccepted() {
        return tosAccepted;
    }

    public void setTosAccepted(Boolean tosAccepted) {
        this.tosAccepted = tosAccepted;
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

    public String getTosAcceptIp() {
        return tosAcceptIp;
    }

    public void setTosAcceptIp(String tosAcceptIp) {
        this.tosAcceptIp = tosAcceptIp;
    }

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }

    public String getRequestedCommunityId() {
        return requestedCommunityId;
    }

    public void setRequestedCommunityId(String requestedCommunityId) {
        this.requestedCommunityId = requestedCommunityId;
    }
}

