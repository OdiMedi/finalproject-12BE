package com.example.finalproject12be.domain.member.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.finalproject12be.domain.member.dto.request.MemberEmailRequest;
import com.example.finalproject12be.domain.member.dto.request.MemberLoginRequest;
import com.example.finalproject12be.domain.member.dto.request.MemberNameRequest;
import com.example.finalproject12be.domain.member.dto.request.MemberNameRequestAdmin;
import com.example.finalproject12be.domain.member.dto.request.MemberPasswordRequest;
import com.example.finalproject12be.domain.member.dto.request.MemberSignupRequest;
import com.example.finalproject12be.domain.member.dto.response.MemberLoginResponse;
import com.example.finalproject12be.domain.member.dto.response.MemberNewNameResponse;
import com.example.finalproject12be.domain.member.entity.Member;
import com.example.finalproject12be.domain.member.service.MemberService;
import com.example.finalproject12be.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@PostMapping("/user/signup")
	public ResponseEntity<String> signup(@RequestBody @Valid final MemberSignupRequest memberSignupRequest) {

		memberService.signup(memberSignupRequest);
		return ResponseEntity.status(HttpStatus.OK).body("회원가입 완료");
	}

	@PostMapping("/user/login")
	public ResponseEntity<MemberLoginResponse> login(
		@RequestBody final MemberLoginRequest memberLoginRequest,
		final HttpServletResponse response) {

		MemberLoginResponse loginResult = memberService.login(memberLoginRequest, response);
		return ResponseEntity.status(HttpStatus.OK).body(loginResult);
	}

	@PostMapping("/user/logout")
	public ResponseEntity<String> logout(HttpServletRequest request,
		final HttpServletResponse response) {
		memberService.logout(request, response);
		return ResponseEntity.ok("로그아웃되었습니다.");
	}

	@PostMapping("/user/change/nickname")
	public ResponseEntity<MemberNewNameResponse> changeNickname(
		@RequestBody @Valid MemberNameRequest memberNameRequest,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	){
		MemberNewNameResponse changeResult = memberService.changeNickname(memberNameRequest.getNewName(), userDetails.getMember());
		return ResponseEntity.status(HttpStatus.OK).body(changeResult);
	}

	@DeleteMapping("/user/signout")
	public ResponseEntity<String> signout(
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		final HttpServletRequest request) {
		memberService.signout(userDetails.getUsername(), request);
		return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
	}

	@PostMapping("/user/find/password")
	public ResponseEntity<Void> findPassword(
		@RequestBody @Valid MemberEmailRequest memberEmailRequest
	){
		memberService.findPassword(memberEmailRequest.getEmail());
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PostMapping("/user/change/password")
	public ResponseEntity<Void> changePassword(
		@RequestBody @Valid MemberPasswordRequest memberPasswordRequest,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	){
		memberService.changePassword(memberPasswordRequest.getNewPassword(), userDetails.getMember());
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PutMapping("/user/change/nickname/admin")
	public ResponseEntity<MemberNewNameResponse> changeNicknameAdmin(
		@RequestBody @Valid MemberNameRequestAdmin memberNameRequest,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	){
		MemberNewNameResponse changeResult = memberService.changeNicknameAdmin(memberNameRequest.getNewName(), userDetails.getMember(), memberNameRequest.getNickname());
		return ResponseEntity.status(HttpStatus.OK).body(changeResult);
	}

	@DeleteMapping("/user/signout/admin")
	public ResponseEntity<String> signOutAdmin(
		@RequestBody @Valid MemberNameRequest memberNameRequest, @AuthenticationPrincipal UserDetailsImpl userDetails,
		final HttpServletRequest request) {
		memberService.signoutAdmin(memberNameRequest, userDetails.getMember(), request);
		return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
	}

}
