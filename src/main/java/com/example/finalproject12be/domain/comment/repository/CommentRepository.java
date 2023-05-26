package com.example.finalproject12be.domain.comment.repository;




import com.example.finalproject12be.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByIdAndMemberId(Long id, Long memberId);
    List<Comment> findAllByStoreIdOrderByCreatedAtAsc(Long storeId);


}