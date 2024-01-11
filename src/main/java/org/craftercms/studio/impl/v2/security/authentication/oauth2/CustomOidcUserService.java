package org.craftercms.studio.impl.v2.security.authentication.oauth2;

import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.beans.ConstructorProperties;

public class CustomOidcUserService extends OidcUserService {
    
    private final UserServiceInternal userServiceInternal;

    @ConstructorProperties({"userServiceInternal"})
    public CustomOidcUserService(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    };

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        var oidcUser = super.loadUser(userRequest);
        User cmsUser;
        try {
            cmsUser = userServiceInternal.getUserByIdOrUsername(0, oidcUser.getEmail());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var customOidcUser = new CustomOidcUser(cmsUser.getId(), oidcUser);
        return customOidcUser;
    }
}