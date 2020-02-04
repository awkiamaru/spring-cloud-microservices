package br.com.course.core.property;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt.config")
@Getter
@Setter
@ToString
public class JWTConfiguration {
    private String loginUrl = "/login/**";
    @NestedConfigurationProperty
    private Header header = new Header();
    private int expiration = 3600;
    private String privateKey = "uATrOX72VlgPFFwQO0RlT6eIDnhk4VjI";
    private String type = "encrypted";

    @Getter
    @Setter
    public static class Header {

        private String name = "Authorization";
        private String prefix = "Bearer";
    }
}
