package impulusecontrol.lend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

import impulusecontrol.lend.BaseEntity;
import impulusecontrol.lend.Category;
import impulusecontrol.lend.model.User;

/**
 * Created by kerrk on 8/7/16.
 */
public class Request extends BaseEntity {

    private User user;

    private String itemName;

    private Date postDate;

    private Date expireDate;

    private Category category;

    private Boolean rental;

    private String description;

    @JsonProperty("location")
    private Location location = new Location();

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
        return location.longitude;
    }

    public void setLongitude(Double longitude) {
        this.location.longitude = longitude;
    }

    public Double getLatitude() {
        return location.latitude;
    }

    public void setLatitude(Double latitude) {
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

    public static class Location {

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
}
