package org.wso2.carbon.device.application.mgt.common.config;/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import javax.xml.bind.annotation.XmlElement;

public class RatingConfiguration {

    private int minRatingValue;
    private int maxRatingValue;

    @XmlElement(name = "MinRatingValue")
    public int getMinRatingValue() {
        return minRatingValue;
    }

    public void setMinRatingValue(int minRatingValue) {
        this.minRatingValue = minRatingValue;
    }

    @XmlElement(name = "MaxRatingValue")
    public int getMaxRatingValue() {
        return maxRatingValue;
    }

    public void setMaxRatingValue(int maxRatingValue) {
        this.maxRatingValue = maxRatingValue;
    }
}
