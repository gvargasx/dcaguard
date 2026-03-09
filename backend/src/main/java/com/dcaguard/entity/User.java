package com.dcaguard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firebase_uid", nullable = false, unique = true, length = 128)
    private String firebaseUid;

    @Column(length = 255)
    private String email;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 20)
    private PlanType planType = PlanType.ANONYMOUS;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public User() {
        //empty
    }

    public User(String firebaseUid, String email, String displayName, PlanType planType) {
        this.firebaseUid = firebaseUid;
        this.email = email;
        this.displayName = displayName;
        this.planType = planType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public PlanType getPlanType() { return planType; }
    public void setPlanType(PlanType planType) { this.planType = planType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public int getMaxPortfolios() {
        return switch (planType) {
            case ANONYMOUS -> 1;
            case FREE -> 3;
            case PRO -> Integer.MAX_VALUE;
        };
    }

    public int getMaxDcaPlans() {
        return switch (planType) {
            case ANONYMOUS -> 2;
            case FREE -> 10;
            case PRO -> Integer.MAX_VALUE;
        };
    }

    public int getMaxHistoryDays() {
        return switch (planType) {
            case ANONYMOUS -> 7;
            case FREE -> 90;
            case PRO -> 365;
        };
    }

    public boolean hasAds() {
        return planType != PlanType.PRO;
    }
}
