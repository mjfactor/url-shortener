package com.mjfactor.url_shortener;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApplicationLogic {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationLogic.class);
    private final UrlRepository urlRepository;

    // Constructor injection (recommended over @Autowired)
    public ApplicationLogic(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    // Response record for URL shortening
    public record UrlShortenResponse(
            String id,
            String url,
            String shortCode,
            String createdAt,
            String updatedAt) {
    }

    @PostMapping("/shorten")
    public ResponseEntity<UrlShortenResponse> shortenUrl(@RequestBody String longUrl) {
        logger.info("Received URL to shorten: {}", longUrl);

        // Clean the URL
        String cleanUrl = longUrl.trim();

        // Check if URL already exists
        Optional<UrlEntity> existingUrl = urlRepository.findByOriginalUrl(cleanUrl);
        if (existingUrl.isPresent()) {
            // Update the existing entry's timestamp and return it
            UrlEntity existing = existingUrl.get();
            existing.setUpdatedAt(LocalDateTime.now());
            UrlEntity savedEntity = urlRepository.save(existing);

            // Format timestamps for response
            String createdAt = formatTimestamp(savedEntity.getCreatedAt());
            String updatedAt = formatTimestamp(savedEntity.getUpdatedAt());

            return ResponseEntity.ok(new UrlShortenResponse(
                    savedEntity.getId(),
                    cleanUrl,
                    "http://localhost:8080/api/" + savedEntity.getShortCode(),
                    createdAt,
                    updatedAt));
        }

        // Generate unique short code
        String shortCode = generateUniqueShortCode(cleanUrl);

        // Create and save URL entity to MongoDB
        UrlEntity urlEntity = new UrlEntity(cleanUrl, shortCode);
        UrlEntity savedEntity = urlRepository.save(urlEntity);

        // Format timestamps for response
        String createdAt = formatTimestamp(savedEntity.getCreatedAt());
        String updatedAt = formatTimestamp(savedEntity.getUpdatedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(new UrlShortenResponse(
                savedEntity.getId(),
                cleanUrl,
                "http://localhost:8080/api/" + shortCode, // Full redirect URL
                createdAt,
                updatedAt));
    }

    private String generateUniqueShortCode(String url) {
        String baseShortCode = Integer.toHexString(Math.abs(url.hashCode()));
        String shortCode = baseShortCode;
        int counter = 1;

        // Keep generating until we find a unique short code
        while (urlRepository.findByShortCode(shortCode).isPresent()) {
            shortCode = baseShortCode + counter;
            counter++;
        }

        return shortCode;
    }

    private String formatTimestamp(LocalDateTime timestamp) {
        return timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
    }

    // Redirect endpoint for short URLs
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToOriginal(@PathVariable String shortCode) {
        logger.info("Redirecting short code: {}", shortCode);

        // Look up the original URL by short code
        Optional<UrlEntity> urlEntity = urlRepository.findByShortCode(shortCode);

        if (urlEntity.isPresent()) {
            String originalUrl = urlEntity.get().getOriginalUrl();

            // Create redirect response
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", originalUrl);

            // Return 302 (Found) redirect
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } else {
            // Short code not found, return 404
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/health")
    public String getHealthStatus() {
        return "Application is running";
    }

}
