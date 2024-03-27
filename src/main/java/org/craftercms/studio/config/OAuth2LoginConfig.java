package org.craftercms.studio.config;

import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;

@Configuration
public class OAuth2LoginConfig {
    private final StudioConfiguration studioConfiguration;

    @Autowired
    public OAuth2LoginConfig(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(this.clientRegistration());
    }

    private ClientRegistration clientRegistration() {
        var clientReg = ClientRegistrations.fromOidcIssuerLocation(studioConfiguration.getProperty("studio.security.sso.azure.issuerUri"))
                .registrationId("azure")
                .clientId(studioConfiguration.getProperty("studio.security.sso.azure.clientId"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .clientSecret(null)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .userNameAttributeName(StandardClaimNames.EMAIL)
                .clientName("azure")
                .build();

        return clientReg;
    }
}