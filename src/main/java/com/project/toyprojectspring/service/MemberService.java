package com.project.toyprojectspring.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.toyprojectspring.entity.MemberEntity;
import com.project.toyprojectspring.repository.MemberRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;

    // 회원 정보 생성
    public MemberEntity addMember(MemberEntity memberEntity) {
        // 회원 누락 정보 검사
        if (memberEntity == null || memberEntity.getEmail() == null) {
            throw new RuntimeException("Invalid arguments");
        }
        final String email = memberEntity.getEmail();

        // 이메일 중복 검사
        if (memberRepository.existsByEmail(email)) {
            log.warn("Email already exists {}", email);
            throw new RuntimeException("Email already exists");
        }

        return memberRepository.save(memberEntity);
    }

    // 회원 정보 갱신
    public Optional<MemberEntity> updateMember(final MemberEntity entity) {
        // 회원 정보 존재 확인
        if (memberRepository.existsById(entity.getId())) {
            memberRepository.save(entity);
        } else {
            throw new RuntimeException("Unknown id");
        }

        return memberRepository.findById(entity.getId());
    }

    // 회원 탈퇴
    public String deleteMember(final MemberEntity entity) {
        if (memberRepository.existsById(entity.getId())) {
            memberRepository.deleteById(entity.getId());
            return "delete successfully";
        } else {
            throw new RuntimeException("id does not exist");
        }
    }

    // 회원 정보 유효성 확인(Email, Password)
    public MemberEntity getByCredentials(final String email, final String password, final PasswordEncoder encoder) {
        final MemberEntity originalMember = memberRepository.findByEmail(email);
        if (originalMember != null && encoder.matches(password, originalMember.getPassword())) {
            return originalMember;
        }
        return null;
    }

    // 회원 정보 유효성 확인(Key)
    public Optional<MemberEntity> findById(String memberId) {
        return memberRepository.findById(memberId);
    }
}
