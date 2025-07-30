package com.mjfactor.url_shortener;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Document(collection = "urls")
public class UrlEntity {

    @Id
    private String id;

    @Field("original_url")
    @Indexed // Add index for faster lookups
    private String originalUrl;

    @Field("short_code")
    @Indexed(unique = true) // Unique index for short codes
    private String shortCode;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("access_count")
    private Long accessCount;

    @Field("expires_at")
    private LocalDateTime expiresAt;

    @Field("ttl_date")
    @Indexed(expireAfterSeconds = 0) // MongoDB TTL index - will expire based on this date
    private Date ttlDate;

    // Default constructor (required by MongoDB)
    public UrlEntity() {
    }

    // Constructor for creating new URLs
    public UrlEntity(String originalUrl, String shortCode) {
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.accessCount = 0L;
        this.expiresAt = LocalDateTime.now().plusMonths(6);
        this.ttlDate = Date.from(this.expiresAt.atZone(ZoneId.systemDefault()).toInstant());
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(Long accessCount) {
        this.accessCount = accessCount;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
        // Update TTL date when expiration is changed
        if (expiresAt != null) {
            this.ttlDate = Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant());
        }
    }

    public Date getTtlDate() {
        return ttlDate;
    }

    public void setTtlDate(Date ttlDate) {
        this.ttlDate = ttlDate;
    }

    // Helper method to check if URL has expired
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
