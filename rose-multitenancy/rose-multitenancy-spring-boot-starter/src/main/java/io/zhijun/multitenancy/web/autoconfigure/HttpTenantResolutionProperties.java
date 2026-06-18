package io.zhijun.multitenancy.web.autoconfigure;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for HTTP tenant resolution.
 */
@ConfigurationProperties(prefix = HttpTenantResolutionProperties.CONFIG_PREFIX)
public class HttpTenantResolutionProperties {

    public static final String CONFIG_PREFIX = "rose.multitenancy.resolution.http";

    /**
     * Whether an HTTP tenant resolution strategy should be used.
     */
    private boolean enabled = true;

    /**
     * Mode of HTTP resolution.
     */
    private HttpResolutionMode resolutionMode = HttpResolutionMode.HEADER;

    /**
     * Configuration for HTTP header tenant resolution.
     */
    private final Header header = new Header();

    /**
     * Configuration for HTTP cookie tenant resolution.
     */
    private final Cookie cookie = new Cookie();

    /**
     * Configuration for HTTP filter resolving the current tenant.
     */
    private final Filter filter = new Filter();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public HttpResolutionMode getResolutionMode() {
        return resolutionMode;
    }

    public void setResolutionMode(HttpResolutionMode resolutionMode) {
        this.resolutionMode = resolutionMode;
    }

    public Header getHeader() {
        return header;
    }

    public Cookie getCookie() {
        return cookie;
    }

    public Filter getFilter() {
        return filter;
    }

    public static class Header {

        /**
         * Name of the HTTP header from which to resolve the current tenant.
         */
        private String headerName = "X-TenantId";

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }

    }

    public static class Cookie {

        /**
         * Name of the HTTP cookie from which to resolve the current tenant.
         */
        private String cookieName = "TENANT-ID";

        public String getCookieName() {
            return cookieName;
        }

        public void setCookieName(String cookieName) {
            this.cookieName = cookieName;
        }

    }

    public static class Filter {

        /**
         * Whether the HTTP filter resolving the current tenant is enabled.
         */
        private boolean enabled = true;

        /**
         * Comma-separated list of HTTP request paths for which the tenant resolution will
         * not be performed.
         */
        private Set<String> ignorePaths = defaultIgnorePaths();

        /**
         * Additional comma-separated list of HTTP request paths for which the tenant
         * resolution will not be performed.
         */
        private Set<String> additionalIgnorePaths = Collections.emptySet();

        /**
         * HTTP request paths for which a tenant identifier is required when not ignored.
         */
        private Set<String> requiredIncludePaths = Collections.emptySet();

        /**
         * HTTP request paths excluded from tenant requirement checks.
         */
        private Set<String> requiredExcludePaths = Collections.emptySet();

        private static Set<String> defaultIgnorePaths() {
            Set<String> paths = new HashSet<String>();
            paths.add("/actuator/**");
            paths.add("/webjars/**");
            paths.add("/css/**");
            paths.add("/js/**");
            paths.add(".ico");
            return paths;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Set<String> getIgnorePaths() {
            return ignorePaths;
        }

        public void setIgnorePaths(Set<String> ignorePaths) {
            this.ignorePaths = ignorePaths;
        }

        public Set<String> getAdditionalIgnorePaths() {
            return additionalIgnorePaths;
        }

        public void setAdditionalIgnorePaths(Set<String> additionalIgnorePaths) {
            this.additionalIgnorePaths = additionalIgnorePaths;
        }

        public Set<String> getRequiredIncludePaths() {
            return requiredIncludePaths;
        }

        public void setRequiredIncludePaths(Set<String> requiredIncludePaths) {
            this.requiredIncludePaths = requiredIncludePaths;
        }

        public Set<String> getRequiredExcludePaths() {
            return requiredExcludePaths;
        }

        public void setRequiredExcludePaths(Set<String> requiredExcludePaths) {
            this.requiredExcludePaths = requiredExcludePaths;
        }

    }

    public enum HttpResolutionMode {
        COOKIE,
        HEADER;
    }

}
