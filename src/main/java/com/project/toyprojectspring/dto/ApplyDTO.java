package com.project.toyprojectspring.dto;

import com.project.toyprojectspring.entity.ApplyEntity;
import com.project.toyprojectspring.entity.MemberEntity;
import com.project.toyprojectspring.entity.PostEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyDTO {
    private Long id;
    private String state;
    private String ownerEmail;
    private String ownerName;
    private String projectTitle;

    public ApplyDTO(final ApplyEntity entity) {
        this.id = entity.getId();
        this.state = entity.getState();
        this.ownerEmail = entity.getOwnerEmail();
        this.ownerName = entity.getOwnerName();
        this.projectTitle = entity.getProjectTitle();
    }

    public static ApplyEntity toEntity(final ApplyDTO dto) {
        ApplyEntity entity = new ApplyEntity();
        entity.setId(dto.getId());
        entity.setState(dto.getState());
        entity.setOwnerEmail(dto.getOwnerEmail());
        entity.setOwnerName(dto.getOwnerName());
        entity.setProjectTitle(dto.getProjectTitle());
        return entity;
    }

}
