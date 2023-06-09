package com.example.finalproject12be.domain.comment.entity;

import com.example.finalproject12be.domain.store.entity.Store;
import com.example.finalproject12be.domain.comment.dto.CommentRequestDto;
import com.example.finalproject12be.common.Timestamped;

import com.example.finalproject12be.domain.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Comment extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMENT_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "STORE_ID")
    @JsonIgnore
    private Store store;

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String contents;

    public Comment(CommentRequestDto commentRequestDto, Store store, Member member) {
        this.contents = commentRequestDto.getContents();
        this.member = member;
        this.store = store;
        this.nickname = member.getNickname();
    }

}
