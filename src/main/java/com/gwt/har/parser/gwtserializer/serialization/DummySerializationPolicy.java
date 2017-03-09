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

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.impl.SerializabilityUtil;

import java.io.Serializable;
import java.util.*;

/**
 * Dummy SerializationPolicy that doesn't do anything.
 *
 */
public class DummySerializationPolicy extends SerializationPolicy {
    private static final Class<?>[] JRE_BLACKLIST = new Class[]{ArrayStoreException.class, AssertionError.class, Boolean.class, Byte.class, Character.class, Class.class, ClassCastException.class, Double.class, Error.class, Float.class, IllegalArgumentException.class, IllegalStateException.class, IndexOutOfBoundsException.class, Integer.class, Long.class, NegativeArraySizeException.class, NullPointerException.class, Number.class, NumberFormatException.class, Short.class, StackTraceElement.class, String.class, StringBuffer.class, StringIndexOutOfBoundsException.class, UnsupportedOperationException.class, ArrayList.class, ConcurrentModificationException.class, Date.class, EmptyStackException.class, EventObject.class, HashMap.class, HashSet.class, MissingResourceException.class, NoSuchElementException.class, Stack.class, TooManyListenersException.class, Vector.class};
    private static final Set<Class<?>> JRE_BLACKSET;

    private boolean isFieldSerializable(Class<?> clazz) {
        return this.isInstantiable(clazz)?true:(Serializable.class.isAssignableFrom(clazz)?!JRE_BLACKSET.contains(clazz):false);
    }

    private boolean isInstantiable(Class<?> clazz) {
        return clazz.isPrimitive()?true:(clazz.isArray()?this.isInstantiable(clazz.getComponentType()):(IsSerializable.class.isAssignableFrom(clazz)?true: SerializabilityUtil.hasCustomFieldSerializer(clazz) != null));
    }

    static {
        JRE_BLACKSET = new HashSet(Arrays.asList(JRE_BLACKLIST));
    }

    @Override
    public boolean shouldDeserializeFields(Class<?> clazz) {
        return this.isFieldSerializable(clazz);
    }

    @Override
    public boolean shouldSerializeFields(Class<?> clazz) {
        return this.isFieldSerializable(clazz);
    }

    @Override
    public void validateDeserialize(Class<?> clazz) throws SerializationException {
        //doesn't validate. It expects that since you're passing an HAR it's already validated
    }

    @Override
    public void validateSerialize(Class<?> clazz) throws SerializationException {
        //doesn't validate. It expects that since you're passing an HAR it's already validated
    }
    
    @Override
    public String toString(){
        return null;
    }
}
