package com.gembaadvantage.auth.config;

import com.gembaadvantage.auth.service.UserProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.List;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    /*
    TODO: Client Credentials should be managed in a more secure way.
     */
    private static final String CLIENT_ID = "my-client";
    // encoding method prefix is required for DelegatingPasswordEncoder which is default since Spring Security 5.0.0.RC1
    // you can use one of bcrypt/noop/pbkdf2/scrypt/sha256
    // you can change default behaviour by providing a bean with the encoder you want
    // more: https://spring.io/blog/2017/11/01/spring-security-5-0-0-rc1-released#password-encoding
    static final String CLIENT_SECRET = "{noop}my-secret";

    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String SCOPE_READ = "read";
    private static final String SCOPE_WRITE = "write";
    private static final String TRUST = "trust";

    @Value("${oauth2.refreshTokenTtl}")
    private static final int refreshTokenTtl = 3600;

    @Value("${oauth2.accessTokenTtl}")
    private static final int accessTokenTtl = 300;

    @Value("${jwt.signing-key}")
    private String jwtSigningKey;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UserProperties userProperties;

    @Bean
    public TokenStore tokenStore() {
        return new InMemoryTokenStore();
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        /*
        TODO: In-memory token stores cannot scale out. If this functionality is required, consider using a database
            for tokens.
         */
        clients
                .inMemory()
                .withClient(CLIENT_ID)
                .secret(CLIENT_SECRET)
                .authorizedGrantTypes(GRANT_TYPE_PASSWORD, AUTHORIZATION_CODE, REFRESH_TOKEN)
                .scopes(SCOPE_READ, SCOPE_WRITE, TRUST)
                .accessTokenValiditySeconds(accessTokenTtl)
                .refreshTokenValiditySeconds(refreshTokenTtl);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        /*
        Create a token enhancer chain that adds custom claims to the token via CustomTokenEnhancer, and
        converts the token to JWT using accessTokenConverter(), so that the claims are contained within the token.
        See https://www.toptal.com/spring/spring-boot-oauth2-jwt-rest-protection.
         */
        final TokenEnhancerChain chain = new TokenEnhancerChain();
        chain.setTokenEnhancers(List.of(new CustomTokenEnhancer(userProperties), accessTokenConverter()));
        endpoints.tokenStore(tokenStore())
                .tokenEnhancer(chain)
                .allowedTokenEndpointRequestMethods(HttpMethod.GET,HttpMethod.POST)
                .authenticationManager(authManager);
    }

    @Bean
    JwtAccessTokenConverter accessTokenConverter() {
        return new JwtAccessTokenConverter();
    }
}
