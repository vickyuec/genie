/*
 *
 *  Copyright 2015 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.genie.web.data.services.impl.jpa.entities;

import com.google.common.collect.Lists;
import com.netflix.genie.common.exceptions.GeniePreconditionException;
import com.netflix.genie.web.data.services.impl.jpa.queries.projections.ClusterCommandsProjection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Representation of the state of the Cluster object.
 *
 * @author amsharma
 * @author tgianos
 * @since 2.0.0
 */
@Getter
@Setter
@ToString(
    callSuper = true,
    doNotUseGetters = true
)
@Entity
@Table(name = "clusters")
@NamedEntityGraphs(
    {
        @NamedEntityGraph(
            name = ClusterEntity.COMMANDS_ENTITY_GRAPH,
            attributeNodes = {
                @NamedAttributeNode("commands")
            }
        ),
        @NamedEntityGraph(
            name = ClusterEntity.COMMANDS_DTO_ENTITY_GRAPH,
            attributeNodes = {
                @NamedAttributeNode(
                    value = "commands",
                    subgraph = "command-sub-graph"
                )
            },
            subgraphs = {
                @NamedSubgraph(
                    name = "command-sub-graph",
                    attributeNodes = {
                        @NamedAttributeNode("executable"),
                        @NamedAttributeNode("setupFile"),
                        @NamedAttributeNode("configs"),
                        @NamedAttributeNode("dependencies"),
                        @NamedAttributeNode("tags"),
                        @NamedAttributeNode(
                            value = "clusterCriteria",
                            subgraph = "criteria-sub-graph"
                        )
                    }
                ),
                @NamedSubgraph(
                    name = "criteria-sub-graph",
                    attributeNodes = {
                        @NamedAttributeNode("tags")
                    }
                )
            }
        ),
        @NamedEntityGraph(
            name = ClusterEntity.DTO_ENTITY_GRAPH,
            attributeNodes = {
                @NamedAttributeNode("setupFile"),
                @NamedAttributeNode("configs"),
                @NamedAttributeNode("dependencies"),
                @NamedAttributeNode("tags")
            }
        )
    }
)
public class ClusterEntity extends BaseEntity implements ClusterCommandsProjection {

    /**
     * The name of the {@link javax.persistence.EntityGraph} which will eagerly load everything needed to access
     * a clusters commands base fields.
     */
    public static final String COMMANDS_ENTITY_GRAPH = "Cluster.commands";

    /**
     * The name of the {@link javax.persistence.EntityGraph} which will eagerly load everything needed to access
     * a clusters commands and create the command DTOs.
     */
    public static final String COMMANDS_DTO_ENTITY_GRAPH = "Cluster.commands.dto";

    /**
     * The name of the {@link javax.persistence.EntityGraph} which will eagerly load everything needed to construct a
     * Cluster DTO.
     */
    public static final String DTO_ENTITY_GRAPH = "Cluster.dto";

    private static final long serialVersionUID = -5674870110962005872L;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "clusters_configs",
        joinColumns = {
            @JoinColumn(name = "cluster_id", referencedColumnName = "id", nullable = false, updatable = false)
        },
        inverseJoinColumns = {
            @JoinColumn(name = "file_id", referencedColumnName = "id", nullable = false, updatable = false)
        }
    )
    @ToString.Exclude
    private Set<FileEntity> configs = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "clusters_dependencies",
        joinColumns = {
            @JoinColumn(name = "cluster_id", referencedColumnName = "id", nullable = false, updatable = false)
        },
        inverseJoinColumns = {
            @JoinColumn(name = "file_id", referencedColumnName = "id", nullable = false, updatable = false)
        }
    )
    @ToString.Exclude
    private Set<FileEntity> dependencies = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "clusters_tags",
        joinColumns = {
            @JoinColumn(name = "cluster_id", referencedColumnName = "id", nullable = false, updatable = false)
        },
        inverseJoinColumns = {
            @JoinColumn(name = "tag_id", referencedColumnName = "id", nullable = false, updatable = false)
        }
    )
    @ToString.Exclude
    private Set<TagEntity> tags = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "clusters_commands",
        joinColumns = {
            @JoinColumn(name = "cluster_id", referencedColumnName = "id", nullable = false, updatable = false)
        },
        inverseJoinColumns = {
            @JoinColumn(name = "command_id", referencedColumnName = "id", nullable = false, updatable = false)
        }
    )
    @OrderColumn(name = "command_order", nullable = false)
    @ToString.Exclude
    private List<CommandEntity> commands = new ArrayList<>();

    /**
     * Default Constructor.
     */
    public ClusterEntity() {
        super();
    }

    /**
     * Set all the files associated as configuration files for this cluster.
     *
     * @param configs The configuration files to set
     */
    public void setConfigs(@Nullable final Set<FileEntity> configs) {
        this.configs.clear();
        if (configs != null) {
            this.configs.addAll(configs);
        }
    }

    /**
     * Set all the files associated as dependency files for this cluster.
     *
     * @param dependencies The dependency files to set
     */
    public void setDependencies(@Nullable final Set<FileEntity> dependencies) {
        this.dependencies.clear();
        if (dependencies != null) {
            this.dependencies.addAll(dependencies);
        }
    }

    /**
     * Set all the tags associated to this cluster.
     *
     * @param tags The dependency tags to set
     */
    public void setTags(@Nullable final Set<TagEntity> tags) {
        this.tags.clear();
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /**
     * Sets the commands for this cluster.
     *
     * @param commands The commands that this cluster supports
     * @throws GeniePreconditionException If the commands are already added to the list
     */
    public void setCommands(@Nullable final List<CommandEntity> commands) throws GeniePreconditionException {
        if (commands != null
            && commands.stream().map(CommandEntity::getUniqueId).distinct().count() != commands.size()) {
            throw new GeniePreconditionException("List of commands to set cannot contain duplicates");
        }

        //Clear references to this cluster in existing commands
        for (final CommandEntity command : this.commands) {
            command.getClusters().remove(this);
        }
        this.commands.clear();

        if (commands != null) {
            // Set the commands for this cluster
            this.commands.addAll(commands);

            //Add the reference in the new commands
            for (final CommandEntity command : this.commands) {
                command.getClusters().add(this);
            }
        }
    }

    /**
     * Add a new command to this cluster. Manages both sides of relationship.
     *
     * @param command The command to add. Not null.
     * @throws GeniePreconditionException If the command is a duplicate of an existing command
     */
    public void addCommand(@NotNull final CommandEntity command) throws GeniePreconditionException {
        if (this.commands.contains(command)) {
            throw new GeniePreconditionException("A command with id " + command.getUniqueId() + " is already added");
        }
        this.commands.add(command);
        command.getClusters().add(this);
    }

    /**
     * Remove a command from this cluster. Manages both sides of relationship.
     *
     * @param command The command to remove. Not null.
     */
    public void removeCommand(@NotNull final CommandEntity command) {
        this.commands.remove(command);
        command.getClusters().remove(this);
    }

    /**
     * Remove all the commands from this application.
     */
    public void removeAllCommands() {
        Lists.newArrayList(this.commands).forEach(this::removeCommand);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        return super.equals(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
