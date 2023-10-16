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

    @Query(value = "SELECT * FROM Post t WHERE t.member_id = ?1", nativeQuery = true)
    List<PostEntity> findPostsByMemberId(String memberId);

    @Query(value = "SELECT * FROM Apply_Entity t WHERE t.member_id = ?1", nativeQuery = true)
    List<ApplyEntity> findAppliesByMemberId(String memberId);
}
