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
import com.project.toyprojectspring.dto.ResponseDTO;
import com.project.toyprojectspring.entity.ApplyEntity;
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
            applyEntity.setMember(memberEntity);

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

        // 참여 실패한 참여 요청을 제외하고 가져오기
        for (ApplyEntity entity : entities) {
            if (!entity.getState().equals("참여 실패")) {
                newEntities.add(entity);
            }
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
        List<ApplyEntity> entities = member.getApplies();

        // entities를 dtos로 스트림 변환
        List<ApplyDTO> dtos = entities.stream().map(ApplyDTO::new).collect(Collectors.toList());

        // ResponseDTO 생성
        ResponseDTO<ApplyDTO> response = ResponseDTO.<ApplyDTO>builder().data(dtos).build();

        return ResponseEntity.ok().body(response);
    }

    // 참여 요청 정보 갱신(모집 글 작성자)
    // 로그인한 사용자만 사용 가능
    @PutMapping("/updateApply/{postId}")
    public ResponseEntity<?> updateApply(@AuthenticationPrincipal String memberId,
            @PathVariable("postId") String postId, @RequestBody List<ApplyDTO> dtos) {
        try {

            List<ApplyEntity> requestEntities = new ArrayList<ApplyEntity>();

            // requestEntities에 갱신할 목록의 entity를 저장
            for (ApplyDTO dto : dtos) {
                ApplyEntity entity = ApplyDTO.toEntity(dto);
                requestEntities.add(entity);
            }

            for (ApplyEntity entity : requestEntities) {
                ApplyEntity newEntity = applyService.findById(entity.getId());
                newEntity.setState(entity.getState());
                applyService.updateApply(newEntity);
            }

            List<ApplyEntity> entities = applyService.retrieveWriterApply(postId);

            // entities를 dtos로 스트림 변환
            List<ApplyDTO> responseDTOs = entities.stream().map(ApplyDTO::new).collect(Collectors.toList());

            // ResponseDTO 생성
            ResponseDTO<ApplyDTO> response = ResponseDTO.<ApplyDTO>builder().data(responseDTOs).build();

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<ApplyDTO> response = ResponseDTO.<ApplyDTO>builder().error(error).build();
            return ResponseEntity.badRequest().body(response);
        }
    }
}
