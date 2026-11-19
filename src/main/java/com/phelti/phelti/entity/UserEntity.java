package com.phelti.phelti.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Column(name = "ubicacion", length = 150)
    private String location;

    @Column(name = "modalidad_preferida", length = 20)
    private String preferredInteractionMode;

    @OneToOne(mappedBy = "user")
    private WalletEntity wallet;

    @ManyToMany
    @JoinTable(
            name = "usuario_habilidad",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_habilidad")
    )
    private Set<SkillEntity> skills = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "usuario_interes",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_interes")
    )
    private Set<InterestEntity> interests = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AvailabilityEntity> availabilities = new ArrayList<>();

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPreferredInteractionMode() {
        return preferredInteractionMode;
    }

    public void setPreferredInteractionMode(String preferredInteractionMode) {
        this.preferredInteractionMode = preferredInteractionMode;
    }

    public Set<SkillEntity> getSkills() {
        return skills;
    }

    public void setSkills(Set<SkillEntity> skills) {
        this.skills = skills;
    }

    public Set<InterestEntity> getInterests() {
        return interests;
    }

    public void setInterests(Set<InterestEntity> interests) {
        this.interests = interests;
    }

    public List<AvailabilityEntity> getAvailabilities() {
        return availabilities;
    }

    public void setAvailabilities(List<AvailabilityEntity> availabilities) {
        this.availabilities = availabilities;
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
