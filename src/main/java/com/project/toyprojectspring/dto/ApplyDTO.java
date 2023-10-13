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
    private MemberEntity member;
    private PostEntity post;

    public ApplyDTO(final ApplyEntity entity) {
        this.id = entity.getId();
        this.state = entity.getState();
        this.member = entity.getMember();
        this.post = entity.getPost();
    }

    public static ApplyEntity toEntity(final ApplyDTO dto) {
        ApplyEntity entity = new ApplyEntity();
        entity.setId(dto.getId());
        entity.setState(dto.getState());
        entity.setMember(dto.getMember());
        entity.setPost(dto.getPost());
        return entity;
    }

}
