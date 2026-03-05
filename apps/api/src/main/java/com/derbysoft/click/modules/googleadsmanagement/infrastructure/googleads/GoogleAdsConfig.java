package com.derbysoft.click.modules.googleadsmanagement.infrastructure.googleads;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "google.ads")
public class GoogleAdsConfig {

    private String credentialsPath;
    private String developerToken;

    public String getCredentialsPath() { return credentialsPath; }
    public void setCredentialsPath(String credentialsPath) { this.credentialsPath = credentialsPath; }

    public String getDeveloperToken() { return developerToken; }
    public void setDeveloperToken(String developerToken) { this.developerToken = developerToken; }
}
