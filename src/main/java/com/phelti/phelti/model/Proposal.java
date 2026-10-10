package com.phelti.phelti.model;

import java.time.Instant;

public class Proposal {

    private Integer proposalId;
    private Integer publicationId;
    private Integer proposingUserId;
    private String message;
    private String proposalStatus;
    private Instant createdAt;

    public Integer getProposalId() {
        return proposalId;
    }

    public void setProposalId(Integer proposalId) {
        this.proposalId = proposalId;
    }

    public Integer getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Integer publicationId) {
        this.publicationId = publicationId;
    }

    public Integer getProposingUserId() {
        return proposingUserId;
    }

    public void setProposingUserId(Integer proposingUserId) {
        this.proposingUserId = proposingUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProposalStatus() {
        return proposalStatus;
    }

    public void setProposalStatus(String proposalStatus) {
        this.proposalStatus = proposalStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
