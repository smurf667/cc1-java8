package de.engehausen.cc1.impl;

import java.io.InputStream;

import de.engehausen.cc1.api.ArtifactDescriptor;

/**
 * Describes the <code>0xCAFED00D</code> artifact.
 */
public class ArtifactDescriptorImpl implements ArtifactDescriptor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream getPOMProperties() {
		return getClass().getResourceAsStream("/META-INF/maven/de.engehausen/0xCAFED00D/pom.properties");
	}

}
