package com.matriz.adapter.rest.mapper;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.matriz.adapter.rest.to.project.MemberTO;
import com.matriz.adapter.rest.to.project.ProjectProgressTO;
import com.matriz.adapter.rest.to.project.ProjectSummaryTO;
import com.matriz.adapter.rest.to.project.ProjectTO;
import com.matriz.domain.project.MemberDetails;
import com.matriz.domain.project.ProjectSummary;
import com.matriz.domain.project.ProjectWithRole;
import com.matriz.domain.task.Progress;

/** Converts project and membership domain objects to transfer objects. */
@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI, uses = UserMapper.class)
public interface ProjectMapper {

    @Mapping(target = ".", source = "project")
    ProjectTO toTO(ProjectWithRole projectWithRole);

    ProjectSummaryTO toSummaryTO(ProjectSummary summary);

    List<ProjectSummaryTO> toSummaryTOs(List<ProjectSummary> summaries);

    @Mapping(target = "role", source = "member.role")
    @Mapping(target = "addedAt", source = "member.addedAt")
    MemberTO toMemberTO(MemberDetails details);

    List<MemberTO> toMemberTOs(List<MemberDetails> details);

    default ProjectProgressTO toProgressTO(UUID projectId, Progress progress) {
        return new ProjectProgressTO(projectId, progress.total(), progress.todo(), progress.inProgress(),
                progress.done(), progress.percentDone());
    }
}
