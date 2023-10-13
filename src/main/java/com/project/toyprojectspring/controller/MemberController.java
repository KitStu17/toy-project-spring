package com.project.toyprojectspring.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.toyprojectspring.dto.MemberDTO;
import com.project.toyprojectspring.dto.ResponseDTO;
import com.project.toyprojectspring.entity.MemberEntity;
import com.project.toyprojectspring.security.TokenProvider;
import com.project.toyprojectspring.service.ApplyService;
import com.project.toyprojectspring.service.MemberService;
import com.project.toyprojectspring.service.PostService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("auth")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private PostService postService;

    @Autowired
    private ApplyService applyService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // JWT 토큰으로 회원 정보 검색
    @GetMapping("/getMember")
    public ResponseEntity<?> retrieveMember(@AuthenticationPrincipal String memberId) {
        try {
            MemberEntity member = memberService.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found"));
            return ResponseEntity.ok().body(member);
        } catch (Exception e) {
            ResponseDTO responseDTO = ResponseDTO.builder().error(e.getMessage()).build();

            return ResponseEntity.badRequest().body(responseDTO);
        }
    }

    // 회원 정보 수정
    @PutMapping("/updateMember")
    public ResponseEntity<?> updateMember(@AuthenticationPrincipal String memberId, @RequestBody MemberDTO memberDTO) {
        try {
            MemberEntity member = memberService.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found"));
            member.setEmail(memberDTO.getEmail());
            member.setPassword(memberDTO.getPassword());
            member.setPosition(memberDTO.getPosition());
            member.setSkills(memberDTO.getSkills());

            MemberEntity responseEntity = memberService.updateMember(member)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found"));

            return ResponseEntity.ok().body(responseEntity);
        } catch (Exception e) {
            ResponseDTO responseDTO = ResponseDTO.builder().error(e.getMessage()).build();

            return ResponseEntity.badRequest().body(responseDTO);
        }
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> registerMember(@RequestBody MemberDTO memberDTO) {
        try {
            MemberEntity member = MemberEntity.builder()
                    .email(memberDTO.getEmail())
                    .name(memberDTO.getName())
                    .password(passwordEncoder.encode(memberDTO.getPassword()))
                    .position(memberDTO.getPosition())
                    .skills(memberDTO.getSkills())
                    .build();

            // DB에 저장된 회원 정보
            MemberEntity registerMember = memberService.addMember(member);

            // 비밀번호가 복호화 된 회원 정보(postman 확인 이후 변경 필요)

            MemberDTO responseMemberDTO = MemberDTO.builder()
                    .id(registerMember.getId())
                    .email(registerMember.getEmail())
                    .name(registerMember.getName())
                    .password(registerMember.getPassword())
                    .position(registerMember.getPosition())
                    .skills(registerMember.getSkills())
                    .build();

            return ResponseEntity.ok().body(responseMemberDTO);

        } catch (Exception e) {
            ResponseDTO responseDTO = ResponseDTO.builder().error(e.getMessage()).build();

            return ResponseEntity.badRequest().body(responseDTO);
        }
    }

    // 로그인
    @PostMapping("/signin")
    public ResponseEntity<?> authenticate(@RequestBody MemberDTO memberDTO) {
        MemberEntity member = memberService.getByCredentials(memberDTO.getEmail(), memberDTO.getPassword(),
                passwordEncoder);

        if (member != null) {
            final String token = tokenProvider.create(member);
            final MemberDTO responseMemberDTO = MemberDTO.builder()
                    .email(member.getEmail())
                    .id(member.getId())
                    .token(token)
                    .build();

            return ResponseEntity.ok().body(responseMemberDTO);
        } else {
            ResponseDTO responseDTO = ResponseDTO.builder().error("Login failed").build();

            return ResponseEntity.badRequest().body(responseDTO);
        }
    }

    // 회원 탈퇴
    @DeleteMapping("/deleteMember")
    public ResponseEntity<?> deleteMember(@AuthenticationPrincipal String memberId, @RequestBody MemberDTO memberDTO) {
        try {
            MemberEntity entity = MemberDTO.toEntity(memberDTO);

            entity.setId(memberId);
            String result = memberService.deleteMember(entity);
            List<String> results = new ArrayList<String>();
            results.add(result);

            ResponseDTO<String> response = ResponseDTO.<String>builder().data(results).build();

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<MemberDTO> response = ResponseDTO.<MemberDTO>builder().error(error).build();

            return ResponseEntity.badRequest().body(response);
        }
    }
}
