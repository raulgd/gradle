/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.artifacts;

import org.gradle.api.Action;
import org.gradle.api.Buildable;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedModuleVersion;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.internal.artifacts.attributes.DefaultArtifactAttributes;
import org.gradle.api.internal.artifacts.ivyservice.dynamicversions.DefaultResolvedModuleVersion;
import org.gradle.api.internal.attributes.AttributeContainerInternal;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.internal.Factory;
import org.gradle.internal.component.model.IvyArtifactName;
import org.gradle.internal.operations.BuildOperationContext;
import org.gradle.internal.progress.BuildOperationDetails;
import org.gradle.internal.progress.BuildOperationExecutor;

import java.io.File;

public class DefaultResolvedArtifact implements ResolvedArtifact, Buildable {
    private final ModuleVersionIdentifier owner;
    private final IvyArtifactName artifact;
    private final ComponentArtifactIdentifier artifactId;
    private final TaskDependency buildDependencies;
    private final AttributeContainer attributes;
    private Factory<File> artifactSource;
    private File file;
    private final BuildOperationExecutor buildOperationExecutor;

    public DefaultResolvedArtifact(ModuleVersionIdentifier owner, IvyArtifactName artifact, ComponentArtifactIdentifier artifactId, TaskDependency buildDependencies, Factory<File> artifactSource, AttributeContainerInternal parentAttributes, ImmutableAttributesFactory attributesFactory, BuildOperationExecutor buildOperationExecutor) {
        this.owner = owner;
        this.artifact = artifact;
        this.artifactId = artifactId;
        this.buildDependencies = buildDependencies;
        this.artifactSource = artifactSource;
        this.buildOperationExecutor = buildOperationExecutor;
        this.attributes = DefaultArtifactAttributes.forIvyArtifactName(artifact, parentAttributes, attributesFactory);
    }

    public DefaultResolvedArtifact(ModuleVersionIdentifier owner, IvyArtifactName artifact, ComponentArtifactIdentifier artifactId, TaskDependency buildDependencies, File artifactFile, AttributeContainerInternal parentAttributes, ImmutableAttributesFactory attributesFactory, BuildOperationExecutor buildOperationExecutor) {
        this.owner = owner;
        this.artifact = artifact;
        this.artifactId = artifactId;
        this.buildDependencies = buildDependencies;
        this.buildOperationExecutor = buildOperationExecutor;
        this.attributes = DefaultArtifactAttributes.forIvyArtifactName(artifact, parentAttributes, attributesFactory);
        this.file = artifactFile;
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return buildDependencies;
    }

    public ResolvedModuleVersion getModuleVersion() {
        return new DefaultResolvedModuleVersion(owner);
    }

    @Override
    public ComponentArtifactIdentifier getId() {
        return artifactId;
    }

    @Override
    public String toString() {
        return artifactId.getDisplayName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        DefaultResolvedArtifact other = (DefaultResolvedArtifact) obj;
        return other.owner.equals(owner) && other.artifactId.equals(artifactId);
    }

    @Override
    public int hashCode() {
        return owner.hashCode() ^ artifactId.hashCode();
    }

    public String getName() {
        return artifact.getName();
    }

    public String getType() {
        return artifact.getType();
    }

    public String getExtension() {
        return artifact.getExtension();
    }

    public String getClassifier() {
        return artifact.getClassifier();
    }

    @Override
    public AttributeContainer getAttributes() {
        return attributes;
    }

    public File getFile() {
        if (file == null) {
            BuildOperationDetails operationDetails = BuildOperationDetails.displayName("Resolve artifact " + artifactId.getDisplayName()).name("Resolve artifact " + artifact.getName()).operationDescriptor(artifactId).build();
            buildOperationExecutor.run(operationDetails, new Action<BuildOperationContext>() {
                @Override
                public void execute(BuildOperationContext buildOperationContext) {
                    file = artifactSource.create();
                }
            });
            artifactSource = null;
        }
        return file;
    }
}
