/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.gwt.har.parser.gwtserializer.model;

import java.io.Serializable;

/**
 * Simple POJO to represent each XHR request and response.
 *
 */
public class XHRRequestResponse implements Serializable {
    private Integer httpStatus;
    private String request;
    private String response;
    private String serviceName;

    public XHRRequestResponse() {
    }

    public XHRRequestResponse(Integer httpStatus, String request, String response, String serviceName) {
        this.httpStatus = httpStatus;
        this.request = request;
        this.response = response;
        this.serviceName = serviceName;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "XHR "+httpStatus+" "+serviceName+"\nreq: "+request+"\nresp: "+response+"\n\n";
    }
}
