package com.project.toyprojectspring.dto;

import java.util.Date;
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
public class PostDTO {
    private String id;
    private String title;
    private Date due;
    private Date postDate;
    private int recruit;
    private int recruitState;
    private int priod;
    private String desc;
    private String state;
    private List<String> skills;
    private MemberEntity member;
    private List<ApplyEntity> applies;

    public PostDTO(final PostEntity entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.due = entity.getDue();
        this.postDate = entity.getPostDate();
        this.recruit = entity.getRecruit();
        this.recruitState = entity.getRecruitState();
        this.priod = entity.getPriod();
        this.desc = entity.getDesc();
        this.state = entity.getState();
        this.skills = entity.getSkills();
        this.member = entity.getMember();
        this.applies = entity.getApplies();
    }

    public static PostEntity toEntity(final PostDTO dto) {
        PostEntity entity = new PostEntity();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setDue(dto.getDue());
        entity.setPostDate(dto.getPostDate());
        entity.setRecruit(dto.getRecruit());
        entity.setRecruitState(dto.getRecruitState());
        entity.setPriod(dto.getPriod());
        entity.setDesc(dto.getDesc());
        entity.setState(dto.getState());
        entity.setSkills(dto.getSkills());
        entity.setMember(dto.getMember());
        entity.setApplies(dto.getApplies());
        return entity;
    }
}
