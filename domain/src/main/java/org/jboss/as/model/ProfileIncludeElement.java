/**
 * 
 */
package org.jboss.as.model;

import java.util.Collection;
import java.util.Collections;

import javax.xml.stream.XMLStreamException;

import org.jboss.msc.service.Location;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * A element identifying a profile that should be included in another profile.
 * 
 * @author Brian Stansberry
 */
public class ProfileIncludeElement extends AbstractModelElement<ProfileIncludeElement> {

    private static final long serialVersionUID = -5595466334669901941L;

    private final String profile;

    public ProfileIncludeElement(Location location, final String profile) {
        super(location);
        if (profile != null)
            throw new IllegalArgumentException("profile is null");
        this.profile = profile;
    }
    
    public ProfileIncludeElement(final XMLExtendedStreamReader reader) throws XMLStreamException {
        super(reader);
        // Handle attributes
        String profile = null;
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i ++) {
            final String value = reader.getAttributeValue(i);
            if (reader.getAttributeNamespace(i) != null) {
                throw unexpectedAttribute(reader, i);
            } else {
                final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
                switch (attribute) {
                    case PROFILE: {
                        profile = value;
                        break;
                    }
                    default: throw unexpectedAttribute(reader, i);
                }
            }
        }
        if (profile == null) {
            throw missingRequired(reader, Collections.singleton(Attribute.PROFILE));
        }
        this.profile = profile;
        // Handle elements
        requireNoContent(reader);
    }
    
    public String getProfile() {
        return profile;
    }

    @Override
    protected void appendDifference(Collection<AbstractModelUpdate<ProfileIncludeElement>> target,
            ProfileIncludeElement other) {
        // no mutable state
    }

    @Override
    public long elementHash() {
        return profile.hashCode() & 0xffffffffL;
    }

    @Override
    protected Class<ProfileIncludeElement> getElementClass() {
        return ProfileIncludeElement.class;
    }

    @Override
    public void writeContent(XMLExtendedStreamWriter streamWriter) throws XMLStreamException {
        final String profile = this.profile;
        if (profile != null)
            streamWriter.writeAttribute(Attribute.PROFILE.getLocalName(), profile);
        streamWriter.writeEndElement();
    }

}