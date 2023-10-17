package com.project.toyprojectspring.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.toyprojectspring.entity.ApplyEntity;
import com.project.toyprojectspring.entity.MemberEntity;
import com.project.toyprojectspring.entity.PostEntity;
import com.project.toyprojectspring.repository.ApplyRepository;
import com.project.toyprojectspring.repository.MemberRepository;
import com.project.toyprojectspring.repository.PostRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ApplyService {
    @Autowired
    ApplyRepository appliesRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    // key로 참가 요청 찾기
    public ApplyEntity findById(Long id) {
        return appliesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Post not found"));
    }

    // 이미 참가 요청을 보낸 적이 있는 모집 글인지 판단
    public Boolean existsByPost(PostEntity post, String memberId) {
        List<ApplyEntity> entities = post.getApplies();
        for (ApplyEntity apply : entities) {
            if (apply.getMember().getId().equals(memberId)) {
                return true;
            }
        }
        return false;
    }

    // 참여 요청 추가
    public ApplyEntity addApply(final ApplyEntity entity) {
        if (entity == null) {
            log.warn("Entity cannot be null");
            throw new RuntimeException("Entity cannot be null");
        }
        if (entity.getMember() == null) {
            log.warn("Unknown Member");
            throw new RuntimeException("Unknown Member");
        }
        if (entity.getPost() == null) {
            log.warn("Unknown Post");
            throw new RuntimeException("Unknown Post");
        }

        return appliesRepository.save(entity);
    }

    // 참여 요청 조회(모집 글 작성자)
    public List<ApplyEntity> retrieveWriterApply(final String postId) {
        return postRepository.findAppliesByPostId(postId);
    }

    // 참여 요청 조회(참여 요청자)
    public List<ApplyEntity> retrieveRequestApply(final String memberId) {
        return memberRepository.findAppliesByMemberId(memberId);
    }

    // 참요 요청 갱신
    public ApplyEntity updateApply(final ApplyEntity entity) {
        validate(entity);

        if (appliesRepository.existsById(entity.getId())) {
            appliesRepository.save(entity);
        } else {
            throw new RuntimeException("Unknown id");
        }

        return appliesRepository.findById(entity.getId())
                .orElseThrow(() -> new EntityNotFoundException("Apply not found"));
    }

    // 참여 요청 유효성 확인
    public void validate(final ApplyEntity entity) {
        if (entity == null) {
            log.warn("Entity cannot be null");
            throw new RuntimeException("Entity cannot be null");
        }
        if (entity.getMember() == null) {
            log.warn("Unknown Member");
            throw new RuntimeException("Unknown Member");
        }
        if (entity.getPost() == null) {
            log.warn("Unknown Post");
            throw new RuntimeException("Unknown Post");
        }
    }
}
