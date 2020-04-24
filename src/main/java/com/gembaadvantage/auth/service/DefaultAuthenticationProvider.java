package com.gembaadvantage.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DefaultAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserProperties userProperties;

    private List<SimpleGrantedAuthority> getRoles(final DummyUser user) {
        return user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        //error if there is no user information
        if (authentication.getPrincipal() == null) {
            throw new BadCredentialsException("User principal not provided.");
        }

        //get user DN
        final String userDn = authentication.getPrincipal().toString();

        /*TODO: Use some external service here to check whether username has
           permission, and throw AuthenticationException if not */

        //For now, try to get the user from the config-driven list. Error if unsuccessful.
        final DummyUser user = userProperties.getUserList().stream()
                .filter(u -> u.getDn().equals(userDn)).findFirst().orElse(null);
        if (user == null) {
            throw new BadCredentialsException(String.format("User %s not found.", userDn));
        }
        return new UsernamePasswordAuthenticationToken(userDn, "", getRoles(user));
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass == UsernamePasswordAuthenticationToken.class;
    }
}