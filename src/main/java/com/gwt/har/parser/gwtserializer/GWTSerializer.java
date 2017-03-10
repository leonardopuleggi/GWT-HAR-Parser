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

import com.google.gwt.user.client.rpc.impl.RequestCallbackAdapter;
import com.google.gwt.user.client.rpc.impl.RequestCallbackAdapter.ResponseReader;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.gwt.har.parser.com.gdevelop.gwt.syncrpc.SyncClientSerializationStreamReader;
import static com.gwt.har.parser.gwtserializer.classloader.ClassloaderUtil.loadClassloader;
import static com.gwt.har.parser.gwtserializer.HARUtil.HARUtil.getRequestsAndResponses;
import com.gwt.har.parser.gwtserializer.model.XHRRequestResponse;
import com.gwt.har.parser.gwtserializer.serialization.RPCPolicyUtil;
import de.sstoehr.harreader.HarReaderException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GWTSerializer class.
 * This class does all the heavy lifting. Load the JARs file from the location, 
 * grabs the HAR file, parse it and then attempt to deserialize requests and response
 * 
 * 
 */
public class GWTSerializer {
    private static final Map<Class<?>, ResponseReader> JPRIMITIVETYPE_TO_RESPONSEREADER = new HashMap<Class<?>, ResponseReader>();

    static {
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(boolean.class,
                ResponseReader.BOOLEAN);
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(byte.class, ResponseReader.BYTE);
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(char.class, ResponseReader.CHAR);
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(double.class,
                ResponseReader.DOUBLE);
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(float.class, ResponseReader.FLOAT);
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(int.class, ResponseReader.INT);
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(long.class, ResponseReader.LONG);
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(short.class, ResponseReader.SHORT);
        JPRIMITIVETYPE_TO_RESPONSEREADER.put(void.class, ResponseReader.VOID);
    }
    
    private static ResponseReader getReaderFor(Class<?> type) {
        ResponseReader primitiveResponseReader = JPRIMITIVETYPE_TO_RESPONSEREADER
                .get(type);
        if (primitiveResponseReader != null) {
            return primitiveResponseReader;
        }

        if (type == String.class) {
            return ResponseReader.STRING;
        }
        if (type == Void.class || type == void.class) {
            return ResponseReader.VOID;
        }

        return ResponseReader.OBJECT;
    }
        
    /**
     * Parse the given HAR file with the JARs in the classpath folder.
     * 
     * @param classpath
     * @param har
     * @param rpcFolder
     * @throws NoSuchMethodException
     * @throws IOException
     * @throws HarReaderException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void parse(String classpath, String har, String rpcFolder) throws IOException, HarReaderException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        //load the necessary JARs
        loadClassloader(classpath);

        //get the GWT RPC requests/responses
        List<XHRRequestResponse> list = getRequestsAndResponses(har);
        RPCPolicyUtil rpcUtil = new RPCPolicyUtil();
        
        //1 based just to print the request/response number
        int i = 1;
        for (XHRRequestResponse each : list) {
            //get the (cached) reader for the specific strong permutation
            SyncClientSerializationStreamReader reader = rpcUtil.loadRPCReader(rpcFolder, each);
            
            //Request parsing
            Class<?> returnType = requestParsing(each, rpcUtil, i);
            
            //Response parsing
            responseParsing(each, reader, i, returnType);
            
            i++;
        }
    }
    
    private static Class<?> requestParsing(XHRRequestResponse each, RPCPolicyUtil rpcUtil, int i) {
        RPCRequest req;
        try {
            req = com.google.gwt.user.server.rpc.RPC.decodeRequest(each.getRequest(), null, rpcUtil);
            printSuccessfulRequest(each, req, i);
            
            Class<?> returnType = req.getMethod() != null? req.getMethod().getReturnType() : null;
            return returnType;
            
        } catch (Exception e) {
            //TODO: try to parse with a dummy policy
            printUnsuccessful(i, true, e);
            return null;
        }
    }
    
    private static void responseParsing(XHRRequestResponse each, SyncClientSerializationStreamReader reader, int i, Class<?> returnType) {
        try {
            //TODO: parse ex as well or test exception
            reader.prepareToRead(each.getResponse().substring(4));
            Object deserialized;
            if (returnType != null) {
                deserialized = getReaderFor(returnType).read(reader);
            }
            else {
                //default to the object reader
                deserialized = RequestCallbackAdapter.ResponseReader.OBJECT.read(reader);
            }
            
            printSuccessfulResponse(each, deserialized, i);
        } catch (Exception e) {
            //e.printStackTrace();
            printUnsuccessful(i, false, e);
        }
    }
    
    private static void printSuccessfulRequest(XHRRequestResponse each, RPCRequest req, int i) {
        System.out.println("-------------------REQUEST " + i + "---Success--------------------\n");
        System.out.println(each.getRequest() + "\n\n");
        System.out.println("toString() =>\n" + req.toString());
        System.out.println("\nvia reflection =>\n" + ToStringBuilder.reflectionToString(req) + "\n\n");
    }
    
    private static void printSuccessfulResponse(XHRRequestResponse each, Object deserialized, int i) {
        System.out.println("-------------------RESPONSE " + i + "---Success--------------------\n");
        System.out.println(each.getResponse() + "\n\n");

        System.out.println("toString() =>\n" + deserialized);
        System.out.println("\nvia reflection =>\n" + ToStringBuilder.reflectionToString(deserialized) + "\n\n");
    }
    
    
    private static void printUnsuccessful(int i, boolean isRequest, Exception e) {
        if (isRequest) {
            System.out.println("-------------------REQUEST  " + i + "---FAILURE--------------------");
        }
        else {
            System.out.println("-------------------RESPONSE " + i + "---FAILURE--------------------");
        }
        System.out.println("\n\nGWT client class couldn't be deserialized/serialized. Have you passed "
                + "the right class folder or the JARs to be loaded? \n" + e.getMessage() + "\n\n");
    }
}
