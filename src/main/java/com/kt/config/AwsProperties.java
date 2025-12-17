package com.kt.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "aws")
@AllArgsConstructor
public class AwsProperties {

    private final Ses ses;
    private final String region;

    @Getter
    public static class Ses {
        private final String accessKey;
        private final String secretKey;
        private final String sendMailFrom;

        public Ses(String accessKey, String secretKey, String sendMailFrom) {
            this.accessKey = accessKey;
            this.secretKey = secretKey;
            this.sendMailFrom = sendMailFrom;
        }
    }
}
