package com.example.finalproject12be.domain.member.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.example.finalproject12be.domain.member.entity.Member;
import com.example.finalproject12be.domain.member.entity.MemberRoleEnum;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MemberSignupRequest {

	@NotBlank(message = "이메일은 필수 입력 값입니다.")
	@Email(message = "이메일 형식에 맞지 않습니다.")
	private String email;

	@Size(min = 8, max = 15)
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,15}$", message = "비밀번호는 8~15자 알파벳 대소문자, 숫자로 작성해주세요.")
	private String password;

	@Size(min = 2, max = 10)
	@Pattern(regexp = "^[a-zA-Z가-힣0-9]{2,10}$", message = "닉네임은 2~10자 한글, 알파벳 대소문자, 숫자로 작성해주세요.")
	private String nickname;

	private boolean admin = false;
	private String adminToken = "";

	public static Member toEntity(MemberSignupRequest memberSignupRequest, String password, MemberRoleEnum role) {
		return Member.of(
			memberSignupRequest.getEmail(),
			password,
			memberSignupRequest.getNickname(),
			role
		);
	}

}
