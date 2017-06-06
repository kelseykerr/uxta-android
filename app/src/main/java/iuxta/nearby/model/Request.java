package iuxta.nearby.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by kerrk on 8/7/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Request extends BaseEntity {

    private User user;

    private String itemName;

    private Date postDate;

    private Date expireDate;

    private Category category;

    private Boolean rental;

    private String description;

    private Type type;

    private Double latitude;

    private Double longitude;

    private String status;

    @JsonProperty("location")
    private Location location = new Location();

    private Boolean inappropriate;

    private Boolean duplicate;

    private List<String> photos;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Double getLongitude() {
        return location != null ? location.longitude : longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
        this.location.longitude = longitude;
    }

    public Double getLatitude() {
        return location != null ? location.latitude : latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
        this.location.latitude = latitude;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Boolean getRental() {
        return rental;
    }

    public void setRental(Boolean rental) {
        this.rental = rental;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonIgnore
    public boolean isClosed() {
        return this.getStatus() != null && this.getStatus().toLowerCase().equals("closed");
    }

    @JsonIgnore
    public boolean isOpen() {
        return this.getStatus() != null && this.getStatus().toLowerCase().equals("open");
    }

    @JsonIgnore
    public boolean isRental() {
        return this.getType().equals(Type.renting) || this.getType().equals(Type.loaning);
    }

    @JsonIgnore
    public boolean isMyRequest(User user) {
        return user.getId().equals(this.getUser().getId());
    }

    @JsonIgnore
    public String getRequesterName() {
        return this.getUser().getFirstName() != null ?
                this.getUser().getFirstName() : this.getUser().getFullName();
    }

    @JsonIgnore
    public boolean isInventoryListing() {
        return this.getType().equals(Type.loaning) || this.getType().equals(Type.selling);
    }

    public Boolean getInappropriate() {
        return inappropriate;
    }

    public void setInappropriate(Boolean inappropriate) {
        this.inappropriate = inappropriate;
    }

    public static class Location implements Serializable {

        public Location() {

        }

        private Double longitude;

        private Double latitude;

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }
    }

    public static enum Type {
        renting, buying, selling, loaning
    }

    public Boolean getDuplicate() {
        return duplicate;
    }

    public void setDuplicate(Boolean duplicate) {
        this.duplicate = duplicate;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }
}
