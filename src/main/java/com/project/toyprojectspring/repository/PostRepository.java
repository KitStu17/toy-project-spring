package com.project.toyprojectspring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.project.toyprojectspring.entity.ApplyEntity;
import com.project.toyprojectspring.entity.PostEntity;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, String> {
    List<PostEntity> findByMemberId(String memberId);

    @Query("SELECT t.applicants FROM Post t WHERE t.id = :postId")
    List<ApplyEntity> findAppliesByPostId(String postId);
}
