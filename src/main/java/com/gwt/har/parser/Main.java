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
package com.gwt.har.parser;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.gwt.har.parser.gwtserializer.GWTSerializer;
import de.sstoehr.harreader.HarReaderException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 *
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String classpath = "";
        String har = "";
        String rpcFolder = "";

        if (args == null || args.length < 3) {
            printDefault();
        } else {
            classpath = args[0];
            har = args[1];
            rpcFolder = args[2];
            printOptions(classpath, har, rpcFolder);
        }

        try {

            GWTSerializer.parse(classpath, har, rpcFolder);

        } catch (NoSuchMethodException | IllegalAccessException e) {
            System.out.println("Cannot load the classes or JAR file. Please check that a security manager for the "
                    + "classloader is not in place");
        } catch (IOException e) {
            System.out.println("Cannot load the classes or JAR file. Have you passed "
                    + "the right class folder or the JARs to be loaded? \n" + e.getMessage());
        } catch (HarReaderException e) {
            System.out.println("Cannot parse the HAR file. Have you passed the right file location? \n" + e.getMessage());
        } catch (InvocationTargetException e) {
            System.out.println("Cannot load the classes or JAR file. Please contact us\n" + e.getMessage());
        } catch (IncompatibleRemoteServiceException e) {
            System.out.println("GWT client class couldn't be deserialized/serialized. Have you passed "
                    + "the right class folder or the JARs to be loaded? \n" + e.getMessage());
        }
    }

    private static void printDefault() {
        System.out.println("You've launched GWT-HAR-Parser with no option (or wrong ones) so it'll just run with the default \"sample-01.har\" and JARs in the application.\n"
                + "\nIf you want to try something different please specify a folder with the classes of JARs for the GWT client and server classes and the HAR\n"
                + "as specified in the sample\n\njava -jar target\\GWT-HAR-Parser-1.0-SNAPSHOT-jar-with-dependencies.jar C:\\temp\\libs C:\\temp\\localhost.har C:\\temp\\rpcs");
    }

    private static void printOptions(String classpath, String har, String rpcFolder) {
        System.out.println("You've launched the GWTHARarser with the following classpath " + classpath + ", the *.gwt.rpc in this folder "+rpcFolder+" and this HAR file " + har + "\n");
    }
}
