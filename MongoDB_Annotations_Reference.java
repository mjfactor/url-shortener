/**
 * SPRING BOOT + MONGODB ANNOTATIONS REFERENCE
 * Complete guide for URL Shortener project
 */

// ================================
// 1. ENTITY/DOCUMENT ANNOTATIONS
// ================================

// Mark a class as MongoDB document
@Document(collection = "collection_name")  // Optional: specify collection name
@Document  // Uses class name as collection

// Mark primary key field
@Id
private String id;

// ================================
// 2. FIELD MAPPING ANNOTATIONS
// ================================

// Map field to different name in MongoDB
@Field("mongo_field_name")
private String javaFieldName;

// Exclude field from being saved to MongoDB
@Transient
private String temporaryField;

// ================================
// 3. INDEX ANNOTATIONS
// ================================

// Create index on field
@Indexed
private String normalIndex;

// Create unique index
@Indexed(unique = true)
private String uniqueField;

// Create TTL index for expiration
@Indexed(expireAfterSeconds = 3600)  // Expires in 1 hour
private LocalDateTime expiryTime;

// Create compound index on class level
@CompoundIndex(name = "compound_idx", def = "{'field1': 1, 'field2': -1}")
public class MyEntity {
    private String field1;
    private String field2;
}

// Create text index for search
@TextIndexed
private String searchableText;

// ================================
// 4. REFERENCE ANNOTATIONS
// ================================

// Reference to another document (like foreign key)
@DBRef
private OtherEntity referencedEntity;

// Document reference (alternative to DBRef)
@DocumentReference
private AnotherEntity linkedEntity;

// ================================
// 5. VERSION/OPTIMISTIC LOCKING
// ================================

// Version field for optimistic locking
@Version
private Long version;

// ================================
// 6. LIFECYCLE/CREATION ANNOTATIONS
// ================================

// Mark constructor for object creation
@PersistenceCreator
public MyEntity(String param1, String param2) {
    this.field1 = param1;
    this.field2 = param2;
}

// Custom type alias (shorter than full class name)
@TypeAlias("short_name")
public class MyVeryLongClassNameEntity {
}

// ================================
// 7. REPOSITORY ANNOTATIONS
// ================================

// Enable MongoDB repositories
@EnableMongoRepositories(basePackages = "com.mjfactor.url_shortener.repository")

// Custom query methods
@Query("{ 'field1' : ?0 }")
List<MyEntity> findByCustomQuery(String value);

// Update operations
@Update("{ '$inc' : { 'count' : 1 } }")
long incrementCount(String id);

// ================================
// 8. CONFIGURATION ANNOTATIONS
// ================================

// MongoDB configuration
@Configuration
@EnableMongoRepositories
public class MongoConfig extends AbstractMongoClientConfiguration {
    
    @Override
    protected String getDatabaseName() {
        return "url_shortener_db";
    }
}

// ================================
// 9. VALIDATION ANNOTATIONS (optional)
// ================================

@NotNull
@NotBlank
@Size(min = 1, max = 100)
@Pattern(regexp = "^https?://.*")
@Email
@URL

// ================================
// PRACTICAL EXAMPLES FOR URL SHORTENER
// ================================

@Document(collection = "shortened_urls")
@CompoundIndex(name = "url_created_idx", def = "{'originalUrl': 1, 'createdAt': -1}")
public class ShortenedUrl {
    
    @Id
    private String id;
    
    @Field("original_url")
    @NotBlank
    @URL
    private String originalUrl;
    
    @Indexed(unique = true)
    @NotBlank
    private String shortCode;
    
    @Indexed
    private LocalDateTime createdAt;
    
    @Indexed(expireAfterSeconds = 0)
    private LocalDateTime expiresAt;
    
    @Version
    private Long version;
    
    @Transient
    private boolean isPopular;  // Calculated field, not stored
}

// Repository interface
public interface ShortenedUrlRepository extends MongoRepository<ShortenedUrl, String> {
    
    @Query("{ 'shortCode' : ?0, 'expiresAt' : { $gt: ?1 } }")
    Optional<ShortenedUrl> findByShortCodeAndNotExpired(String shortCode, LocalDateTime now);
    
    @Update("{ '$inc' : { 'accessCount' : 1 }, '$set' : { 'updatedAt' : ?1 } }")
    void incrementAccessCount(String id, LocalDateTime now);
    
    // Automatic query derivation (no @Query needed)
    List<ShortenedUrl> findByOriginalUrlContaining(String urlPart);
    List<ShortenedUrl> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    void deleteByExpiresAtBefore(LocalDateTime now);
}

// Service class annotations
@Service
@Transactional
public class UrlShortenerService {
    
    @Autowired
    private ShortenedUrlRepository repository;
    
    // Method implementations...
}

// Controller annotations
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Validated
public class UrlShortenerController {
    
    @PostMapping("/shorten")
    public ResponseEntity<UrlShortenResponse> shortenUrl(@Valid @RequestBody UrlRequest request) {
        // Implementation...
    }
}
