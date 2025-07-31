package com.mjfactor.url_shortener;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public String getUsageGuidelines() {
        return """
                URL Shortener Service - Usage Guidelines
                =======================================

                Base URL: https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/api

                Available Endpoints:

                1. CREATE SHORT URL
                   POST /api/shorten
                   Content-Type: application/json
                   Body: "https://your-long-url.com"

                   Example:
                   curl -X POST https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/api/shorten \\
                     -H "Content-Type: application/json" \\
                     -d "https://www.example.com"

                2. ACCESS SHORT URL (redirects to original)
                   GET /api/{shortCode}

                   Example:
                   curl -L https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/api/abc123

                3. GET URL STATISTICS
                   GET /api/shorten/{shortCode}/stats

                   Example:
                   curl https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/api/shorten/abc123/stats

                4. UPDATE URL
                   PUT /api/shorten/{shortCode}
                   Content-Type: application/json
                   Body: "https://new-url.com"

                   Example:
                   curl -X PUT https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/api/shorten/abc123 \\
                     -H "Content-Type: application/json" \\
                     -d "https://www.new-example.com"

                5. DELETE URL
                   DELETE /api/shorten/{shortCode}

                   Example:
                   curl -X DELETE https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/api/shorten/abc123

                6. HEALTH CHECK
                   GET /api/health

                   Example:
                   curl https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/api/health

                Notes:
                - URLs must include http:// or https://
                - Short codes are automatically generated
                - Access counts are tracked for each URL
                - URLs have expiration dates
                """;
    }
}
