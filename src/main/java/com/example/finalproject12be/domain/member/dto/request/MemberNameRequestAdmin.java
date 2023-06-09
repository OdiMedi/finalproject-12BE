package com.example.finalproject12be.domain.member.dto.request;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;

@Getter
public class MemberNameRequestAdmin {

	@Size(min = 2, max = 10)
	@Pattern(regexp = "^[a-zA-Z가-힣0-9]{2,10}$", message = "닉네임은 2~10자 한글, 알파벳 대소문자, 숫자로 작성해주세요.")
	private String newName;

	@Size(min = 2, max = 10)
	@Pattern(regexp = "^[a-zA-Z가-힣0-9]{2,10}$", message = "닉네임은 2~10자 한글, 알파벳 대소문자, 숫자로 작성해주세요.")
	private String nickname;

}
