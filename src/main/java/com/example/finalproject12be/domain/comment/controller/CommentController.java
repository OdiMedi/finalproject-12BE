package com.example.finalproject12be.domain.comment.controller;



import com.example.finalproject12be.common.ResponseMsgDto;
import com.example.finalproject12be.domain.comment.dto.CommentRequestDto;
import com.example.finalproject12be.domain.comment.dto.CommentResponseDto;
import com.example.finalproject12be.domain.comment.service.CommentService;

import com.example.finalproject12be.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    // 댓글 생성 responseEntity
//    @PostMapping("/{store-id}")
//    public ResponseEntity<CommentResponseDto> createComment(
//            @PathVariable("store-id") Long storeId,
//            @Valid @RequestBody CommentRequestDto commentRequestDto,
//            @AuthenticationPrincipal UserDetailsImpl userDetails) {
//        return commentService.createComment(commentRequestDto, userDetails.getMember(), storeId);
//    }

    // 댓글 생성 api명세서에 맞춰서 코드 내려주기
    @PostMapping("/{store-id}")
    public ResponseMsgDto<CommentResponseDto> createComment(
            @PathVariable("store-id") Long storeId,
            @Valid @RequestBody CommentRequestDto commentRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commentService.createComment(commentRequestDto, userDetails, storeId);
    }

    // 댓글 조회
    @GetMapping("/{store-id}")
    public ResponseEntity<List<CommentResponseDto>> getComments(
            @PathVariable("store-id") Long storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commentService.getComments(storeId, userDetails);
    }

    // 마이페이지 댓글 조회
    @GetMapping("/myComment")
    public ResponseEntity<List<CommentResponseDto>> getUserComments(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<CommentResponseDto> comments = commentService.getUserComments(userDetails);
        return ResponseEntity.ok(comments);
    }

    // 댓글 수정
    @PutMapping("/{store-id}/{comment-id}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable("store-id") Long storeId,
            @PathVariable("comment-id") Long commentId,
            @Valid @RequestBody CommentRequestDto commentRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commentService.updateComment(commentId, commentRequestDto, userDetails.getMember());
    }

    // 댓글 삭제
    @DeleteMapping("/{store-id}/{comment-id}")
    public ResponseEntity<String> deleteComment(
            @PathVariable("store-id") Long storeId,
            @PathVariable("comment-id") Long commentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        commentService.deleteComment(commentId, userDetails.getMember());
        return ResponseEntity.ok("댓글이 삭제되었습니다.");
    }
}