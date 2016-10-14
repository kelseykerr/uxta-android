package superstartupteam.nearby.model;

import java.util.List;

/**
 * Created by kerrk on 7/16/16.
 */
public class User extends BaseEntity {

    private String userId;

    private String facebookId;

    private String gender;

    private String email;

    private String name;

    private String accessToken;

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

    public User() {

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
}
