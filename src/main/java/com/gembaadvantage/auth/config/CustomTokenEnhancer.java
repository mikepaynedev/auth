package com.gembaadvantage.auth.config;

import com.gembaadvantage.auth.service.DummyUser;
import com.gembaadvantage.auth.service.UserProperties;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableAuthorizationServer
public class CustomTokenEnhancer implements TokenEnhancer {

    private UserProperties userProperties;

    public CustomTokenEnhancer(final UserProperties userProperties) {
        this.userProperties = userProperties;
    }

    private OAuth2AccessToken decorateToken(OAuth2AccessToken accessToken, DummyUser user) {
        //add some example extra info to the token.
        final Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("display_name", user.getDisplayName());
        additionalInfo.put("badge", user.getBadge());

        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        if (authentication != null) {
            final String userDn = authentication.getPrincipal().toString();

            /*TODO: Put additional information required by the client here, such as user roles obtained
            *  from a 3rd party. */

            //For now, try to get the user from the config-driven list. Error if unsuccessful.
            return  userProperties.getUserList()
                    .stream()
                    .filter(u -> StringUtils.equals(userDn, u.getDn()))
                    .findFirst()
                    .map(u -> decorateToken(accessToken, u))
                    .orElseThrow(() -> new BadCredentialsException(String.format("User %s not found.",userDn)));

        }

        return accessToken;
    }
}

