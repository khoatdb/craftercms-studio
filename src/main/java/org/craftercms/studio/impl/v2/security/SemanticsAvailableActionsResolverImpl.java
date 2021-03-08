/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.security;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.WorkflowItem;
import org.craftercms.studio.api.v2.security.SemanticsAvailableActionsResolver;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.security.SecurityService;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.model.rest.content.DetailedItem;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v2.dal.ItemState.isInWorkflow;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_EDIT;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.CONTENT_UPLOAD;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.PUBLISH;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.PUBLISH_APPROVE;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.PUBLISH_SCHEDULE;
import static org.craftercms.studio.api.v2.security.ContentItemPossibleActionsConstants.getPossibleActionsForItemState;
import static org.craftercms.studio.api.v2.security.ContentItemPossibleActionsConstants.getPossibleActionsForObject;

public class SemanticsAvailableActionsResolverImpl implements SemanticsAvailableActionsResolver {

    private SecurityService securityService;
    private ContentServiceInternal contentServiceInternal;
    private ServicesConfig servicesConfig;
    private WorkflowServiceInternal workflowServiceInternal;
    private UserServiceInternal userServiceInternal;

    @Override
    public long calculateContentItemAvailableActions(String username, String siteId, Item item)
            throws ServiceLayerException, UserNotFoundException {
        long userPermissionsBitmap = securityService.getAvailableActions(username, siteId, item.getPath());
        long systemTypeBitmap = getPossibleActionsForObject(item.getSystemType());
        long workflowStateBitmap = getPossibleActionsForItemState(item.getState());

        long result = (userPermissionsBitmap & systemTypeBitmap) & workflowStateBitmap;
        long toReturn = applySpecialUseCaseFilters(username, siteId, item, result);
        return toReturn;
    }

    @Override
    public long calculateContentItemAvailableActions(String username, String siteId, DetailedItem detailedItem)
            throws ServiceLayerException, UserNotFoundException {
        long userPermissionsBitmap = securityService.getAvailableActions(username, siteId, detailedItem.getPath());
        long systemTypeBitmap = getPossibleActionsForObject(detailedItem.getSystemType());
        long workflowStateBitmap = getPossibleActionsForItemState(detailedItem.getState());

        long result = (userPermissionsBitmap & systemTypeBitmap) & workflowStateBitmap;
        long toReturn = applySpecialUseCaseFilters(username, siteId, detailedItem, result);
        return toReturn;
    }

    private long applySpecialUseCaseFilters(String username, String siteId, Item item, long availableActions)
            throws ServiceLayerException, UserNotFoundException {
        if ((availableActions & CONTENT_EDIT) > 0 && (!contentServiceInternal.isEditable(item))) {
            availableActions = availableActions & ~CONTENT_EDIT;
        }

        if ((availableActions & CONTENT_UPLOAD) > 0 &&
                (StringUtils.equals(item.getSystemType(), CONTENT_TYPE_FOLDER) ||
                        !StudioUtils.matchesPatterns(item.getPath(), servicesConfig.getAssetPatterns(siteId)))) {
            availableActions = availableActions & ~CONTENT_UPLOAD;
        }

        if (servicesConfig.isRequirePeerReview(siteId)) {
            if (StringUtils.equals(username, item.getModifier())) {
                availableActions = availableActions & ~PUBLISH_SCHEDULE;
                availableActions = availableActions & ~PUBLISH;
            }

            if (isInWorkflow(item.getState())) {
                WorkflowItem workflow = workflowServiceInternal.getWorkflowEntry(siteId, item.getPath());
                User user = userServiceInternal.getUserByIdOrUsername(-1, username);
                if (user.getId() == workflow.getId()) {
                    availableActions = availableActions & ~PUBLISH_APPROVE;
                    availableActions = availableActions & ~PUBLISH_SCHEDULE;
                }
            }

        }

        return availableActions;
    }

    private long applySpecialUseCaseFilters(String username, String siteId, DetailedItem detailedItem,
                                            long availableActions)
            throws ServiceLayerException, UserNotFoundException {
        if ((availableActions & CONTENT_EDIT) > 0 && (!contentServiceInternal.isEditable(detailedItem))) {
            availableActions = availableActions & ~CONTENT_EDIT;
        }

        if ((availableActions & CONTENT_UPLOAD) > 0 &&
                (StringUtils.equals(detailedItem.getSystemType(), CONTENT_TYPE_FOLDER) ||
                        !StudioUtils.matchesPatterns(detailedItem.getPath(), servicesConfig.getAssetPatterns(siteId)))) {
            availableActions = availableActions & ~CONTENT_UPLOAD;
        }

        if (servicesConfig.isRequirePeerReview(siteId)) {
            if (StringUtils.equals(username, detailedItem.getSandbox().getModifier())) {
                availableActions = availableActions & ~PUBLISH_SCHEDULE;
                availableActions = availableActions & ~PUBLISH;
            }

            if (isInWorkflow(detailedItem.getState())) {
                WorkflowItem workflow = workflowServiceInternal.getWorkflowEntry(siteId, detailedItem.getPath());
                User user = userServiceInternal.getUserByIdOrUsername(-1, username);
                if (user.getId() == workflow.getId()) {
                    availableActions = availableActions & ~PUBLISH_APPROVE;
                    availableActions = availableActions & ~PUBLISH_SCHEDULE;
                }
            }

        }

        return availableActions;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public ContentServiceInternal getContentServiceInternal() {
        return contentServiceInternal;
    }

    public void setContentServiceInternal(ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public WorkflowServiceInternal getWorkflowServiceInternal() {
        return workflowServiceInternal;
    }

    public void setWorkflowServiceInternal(WorkflowServiceInternal workflowServiceInternal) {
        this.workflowServiceInternal = workflowServiceInternal;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }
}
