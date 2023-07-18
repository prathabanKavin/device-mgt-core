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
package io.entgra.device.mgt.core.apimgt.application.extension.internal;

import io.entgra.device.mgt.core.apimgt.application.extension.APIManagementProviderService;
import io.entgra.device.mgt.core.identity.jwt.client.extension.service.JWTClientManagerService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.Hashtable;

public class APIApplicationManagerExtensionDataHolder {
    private static APIApplicationManagerExtensionDataHolder thisInstance = new APIApplicationManagerExtensionDataHolder();
    private APIManagementProviderService apiManagementProviderService;
    private RealmService realmService;
    private TenantManager tenantManager;
    private TenantRegistryLoader tenantRegistryLoader;
    private TenantIndexingLoader indexLoader;
    private JWTClientManagerService jwtClientManagerService;

    private APIApplicationManagerExtensionDataHolder() {
    }


    public static APIApplicationManagerExtensionDataHolder getInstance() {
        return thisInstance;
    }

    public APIManagementProviderService getAPIManagementProviderService() {
        return apiManagementProviderService;
    }

    public void setAPIManagementProviderService(
            APIManagementProviderService apiManagementProviderService) {
        this.apiManagementProviderService = apiManagementProviderService;
    }

    public RealmService getRealmService() {
        if (realmService == null) {
            throw new IllegalStateException("Realm service is not initialized properly");
        }
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
        setTenantManager(realmService != null ?
                realmService.getTenantManager() : null);
    }

    private void setTenantManager(TenantManager tenantManager) {
        this.tenantManager = tenantManager;
    }

    public TenantManager getTenantManager() {
        if (tenantManager == null) {
            throw new IllegalStateException("Tenant manager is not initialized properly");
        }
        return tenantManager;
    }

    public void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader){
        this.tenantRegistryLoader = tenantRegistryLoader;
    }

    public TenantRegistryLoader getTenantRegistryLoader(){
        return tenantRegistryLoader;
    }

    public void setIndexLoaderService(TenantIndexingLoader indexLoader) {
        this.indexLoader = indexLoader;
    }

    public TenantIndexingLoader getIndexLoaderService(){
        return indexLoader;
    }

    public JWTClientManagerService getJwtClientManagerService() {
        if (jwtClientManagerService == null) {
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            jwtClientManagerService = (JWTClientManagerService)ctx.getOSGiService(JWTClientManagerService.class, (Hashtable)null);
        }
        return jwtClientManagerService;
    }

    public void setJwtClientManagerService(JWTClientManagerService jwtClientManagerService) {
        this.jwtClientManagerService = jwtClientManagerService;
    }
}
