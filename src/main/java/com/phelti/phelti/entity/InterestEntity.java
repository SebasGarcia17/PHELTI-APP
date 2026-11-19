package com.phelti.phelti.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "interes")
public class InterestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interes")
    private Integer interestId;

    @Column(name = "nombre", nullable = false, unique = true, length = 80)
    private String name;

    public InterestEntity() {
    }

    public Integer getInterestId() {
        return interestId;
    }

    public void setInterestId(Integer interestId) {
        this.interestId = interestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof InterestEntity that)) {
            return false;
        }

        return interestId != null && interestId.equals(that.getInterestId());
    }

    @Override
    public int hashCode() {
        return InterestEntity.class.hashCode();
    }
}
