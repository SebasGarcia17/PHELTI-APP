package com.phelti.phelti.model;

import java.math.BigDecimal;

public class Publication {

    private Integer publicationId;
    private Integer userId;
    private Integer categoryId;

    private String publicationType;
    private String title;
    private String description;
    private String interactionMode;

    private BigDecimal pheltAmount;

    private boolean negotiable;
    private String publicationStatus;

    public Integer getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Integer publicationId) {
        this.publicationId = publicationId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getPublicationType() {
        return publicationType;
    }

    public void setPublicationType(String publicationType) {
        this.publicationType = publicationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInteractionMode() {
        return interactionMode;
    }

    public void setInteractionMode(String interactionMode) {
        this.interactionMode = interactionMode;
    }

    public BigDecimal getPheltAmount() {
        return pheltAmount;
    }

    public void setPheltAmount(BigDecimal pheltAmount) {
        this.pheltAmount = pheltAmount;
    }

    public boolean isNegotiable() {
        return negotiable;
    }

    public void setNegotiable(boolean negotiable) {
        this.negotiable = negotiable;
    }

    public String getPublicationStatus() {
        return publicationStatus;
    }

    public void setPublicationStatus(String publicationStatus) {
        this.publicationStatus = publicationStatus;
    }
}