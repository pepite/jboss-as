/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.model;

import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.jboss.msc.service.Location;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamException;

/**
 * An element representing a list of properties (name/value pairs).
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class PropertiesElement extends AbstractModelElement<PropertiesElement> {

    private static final long serialVersionUID = 1614693052895734582L;

    private transient final SortedMap<String, String> properties = new TreeMap<String, String>();

    public PropertiesElement(final Location location) {
        super(location);
    }

    /** {@inheritDoc} */
    public long elementHash() {
        long total = 0;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            total = Long.rotateLeft(total, 1) ^ ((long)entry.getKey().hashCode() << 32L | entry.getValue().hashCode() & 0xffffffffL);
        }
        return total;
    }

    /** {@inheritDoc} */
    protected void appendDifference(final Collection<AbstractModelUpdate<PropertiesElement>> target, final PropertiesElement other) {
        calculateDifference(target, properties, other.properties, new DifferenceHandler<String, String, PropertiesElement>() {
            public void handleAdd(final Collection<AbstractModelUpdate<PropertiesElement>> target, final String name, final String newElement) {
                target.add(new PropertyAdd(name, newElement));
            }

            public void handleRemove(final Collection<AbstractModelUpdate<PropertiesElement>> target, final String name, final String oldElement) {
                target.add(new PropertyRemove(name));
            }

            public void handleChange(final Collection<AbstractModelUpdate<PropertiesElement>> target, final String name, final String oldElement, final String newElement) {
                target.add(new PropertyRemove(name));
                target.add(new PropertyAdd(name, newElement));
            }
        });
    }

    /** {@inheritDoc} */
    protected Class<PropertiesElement> getElementClass() {
        return PropertiesElement.class;
    }

    /** {@inheritDoc} */
    public void writeContent(final XMLExtendedStreamWriter streamWriter) throws XMLStreamException {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            streamWriter.writeEmptyElement("property");
            streamWriter.writeAttribute("name", entry.getKey());
            streamWriter.writeAttribute("value", entry.getValue());
        }
        streamWriter.writeEndElement();
    }

    void addProperty(final String name, final String value) {
        if (properties.containsKey(name)) {
            throw new IllegalArgumentException("Property already exists");
        }
        properties.put(name, value);
    }

    String removeProperty(final String name) {
        final String old = properties.remove(name);
        if (old == null) {
            throw new IllegalArgumentException("Property does not exist");
        }
        return old;
    }

    public int size() {
        return properties.size();
    }

    /**
     * Get the value of a property defined in this element.
     *
     * @param name the property name
     * @return the value, or {@code null} if the property does not exist
     */
    public String getProperty(final String name) {
        return properties.get(name);
    }
}