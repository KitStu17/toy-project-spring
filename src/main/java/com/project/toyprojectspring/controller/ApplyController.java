package com.project.toyprojectspring.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.toyprojectspring.dto.ApplyDTO;
import com.project.toyprojectspring.dto.MemberDTO;
import com.project.toyprojectspring.dto.ResponseDTO;
import com.project.toyprojectspring.entity.ApplyEntity;
import com.project.toyprojectspring.entity.MemberEntity;
import com.project.toyprojectspring.entity.PostEntity;
import com.project.toyprojectspring.repository.PostRepository;
import com.project.toyprojectspring.service.ApplyService;
import com.project.toyprojectspring.service.MailService;
import com.project.toyprojectspring.service.MemberService;
import com.project.toyprojectspring.service.PostService;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("apply")
public class ApplyController {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private PostService postService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MailService mailService;

    @Autowired
    private PostRepository postRepository;

    // 참여 요청 추가
    // 로그인한 사용자만 사용 가능
    @PostMapping("/addApply/{postId}")
    public ResponseEntity<?> addApply(@AuthenticationPrincipal String memberId, @PathVariable("postId") String postId,
            @RequestBody ApplyDTO applyDTO) {
        try {
            ApplyEntity applyEntity = ApplyDTO.toEntity(applyDTO);
            MemberEntity memberEntity = memberService.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found"));
            PostEntity postEntity = postService.findById(postId);

            applyEntity.setPost(postEntity);
            applyEntity.setOwnerEmail(memberEntity.getEmail());
            applyEntity.setOwnerName(memberEntity.getName());
            applyEntity.setProjectTitle(postEntity.getTitle());
            applyEntity.setMember(memberEntity);

            if (!applyService.existsByPost(postEntity, memberId)) {
                // DB에 참여 요청 정보 저장
                ApplyEntity entity = applyService.addApply(applyEntity);

                // 모집 글 작성자에게 이메일 전송
                mailService.sendSimpleApplyMessage(memberEntity.getEmail(), postEntity, memberEntity);

                List<ApplyEntity> entities = new ArrayList<ApplyEntity>();
                entities.add(entity);

                List<ApplyDTO> dtos = entities.stream().map(ApplyDTO::new).collect(Collectors.toList());

                // ResponseDTO 생성
                ResponseDTO<ApplyDTO> response = ResponseDTO.<ApplyDTO>builder().data(dtos).build();

                return ResponseEntity.ok().body(response);
            } else {
                String error = "already applied";
                ResponseDTO<ApplyDTO> response = ResponseDTO.<ApplyDTO>builder().error(error).build();
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<ApplyDTO> response = ResponseDTO.<ApplyDTO>builder().error(error).build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 참여 요청 가져오기(모집 글 작성자)
    // 로그인한 사용자만 사용 가능
    @GetMapping("/applyListWriter/{postId}")
    public ResponseEntity<?> retrieveWriterApply(@AuthenticationPrincipal String memberId,
            @PathVariable("postId") String postId) {
        // repository에서 memberId가 일치하는 entity 가져오기
        PostEntity post = postService.findById(postId);
        List<ApplyEntity> entities = post.getApplies();
        List<ApplyEntity> newEntities = new ArrayList<ApplyEntity>();

        for (ApplyEntity entity : entities) {
            newEntities.add(entity);
        }

        // entities를 dtos로 스트림 변환
        List<ApplyDTO> dtos = newEntities.stream().map(ApplyDTO::new).collect(Collectors.toList());

        // ResponseDTO 생성
        ResponseDTO<ApplyDTO> response = ResponseDTO.<ApplyDTO>builder().data(dtos).build();

        return ResponseEntity.ok().body(response);
    }

    // 참여 요청 가져오기(참여 요청자)
    // 로그인한 사용자만 사용 가능
    @GetMapping("/applyListRequest")
    public ResponseEntity<?> retrieveRequestApply(@AuthenticationPrincipal String memberId) {
        // repository에서 memberId가 일치하는 entity 가져오기
        MemberEntity member = memberService.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        List<MemberEntity> members = new ArrayList<MemberEntity>();
        members.add(member);
        List<ApplyEntity> entities = member.getApplies();

        // entities를 dtos로 스트림 변환
        List<ApplyDTO> dtos = entities.stream().map(ApplyDTO::new).collect(Collectors.toList());

        // List<MemberDTO> dtos =
        // members.stream().map(MemberDTO::new).collect(Collectors.toList());

        // ResponseDTO 생성
        ResponseDTO<ApplyDTO> response = ResponseDTO.<ApplyDTO>builder().data(dtos).build();

        return ResponseEntity.ok().body(response);
    }

    // 참여 요청 정보 갱신(모집 글 작성자)
    // 로그인한 사용자만 사용 가능
    @PutMapping("/updateApply/{postId}")
    public ResponseEntity<?> updateApply(@AuthenticationPrincipal String memberId,
            @PathVariable("postId") String postId, @RequestBody ApplyDTO dto) {
        try {
            // 참여 요청 상태 변경
            ApplyEntity apply = ApplyDTO.toEntity(dto);
            ApplyEntity newEntity = applyService.findById(apply.getId());
            newEntity.setState(apply.getState());
            applyService.updateApply(newEntity);

            // 참여 상태 변경에 따라 모집 글 상태도 변경
            PostEntity post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Post not found"));
            List<ApplyEntity> applies = post.getApplies();

            int count = 0;
            for (ApplyEntity item : applies) {
                if (item.getState().equals("참여 성공")) {
                    count++;
                }
            }

            post.setRecruitState(count);
            if (post.getRecruit() == count) {
                post.setState("진행 중");
            }
            postRepository.save(post);

            String message = "update successfully";

            List<String> entities = new ArrayList<String>();
            entities.add(message);

            // ResponseDTO 생성
            ResponseDTO<String> response = ResponseDTO.<String>builder().data(entities).build();

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<ApplyDTO> response = ResponseDTO.<ApplyDTO>builder().error(error).build();
            return ResponseEntity.badRequest().body(response);
        }
    }
}
