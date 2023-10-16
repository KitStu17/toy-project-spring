package com.project.toyprojectspring.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.project.toyprojectspring.entity.ApplyEntity;
import com.project.toyprojectspring.entity.MemberEntity;
import com.project.toyprojectspring.entity.PostEntity;
import com.project.toyprojectspring.repository.MemberRepository;
import com.project.toyprojectspring.repository.PostRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    // key로 모집 글 찾기
    public PostEntity findById(String id) {
        return postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Post not found"));
    }

    // 현재 날짜 생성(String)
    private String nowDate() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String formatedNow = now.format(formatter);
        return formatedNow;
    }

    // 모집 글 추가
    public List<PostEntity> addPost(final PostEntity post) {
        postRepository.save(post);

        return postRepository.findAll(Sort.by(Sort.Direction.DESC, "postDate"));
    }

    // 모집 글 목록 출력
    // 모집 글 상태가 "진행 중"인 것만 가져오기
    // 모집 일자가 현재 날짜를 지났으면 "진행 중" 상태로 변경
    public List<PostEntity> retrievePost() {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

        try {
            Date date = formatter.parse(nowDate());
            List<PostEntity> entities = postRepository.findAll(Sort.by(Sort.Direction.DESC, "postDate"));
            List<PostEntity> newEntities = new ArrayList<PostEntity>();
            for (PostEntity entity : entities) {
                if (entity.getState().equals("모집 중") && (entity.getDue().before(date))) {
                    entity.setState("진행 중");
                    postRepository.save(entity);
                }
            }
            newEntities = postRepository.findByRecruitPosts("모집 중");
            return newEntities;
        } catch (Exception e) {
            return new ArrayList<PostEntity>();
        }
    }

    // 모집 글 작성자 찾기
    public MemberEntity findBymemberId(String memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        return member;
    }

    // 모집 글 수정
    public List<PostEntity> updatePost(final PostEntity post) {
        validate(post);

        if (postRepository.existsById(post.getId())) {
            postRepository.save(post);
        } else {
            throw new RuntimeException("Unknown id");
        }

        return postRepository.findAll();
    }

    // 모집 글 삭제
    public List<PostEntity> deletePost(final PostEntity post) {
        validate(post);

        if (postRepository.existsById(post.getId())) {
            postRepository.deleteById(post.getId());
        } else {
            throw new RuntimeException("Unknown id");
        }

        return postRepository.findAll();
    }

    public void validate(final PostEntity entity) {
        if (entity == null) {
            log.warn("Entity cannot be null");
            throw new RuntimeException("Entity cannot be null");
        }
        if (entity.getMember() == null) {
            log.warn("Unknown Member");
            throw new RuntimeException("Unknown Member");
        }
    }
}
