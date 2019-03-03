/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.application.mgt.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.api.services.AppMgtAPI;
import org.wso2.carbon.device.application.mgt.common.config.UIConfiguration;
import org.wso2.carbon.device.application.mgt.common.services.ConfigManager;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Implementation of Application Management related APIs.
 */
@Produces({"application/json"})
@Path("/app-mgt")
public class AppMgtAPIImpl implements AppMgtAPI {

    private static Log log = LogFactory.getLog(AppMgtAPIImpl.class);

    @GET
    @Override
    @Consumes("application/json")
    @Path("/ui-config")
    public Response getUiConfig() {
        ConfigManager configManager = APIUtil.getConfigManager();
        try {
            UIConfiguration uiConfiguration = configManager.getUIConfiguration();
            return Response.status(Response.Status.OK).entity(uiConfiguration).build();

        }catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting the application list for publisher ";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}
