package com.project.toyprojectspring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.project.toyprojectspring.entity.ApplyEntity;
import com.project.toyprojectspring.entity.MemberEntity;
import com.project.toyprojectspring.entity.PostEntity;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, String> {
    MemberEntity findByEmail(String email);

    Boolean existsByEmail(String email);

    MemberEntity findByEmailAndPassword(String email, String password);

    @Query("SELECT t.posts FROM Member t WHERE t.id = :memberId")
    List<PostEntity> findPostsByMemberId(String memberId);

    @Query("SELECT t.applicants FROM Member t WHERE t.id = :memberId")
    List<ApplyEntity> findAppliesByMemberId(String memberId);
}
