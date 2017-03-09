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

package com.gwt.har.parser.gwtserializer.HARUtil;

import com.gwt.har.parser.gwtserializer.classloader.ClassloaderUtil;
import com.gwt.har.parser.gwtserializer.model.XHRRequestResponse;
import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarContent;
import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarPostData;
import de.sstoehr.harreader.model.HttpMethod;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * Util class to read the HAR files with the GWT RPC request/response.
 * 
 * @author 
 */
public class HARUtil {
    /**
     * see ServerSerializationStreamReader from GWT
     */
    private static final Pattern ALLOWED_STRONG_NAME = Pattern.compile("[a-zA-Z0-9_]+");
    
    //default HAR
    private static final String DEFAULT_HAR = "HARs/sample-01.har";
    
    /**
     * Returns a list of the GWT XHR request and response in the HAR file passed. 
     * 
     * @param fileName
     * @return
     * @throws IOException
     * @throws de.sstoehr.harreader.HarReaderException in case it cannot read the HAR file
     */
    public static List<XHRRequestResponse> getRequestsAndResponses(String fileName) throws IOException, HarReaderException {
        File harToRead = null;
        if (fileName != null && fileName.length() > 0) {
            File t = new File(fileName);
            if (t.exists() && t.isFile()) {
                harToRead = t;
            }
        }
        if (harToRead == null) {
            //fallback to the sample one
            harToRead = ClassloaderUtil.getResourceAsFile(DEFAULT_HAR);
        }

        List<XHRRequestResponse> result = new ArrayList<>();

        HarReader harReader = new HarReader();
        Har har = harReader.readFromFile(harToRead);

        for (HarEntry entry : har.getLog().getEntries()) {
            //read only the POST XHR request/response
            String req = null, resp = null;
            Integer httpStatus = null;
            if (entry != null && entry.getRequest() != null && HttpMethod.POST.equals(entry.getRequest().getMethod())) {
                HarPostData post = entry.getRequest().getPostData();
                if (post != null && post.getText().length() > 0) {
                    req = post.getText();
                }
            }
            if (entry != null && entry.getResponse() != null) {
                httpStatus = entry.getResponse().getStatus();
                HarContent content = entry.getResponse().getContent();
                if (content != null) {
                    resp = content.getText();
                }
            }

            //finally create the object to serialize/deserialize
            //check that it's really a GWT RPC via the ALLOWED_STRONG_NAME in verifyIfGWTRequestResponse
            if (httpStatus != null && req != null && resp != null) {
                XHRRequestResponse xhr = verifyIfGWTRequestResponse(httpStatus, req, resp);
                if (xhr != null) {
                    result.add(xhr);
                }
            }
        }
        return result;
    }
    
        /**
     * Basic check to see if the POST is actually a GWT RPC request/response. At
     * least the URL and the strongName MUST be there.
     *
     * @param httpStatus
     * @param req
     * @param resp
     * @return
     */
    public static XHRRequestResponse verifyIfGWTRequestResponse(Integer httpStatus, String req, String resp) {
        XHRRequestResponse result = null;

        UrlValidator defaultValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);
        //Quick sample
        // 7|0|6|http://127.0.0.1:8888/hellogwt/|D0E17CC147E447902BB3860716D1FB05|com.example.client.GreetingService|greetServer|java.lang.String/2004016611|GWT User|1|2|3|4|1|5|6|
        
        String[] tokens = req.split("\\|");
        if (tokens.length > 6) {
            String url = tokens[3];
            if (!defaultValidator.isValid(url)) {
                return null;
            }
            String strongName = tokens[4];
            String service = tokens[5];
            if (strongName != null && ALLOWED_STRONG_NAME.matcher(strongName).matches()) {
                return new XHRRequestResponse(httpStatus, req, resp, service);
            }
        }
        return result;
    }
    
}
