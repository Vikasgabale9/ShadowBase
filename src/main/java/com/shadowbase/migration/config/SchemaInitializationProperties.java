package com.shadowbase.migration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shadowbase.schema-initialization")
public class SchemaInitializationProperties {

    private String pgDumpPath = "pg_dump";
    private String psqlPath = "psql";

    public String getPgDumpPath() {
        return pgDumpPath;
    }

    public void setPgDumpPath(String pgDumpPath) {
        this.pgDumpPath = pgDumpPath;
    }

    public String getPsqlPath() {
        return psqlPath;
    }

    public void setPsqlPath(String psqlPath) {
        this.psqlPath = psqlPath;
    }
}