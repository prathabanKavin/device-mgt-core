/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.subtype.mgt.internal;

import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceManagementConfig;
import io.entgra.device.mgt.core.device.mgt.core.config.datasource.DataSourceConfig;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.subtype.mgt.dao.DeviceSubTypeDAOFactory;
import io.entgra.device.mgt.core.subtype.mgt.impl.DeviceSubTypeServiceImpl;
import io.entgra.device.mgt.core.subtype.mgt.spi.DeviceSubTypeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.registry.core.service.RegistryService;

@Component(
        name = "io.entgra.device.mgt.core.subtype.mgt.internal.DeviceSubTypeMgtServiceComponent",
        immediate = true)
public class DeviceSubTypeMgtServiceComponent {

    private static final Log log = LogFactory.getLog(DeviceSubTypeMgtServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Activating Device SubType Management Service Component");
        }
        try {
            BundleContext bundleContext = componentContext.getBundleContext();

            DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
            DataSourceConfig dsConfig = config.getDeviceManagementConfigRepository().getDataSourceConfig();
            DeviceSubTypeDAOFactory.init(dsConfig);

            DeviceSubTypeService deviceSubTypeService = new DeviceSubTypeServiceImpl();
            DeviceSubTypeMgtDataHolder.getInstance().setDeviceSubTypeService(deviceSubTypeService);
            bundleContext.registerService(DeviceSubTypeService.class, deviceSubTypeService, null);

            if (log.isDebugEnabled()) {
                log.debug("Device SubType Management Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Device SubType Management Service Component", e);
        }
    }
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Device SubType Management Service Component");
        }
    }

    @SuppressWarnings("unused")
    @Reference(
            name = "data.mgt.provider.service",
            service = io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDeviceManagementService")
    protected void setDeviceManagementService(DeviceManagementProviderService deviceManagementProviderService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Device Management Service to Device SubType Mgt SC");
        }
        DeviceSubTypeMgtDataHolder.getInstance().setDeviceManagementProviderService(deviceManagementProviderService);
    }

    @SuppressWarnings("unused")
    protected void unsetDeviceManagementService(DeviceManagementProviderService deviceManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing Device Management Service from Device SubType Mgt SC");
        }
        DeviceSubTypeMgtDataHolder.getInstance().setDeviceManagementProviderService(null);
    }

    @Reference(
            name = "datasource.service",
            service = org.wso2.carbon.ndatasource.core.DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDataSourceService")
    protected void setDataSourceService(DataSourceService dataSourceService) {
        /* This is to avoid mobile device management component getting initialized before the underlying datasources
        are registered */
        if (log.isDebugEnabled()) {
            log.debug("Data source service set to Device SubType Mgt component");
        }
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        //do nothing
        if (log.isDebugEnabled()) {
            log.debug("Removing Data Source service from Device SubType Mgt component");
        }
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService set to Device SubType Mgt component");
        }
        DeviceSubTypeMgtDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing RegistryService from Device SubType Mgt component");
        }
        DeviceSubTypeMgtDataHolder.getInstance().setRegistryService(null);
    }


}
