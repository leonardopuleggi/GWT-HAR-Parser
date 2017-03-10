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
package com.gwt.har.parser.gwtserializer.serialization;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;
import com.gwt.har.parser.com.gdevelop.gwt.syncrpc.SyncClientSerializationStreamReader;
import com.gwt.har.parser.gwtserializer.model.XHRRequestResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RPC policy loader.
 * It takes a folder and try to load the policy file (gwt.rpc) in that folder.
 * 
 * @author
 */
public class RPCPolicyUtil implements SerializationPolicyProvider {
    private final Map<String, SyncClientSerializationStreamReader> readers = new HashMap<>();

    /**
     * Loads the .gwt.rpc file and returns a cached reader to parse the request/response.
     * 
     * @param baseFolder
     * @param xhr
     * @return 
     */
    public SyncClientSerializationStreamReader loadRPCReader(String baseFolder, XHRRequestResponse xhr) {
        if(!baseFolder.endsWith(File.separator)) {
            baseFolder += File.separator;
        }
        
        if (xhr.getStrongName() != null) {
            SyncClientSerializationStreamReader reader = readers.get(xhr.getStrongName());
            if (reader != null) {
                return reader;
            } else {
                try {
                    List<ClassNotFoundException> classNotFoundExceptions = new ArrayList<>();
                    SerializationPolicy policy = SerializationPolicyLoader.loadFromStream(new FileInputStream(baseFolder+xhr.getStrongName()+".gwt.rpc"), classNotFoundExceptions);
                    reader = new SyncClientSerializationStreamReader(policy);
                    readers.put(xhr.getStrongName(), reader);
                } catch (IOException | ParseException ex) {
                    System.out.println("Cannot load the "+xhr.getStrongName()+".gwt.rpc file. Have you passed the right folder for these?");
                    //return a reader with a dummy policy
                    reader = new SyncClientSerializationStreamReader(new DummySerializationPolicy());
                    readers.put(xhr.getStrongName(), reader);
                    return reader;
                }
            }
        }
        //return a reader with a dummy policy in case the XHR doesn't contain the strong name
        return new SyncClientSerializationStreamReader(new DummySerializationPolicy());
    }

    @Override
    public SerializationPolicy getSerializationPolicy(String moduleBaseURL, String serializationPolicyStrongName) {
        SyncClientSerializationStreamReader reader = readers.get(serializationPolicyStrongName);
        if (reader != null) {
            return reader.getSerializationPolicy();
        }
        return new DummySerializationPolicy();
    }
    
}
