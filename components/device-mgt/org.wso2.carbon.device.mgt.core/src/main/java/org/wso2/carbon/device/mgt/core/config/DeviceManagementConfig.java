/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.config;

import org.wso2.carbon.device.mgt.common.enrollment.notification.EnrollmentNotificationConfiguration;
import org.wso2.carbon.device.mgt.common.roles.config.DefaultRoles;
import org.wso2.carbon.device.mgt.core.config.analytics.OperationAnalyticsConfiguration;
import org.wso2.carbon.device.mgt.core.config.archival.ArchivalConfiguration;
import org.wso2.carbon.device.mgt.core.config.cache.CertificateCacheConfiguration;
import org.wso2.carbon.device.mgt.core.config.cache.DeviceCacheConfiguration;
import org.wso2.carbon.device.mgt.core.config.geo.location.GeoLocationConfiguration;
import org.wso2.carbon.device.mgt.core.config.identity.IdentityConfigurations;
import org.wso2.carbon.device.mgt.core.config.keymanager.KeyManagerConfigurations;
import org.wso2.carbon.device.mgt.core.config.pagination.PaginationConfiguration;
import org.wso2.carbon.device.mgt.core.config.policy.PolicyConfiguration;
import org.wso2.carbon.device.mgt.core.config.pull.notification.PullNotificationConfiguration;
import org.wso2.carbon.device.mgt.core.config.push.notification.PushNotificationConfiguration;
import org.wso2.carbon.device.mgt.core.config.remote.session.RemoteSessionConfiguration;
import org.wso2.carbon.device.mgt.core.config.status.task.DeviceStatusTaskConfig;
import org.wso2.carbon.device.mgt.core.config.task.TaskConfiguration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents Device Mgt configuration.
 */
@XmlRootElement(name = "DeviceMgtConfiguration")
@SuppressWarnings("unused")
public final class DeviceManagementConfig {

    private DeviceManagementConfigRepository deviceManagementConfigRepository;
    private TaskConfiguration taskConfiguration;
    private IdentityConfigurations identityConfigurations;
    private KeyManagerConfigurations keyManagerConfigurations;
    private PolicyConfiguration policyConfiguration;
    private PaginationConfiguration paginationConfiguration;
    private PushNotificationConfiguration pushNotificationConfiguration;
    private PullNotificationConfiguration pullNotificationConfiguration;
    private DeviceStatusTaskConfig deviceStatusTaskConfig;
    private DeviceCacheConfiguration deviceCacheConfiguration;
    private CertificateCacheConfiguration certificateCacheConfiguration;
    private OperationAnalyticsConfiguration operationAnalyticsConfiguration;
    private GeoLocationConfiguration geoLocationConfiguration;
    private String defaultGroupsConfiguration;
    private RemoteSessionConfiguration remoteSessionConfiguration;
    private ArchivalConfiguration archivalConfiguration;
    private EnrollmentNotificationConfiguration enrollmentNotificationConfiguration;
    private DefaultRoles defaultRoles;

    @XmlElement(name = "ManagementRepository", required = true)
    public DeviceManagementConfigRepository getDeviceManagementConfigRepository() {
        return deviceManagementConfigRepository;
    }

    public void setDeviceManagementConfigRepository(DeviceManagementConfigRepository deviceManagementConfigRepository) {
        this.deviceManagementConfigRepository = deviceManagementConfigRepository;
    }

    @XmlElement(name = "IdentityConfiguration", required = true)
    public IdentityConfigurations getIdentityConfigurations() {
        return identityConfigurations;
    }


    public void setIdentityConfigurations(IdentityConfigurations identityConfigurations) {
        this.identityConfigurations = identityConfigurations;
    }

    @XmlElement(name = "KeyManagerConfiguration", required = true)
    public KeyManagerConfigurations getKeyManagerConfigurations() {
        return keyManagerConfigurations;
    }

    public void setKeyManagerConfigurations(KeyManagerConfigurations keyManagerConfigurations) {
        this.keyManagerConfigurations = keyManagerConfigurations;
    }

    @XmlElement(name = "PolicyConfiguration", required = true)
    public PolicyConfiguration getPolicyConfiguration() {
        return policyConfiguration;
    }

    public void setPolicyConfiguration(PolicyConfiguration policyConfiguration) {
        this.policyConfiguration = policyConfiguration;
    }

    @XmlElement(name = "TaskConfiguration", required = true)
    public TaskConfiguration getTaskConfiguration() {
        return taskConfiguration;
    }

    public void setTaskConfiguration(TaskConfiguration taskConfiguration) {
        this.taskConfiguration = taskConfiguration;
    }

    @XmlElement(name = "PaginationConfiguration", required = true)
    public PaginationConfiguration getPaginationConfiguration() {
        return paginationConfiguration;
    }

    public void setPaginationConfiguration(PaginationConfiguration paginationConfiguration) {
        this.paginationConfiguration = paginationConfiguration;
    }

    @XmlElement(name = "PushNotificationConfiguration", required = true)
    public PushNotificationConfiguration getPushNotificationConfiguration() {
        return pushNotificationConfiguration;
    }

    public void setPushNotificationConfiguration(PushNotificationConfiguration pushNotificationConfiguration) {
        this.pushNotificationConfiguration = pushNotificationConfiguration;
    }

    @XmlElement(name = "PullNotificationConfiguration", required = true)
    public PullNotificationConfiguration getPullNotificationConfiguration() {
        return pullNotificationConfiguration;
    }

    public void setPullNotificationConfiguration(PullNotificationConfiguration pullNotificationConfiguration) {
        this.pullNotificationConfiguration = pullNotificationConfiguration;
    }

    @XmlElement(name = "DeviceStatusTaskConfig", required = true)
    public DeviceStatusTaskConfig getDeviceStatusTaskConfig() {
        return deviceStatusTaskConfig;
    }

    public void setDeviceStatusTaskConfig(DeviceStatusTaskConfig deviceStatusTaskConfig) {
        this.deviceStatusTaskConfig = deviceStatusTaskConfig;
    }

    @XmlElement(name = "DeviceCacheConfiguration", required = true)
    public DeviceCacheConfiguration getDeviceCacheConfiguration() {
        return deviceCacheConfiguration;
    }

    public void setDeviceCacheConfiguration(DeviceCacheConfiguration deviceCacheConfiguration) {
        this.deviceCacheConfiguration = deviceCacheConfiguration;
    }

    @XmlElement(name = "CertificateCacheConfiguration", required = true)
    public CertificateCacheConfiguration getCertificateCacheConfiguration() {
        return certificateCacheConfiguration;
    }

    public void setCertificateCacheConfiguration(CertificateCacheConfiguration certificateCacheConfiguration) {
        this.certificateCacheConfiguration = certificateCacheConfiguration;
    }

    @XmlElement(name = "OperationAnalyticsConfiguration", required = true)
    public OperationAnalyticsConfiguration getOperationAnalyticsConfiguration() {
        return operationAnalyticsConfiguration;
    }

    public void setOperationAnalyticsConfiguration(OperationAnalyticsConfiguration operationAnalyticsConfiguration) {
        this.operationAnalyticsConfiguration = operationAnalyticsConfiguration;
    }

    @XmlElement(name = "GeoLocationConfiguration", required = true)
    public GeoLocationConfiguration getGeoLocationConfiguration() {
        return geoLocationConfiguration;
    }

    public void setGeoLocationConfiguration(GeoLocationConfiguration geoLocationConfiguration) {
        this.geoLocationConfiguration = geoLocationConfiguration;
    }

    @XmlElement(name = "DefaultGroupsConfiguration", required = true)
    public String getDefaultGroupsConfiguration() {
        return defaultGroupsConfiguration;
    }

    public void setDefaultGroupsConfiguration(String defaultGroupsConfiguration) {
        this.defaultGroupsConfiguration = defaultGroupsConfiguration;
    }

    @XmlElement(name = "ArchivalConfiguration", required = true)
    public ArchivalConfiguration getArchivalConfiguration() {
        return archivalConfiguration;
    }

    public void setArchivalConfiguration(ArchivalConfiguration archivalConfiguration) {
        this.archivalConfiguration = archivalConfiguration;
    }
    @XmlElement(name = "RemoteSessionConfiguration", required = true)
    public RemoteSessionConfiguration getRemoteSessionConfiguration() {
        return remoteSessionConfiguration;
    }

    public void setRemoteSessionConfiguration(RemoteSessionConfiguration remoteSessionConfiguration) {
        this.remoteSessionConfiguration = remoteSessionConfiguration;
    }

    @XmlElement(name = "EnrolmentNotificationConfiguration", required = true)
    public EnrollmentNotificationConfiguration getEnrollmentNotificationConfiguration() {
        return enrollmentNotificationConfiguration;
    }

    public void setEnrollmentNotificationConfiguration(
            EnrollmentNotificationConfiguration enrollmentNotificationConfiguration) {
        this.enrollmentNotificationConfiguration = enrollmentNotificationConfiguration;
    }

    @XmlElement(name = "DefaultRoles", required = true)
    public DefaultRoles getDefaultRoles() { return defaultRoles; }

    public void setDefaultRoles(DefaultRoles defaultRoles) { this.defaultRoles = defaultRoles; }
}

