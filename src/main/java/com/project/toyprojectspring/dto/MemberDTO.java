package com.project.toyprojectspring.dto;

import java.util.List;

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
public class MemberDTO {
    private String token;
    private String id;
    private String name;
    private String email;
    private String password;
    private String position;
    private List<String> skills;
    private List<PostEntity> posts;
    private List<ApplyEntity> applies;

    public static MemberEntity toEntity(final MemberDTO dto) {
        MemberEntity entity = new MemberEntity();
        entity.setEmail(dto.getEmail());
        entity.setName(dto.getName());
        entity.setPassword(dto.getPassword());
        entity.setPosition(dto.getPosition());
        entity.setSkills(dto.getSkills());
        entity.setPosts(dto.getPosts());
        entity.setApplies(dto.getApplies());

        return entity;
    }
}
