package com.mjfactor.url_shortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import org.springframework.web.bind.annotation.PutMapping;
import java.net.URI;
import java.net.URISyntaxException;

@SpringBootApplication
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UrlShortenerApplication {
	private static final Logger logger = LoggerFactory.getLogger(UrlShortenerApplication.class);

	private final UrlRepository urlRepository;

	// Constructor injection for UrlRepository
	public UrlShortenerApplication(UrlRepository urlRepository) {
		this.urlRepository = urlRepository;
	}

	// Response record for URL shortening
	public record UrlShortenResponse(
			String id,
			String url,
			String shortCode,
			String createdAt,
			String updatedAt,
			String expiresAt) {
	}

	// Response record for URL statistics
	public record UrlStatsResponse(
			String id,
			String originalUrl,
			String shortCode,
			String createdAt,
			String updatedAt,
			String expiresAt,
			Long accessCount) {
	}

	// Response record for error messages
	public record ErrorResponse(
			String error,
			String message) {
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

	private boolean isUrlExpired(UrlEntity urlEntity) {
		return urlEntity.getExpiresAt() != null && LocalDateTime.now().isAfter(urlEntity.getExpiresAt());
	}

	private void validateUrl(String url) throws IllegalArgumentException {
		// Check for null or empty URL
		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("URL cannot be null or empty");
		}

		String cleanUrl = url.trim();

		// Check URL length (reasonable limit)
		if (cleanUrl.length() > 2048) {
			throw new IllegalArgumentException("URL is too long (maximum 2048 characters)");
		}

		// Basic URL format validation
		try {
			URI uri = new URI(cleanUrl);
			String scheme = uri.getScheme();

			if (scheme == null) {
				throw new IllegalArgumentException("URL must include a protocol (http:// or https://)");
			}

			scheme = scheme.toLowerCase();

			// Only allow http and https protocols
			if (!scheme.equals("http") && !scheme.equals("https")) {
				throw new IllegalArgumentException("Only HTTP and HTTPS URLs are supported");
			}

			// Ensure the URI has a host
			if (uri.getHost() == null || uri.getHost().isEmpty()) {
				throw new IllegalArgumentException("URL must include a valid host");
			}
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid URL format: " + e.getMessage());
		}
	}

	// Endpoint to create a new shortened URL or return existing one
	@PostMapping("/shorten")
	public ResponseEntity<?> shortenUrl(@RequestBody String longUrl) {
		logger.info("Received URL to shorten: {}", longUrl);
		try {
			validateUrl(longUrl);

			String cleanUrl = longUrl.trim();

			// Check if URL already exists
			Optional<UrlEntity> existingUrl = urlRepository.findByOriginalUrl(cleanUrl);
			if (existingUrl.isPresent()) {
				UrlEntity existing = existingUrl.get();

				// Check if the existing URL has expired
				if (isUrlExpired(existing)) {
					// Delete expired URL and create a new one
					urlRepository.delete(existing);
				} else {
					// Update the existing entry's timestamp and return it
					existing.setUpdatedAt(LocalDateTime.now());
					UrlEntity savedEntity = urlRepository.save(existing);

					// Format timestamps for response
					String createdAt = formatTimestamp(savedEntity.getCreatedAt());
					String updatedAt = formatTimestamp(savedEntity.getUpdatedAt());
					String expiresAt = formatTimestamp(savedEntity.getExpiresAt());

					return ResponseEntity.ok(new UrlShortenResponse(
							savedEntity.getId(),
							cleanUrl,
							"http://localhost:8080/api/" + savedEntity.getShortCode(),
							createdAt,
							updatedAt,
							expiresAt));
				}
			}

			// Generate unique short code
			String shortCode = generateUniqueShortCode(cleanUrl);

			// Create and save URL entity to MongoDB
			UrlEntity urlEntity = new UrlEntity(cleanUrl, shortCode);
			UrlEntity savedEntity = urlRepository.save(urlEntity);

			// Format timestamps for response
			String createdAt = formatTimestamp(savedEntity.getCreatedAt());
			String updatedAt = formatTimestamp(savedEntity.getUpdatedAt());
			String expiresAt = formatTimestamp(savedEntity.getExpiresAt());

			return ResponseEntity.status(HttpStatus.CREATED).body(new UrlShortenResponse(
					savedEntity.getId(),
					cleanUrl,
					"http://localhost:8080/api/" + shortCode, // Full redirect URL
					createdAt,
					updatedAt,
					expiresAt));
		} catch (IllegalArgumentException e) {
			logger.warn("Validation error for URL: {}, Error: {}", longUrl, e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
					"Validation Error",
					e.getMessage()));
		}
	}

	// Endpoint to redirect short URLs to their original URLs and track access count
	@GetMapping("/{shortCode}")
	public ResponseEntity<Void> redirectToOriginal(@PathVariable String shortCode) {
		logger.info("Redirecting short code: {}", shortCode);

		// Look up the original URL by short code
		Optional<UrlEntity> urlEntity = urlRepository.findByShortCode(shortCode);

		if (urlEntity.isPresent()) {
			UrlEntity entity = urlEntity.get();

			// Check if URL has expired
			if (isUrlExpired(entity)) {
				logger.info("URL has expired for short code: {}", shortCode);
				// Optionally delete the expired URL
				urlRepository.delete(entity);
				return ResponseEntity.notFound().build();
			}

			String originalUrl = entity.getOriginalUrl();

			// Increment access count
			entity.setAccessCount(entity.getAccessCount() != null ? entity.getAccessCount() + 1 : 1L);
			urlRepository.save(entity);

			// Create redirect response
			HttpHeaders headers = new HttpHeaders();
			headers.add("Location", originalUrl);

			return ResponseEntity.status(HttpStatus.FOUND)
					.headers(headers)
					.build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// Endpoint to update the original URL for an existing short code
	@PutMapping("/shorten/{shortCode}")
	public ResponseEntity<?> updateUrl(@PathVariable String shortCode,
			@RequestBody String newLongUrl) {
		logger.info("Updating URL for short code: {}", shortCode);
		try {
			validateUrl(newLongUrl);
			String cleanUrl = newLongUrl.trim();

			// Find the existing URL entity by short code
			Optional<UrlEntity> existingUrl = urlRepository.findByShortCode(shortCode);
			if (existingUrl.isPresent()) {
				UrlEntity urlEntity = existingUrl.get();

				// Check if URL has expired
				if (isUrlExpired(urlEntity)) {
					logger.info("Cannot update expired URL for short code: {}", shortCode);
					return ResponseEntity.notFound().build();
				}

				urlEntity.setOriginalUrl(cleanUrl);
				urlEntity.setUpdatedAt(LocalDateTime.now());

				// Save the updated entity
				UrlEntity savedEntity = urlRepository.save(urlEntity);

				// Format timestamps for response
				String createdAt = formatTimestamp(savedEntity.getCreatedAt());
				String updatedAt = formatTimestamp(savedEntity.getUpdatedAt());
				String expiresAt = formatTimestamp(savedEntity.getExpiresAt());

				return ResponseEntity.ok(new UrlShortenResponse(
						savedEntity.getId(),
						cleanUrl,
						"http://localhost:8080/api/" + savedEntity.getShortCode(),
						createdAt,
						updatedAt,
						expiresAt));
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (IllegalArgumentException e) {
			logger.error("Validation error updating URL for short code: {}, Error: {}", shortCode, e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
					"Validation Error",
					e.getMessage()));
		}

	}

	// Endpoint to delete a shortened URL by short code
	@DeleteMapping("/shorten/{shortCode}")
	public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode) {
		Optional<UrlEntity> urlEntity = urlRepository.findByShortCode(shortCode);
		if (urlEntity.isPresent()) {
			UrlEntity entity = urlEntity.get();

			// Check if URL has already expired
			if (isUrlExpired(entity)) {
				logger.info("URL already expired for short code: {}", shortCode);
			}

			// Delete the URL (whether expired or not)
			urlRepository.deleteByShortCode(shortCode);
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// Endpoint to get statistics for a shortened URL including access count
	@GetMapping("/shorten/{shortCode}/stats")
	public ResponseEntity<UrlStatsResponse> getUrlStats(@PathVariable String shortCode) {
		Optional<UrlEntity> urlEntity = urlRepository.findByShortCode(shortCode);
		if (urlEntity.isPresent()) {
			UrlEntity entity = urlEntity.get();

			// Check if URL has expired
			if (isUrlExpired(entity)) {
				logger.info("URL has expired for short code: {}", shortCode);
				// Optionally delete the expired URL
				urlRepository.delete(entity);
				return ResponseEntity.notFound().build();
			}

			UrlStatsResponse stats = new UrlStatsResponse(
					entity.getId(),
					entity.getOriginalUrl(),
					entity.getShortCode(),
					formatTimestamp(entity.getCreatedAt()),
					formatTimestamp(entity.getUpdatedAt()),
					formatTimestamp(entity.getExpiresAt()),
					entity.getAccessCount() != null ? entity.getAccessCount() : 0L);
			return ResponseEntity.ok(stats);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// Health check endpoint to verify application status
	@GetMapping("/health")
	public String getHealthStatus() {
		return "Application is running";
	}

	public static void main(String[] args) {
		SpringApplication.run(UrlShortenerApplication.class, args);
	}

}
