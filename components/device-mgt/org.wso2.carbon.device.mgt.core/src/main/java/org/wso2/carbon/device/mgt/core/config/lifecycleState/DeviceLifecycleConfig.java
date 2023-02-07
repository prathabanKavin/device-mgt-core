/*
 *   Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.config.lifecycleState;

import org.wso2.carbon.device.mgt.common.configuration.mgt.DeviceLifecycleState;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Represents Device lifecycle status configuration.
 */
@XmlRootElement(name = "LifecycleManagementConfiguration")
public class DeviceLifecycleConfig {

    private List<DeviceLifecycleState> deviceLifecycleStates;

    @XmlElementWrapper(name = "LifecycleStates")
    @XmlElement(name = "LifecycleState")
    public List<DeviceLifecycleState> getDeviceLifecycleStates() {
        return deviceLifecycleStates;
    }

    public void setDeviceLifecycleStates(List<DeviceLifecycleState> deviceLifecycleStates) {
        this.deviceLifecycleStates = deviceLifecycleStates;
    }
}
