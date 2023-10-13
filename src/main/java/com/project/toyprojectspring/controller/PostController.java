package com.project.toyprojectspring.controller;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.toyprojectspring.dto.PostDTO;
import com.project.toyprojectspring.dto.ResponseDTO;
import com.project.toyprojectspring.entity.MemberEntity;
import com.project.toyprojectspring.entity.PostEntity;
import com.project.toyprojectspring.service.ApplyService;
import com.project.toyprojectspring.service.MailService;
import com.project.toyprojectspring.service.MemberService;
import com.project.toyprojectspring.service.PostService;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("post")
public class PostController {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private PostService postService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MailService mailService;

    private String nowDate() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String formatedNow = now.format(formatter);
        return formatedNow;
    }

    // 모집 글 작성
    // 로그인 필요
    @PostMapping("/addPost")
    public ResponseEntity<?> addPost(@AuthenticationPrincipal String memberId, @RequestBody PostDTO postDTO) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

            PostEntity postEntity = PostDTO.toEntity(postDTO);
            MemberEntity member = memberService.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found"));
            Date date = formatter.parse(nowDate());

            postEntity.setId(null);
            postEntity.setMember(member);
            postEntity.setPostDate(date);

            // 모집 글 중 상태가 "모집 중"인 것만 가져오기
            List<PostEntity> entities = postService.addPost(postEntity);
            List<PostEntity> newEntities = new ArrayList<PostEntity>();
            for (PostEntity entity : entities) {
                if (entity.getState() == "모집 중") {
                    newEntities.add(entity);
                }
            }

            // newEntities를 dtos로 스트림 변환
            List<PostDTO> dtos = newEntities.stream().map(PostDTO::new).collect(Collectors.toList());

            // ResponseDTO 생성
            ResponseDTO<PostDTO> response = ResponseDTO.<PostDTO>builder().data(dtos).build();

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<PostDTO> response = ResponseDTO.<PostDTO>builder().error(error).build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 모집 글 목록 가져오기
    // 로그인 하지 않아도 실행 가능
    @GetMapping("/retrievePost")
    public ResponseEntity<?> retrievePostList(@AuthenticationPrincipal String memberId) {

        // 모집 글 중 상태가 "모집 중"인 것만 가져오기
        List<PostEntity> entities = postService.retrievePost();

        // entities를 dtos로 스트림 변환
        List<PostDTO> dtos = entities.stream().map(PostDTO::new).collect(Collectors.toList());

        // ResponseDTO 생성
        ResponseDTO<PostDTO> response = ResponseDTO.<PostDTO>builder().data(dtos).build();

        return ResponseEntity.ok().body(response);
    }

    // 모집 글 상태 변경
    @PostMapping("/updatePost")
    public ResponseEntity<?> updatePost(@AuthenticationPrincipal String memberId, @RequestBody PostDTO postDTO) {
        try {
            PostEntity postEntity = PostDTO.toEntity(postDTO);
            MemberEntity member = memberService.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found"));
            postEntity.setMember(member);
            List<PostEntity> entities = postService.updatePost(postEntity);

            List<PostEntity> newEntities = new ArrayList<PostEntity>();
            for (PostEntity entity : entities) {
                if (entity.getState() == "모집 중") {
                    newEntities.add(entity);
                }
            }

            // newEntities를 dtos로 스트림 변환
            List<PostDTO> dtos = newEntities.stream().map(PostDTO::new).collect(Collectors.toList());

            // ResponseDTO 생성
            ResponseDTO<PostDTO> response = ResponseDTO.<PostDTO>builder().data(dtos).build();

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<PostDTO> response = ResponseDTO.<PostDTO>builder().error(error).build();
            return ResponseEntity.badRequest().body(response);
        }
    }
}
