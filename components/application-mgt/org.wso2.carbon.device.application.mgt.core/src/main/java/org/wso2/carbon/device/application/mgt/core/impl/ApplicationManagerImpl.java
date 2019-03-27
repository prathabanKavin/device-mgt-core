/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.AppLifecycleState;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.ApplicationSubscriptionType;
import org.wso2.carbon.device.application.mgt.common.ApplicationType;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.User;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.TransactionManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationReleaseDAO;
import org.wso2.carbon.device.application.mgt.core.dao.LifecycleStateDAO;
import org.wso2.carbon.device.application.mgt.core.dao.VisibilityDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.BadRequestException;
import org.wso2.carbon.device.application.mgt.core.exception.ForbiddenException;
import org.wso2.carbon.device.application.mgt.core.exception.LifeCycleManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.exception.VisibilityManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.lifecycle.LifecycleStateManger;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;

import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Default Concrete implementation of Application Management related implementations.
 */
public class ApplicationManagerImpl implements ApplicationManager {

    private static final Log log = LogFactory.getLog(ApplicationManagerImpl.class);
    private VisibilityDAO visibilityDAO;
    private ApplicationDAO applicationDAO;
    private ApplicationReleaseDAO applicationReleaseDAO;
    private LifecycleStateDAO lifecycleStateDAO;
    private LifecycleStateManger lifecycleStateManger;


    public ApplicationManagerImpl() {
        initDataAccessObjects();
        lifecycleStateManger = DataHolder.getInstance().getLifecycleStateManager();
    }

    private void initDataAccessObjects() {
        this.visibilityDAO = ApplicationManagementDAOFactory.getVisibilityDAO();
        this.applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
        this.lifecycleStateDAO =  ApplicationManagementDAOFactory.getLifecycleStateDAO();
        this.applicationReleaseDAO = ApplicationManagementDAOFactory.getApplicationReleaseDAO();
    }

    /***
     * The responsbility of this method is the creating an application.
     * @param application Application that need to be created.
     * @return {@link Application}
     * @throws RequestValidatingException if application creating request is invalid, returns {@link RequestValidatingException}
     * @throws ApplicationManagementException Catch all other throwing exceptions and returns {@link ApplicationManagementException}
     */
    @Override
    public Application createApplication(Application application)
            throws RequestValidatingException, ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        application.setUser(new User(userName, tenantId));
        if (log.isDebugEnabled()) {
            log.debug("Create Application received for the tenant : " + tenantId + " From" + " the user : " +
                    userName);
        }
        validateAppCreatingRequest(application, tenantId);
        //todo throw different exception
        validateAppReleasePayload(application.getApplicationReleases().get(0));
        DeviceType deviceType;
        ApplicationRelease applicationRelease;
        List<ApplicationRelease> applicationReleases = new ArrayList<>();
        try {
            // Getting the device type details to get device type ID for internal mappings
            deviceType = Util.getDeviceManagementService().getDeviceType(application.getDeviceType());

            ConnectionManagerUtil.beginDBTransaction();
            if (deviceType == null) {
                log.error("Device type is not matched with application type");
                ConnectionManagerUtil.rollbackDBTransaction();
                return null;
            }
            if (!application.getUnrestrictedRoles().isEmpty()) {
                application.setIsRestricted(true);
            }

            // Insert to application table
            int appId = this.applicationDAO.createApplication(application, deviceType.getId());

            if (appId == -1) {
                log.error("Application creation is Failed");
                ConnectionManagerUtil.rollbackDBTransaction();
                return null;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("New Application entry added to AP_APP table. App Id:" + appId);
                }
                if (!application.getTags().isEmpty()) {
                    this.applicationDAO.addTags(application.getTags(), appId, tenantId);
                    if (log.isDebugEnabled()) {
                        log.debug("New tags entry added to AP_APP_TAG table. App Id:" + appId);
                    }
                }
                if (application.getIsRestricted()) {
                    this.visibilityDAO.addUnrestrictedRoles(application.getUnrestrictedRoles(), appId, tenantId);
                    if (log.isDebugEnabled()) {
                        log.debug("New restricted roles to app ID mapping added to AP_UNRESTRICTED_ROLE table."
                                + " App Id:" + appId);
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Creating a new release. App Id:" + appId);
                }
                applicationRelease = application.getApplicationReleases().get(0);
                applicationRelease = this.applicationReleaseDAO.createRelease(applicationRelease, appId, tenantId);

                if (log.isDebugEnabled()) {
                    log.debug("Changing lifecycle state. App Id:" + appId);
                }
                LifecycleState lifecycleState = getLifecycleStateInstant(AppLifecycleState.CREATED.toString(),
                        AppLifecycleState.CREATED.toString());
                this.lifecycleStateDAO.addLifecycleState(lifecycleState, appId, applicationRelease.getUuid(), tenantId);
                applicationRelease.setLifecycleState(lifecycleState);
                applicationReleases.add(applicationRelease);
                application.setApplicationReleases(applicationReleases);

                ConnectionManagerUtil.commitDBTransaction();
            }
            return application;
        } catch (DeviceManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occurred while getting device type id of " + application.getType(), e);
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occured while adding lifecycle state. application name: " + application.getName()
                            + " application type: is " + application.getType(), e);
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occured while adding application or application release. application name: " + application
                            .getName() + " application type: " + application.getType(), e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementException("Error occured while getting database connection. ", e);
        } catch (VisibilityManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occured while adding unrestricted roles. application name: " + application.getName()
                            + " application type: " + application.getType(), e);
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException("Error occured while disabling AutoCommit. ", e);
        }
    }

    @Override
    public ApplicationList getApplications(Filter filter) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        ApplicationList applicationList;
        List<ApplicationRelease> applicationReleases;

        filter = validateFilter(filter);
        if (filter == null) {
            throw new ApplicationManagementException("Filter validation failed, Please verify the request payload");
        }

        try {
            ConnectionManagerUtil.openDBConnection();
            applicationList = applicationDAO.getApplications(filter, tenantId);
            if(applicationList != null && applicationList.getApplications() != null && !applicationList
                    .getApplications().isEmpty()) {
                if (!isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                    applicationList = getRoleRestrictedApplicationList(applicationList, userName);
                }
                for (Application application : applicationList.getApplications()) {
                    applicationReleases = getReleases(application, filter.getCurrentAppReleaseState());
                    application.setApplicationReleases(applicationReleases);
                }
            }
            return applicationList;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while checking whether the user " + userName + " of tenant " + tenantId
                            + " has the publisher permission", e);
        } catch (ApplicationManagementDAOException e) {
            throw new ApplicationManagementException(
                    "DAO exception while getting applications for the user " + userName + " of tenant " + tenantId, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public ApplicationRelease createRelease(int applicationId, ApplicationRelease applicationRelease)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        Application application = getApplicationIfAccessible(applicationId);
        validateAppReleasePayload(applicationRelease);
        if (log.isDebugEnabled()) {
            log.debug("Application release request is received for the application " + application.toString());
        }
        try {
            ConnectionManagerUtil.beginDBTransaction();
            Application existingApplication = this.applicationDAO.getApplicationById(applicationId, tenantId);
            if (existingApplication == null){
                throw new NotFoundException(
                        "Couldn't find application for the application Id: " + applicationId);
            }
            if (this.applicationReleaseDAO
                    .verifyReleaseExistenceByHash(applicationId, applicationRelease.getAppHashValue(), tenantId)) {
                throw new BadRequestException("Application release exists for the application Id: " + applicationId
                        + " and uploaded binary file");
            }
            String packageName = this.applicationReleaseDAO.getPackageName(applicationId, tenantId);
            if (packageName != null && !packageName.equals(applicationRelease.getPackageName())) {
                throw new BadRequestException(
                        "Package name in the payload is different from the existing package name of other application releases.");
            }
            applicationRelease = this.applicationReleaseDAO
                    .createRelease(applicationRelease, application.getId(), tenantId);
            LifecycleState lifecycleState = getLifecycleStateInstant(AppLifecycleState.CREATED.toString(),
                    AppLifecycleState.CREATED.toString());
            this.lifecycleStateDAO.addLifecycleState(lifecycleState, applicationId, applicationRelease.getUuid(), tenantId);
            ConnectionManagerUtil.commitDBTransaction();
            return applicationRelease;
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occurred while staring application release creating transaction for application Id: "
                            + applicationId, e);
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Error occurred while adding application release into IoTS app management Application id of the "
                            + "application release: " + applicationId, e);

        }  catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            //            todo throws when adding lifecycle state
            throw new ApplicationManagementException(
                    "Error occurred while adding application release into IoTS app management Application id of the "
                            + "application release: " + applicationId, e);
        }
    }

    @Override
    public String getUuidOfLatestRelease(int appId) throws ApplicationManagementException {
        try {
            ConnectionManagerUtil.openDBConnection();
            return applicationDAO.getUuidOfLatestRelease(appId);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }

    }

    @Override
    public Application getApplicationById(int appId, String state) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        boolean isAppAllowed = false;
        boolean isOpenConnection = false;
        List<ApplicationRelease> applicationReleases = null;
        try {
            if (state != null) {
                ConnectionManagerUtil.openDBConnection();
                isOpenConnection = true;
            }
            application = this.applicationDAO.getApplicationById(appId, tenantId);
            if (application == null) {
                throw new NotFoundException("Couldn't find an application for application Id: " + appId);
            }
            if (isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                applicationReleases = getReleases(application, state);
                application.setApplicationReleases(applicationReleases);
                return application;
            }

            if (!application.getUnrestrictedRoles().isEmpty()) {
                if (isRoleExists(application.getUnrestrictedRoles(), userName)) {
                    isAppAllowed = true;
                }
            } else {
                isAppAllowed = true;
            }

            if (!isAppAllowed) {
                return null;
            }
            applicationReleases = getReleases(application, state);
            application.setApplicationReleases(applicationReleases);
            return application;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the application id " + appId);
        } finally {
            if (isOpenConnection) {
                ConnectionManagerUtil.closeDBConnection();
            }
        }
    }

    @Override
    public Application getApplicationByUuid(String uuid, String state) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        boolean isAppAllowed = false;
        List<ApplicationRelease> applicationReleases;
        try {
            ConnectionManagerUtil.openDBConnection();
            application = this.applicationDAO.getApplicationByUUID(uuid, tenantId);
            if (application == null) {
                throw new NotFoundException("Couldn't find an application for application release UUID:: " + uuid);
            }
            if (isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                applicationReleases = getReleases(application, state);
                application.setApplicationReleases(applicationReleases);
                return application;
            }

            if (!application.getUnrestrictedRoles().isEmpty()) {
                if (isRoleExists(application.getUnrestrictedRoles(), userName)) {
                    isAppAllowed = true;
                }
            } else {
                isAppAllowed = true;
            }

            if (!isAppAllowed) {
                return null;
            }
            applicationReleases = getReleases(application, state);
            application.setApplicationReleases(applicationReleases);
            return application;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the application release UUID " + uuid);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private boolean isRoleExists(Collection<String> unrestrictedRoleList, String userName)
            throws UserStoreException {
        String[] roleList;
        roleList = getRolesOfUser(userName);
        for (String unrestrictedRole : unrestrictedRoleList) {
            for (String role : roleList) {
                if (unrestrictedRole.equals(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String[] getRolesOfUser(String userName) throws UserStoreException {
        UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        String[] roleList = {};
        if (userRealm != null) {
            roleList = userRealm.getUserStoreManager().getRoleListOfUser(userName);
        } else {
            log.error("role list is empty of user :" + userName);
        }
        return roleList;
    }

    public Application getApplication(String appType, String appName) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        boolean isAppAllowed = false;
        List<ApplicationRelease> applicationReleases;
        try {
            ConnectionManagerUtil.openDBConnection();
            application = this.applicationDAO.getApplication(appName, appType, tenantId);
            if (isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                applicationReleases = getReleases(application, null);
                application.setApplicationReleases(applicationReleases);
                return application;
            }

            if (!application.getUnrestrictedRoles().isEmpty()) {
                if (isRoleExists(application.getUnrestrictedRoles(), userName)) {
                    isAppAllowed = true;
                }
            } else {
                isAppAllowed = true;
            }

            if (!isAppAllowed) {
                return null;
            }

            applicationReleases = getReleases(application, null);
            application.setApplicationReleases(applicationReleases);
            return application;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the " + "application name " + appName);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public Application getApplicationByRelease(String appReleaseUUID) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        try {
            ConnectionManagerUtil.openDBConnection();
            application = this.applicationDAO.getApplicationByRelease(appReleaseUUID, tenantId);

            if (application.getUnrestrictedRoles().isEmpty() || isRoleExists(application.getUnrestrictedRoles(),
                                                                             userName)) {
                return application;
            }
            return null;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the application UUID " + appReleaseUUID);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    public boolean verifyApplicationExistenceById(int appId) throws ApplicationManagementException {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
            boolean isAppExist;
            ConnectionManagerUtil.openDBConnection();
            isAppExist = this.applicationDAO.verifyApplicationExistenceById(appId, tenantId);
            return isAppExist;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    public Boolean isUserAllowable(List<String> unrestrictedRoles, String userName)
            throws ApplicationManagementException {
        try {
            return isRoleExists(unrestrictedRoles, userName);
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while verifying whether user have assigned" + "unrestricted roles or not", e);
        }
    }

    private List<ApplicationRelease> getReleases(Application application, String releaseState)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        List<ApplicationRelease> applicationReleases;
        if (log.isDebugEnabled()) {
            log.debug("Request is received to retrieve all the releases related with the application " + application
                    .toString());
        }
        applicationReleases = this.applicationReleaseDAO.getReleases(application.getId(), tenantId);
        for (ApplicationRelease applicationRelease : applicationReleases) {
            LifecycleState lifecycleState = null;
            try {
                lifecycleState = this.lifecycleStateDAO.getLatestLifeCycleStateByReleaseID(applicationRelease.getId());
            } catch (LifeCycleManagementDAOException e) {
                throw new ApplicationManagementException(
                        "Error occurred while getting the latest lifecycle state for the application release UUID: "
                                + applicationRelease.getUuid(), e);
            }
            if (lifecycleState != null) {
                applicationRelease.setLifecycleState(lifecycleState);
            }
        }
        return filterAppReleaseByCurrentState(applicationReleases, releaseState);
    }

    private List<ApplicationRelease> filterAppReleaseByCurrentState(List<ApplicationRelease> applicationReleases,
            String state) {
        List<ApplicationRelease> filteredReleases = new ArrayList<>();

        if (state != null && !state.isEmpty()) {
            for (ApplicationRelease applicationRelease : applicationReleases) {
                if (state.equals(applicationRelease.getLifecycleState().getCurrentState())) {
                    filteredReleases.add(applicationRelease);
                }
            }

            if (AppLifecycleState.PUBLISHED.toString()
                    .equals(state) && filteredReleases.size() > 1) {
                log.warn("There are more than one application releases is found which is in PUBLISHED state");
                filteredReleases.sort((r1, r2) -> {
                    if (r1.getLifecycleState().getUpdatedAt().after(r2.getLifecycleState().getUpdatedAt())) {
                        return -1;
                    } else if (r2.getLifecycleState().getUpdatedAt().after(r1.getLifecycleState().getUpdatedAt())) {
                        return 1;
                    }
                    return 0;
                });
            }
            return filteredReleases;
        }
        return applicationReleases;
    }

    @Override
    public List<String> deleteApplication(int applicationId) throws ApplicationManagementException {
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        List<String> storedLocations = new ArrayList<>();
        Application application;

        try {
            if (!isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                throw new ApplicationManagementException(
                        "You don't have permission to delete this application. In order to delete an application you "
                                + "need to have admin permission");
            }
            ConnectionManagerUtil.beginDBTransaction();
            application = getApplicationIfAccessible(applicationId);
            if (application == null) {
                throw new ApplicationManagementException("Invalid Application");
            }
            List<ApplicationRelease> applicationReleases = getReleases(application, null);
            if (log.isDebugEnabled()) {
                log.debug("Request is received to delete applications which are related with the application id "
                        + applicationId);
            }
            for (ApplicationRelease applicationRelease : applicationReleases) {
                LifecycleState appLifecycleState = getLifecycleState(applicationId, applicationRelease.getUuid());
                LifecycleState newAppLifecycleState = getLifecycleStateInstant(AppLifecycleState.REMOVED.toString(),
                        appLifecycleState.getCurrentState());
                if (lifecycleStateManger.isValidStateChange(newAppLifecycleState.getPreviousState(),
                        newAppLifecycleState.getCurrentState(),userName,tenantId)) {
                    this.lifecycleStateDAO
                            .addLifecycleState(newAppLifecycleState, applicationId, applicationRelease.getUuid(),
                                    tenantId);
                } else {
                    //                    todo move to appropriate lifecycle changing flow and end by remving release
                }

                storedLocations.add(applicationRelease.getAppHashValue());
            }
            this.applicationDAO.deleteApplication(applicationId);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (UserStoreException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occured while check whether current user has the permission to delete an application";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            //            todo
            String msg = "Error occured while check whether current user has the permission to delete an application";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        }
        return storedLocations;
    }

    @Override
    public String deleteApplicationRelease(int applicationId, String releaseUuid, boolean handleConnections)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        Application application = getApplicationIfAccessible(applicationId);
        if (application == null) {
            throw new ApplicationManagementException("Invalid Application ID is received");
        }
        try {
            ConnectionManagerUtil.beginDBTransaction();
            ApplicationRelease applicationRelease = getAppReleaseIfExists(applicationId, releaseUuid);
            LifecycleState appLifecycleState = getLifecycleState(applicationId, applicationRelease.getUuid());
            String currentState = appLifecycleState.getCurrentState();
            if (AppLifecycleState.DEPRECATED.toString().equals(currentState) || AppLifecycleState
                    .REJECTED.toString().equals(currentState) || AppLifecycleState.UNPUBLISHED.toString().equals
                    (currentState)) {
                LifecycleState newAppLifecycleState = getLifecycleStateInstant(AppLifecycleState.REMOVED.toString(),
                        appLifecycleState.getCurrentState());
                String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
                if (lifecycleStateManger.isValidStateChange(newAppLifecycleState.getPreviousState(),
                        newAppLifecycleState.getCurrentState(),userName,tenantId)) {
                    this.lifecycleStateDAO
                            .addLifecycleState(newAppLifecycleState, applicationId, applicationRelease.getUuid(),
                                    tenantId);
                    ConnectionManagerUtil.commitDBTransaction();
                } else {
//                    todo
                    ConnectionManagerUtil.rollbackDBTransaction();
                    throw new ApplicationManagementException("Lifecycle State Validation failed. Application Id: " +
                            applicationId + " Application release UUID: "  + releaseUuid);                }
            } else {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new ApplicationManagementException("Can't delete the application release, You have to move the " +
                        "lifecycle state from " + currentState + " to acceptable " +
                        "state");
            }
            return applicationRelease.getAppHashValue();
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
//            todo
            throw new ApplicationManagementException("Can't delete the application release, You have to move the " +
                    "lifecycle state from " + "" + " to acceptable " +
                    "state");
        }
    }

    /**
     * To check whether current user has the permission to do some secured operation.
     *
     * @param username   Name of the User.
     * @param tenantId   ID of the tenant.
     * @param permission Permission that need to be checked.
     * @return true if the current user has the permission, otherwise false.
     * @throws UserStoreException UserStoreException
     */
    private boolean isAdminUser(String username, int tenantId, String permission) throws UserStoreException {
        UserRealm userRealm = DataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
        return userRealm != null && userRealm.getAuthorizationManager() != null && userRealm.getAuthorizationManager()
                .isUserAuthorized(MultitenantUtils.getTenantAwareUsername(username), permission,
                                  CarbonConstants.UI_PERMISSION_ACTION);
    }

    /**
     * To validate the application creating request
     *
     * @param application Application that need to be created
     * @throws RequestValidatingException Validation Exception
     */
    private void validateAppCreatingRequest(Application application, int tenantId) throws RequestValidatingException {

        Boolean isValidApplicationType;
        Filter filter = new Filter();
        try {
            filter.setFullMatch(true);
            filter.setAppName(application.getName().trim());
            filter.setOffset(0);
            filter.setLimit(1);
            if (application.getName() == null) {
                throw new RequestValidatingException("Application name cannot be empty");
            }
            if (application.getUser() == null || application.getUser().getUserName() == null
                    || application.getUser().getTenantId() == -1) {
                throw new RequestValidatingException("Username and tenant Id cannot be empty");
            }
            if (application.getAppCategory() == null) {
                throw new RequestValidatingException("Application category can't be empty");
            }

            isValidApplicationType = isValidAppType(application.getType());

            if (!isValidApplicationType) {
                throw new RequestValidatingException(
                        "App Type contains in the application creating payload doesn't match with supported app types");
            }

            if (application.getApplicationReleases().size() > 1 ){
                throw new RequestValidatingException(
                        "Invalid payload. Application creating payload should contains one application release, but "
                                + "the payload contains more than one");
            }

            //Check whether application is already existing one or not
            ConnectionManagerUtil.openDBConnection();
            ApplicationList applicationList = applicationDAO.getApplications(filter, tenantId);
            if (applicationList != null && applicationList.getApplications() != null && !applicationList
                    .getApplications().isEmpty()) {
                throw new RequestValidatingException(
                        "Already an application registered with same name - " + applicationList.getApplications().get(0)
                                .getName());
            }
        } catch (ApplicationManagementDAOException e) {
            throw new RequestValidatingException(
                    "Error occured while getting existing applications for application name: " + application.getName()
                            + " and application type " + application.getType() + ". Tenant ID is " + tenantId, e);
        } catch (DBConnectionException e) {
            throw new RequestValidatingException(
                    "Error occured while getting database connection to get existing applications for application name: "
                            + application.getName() + " and application type: " + application.getType()
                            + ". Tenant id is " + tenantId, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /***
     * To verify whether application type is valid one or not
     * @param appType application type {@link ApplicationType}
     * @return true returns if appType is valid on, otherwise returns false
     */
    private Boolean isValidAppType(String appType) {
        if (appType == null) {
            return false;
        }
        for (ApplicationType applicationType : ApplicationType.values()) {
            if (applicationType.toString().equals(appType)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Get the application if application is an accessible one.
     *
     * @param applicationId ID of the Application.
     * @return Application related with the UUID
     */
    public Application getApplicationIfAccessible(int applicationId) throws ApplicationManagementException {
        if (applicationId <= 0) {
            throw new ApplicationManagementException("Application id could,t be a negative integer. Hence please add " +
                                                             "valid application id.");
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Application application;
        boolean isAppAllowed = false;
        try {
            application = this.applicationDAO.getApplicationById(applicationId, tenantId);
            if (isAdminUser(userName, tenantId, CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION)) {
                return application;
            }

            if (application != null && !application.getUnrestrictedRoles().isEmpty()) {
                if (isRoleExists(application.getUnrestrictedRoles(), userName)) {
                    isAppAllowed = true;
                }
            } else {
                isAppAllowed = true;
            }

            if (!isAppAllowed) {
                throw new NotFoundException("Application of the " + applicationId
                        + " does not exist. Please check whether user have permissions to access the application.");
            }
            return application;
        } catch (UserStoreException e) {
            throw new ApplicationManagementException(
                    "User-store exception while getting application with the " + "application id " + applicationId, e);
        }
    }

    /**
     * Get the application release for given UUID if application release is exists and application id is valid one.
     *
     * @param applicationUuid UUID of the Application.
     * @return Application related with the UUID
     */
    private ApplicationRelease getAppReleaseIfExists(int applicationId, String applicationUuid) throws
                                                                                                    ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        ApplicationRelease applicationRelease;

        if (applicationId <= 0) {
            throw new ApplicationManagementException(
                    "Application id could,t be a negative integer. Hence please add " +
                            "valid application id.");
        }
        if (applicationUuid == null) {
            throw new ApplicationManagementException("Application UUID is null. Application UUID is a required "
                    + "parameter to get the relevant application.");
        }
        applicationRelease = this.applicationReleaseDAO.getReleaseByIds(applicationId, applicationUuid, tenantId);
        if (applicationRelease == null) {
            log.error("Doesn't exist a application release for application ID:  " + applicationId
                    + "and application UUID: " + applicationUuid);
        }
        return applicationRelease;

    }

    private ApplicationRelease updateRelease(int appId, ApplicationRelease applicationRelease) throws
                                                                                              ApplicationManagementException {
        validateAppReleasePayload(applicationRelease);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        if (log.isDebugEnabled()) {
            log.debug("Updating the Application release. UUID: " + applicationRelease.getUuid() + ", " +
                              "Application Id: " + appId);
        }

        applicationRelease = this.applicationReleaseDAO.updateRelease(appId, applicationRelease, tenantId);
        return applicationRelease;

    }

    @Override
    public ApplicationRelease updateApplicationImageArtifact(int appId, String uuid, InputStream iconFileStream, InputStream
            bannerFileStream, List<InputStream> attachments)
            throws ApplicationManagementException, ResourceManagementException {
        ApplicationStorageManager applicationStorageManager = Util.getApplicationStorageManager();
        ApplicationRelease applicationRelease;
        try {
            ConnectionManagerUtil.getDBConnection();
            applicationRelease = getAppReleaseIfExists(appId, uuid);
            if (applicationRelease == null) {
                throw new NotFoundException("No App release associated with the app Id " + appId + "and UUID "+ uuid);
            }
            LifecycleState lifecycleState = getLifecycleState(appId, applicationRelease.getUuid());
            if (AppLifecycleState.PUBLISHED.toString().equals(lifecycleState.getCurrentState()) ||
                    AppLifecycleState.DEPRECATED.toString().equals(lifecycleState.getCurrentState())) {
                throw new ForbiddenException("Can't Update the application release in " +
                        "PUBLISHED or DEPRECATED state. Hence please demote the application and update " +
                        "the application release");
            }
            ApplicationRelease updatedRelease = applicationStorageManager
                    .updateImageArtifacts(applicationRelease, iconFileStream, bannerFileStream, attachments);
            return updateRelease(appId, updatedRelease);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public ApplicationRelease updateApplicationArtifact(int appId, String uuid, InputStream binaryFile)
            throws ApplicationManagementException, ResourceManagementException, RequestValidatingException, DeviceManagementException {
        ApplicationStorageManager applicationStorageManager = Util.getApplicationStorageManager();
        ApplicationRelease applicationRelease;
        try {
            ConnectionManagerUtil.openDBConnection();
            applicationRelease = getAppReleaseIfExists(appId, uuid);
            Application application = getApplicationById(appId, null);

            List<DeviceType> deviceTypes = Util.getDeviceManagementService().getDeviceTypes();
            for (DeviceType deviceType:deviceTypes) {
                if (deviceType.getId() == application.getDeviceTypeId()) {
                    application.setDeviceType(deviceType.getName());
                }
            }
            if (applicationRelease == null) {
                throw new NotFoundException("No App release associated with the app Id " + appId + "and UUID "+ uuid);
            }
            applicationStorageManager
                    .updateReleaseArtifacts(applicationRelease, application.getType(), application.getDeviceType(),
                            binaryFile);
            updateRelease(appId, applicationRelease);
            return applicationRelease;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public boolean isAcceptableAppReleaseUpdate(int appId, String appReleaseUuid)
            throws ApplicationManagementException {
        LifecycleState lifecycleState = getLifecycleState(appId, appReleaseUuid);
        return AppLifecycleState.CREATED.toString().equals(lifecycleState.getCurrentState()) || AppLifecycleState
                .IN_REVIEW.toString().equals(lifecycleState.getCurrentState()) ||
                AppLifecycleState.REJECTED.toString().equals(lifecycleState.getCurrentState());
    }

    /**
     * To get role restricted application list.
     *
     * @param applicationList list of applications.
     * @param userName        user name
     * @return Application related with the UUID
     */
    private ApplicationList getRoleRestrictedApplicationList(ApplicationList applicationList, String userName)
            throws ApplicationManagementException {
        ApplicationList roleRestrictedApplicationList = new ApplicationList();
        ArrayList<Application> unRestrictedApplications = new ArrayList<>();
        for (Application application : applicationList.getApplications()) {
            if (application.getUnrestrictedRoles().isEmpty()) {
                unRestrictedApplications.add(application);
            } else {
                try {
                    if (isRoleExists(application.getUnrestrictedRoles(), userName)) {
                        unRestrictedApplications.add(application);
                    }
                } catch (UserStoreException e) {
                    throw new ApplicationManagementException("Role restriction verifying is failed");
                }
            }
        }
        roleRestrictedApplicationList.setApplications(unRestrictedApplications);
        return roleRestrictedApplicationList;
    }

    /**
     * To validate a app release creating request and app updating request to make sure all the pre-conditions satisfied.
     *
     * @param applicationRelease ApplicationRelease that need to be created.
     * @throws ApplicationManagementException Application Management Exception.
     */
    private void validateAppReleasePayload(ApplicationRelease applicationRelease)
            throws ApplicationManagementException {
        if (applicationRelease.getVersion() == null) {
            throw new ApplicationManagementException("ApplicationRelease version name is a mandatory parameter for "
                                                             + "creating release. It cannot be found.");
        }
    }

    @Override
    public LifecycleState getLifecycleState(int applicationId, String releaseUuid) throws
                                                                                       ApplicationManagementException {
        LifecycleState lifecycleState;
        try {
            ConnectionManagerUtil.openDBConnection();
            lifecycleState = this.lifecycleStateDAO.getLatestLifeCycleState(applicationId, releaseUuid);
            if (lifecycleState == null) {
                throw new NotFoundException(
                        "Couldn't find the lifecycle data for appid: " + applicationId + " and app release UUID: "
                                + releaseUuid);

            }
            lifecycleState.setNextStates(new ArrayList<>(lifecycleStateManger.getNextLifecycleStates(lifecycleState.getCurrentState())));

        } catch (ApplicationManagementException e) {
            throw new ApplicationManagementException("Failed to get application and application management", e);
        } catch (LifeCycleManagementDAOException e) {
            throw new ApplicationManagementException("Failed to get lifecycle state from database", e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
        return lifecycleState;
    }

    @Override
    public void changeLifecycleState(int applicationId, String releaseUuid, LifecycleState state)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            if (!this.applicationDAO.verifyApplicationExistenceById(applicationId, tenantId)) {
                throw new NotFoundException("Couldn't find application for the application Id: " + applicationId);
            }
            if (!this.applicationReleaseDAO.verifyReleaseExistence(applicationId, releaseUuid, tenantId)) {
                throw new NotFoundException("Couldn't find application release for the application Id: " + applicationId
                        + " application release uuid: " + releaseUuid);
            }
            LifecycleState currentState = this.lifecycleStateDAO.getLatestLifeCycleState(applicationId, releaseUuid);
            if (currentState == null) {
                throw new ApplicationManagementException(
                        "Couldn't find latest lifecycle state for the appId: " + applicationId
                                + " and application release UUID: " + releaseUuid);
            }
            state.setPreviousState(currentState.getCurrentState());
            String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            state.setUpdatedBy(userName);

            if (state.getCurrentState() != null && state.getPreviousState() != null) {
                if (lifecycleStateManger.isValidStateChange(state.getPreviousState(), state.getCurrentState(),userName,
                        tenantId)) {
                    //todo if current state of the adding lifecycle state is PUBLISHED, need to check whether is there
                    //todo any other application release in PUBLISHED state for the application( i.e for the appid)
                    this.lifecycleStateDAO.addLifecycleState(state, applicationId, releaseUuid, tenantId);
                    ConnectionManagerUtil.commitDBTransaction();
                } else {
                    ConnectionManagerUtil.rollbackDBTransaction();
                    log.error("Invalid lifecycle state transition from '" + state.getPreviousState() + "'" + " to '"
                            + state.getCurrentState() + "'");
                    throw new ApplicationManagementException(
                            "Lifecycle State Validation failed. Application Id: " + applicationId
                                    + " Application release UUID: " + releaseUuid);
                }
            }
        } catch (LifeCycleManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationManagementException(
                    "Failed to add lifecycle state. Application Id: " + applicationId + " Application release UUID: "
                            + releaseUuid, e);
        }
    }

    @Override
    public Application updateApplication(Application application) throws ApplicationManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        Application existingApplication = getApplicationIfAccessible(application.getId());
        List<String> addingRoleList;
        List<String> removingRoleList;
        List<String> addingTags;
        List<String> removingTags;


        if (existingApplication == null) {
            throw new NotFoundException("Tried to update Application which is not in the publisher, " +
                                                "Please verify application details");
        }
        if (!existingApplication.getType().equals(application.getType())) {
            throw new ApplicationManagementException("You are trying to change the application type and it is not " +
                                                             "possible after you create an application. Therefore " +
                                                             "please remove this application and publish " +
                                                             "new application with type: " + application.getType());
        }
        if (!existingApplication.getSubType().equals(application.getSubType())) {
            if (ApplicationSubscriptionType.PAID.toString().equals(existingApplication.getSubType()) && (
                    !"".equals(application.getPaymentCurrency()) || application.getPaymentCurrency() != null)) {
                throw new ApplicationManagementException("If you are going to change Non-Free app as Free app, "
                        + "currency attribute in the application updating " + "payload should be null or \"\"");
            } else if (ApplicationSubscriptionType.FREE.toString().equals(existingApplication.getSubType()) && (
                    application.getPaymentCurrency() == null || "".equals(application.getPaymentCurrency()))) {
                throw new ApplicationManagementException("If you are going to change Free app as Non-Free app, "
                        + "currency attribute in the application payload " + "should not be null or \"\"");
            }
        }
        if (existingApplication.getIsRestricted() != application.getIsRestricted()) {
            if (!existingApplication.getIsRestricted() && existingApplication.getUnrestrictedRoles() == null) {
                if (application.getUnrestrictedRoles() == null || application.getUnrestrictedRoles().isEmpty()) {
                    throw new ApplicationManagementException("If you are going to add role restriction for non role "
                            + "restricted Application, Unrestricted role list " + "won't be empty or null");
                }
                visibilityDAO.addUnrestrictedRoles(application.getUnrestrictedRoles(), application.getId(), tenantId);
            } else if (existingApplication.getIsRestricted() && existingApplication.getUnrestrictedRoles() != null) {
                if (application.getUnrestrictedRoles() != null && !application.getUnrestrictedRoles().isEmpty()) {
                    throw new ApplicationManagementException("If you are going to remove role restriction from role "
                            + "restricted Application, Unrestricted role list should be empty or null");
                }
                visibilityDAO.deleteUnrestrictedRoles(existingApplication.getUnrestrictedRoles(), application.getId(),
                        tenantId);
            }
        } else if (existingApplication.getIsRestricted() == application.getIsRestricted()
                && existingApplication.getIsRestricted()) {
            addingRoleList = getDifference(application.getUnrestrictedRoles(),
                    existingApplication.getUnrestrictedRoles());
            removingRoleList = getDifference(existingApplication.getUnrestrictedRoles(),
                    application.getUnrestrictedRoles());
            if (!addingRoleList.isEmpty()) {
                visibilityDAO.addUnrestrictedRoles(addingRoleList, application.getId(), tenantId);

            }
            if (!removingRoleList.isEmpty()) {
                visibilityDAO.deleteUnrestrictedRoles(removingRoleList, application.getId(), tenantId);
            }
        }
        addingTags = getDifference(existingApplication.getTags(), application.getTags());
        removingTags = getDifference(application.getTags(), existingApplication.getTags());
        if (!addingTags.isEmpty()) {
            applicationDAO.addTags(addingTags, application.getId(), tenantId);
        }
        if (!removingTags.isEmpty()) {
            applicationDAO.deleteTags(removingTags, application.getId(), tenantId);
        }

        return applicationDAO.editApplication(application, tenantId);
    }

    private Filter validateFilter(Filter filter) {
        if (filter != null && filter.getAppType() != null) {
            boolean isValidRequest = false;
            for (ApplicationType applicationType : ApplicationType.values()) {
                if (applicationType.toString().equals(filter.getAppType())) {
                    isValidRequest = true;
                    break;
                }
            }
            if (!isValidRequest) {
                return null;
            }
        }
        return filter;
    }

    private <T> List<T> getDifference(List<T> list1, Collection<T> list2) {
        List<T> list = new ArrayList<>();
        for (T t : list1) {
            if(!list2.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }

    /***
     * By invoking the method, it returns Lifecycle State Instance.
     * @param currentState Current state of the lifecycle
     * @param previousState Previouse state of the Lifecycle
     * @return {@link LifecycleState}
     */
    private LifecycleState getLifecycleStateInstant(String currentState, String previousState) {
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        LifecycleState lifecycleState = new LifecycleState();
        lifecycleState.setCurrentState(currentState);
        lifecycleState.setPreviousState(previousState);
        lifecycleState.setUpdatedBy(userName);
        return lifecycleState;
    }
}
