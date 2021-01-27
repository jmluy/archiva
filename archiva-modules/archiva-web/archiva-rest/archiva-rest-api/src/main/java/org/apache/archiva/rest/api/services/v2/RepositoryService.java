package org.apache.archiva.rest.api.services.v2;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.archiva.components.rest.model.PagedResult;
import org.apache.archiva.redback.authorization.RedbackAuthorization;
import org.apache.archiva.rest.api.model.v2.Artifact;
import org.apache.archiva.rest.api.model.v2.ArtifactTransferRequest;
import org.apache.archiva.rest.api.model.v2.Repository;
import org.apache.archiva.rest.api.model.v2.RepositoryStatistics;
import org.apache.archiva.rest.api.model.v2.ScanStatus;
import org.apache.archiva.security.common.ArchivaRoleConstants;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.archiva.rest.api.services.v2.RestConfiguration.DEFAULT_PAGE_LIMIT;

/**
 * @author Martin Stockhammer <martin_s@apache.org>
 * @since 3.0
 */
@Path( "repositories" )
@Tag(name = "v2")
@Tag(name = "v2/Repositories")
@Schema(name="RepositoryService",description = "Manage repositories of all types")
public interface RepositoryService
{

    @Path( "" )
    @GET
    @Produces( {APPLICATION_JSON} )
    @RedbackAuthorization( permissions = ArchivaRoleConstants.OPERATION_MANAGE_CONFIGURATION )
    @Operation( summary = "Returns all managed repositories.",
        parameters = {
            @Parameter( name = "q", description = "Search term" ),
            @Parameter( name = "offset", description = "The offset of the first element returned" ),
            @Parameter( name = "limit", description = "Maximum number of items to return in the response" ),
            @Parameter( name = "orderBy", description = "List of attribute used for sorting (key, value)" ),
            @Parameter( name = "order", description = "The sort order. Either ascending (asc) or descending (desc)" ),
            @Parameter( name = "locale", description = "The locale for name and description" )
        },
        security = {
            @SecurityRequirement(
                name = ArchivaRoleConstants.OPERATION_MANAGE_CONFIGURATION
            )
        },
        responses = {
            @ApiResponse( responseCode = "200",
                description = "If the list could be returned",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = PagedResult.class ) )
            ),
            @ApiResponse( responseCode = "403", description = "Authenticated user is not permitted to gather the information",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) )
        }
    )
    PagedResult<Repository> getRepositories( @QueryParam( "q" ) @DefaultValue( "" ) String searchTerm,
                                             @QueryParam( "offset" ) @DefaultValue( "0" ) Integer offset,
                                             @QueryParam( "limit" ) @DefaultValue( value = DEFAULT_PAGE_LIMIT ) Integer limit,
                                             @QueryParam( "orderBy" ) @DefaultValue( "id" ) List<String> orderBy,
                                             @QueryParam( "order" ) @DefaultValue( "asc" ) String order,
                                             @QueryParam( "locale" ) String localeString) throws ArchivaRestServiceException;

    @Path( "managed/{id}/statistics" )
    @GET
    @Produces( {APPLICATION_JSON} )
    @RedbackAuthorization( permissions = ArchivaRoleConstants.OPERATION_MANAGE_CONFIGURATION )
    @Operation( summary = "Returns repository statistic data.",
        security = {
            @SecurityRequirement(
                name = ArchivaRoleConstants.OPERATION_MANAGE_CONFIGURATION
            )
        },
        responses = {
            @ApiResponse( responseCode = "200",
                description = "If the statistics could be returned",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = RepositoryStatistics.class ) )
            ),
            @ApiResponse( responseCode = "403", description = "Authenticated user is not permitted to gather the information",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) ),
            @ApiResponse( responseCode = "404", description = "The repository does not exist",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) )
        }
    )
    RepositoryStatistics getManagedRepositoryStatistics( @PathParam( "id" ) String repositoryId )
        throws ArchivaRestServiceException;

    @Path ("managed/{id}/scan/schedule")
    @POST
    @Produces ({ APPLICATION_JSON })
    @Consumes({ APPLICATION_JSON })
    @RedbackAuthorization (permissions = ArchivaRoleConstants.OPERATION_RUN_INDEXER)
    @Operation( summary = "Returns repository statistic data.",
        security = {
            @SecurityRequirement(
                name = ArchivaRoleConstants.OPERATION_RUN_INDEXER
            )
        },
        responses = {
            @ApiResponse( responseCode = "200",
                description = "If the statistics could be returned"
            ),
            @ApiResponse( responseCode = "403", description = "Authenticated user is not permitted to gather the information",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) ),
            @ApiResponse( responseCode = "404", description = "The repository does not exist",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) )
        }
    )
    Response scheduleRepositoryScan( @PathParam ("id") String repositoryId,
                                     @QueryParam ("fullScan") boolean fullScan )
        throws ArchivaRestServiceException;


    @Path ("managed/{id}/scan/now")
    @POST
    @Produces ({ APPLICATION_JSON })
    @Consumes({ APPLICATION_JSON })
    @RedbackAuthorization (permissions = ArchivaRoleConstants.OPERATION_RUN_INDEXER)
    @Operation( summary = "Runs a repository scan instantly and waits for the response.",
        security = {
            @SecurityRequirement(
                name = ArchivaRoleConstants.OPERATION_RUN_INDEXER
            )
        },
        responses = {
            @ApiResponse( responseCode = "200",
                description = "If the statistics could be returned",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = RepositoryStatistics.class ) )
            ),
            @ApiResponse( responseCode = "403", description = "Authenticated user is not permitted to gather the information",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) ),
            @ApiResponse( responseCode = "404", description = "The repository does not exist",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) )
        }
    )
    RepositoryStatistics scanRepositoryImmediately( @PathParam ("id") String repositoryId )
        throws ArchivaRestServiceException;


    /**
     * Returns the scan status of the given repository
     * @param repositoryId the repository identifier
     * @return the status
     * @throws ArchivaRestServiceException
     * @since 3.0
     */
    @Path ("managed/{id}/scan/status")
    @GET
    @Produces ({ APPLICATION_JSON })
    @RedbackAuthorization (permissions = ArchivaRoleConstants.OPERATION_RUN_INDEXER)
    @Operation( summary = "Returns status of running and scheduled scans.",
        security = {
            @SecurityRequirement(
                name = ArchivaRoleConstants.OPERATION_RUN_INDEXER
            )
        },
        responses = {
            @ApiResponse( responseCode = "200",
                description = "If the status could be returned",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ScanStatus.class ) )
            ),
            @ApiResponse( responseCode = "403", description = "Authenticated user is not permitted to gather the information",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) ),
            @ApiResponse( responseCode = "404", description = "The repository does not exist",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) )
        }
    )
    ScanStatus getScanStatus( @PathParam ("id") String repositoryId )
        throws ArchivaRestServiceException;

    @Path ("managed/{id}/scan")
    @DELETE
    @Produces ({ APPLICATION_JSON })
    @RedbackAuthorization (permissions = ArchivaRoleConstants.OPERATION_RUN_INDEXER)
    @Operation( summary = "Removes a scan task from the queue.",
        security = {
            @SecurityRequirement(
                name = ArchivaRoleConstants.OPERATION_RUN_INDEXER
            )
        },
        responses = {
            @ApiResponse( responseCode = "200",
                description = "If the task was removed successfully"
            ),
            @ApiResponse( responseCode = "403", description = "Authenticated user is not permitted to gather the information",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) ),
            @ApiResponse( responseCode = "404", description = "The repository does not exist",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) )
        }
    )
    Response removeScanningTaskFromQueue( @PathParam ("id") String repositoryId )
        throws ArchivaRestServiceException;

    /**
     * permissions are checked in impl
     * will copy an artifact from the source repository to the target repository
     */
    @Path ("managed/{id}/artifact/copy")
    @POST
    @Produces({APPLICATION_JSON})
    @Consumes({ APPLICATION_JSON })
    @RedbackAuthorization (noPermission = true)
    @Operation( summary = "Copies a artifact between repositories.",
        security = {
            @SecurityRequirement(
                name = ArchivaRoleConstants.OPERATION_RUN_INDEXER
            )
        },
        responses = {
            @ApiResponse( responseCode = "200",
                description = "If the artifact was copied"
            ),
            @ApiResponse( responseCode = "403", description = "Authenticated user is not permitted to gather the information",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) ),
            @ApiResponse( responseCode = "404", description = "The repository does not exist, or if the artifact was not found",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) )
        }
    )
    Response copyArtifact( ArtifactTransferRequest artifactTransferRequest )
        throws ArchivaRestServiceException;

    @Path ("managed/{id}/index/download/start")
    @POST
    @Produces ({ APPLICATION_JSON })
    @Consumes({ APPLICATION_JSON })
    @RedbackAuthorization (permissions = ArchivaRoleConstants.OPERATION_RUN_INDEXER)
    @Operation( summary = "Schedules a task for remote index download.",
        security = {
            @SecurityRequirement(
                name = ArchivaRoleConstants.OPERATION_RUN_INDEXER
            )
        },
        responses = {
            @ApiResponse( responseCode = "200",
                description = "If the task was scheduled"
            ),
            @ApiResponse( responseCode = "403", description = "Authenticated user is not permitted to gather the information",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) ),
            @ApiResponse( responseCode = "404", description = "The repository does not exist",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) )
        }
    )
    Response scheduleDownloadRemoteIndex( @PathParam ("id") String repositoryId,
                                              @QueryParam ("now") boolean now,
                                              @QueryParam ("fullDownload") boolean fullDownload )
        throws ArchivaRestServiceException;


    @Path ("managed/{id}/artifact")
    @DELETE
    @Consumes ({ APPLICATION_JSON })
    @RedbackAuthorization (noPermission = true)
    @Operation( summary = "Deletes a artifact in the repository.",
        security = {
            @SecurityRequirement(
                name = ArchivaRoleConstants.OPERATION_RUN_INDEXER
            )
        },
        responses = {
            @ApiResponse( responseCode = "200",
                description = "If the artifact was deleted"
            ),
            @ApiResponse( responseCode = "403", description = "Authenticated user is not permitted to gather the information",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) ),
            @ApiResponse( responseCode = "404", description = "The repository or the artifact does not exist",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) )
        }
    )
    Response deleteArtifact( Artifact artifact )
        throws ArchivaRestServiceException;

    @Path ("index_downloads")
    @GET
    @Produces ({ APPLICATION_JSON })
    @RedbackAuthorization (noPermission = true)
    @Operation( summary = "Returns a list of running downloads from the remote repository.",
        security = {
            @SecurityRequirement(
                name = ArchivaRoleConstants.OPERATION_RUN_INDEXER
            )
        },
        responses = {
            @ApiResponse( responseCode = "200",
                description = "If the artifact was deleted"
            ),
            @ApiResponse( responseCode = "403", description = "Authenticated user is not permitted to gather the information",
                content = @Content( mediaType = APPLICATION_JSON, schema = @Schema( implementation = ArchivaRestError.class ) ) )
        }
    )
    List<String> getRunningRemoteDownloads();


}
