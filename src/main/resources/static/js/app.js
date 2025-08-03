// URL Shortener Application JavaScript
class UrlShortenerApp {
    constructor() {
        this.baseApiUrl = '/api';
        this.init();
    }

    init() {
        this.bindEvents();
        this.showWelcomeMessage();
    }

    bindEvents() {
        // Shorten URL form
        const shortenForm = document.getElementById('shortenForm');
        if (shortenForm) {
            shortenForm.addEventListener('submit', (e) => this.handleShortenUrl(e));
        }

        // Stats form
        const statsForm = document.getElementById('statsForm');
        if (statsForm) {
            statsForm.addEventListener('submit', (e) => this.handleGetStats(e));
        }

        // Copy button
        const copyBtn = document.getElementById('copyBtn');
        if (copyBtn) {
            copyBtn.addEventListener('click', () => this.copyShortUrl());
        }
    }

    showWelcomeMessage() {
        this.showToast('Welcome to URL Shortener! Start by entering a URL to shorten.', 'success');
    }

    async handleShortenUrl(event) {
        event.preventDefault();

        const longUrlInput = document.getElementById('longUrl');
        const longUrl = longUrlInput.value.trim();

        if (!longUrl) {
            this.showToast('Please enter a URL to shorten', 'error');
            return;
        }

        if (!this.isValidUrl(longUrl)) {
            this.showToast('Please enter a valid URL (must include http:// or https://)', 'error');
            return;
        }

        this.showLoading(true);

        try {
            const response = await fetch(`${this.baseApiUrl}/shorten`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(longUrl)
            });

            const data = await response.json();

            if (response.ok) {
                this.displayShortenResult(data);
                this.showToast('URL shortened successfully!', 'success');
                longUrlInput.value = '';
            } else {
                this.showToast(data.message || 'Failed to shorten URL', 'error');
            }
        } catch (error) {
            console.error('Error shortening URL:', error);
            this.showToast('Network error. Please try again.', 'error');
        } finally {
            this.showLoading(false);
        }
    }

    async handleGetStats(event) {
        event.preventDefault();

        const shortCodeInput = document.getElementById('shortCode');
        const shortCode = shortCodeInput.value.trim();

        if (!shortCode) {
            this.showToast('Please enter a short code', 'error');
            return;
        }

        this.showLoading(true);

        try {
            const response = await fetch(`${this.baseApiUrl}/shorten/${shortCode}/stats`);

            if (response.ok) {
                const data = await response.json();
                this.displayStatsResult(data);
                this.showToast('Statistics retrieved successfully!', 'success');
            } else if (response.status === 404) {
                this.showToast('Short code not found or has expired', 'error');
                this.hideStatsResult();
            } else {
                this.showToast('Failed to retrieve statistics', 'error');
                this.hideStatsResult();
            }
        } catch (error) {
            console.error('Error getting stats:', error);
            this.showToast('Network error. Please try again.', 'error');
            this.hideStatsResult();
        } finally {
            this.showLoading(false);
        }
    }

    displayShortenResult(data) {
        const resultSection = document.getElementById('shortenResult');
        const shortUrlInput = document.getElementById('shortUrl');
        const originalUrlSpan = document.getElementById('originalUrl');
        const createdAtSpan = document.getElementById('createdAt');
        const expiresAtSpan = document.getElementById('expiresAt');

        if (resultSection && shortUrlInput && originalUrlSpan && createdAtSpan && expiresAtSpan) {
            shortUrlInput.value = data.shortCode;
            originalUrlSpan.textContent = this.truncateUrl(data.url);
            originalUrlSpan.title = data.url;
            createdAtSpan.textContent = this.formatDate(data.createdAt);
            expiresAtSpan.textContent = this.formatDate(data.expiresAt);

            resultSection.style.display = 'block';
            resultSection.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
    }

    displayStatsResult(data) {
        const statsSection = document.getElementById('statsResult');
        const elements = {
            statsOriginalUrl: document.getElementById('statsOriginalUrl'),
            statsShortCode: document.getElementById('statsShortCode'),
            accessCount: document.getElementById('accessCount'),
            statsCreatedAt: document.getElementById('statsCreatedAt'),
            statsUpdatedAt: document.getElementById('statsUpdatedAt'),
            statsExpiresAt: document.getElementById('statsExpiresAt')
        };

        // Check if all elements exist
        const allElementsExist = Object.values(elements).every(el => el !== null);

        if (statsSection && allElementsExist) {
            elements.statsOriginalUrl.textContent = this.truncateUrl(data.originalUrl);
            elements.statsOriginalUrl.title = data.originalUrl;
            elements.statsShortCode.textContent = data.shortCode;
            elements.accessCount.textContent = data.accessCount.toLocaleString();
            elements.statsCreatedAt.textContent = this.formatDate(data.createdAt);
            elements.statsUpdatedAt.textContent = this.formatDate(data.updatedAt);
            elements.statsExpiresAt.textContent = this.formatDate(data.expiresAt);

            statsSection.style.display = 'block';
            statsSection.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
    }

    hideStatsResult() {
        const statsSection = document.getElementById('statsResult');
        if (statsSection) {
            statsSection.style.display = 'none';
        }
    }

    async copyShortUrl() {
        const shortUrlInput = document.getElementById('shortUrl');

        if (!shortUrlInput) {
            this.showToast('No URL to copy', 'error');
            return;
        }

        try {
            await navigator.clipboard.writeText(shortUrlInput.value);
            this.showToast('Short URL copied to clipboard!', 'success');

            // Visual feedback
            const copyBtn = document.getElementById('copyBtn');
            if (copyBtn) {
                const originalText = copyBtn.innerHTML;
                copyBtn.innerHTML = '<i class="fas fa-check"></i> Copied!';
                copyBtn.style.background = '#10b981';

                setTimeout(() => {
                    copyBtn.innerHTML = originalText;
                    copyBtn.style.background = '';
                }, 2000);
            }
        } catch (error) {
            console.error('Failed to copy:', error);

            // Fallback for older browsers
            shortUrlInput.select();
            shortUrlInput.setSelectionRange(0, 99999);

            try {
                document.execCommand('copy');
                this.showToast('Short URL copied to clipboard!', 'success');
            } catch (fallbackError) {
                this.showToast('Failed to copy. Please copy manually.', 'error');
            }
        }
    }

    isValidUrl(string) {
        try {
            const url = new URL(string);
            return url.protocol === 'http:' || url.protocol === 'https:';
        } catch (_) {
            return false;
        }
    }

    truncateUrl(url, maxLength = 50) {
        if (url.length <= maxLength) {
            return url;
        }
        return url.substring(0, maxLength) + '...';
    }

    formatDate(dateString) {
        try {
            const date = new Date(dateString);
            return date.toLocaleString('en-US', {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
                hour12: true
            });
        } catch (error) {
            console.error('Error formatting date:', error);
            return dateString;
        }
    }

    showLoading(show) {
        const loadingSpinner = document.getElementById('loadingSpinner');
        if (loadingSpinner) {
            loadingSpinner.style.display = show ? 'flex' : 'none';
        }
    }

    showToast(message, type = 'success') {
        const toastContainer = document.getElementById('toastContainer');
        if (!toastContainer) return;

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;

        // Add icon based on type
        let icon = '';
        switch (type) {
            case 'success':
                icon = '<i class="fas fa-check-circle"></i>';
                break;
            case 'error':
                icon = '<i class="fas fa-exclamation-circle"></i>';
                break;
            case 'warning':
                icon = '<i class="fas fa-exclamation-triangle"></i>';
                break;
            default:
                icon = '<i class="fas fa-info-circle"></i>';
        }

        toast.innerHTML = `${icon} ${message}`;

        toastContainer.appendChild(toast);

        // Auto remove after 5 seconds
        setTimeout(() => {
            if (toast.parentNode) {
                toast.style.animation = 'toastSlide 0.3s ease-out reverse';
                setTimeout(() => {
                    toast.remove();
                }, 300);
            }
        }, 5000);

        // Remove on click
        toast.addEventListener('click', () => {
            toast.style.animation = 'toastSlide 0.3s ease-out reverse';
            setTimeout(() => {
                toast.remove();
            }, 300);
        });
    }

    // Utility method to test API health
    async checkApiHealth() {
        try {
            const response = await fetch(`${this.baseApiUrl}/health`);
            if (response.ok) {
                const status = await response.text();
                console.log('API Health:', status);
                return true;
            }
        } catch (error) {
            console.error('API Health Check Failed:', error);
            return false;
        }
        return false;
    }
}

// Enhanced form validation
document.addEventListener('DOMContentLoaded', function () {
    // Initialize the app
    const app = new UrlShortenerApp();

    // Add real-time validation to URL input
    const longUrlInput = document.getElementById('longUrl');
    if (longUrlInput) {
        longUrlInput.addEventListener('input', function () {
            const url = this.value.trim();
            if (url && !app.isValidUrl(url)) {
                this.style.borderColor = '#ef4444';
            } else {
                this.style.borderColor = '';
            }
        });
    }

    // Add enter key support for short code input
    const shortCodeInput = document.getElementById('shortCode');
    if (shortCodeInput) {
        shortCodeInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const statsForm = document.getElementById('statsForm');
                if (statsForm) {
                    statsForm.dispatchEvent(new Event('submit'));
                }
            }
        });
    }

    // Check API health on load
    app.checkApiHealth().then(isHealthy => {
        if (!isHealthy) {
            app.showToast('Warning: API might not be available', 'warning');
        }
    });

    console.log('URL Shortener App initialized successfully!');
});
