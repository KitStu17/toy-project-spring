package com.project.toyprojectspring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.toyprojectspring.entity.ApplyEntity;

@Repository
public interface ApplyRepository extends JpaRepository<ApplyEntity, Long> {
    List<ApplyEntity> findByMemberId(String memberId);

    List<ApplyEntity> findByPostId(String postId);
}
