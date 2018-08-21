/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.craftercms.studio.api.v2.service.security;

import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationSystemException;
import org.craftercms.studio.api.v1.exception.security.BadCredentialsException;
import org.craftercms.studio.api.v1.exception.security.PasswordDoesNotMatchException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.model.Group;
import org.craftercms.studio.model.User;

import java.util.List;

public interface SecurityProvider {
    List<User> getAllUsersForSite(int orgId, List<String> groupNames, int offset, int limit, String sort);

    boolean createUser(User user) throws UserAlreadyExistsException;

    void updateUser(User user);

    void deleteUsers(List<Integer> userIds, List<String> usernames);

    User getUserByIdOrUsername(int userId, String username);

    void enableUsers(List<Integer> userIds, List<String> usernames, boolean enabled);

    List<Group> getUserGroups(long userId, String username);

    List<Group> getAllGroups(int orgId, int offset, int limit, String sort);

    void createGroup(int orgId, String groupName, String groupDescription);

    void updateGroup(int orgId, Group group);

    void deleteGroup(int groupId);

    Group getGroup(int groupId);

    List<User> getGroupMembers(int groupId, int offset, int limit, String sort);

    void addGroupMembers(int groupId, List<Integer> userIds, List<String> usernames);

    void removeGroupMembers(int groupId, List<Integer> userIds, List<String> usernames);

    int getAllUsersTotal();

    String getCurrentUser();

    String authenticate(String username, String password) throws BadCredentialsException, AuthenticationSystemException, EntitlementException;

    boolean validateTicket(String ticket);

    String getCurrentToken();

    boolean logout();

    boolean changePassword(String username, String current, String newPassword)
            throws PasswordDoesNotMatchException, UserExternallyManagedException;

    boolean setUserPassword(String username, String newPassword)
            throws UserNotFoundException, UserExternallyManagedException;

    boolean userExists(String username);
}
