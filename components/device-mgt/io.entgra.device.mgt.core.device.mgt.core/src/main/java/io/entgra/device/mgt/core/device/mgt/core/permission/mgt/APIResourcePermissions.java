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
package io.entgra.device.mgt.core.device.mgt.core.permission.mgt;

import io.entgra.device.mgt.core.device.mgt.common.permission.mgt.Permission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIResourcePermissions {
    private Map<String, List<Permission>> apiResourcePermissions;

    public APIResourcePermissions() {
        apiResourcePermissions = new HashMap<>();
    }

    public void addPermissionList(String context, List<Permission> permissions){
        apiResourcePermissions.put(context, permissions);
    }

    public List<Permission> getPermissions(String context) {
        return apiResourcePermissions.get(context);
    }
}