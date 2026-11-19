package com.phelti.phelti.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "confirmacion")
public class ConfirmationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_confirmacion")
    private Integer confirmationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_actividad", nullable = false)
    private ActivityEntity activity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UserEntity user;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime confirmedAt;

    public ConfirmationEntity() {
    }

    public Integer getConfirmationId() {
        return confirmationId;
    }

    public void setConfirmationId(Integer confirmationId) {
        this.confirmationId = confirmationId;
    }

    public ActivityEntity getActivity() {
        return activity;
    }

    public void setActivity(ActivityEntity activity) {
        this.activity = activity;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
}
