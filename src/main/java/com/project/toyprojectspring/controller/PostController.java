package com.project.toyprojectspring.controller;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // 현재 날짜를 반환하는 코드
    private String nowDate() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String formatedNow = now.format(formatter);
        return formatedNow;
    }

    // 두 List에 중복된 값이 존재하는지 확인하는 코드
    private boolean hasCommonElements(List<String> list1, List<String> list2) {
        for (String element : list1) {
            if (list2.contains(element)) {
                return true;
            }
        }
        return false;
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
                if (entity.getState().equals("모집 중")) {
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

    // 모집 글 목록 가져오기(로그인한 유저)
    // 로그인한 유저만 실행 가능
    @GetMapping("/retrieveLoginPost")
    public ResponseEntity<?> retrieveLoginPost(@AuthenticationPrincipal String memberId) {
        // 모집 글 중 상태가 "모집 중"인 것만 가져오기
        List<PostEntity> entities = postService.retrievePost();

        // 로그인한 회원의 관심 스택(filter) 가져오기
        MemberEntity member = memberService.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        List<String> filter = member.getSkills();

        // 회원의 관심 스택에 포함되는 모집 글만 가져오기
        entities.removeIf((item) -> !hasCommonElements(filter, item.getStacks()));

        // entities를 dtos로 스트림 변환
        List<PostDTO> dtos = entities.stream().map(PostDTO::new).collect(Collectors.toList());

        // ResponseDTO 생성
        ResponseDTO<PostDTO> response = ResponseDTO.<PostDTO>builder().data(dtos).build();

        return ResponseEntity.ok().body(response);

    }

    // 모집 글 목록 검색(로그인한 유저)
    // 로그인한 유저만 실행 가능
    @PostMapping("/searchLoginPost/{category}")
    public ResponseEntity<?> searchLoginPost(@AuthenticationPrincipal String memberId, @PathVariable String category,
            @RequestBody Map<String, String> condition) {
        // 모집 글 중 상태가 "모집 중"인 것만 가져오기
        List<PostEntity> entities = postService.retrievePost();
        List<PostEntity> newEntities = new ArrayList<PostEntity>();

        // 로그인한 회원의 관심 스택(filter) 가져오기
        MemberEntity member = memberService.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        List<String> filter = member.getSkills();

        // 회원의 관심 스택에 포함되는 모집 글만 가져오기
        entities.removeIf((item) -> !hasCommonElements(filter, item.getStacks()));

        if (!condition.get("context").equals("")) {
            if (category.equals("none")) {
                for (PostEntity post : entities) {
                    if (post.getTitle().contains(condition.get("context"))
                            || post.getMember().getName().contains(condition.get("context"))) {
                        newEntities.add(post);
                    }
                }
            } else if (category.equals("title")) {
                for (PostEntity post : entities) {
                    if (post.getTitle().contains(condition.get("context"))) {
                        newEntities.add(post);
                    }
                }
            } else if (category.equals("ownerName")) {
                for (PostEntity post : entities) {
                    if (post.getMember().getName().contains(condition.get("context"))) {
                        newEntities.add(post);
                    }
                }
            }
        } else {
            newEntities = entities;
        }

        // entities를 dtos로 스트림 변환
        List<PostDTO> dtos = newEntities.stream().map(PostDTO::new).collect(Collectors.toList());

        // ResponseDTO 생성
        ResponseDTO<PostDTO> response = ResponseDTO.<PostDTO>builder().data(dtos).build();

        return ResponseEntity.ok().body(response);
    }

    // 모집 글 목록 가져오기(로그인하지 않은 유저)
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

    // 모집 글 목록 검색(로그인하지 않은 유저)
    // 로그인 하지 않아도 실행 가능
    @PostMapping("/searchPost/{category}")
    public ResponseEntity<?> searchPost(@AuthenticationPrincipal String memberId, @PathVariable String category,
            @RequestBody Map<String, String> condition) {
        // 모집 글 중 상태가 "모집 중"인 것만 가져오기
        List<PostEntity> entities = postService.retrievePost();
        List<PostEntity> newEntities = new ArrayList<PostEntity>();
        // String state = "";

        if (!condition.get("context").equals("")) {
            if (category.equals("none")) {
                for (PostEntity post : entities) {
                    if (post.getTitle().contains(condition.get("context"))
                            || post.getMember().getName().contains(condition.get("context"))) {
                        newEntities.add(post);
                    }
                }
            } else if (category.equals("title")) {
                for (PostEntity post : entities) {
                    if (post.getTitle().contains(condition.get("context"))) {
                        newEntities.add(post);
                    }
                }
            } else if (category.equals("ownerName")) {
                for (PostEntity post : entities) {
                    if (post.getMember().getName().contains(condition.get("context"))) {
                        newEntities.add(post);
                    }
                }
            }
        } else {
            newEntities = entities;
        }

        // List<String> result = new ArrayList<String>();
        // result.add(state);

        // ResponseDTO<String> response =
        // ResponseDTO.<String>builder().data(result).build();

        // entities를 dtos로 스트림 변환
        List<PostDTO> dtos = newEntities.stream().map(PostDTO::new).collect(Collectors.toList());

        // ResponseDTO 생성
        ResponseDTO<PostDTO> response = ResponseDTO.<PostDTO>builder().data(dtos).build();

        return ResponseEntity.ok().body(response);
    }

    // 모집 글 상세보기
    // 로그인 하지 않아도 실행 가능
    @GetMapping("/retrieveDetail/{postId}")
    public ResponseEntity<?> retrieveDetail(@AuthenticationPrincipal String memberId, @PathVariable String postId) {

        PostEntity post = postService.findById(postId);

        // post를 dtos로 스트림 변환
        PostDTO dto = new PostDTO(post);
        List<PostDTO> dtos = new ArrayList<PostDTO>();
        dtos.add(dto);

        // ResponseDTO 생성
        ResponseDTO<PostDTO> response = ResponseDTO.<PostDTO>builder().data(dtos).build();

        return ResponseEntity.ok().body(response);
    }

    // 내가 작성한 모집 글 보기
    // 로그인한 유저만 실행 가능
    @GetMapping("/myPost")
    public ResponseEntity<?> getMyPost(@AuthenticationPrincipal String memberId) {
        // 로그인한 회원이 작성한 게시물 가져오기
        MemberEntity member = memberService.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        List<PostEntity> entities = member.getPosts();

        // entities를 dtos로 스트림 변환
        // List<PostDTO> dtos =
        // entities.stream().map(PostDTO::new).collect(Collectors.toList());

        // ResponseDTO 생성
        ResponseDTO<PostEntity> response = ResponseDTO.<PostEntity>builder().data(entities).build();

        return ResponseEntity.ok().body(response);
    }

    // 모집 글 상태 변경
    // 로그인한 유저만 실행 가능
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
                if (entity.getState().equals("모집 중")) {
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
