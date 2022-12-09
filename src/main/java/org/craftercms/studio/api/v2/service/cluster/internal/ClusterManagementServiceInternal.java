/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.api.v2.service.cluster.internal;

import org.craftercms.studio.api.v2.dal.ClusterMember;

import java.util.List;

public interface ClusterManagementServiceInternal {

    /**
     * Get all members for cluster
     *
     * @return List of all cluster memebers
     */
    List<ClusterMember> getAllMembers();

    /**
     * Get member by local address
     * @param localAddress node address
     * @return cluster member
     */
    ClusterMember getMemberByLocalAddress(String localAddress);

    /**
     * Remove members from cluster
     *
     * @param memberIds List of member ids
     * @return true if successful, otherwise false
     */
    boolean removeMembers(List<Long> memberIds);
}
