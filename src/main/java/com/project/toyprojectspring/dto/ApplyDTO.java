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

    public ApplyDTO(final ApplyEntity entity) {
        this.id = entity.getId();
        this.state = entity.getState();
    }

    public static ApplyEntity toEntity(final ApplyDTO dto) {
        ApplyEntity entity = new ApplyEntity();
        entity.setId(dto.getId());
        entity.setState(dto.getState());
        return entity;
    }

}
