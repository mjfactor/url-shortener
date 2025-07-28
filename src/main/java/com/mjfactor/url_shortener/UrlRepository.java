package com.mjfactor.url_shortener;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UrlRepository extends MongoRepository<UrlEntity, String> {

    // Find URL by short code
    Optional<UrlEntity> findByShortCode(String shortCode);

    // Find URL by original URL (to avoid duplicates)
    Optional<UrlEntity> findByOriginalUrl(String originalUrl);

    // Delete URL by short code
    Optional<UrlEntity> deleteByShortCode(String shortCode);
}
