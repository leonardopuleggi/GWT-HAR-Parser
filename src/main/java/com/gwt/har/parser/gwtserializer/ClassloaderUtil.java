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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Util class to read the classpath with the GWT RPC request/response.
 * 
 * @author 
 */
public class ClassloaderUtil {
    //default HAR and client JAR
    private static final String CLIENT_JAR = "helloWorldJAR/helloWorld.jar";

    /**
     * Load the classes necessary for the serialization/deserialization
     *
     * @param classpath
     * @throws IOException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void loadClassloader(String classpath) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //JARs in external folder
        URL[] jars = null;
        //internal JAR - this is necessary for the RemoteServlet interface
        //ClassPathResource gwtServletJAR = new ClassPathResource(SERVLET_JAR);

        if (classpath.length() == 0) {
            //by default (nothing specified at command line) load
            //the client JAR
            File helloJar = getResourceAsFile(CLIENT_JAR);
            if (helloJar != null) {
                jars = new URL[]{helloJar.toURI().toURL()};
            } else {
                //shoud never go here, it'll fail on deserialization
                jars = new URL[]{};
            }
        } else {
            File f = new File(classpath);

            if (f.exists() && f.isDirectory()) {
                System.out.println("Reading JARs in folder " + f.getAbsolutePath());
                File[] files = f.listFiles((File dir, String name) -> (name != null && name.toLowerCase().contains(".jar")));
                jars = new URL[files.length];

                for (int i = 0; i < files.length; i++) {
                    if (files[i].getName().endsWith(".jar")) {
                        System.out.println("...reading JAR " + files[i]);
                        jars[i] = files[i].toURI().toURL();
                    }
                }
                System.out.println("Done");
            } else if (f.exists() && f.isFile() && classpath.endsWith(".jar")) {
                System.out.println("Reading JAR " + f.getAbsolutePath());
                jars = new URL[]{f.toURI().toURL()};
            } else {
                System.out.println("WRONG PATH! Please specify a directory with multiple JAR files in it or the full path for a single JAR file " + classpath);
            }
        }

        //set this classloader in the current thread. GWT parser will get them from this classloader
        URLClassLoader child = new URLClassLoader(jars);
        Thread.currentThread().setContextClassLoader(child);

        //add the classpath in the classpath of the app as well with this hack
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
        for (URL jar : jars) {
            method.invoke(ClassLoader.getSystemClassLoader(), new Object[]{jar});
        }
    }

    /**
     * Returns a temporary file read from the JAR file of the application
     * itself.
     *
     * @param resourcePath
     * @return
     */
    public static File getResourceAsFile(String resourcePath) {
        try {
            InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
            if (in == null) {
                return null;
            }

            File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
            tempFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                //copy stream
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            return null;
        }
    }

}
