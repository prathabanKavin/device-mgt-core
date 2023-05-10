/*
 * Copyright (c) 2023, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.subtype.mgt.dao;

import io.entgra.device.mgt.subtype.mgt.dao.impl.DeviceSubTypeDAOImpl;
import io.entgra.device.mgt.subtype.mgt.dao.impl.DeviceSubTypeMySQLDAOImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import io.entgra.device.mgt.subtype.mgt.dao.util.ConnectionManagerUtil;
import org.wso2.carbon.device.mgt.core.config.datasource.DataSourceConfig;

public class DeviceSubTypeDAOFactory {
    private static final Log log = LogFactory.getLog(DeviceSubTypeDAOFactory.class);
    private static String databaseEngine;

    public static void init(DataSourceConfig dataSourceConfiguration) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing Device SubType Mgt Data Source");
        }
        ConnectionManagerUtil.resolveDataSource(dataSourceConfiguration);
        databaseEngine = ConnectionManagerUtil.getDatabaseType();
    }

    public static DeviceSubTypeDAO getDeviceSubTypeDAO() {
        if (databaseEngine != null) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (databaseEngine) {
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_MYSQL:
                    return new DeviceSubTypeMySQLDAOImpl();
                default:
                    return new DeviceSubTypeDAOImpl();
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }
}