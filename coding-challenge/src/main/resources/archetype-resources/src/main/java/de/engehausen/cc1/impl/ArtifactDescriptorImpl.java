package de.engehausen.cc1.impl;

import java.io.InputStream;

import de.engehausen.cc1.api.ArtifactDescriptor;

/**
 * Describes your artifact. You do not need to change this class.
 * Notice the implementation utilizes the default implementation of
 * {@link ArtifactDescriptor#getPomKey(de.engehausen.cc1.api.ArtifactDescriptor.Key)}.
 */
public class ArtifactDescriptorImpl implements ArtifactDescriptor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream getPOMProperties() {
		return getClass().getResourceAsStream("/META-INF/maven/${groupId}/${artifactId}/pom.properties");
	}

}
