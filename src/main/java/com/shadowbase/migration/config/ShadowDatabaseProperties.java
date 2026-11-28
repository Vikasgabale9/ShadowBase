
package com.shadowbase.migration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shadowbase.shadow-db")
public class ShadowDatabaseProperties {

    private String adminUrl;
    private String username;
    private String password;
    private String databasePrefix = "shadowbase_shadow_";

    public String getAdminUrl() {
        return adminUrl;
    }

    public void setAdminUrl(String adminUrl) {
        this.adminUrl = adminUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabasePrefix() {
        return databasePrefix;
    }

    public void setDatabasePrefix(String databasePrefix) {
        this.databasePrefix = databasePrefix;
    }
}
