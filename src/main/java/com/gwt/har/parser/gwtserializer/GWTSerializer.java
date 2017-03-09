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

package com.gwt.har.parser.gwtserializer;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.RequestCallbackAdapter;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.gwt.har.parser.com.gdevelop.gwt.syncrpc.SyncClientSerializationStreamReader;
import static com.gwt.har.parser.gwtserializer.ClassloaderUtil.loadClassloader;
import static com.gwt.har.parser.gwtserializer.HARUtil.HARUtil.getRequestsAndResponses;
import com.gwt.har.parser.gwtserializer.model.XHRRequestResponse;
import com.gwt.har.parser.gwtserializer.serialization.DummySerializationPolicy;
import com.gwt.har.parser.gwtserializer.serialization.DummySerializationPolicyProvider;
import de.sstoehr.harreader.HarReaderException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * GWTSerializer class.
 * This class does all the heavy lifting. Load the JARs file from the location, 
 * grabs the HAR file, parse it and then attempt to deserialize requests and response
 * 
 * 
 */
public class GWTSerializer {
    /**
     * Parse the given HAR file with the JARs in the classpath folder.
     * 
     * @param classpath
     * @param har
     * @throws NoSuchMethodException
     * @throws IOException
     * @throws HarReaderException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void parse(String classpath, String har) throws IOException, HarReaderException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        //load the necessary JARs
        loadClassloader(classpath);

        //get the GWT RPC requests/responses
        List<XHRRequestResponse> list = getRequestsAndResponses(har);
        SyncClientSerializationStreamReader reader = new SyncClientSerializationStreamReader(new DummySerializationPolicy());

        int i = 1;
        for (XHRRequestResponse each : list) {
            //Request parsing
            try {
                RPCRequest req = com.google.gwt.user.server.rpc.RPC.decodeRequest(each.getRequest(), null, new DummySerializationPolicyProvider());
                System.out.println("-------------------REQUEST " + i + "---Success--------------------\n");
                System.out.println(each.getRequest() + "\n\n");
                System.out.println("toString() =>\n" + req.toString());
                System.out.println("\nvia reflection =>\n" + ToStringBuilder.reflectionToString(req) + "\n\n");

            } catch (IncompatibleRemoteServiceException e) {
                System.out.println("-------------------REQUEST " + i + "---FAILURE--------------------");
                System.out.println("\n\nGWT client class couldn't be deserialized/serialized. Have you passed "
                        + "the right class folder or the JARs to be loaded? \n" + e.getMessage() + "\n\n");
            }
            
            //Response parsing
            try {
                //TODO: parse ex as well or test exception
                reader.prepareToRead(each.getResponse().substring(4));
                Object deserialized = RequestCallbackAdapter.ResponseReader.OBJECT.read(reader);

                System.out.println("-------------------RESPONSE " + i + "---Success--------------------\n");
                System.out.println(each.getResponse()+ "\n\n");
                
                System.out.println("toString() =>\n" + deserialized);
                System.out.println("\nvia reflection =>\n" + ToStringBuilder.reflectionToString(deserialized) + "\n\n");

            } catch (SerializationException e) {
                System.out.println("-------------------RESPONSE " + i + "---FAILURE--------------------");
                System.out.println("Cannot serialize/deserialize. One of the class necessary to serialize and deserialize was not found. Have you passed "
                        + "the right class folder or the JARs to be loaded? \n" + e.getMessage() + "\n\n");
            }
            i++;
        }
    }
}
