package com.phelti.phelti.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer userId;

    @Column(name = "nombre", nullable = false, length = 100)
    private String name;

    @Column(name = "correo", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "descripcion")
    private String description;

    @Column(name = "promedio_calificacion", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @OneToOne(mappedBy = "user")
    private WalletEntity wallet;

    @OneToMany(mappedBy = "requester")
    private List<ActivityEntity> requestedActivities = new ArrayList<>();

    @OneToMany(mappedBy = "participant")
    private List<ActivityEntity> participatingActivities = new ArrayList<>();

    public UserEntity() {
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public WalletEntity getWallet() {
        return wallet;
    }

    public void setWallet(WalletEntity wallet) {
        this.wallet = wallet;
    }

    public List<ActivityEntity> getRequestedActivities() {
        return requestedActivities;
    }

    public void setRequestedActivities(List<ActivityEntity> requestedActivities) {
        this.requestedActivities = requestedActivities;
    }

    public List<ActivityEntity> getParticipatingActivities() {
        return participatingActivities;
    }

    public void setParticipatingActivities(List<ActivityEntity> participatingActivities) {
        this.participatingActivities = participatingActivities;
    }
}