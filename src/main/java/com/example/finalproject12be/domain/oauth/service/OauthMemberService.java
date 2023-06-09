package com.example.finalproject12be.domain.oauth.service;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.finalproject12be.domain.member.dto.TokenDto;
import com.example.finalproject12be.domain.member.entity.Member;
import com.example.finalproject12be.domain.member.entity.MemberRoleEnum;
import com.example.finalproject12be.domain.member.entity.RefreshToken;
import com.example.finalproject12be.domain.member.repository.MemberRepository;
import com.example.finalproject12be.domain.member.repository.RefreshTokenRepository;
import com.example.finalproject12be.domain.oauth.dto.KakaoMemberInfoRequest;

import com.example.finalproject12be.security.jwt.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OauthMemberService {

	private final MemberRepository memberRepository;
	private final JwtUtil jwtUtil;
	private final PasswordEncoder passwordEncoder;
	private final RefreshTokenRepository refreshTokenRepository;


	public String[] kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {

		String[] tokenArray = getToken(code);
		// 1. "인가 코드"로 "액세스, refresh 토큰" 요청
		String kakaoAccessToken = tokenArray[0];
		String kakaoRefreshToken = tokenArray[1];

		// 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
		KakaoMemberInfoRequest kakaoMemberInfo = getKakaoUserInfo(kakaoAccessToken);

		// 3. 필요시에 회원 가입
		Member kakaoMember = registerKakaoUserIfNeeded(kakaoMemberInfo);

		// 4. JWT 토큰 반환
		TokenDto tokenDto = jwtUtil.createAllToken(kakaoMember.getEmail(), MemberRoleEnum.USER); // Access, Refresh 토큰 생성

		Optional<RefreshToken> refreshToken = refreshTokenRepository.findByEmail(kakaoMember.getEmail());
		if(refreshToken.isPresent()) {
			RefreshToken updateToken = refreshToken.get().updateToken(tokenDto.getRefreshToken().substring(7));
			refreshTokenRepository.save(updateToken);
		} else {
			RefreshToken newToken =  new RefreshToken(tokenDto.getRefreshToken().substring(7), kakaoMember.getEmail());
			refreshTokenRepository.save(newToken);
		}
		//        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, createToken);

		String[] tokenArrayResult = new String[5];
		tokenArrayResult[0] = tokenDto.getAccessToken();
		tokenArrayResult[1] = tokenDto.getRefreshToken();
		tokenArrayResult[2] = kakaoMemberInfo.getEmail();
		tokenArrayResult[3]	=  kakaoMember.getNickname();//

		tokenArrayResult[4] = "Bearer " + kakaoAccessToken;

		System.out.println(kakaoAccessToken + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

		return tokenArrayResult;
	}

	// 1. "인가 코드"로 "액세스 토큰" 요청
	private String[] getToken(String code) throws JsonProcessingException {

		// HTTP Header 생성
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		// HTTP Body 생성
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "authorization_code");

		//body.add("client_id", "048f9445160611c1cc986c481c2d6b94");//내 앱 rest api 키
		//body.add("redirect_uri", "http://localhost:8080/user/signin/kakao");

		body.add("client_id", "7463ed7e96bc168b9023480e535add90");//오디약 rest api 키
		//body.add("redirect_uri", "https://finalproject-12-fe.vercel.app/user/signin/kakao");//오디약 redirect url
		body.add("redirect_uri", "http://localhost:3000/user/signin/kakao");// 프런트 로컬 redirect url

		body.add("code", code);

		// HTTP 요청 보내기
		HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
			new HttpEntity<>(body, headers);
		RestTemplate rt = new RestTemplate();
		ResponseEntity<String> response = rt.exchange(
			"https://kauth.kakao.com/oauth/token",
			HttpMethod.POST,
			kakaoTokenRequest,
			String.class
		);

		// HTTP 응답 (JSON) -> 액세스 토큰 파싱
		String responseBody = response.getBody();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(responseBody);

		String[] tokenArray = new String[2];
		tokenArray[0] = jsonNode.get("access_token").asText();
		tokenArray[1] = jsonNode.get("refresh_token").asText();

		return tokenArray;
	}



	// 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
	private KakaoMemberInfoRequest getKakaoUserInfo(String accessToken) throws JsonProcessingException {

		// HTTP Header 생성
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		// HTTP 요청 보내기
		HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);

		RestTemplate rt = new RestTemplate();
		ResponseEntity<String> response = rt.exchange(
			"https://kapi.kakao.com/v2/user/me",
			HttpMethod.POST,
			kakaoUserInfoRequest,
			String.class
		);

		String responseBody = response.getBody();

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(responseBody);

		Long id = jsonNode.get("id").asLong();
		String nickname = jsonNode.get("properties")
			.get("nickname").asText();
		String email = jsonNode.get("kakao_account")
			.get("email").asText();

		log.info("카카오 사용자 정보: " + id + ", " + nickname + ", " + email);
		return new KakaoMemberInfoRequest(id, email, nickname);
	}

	// 3. 필요시에 회원가입
	private Member registerKakaoUserIfNeeded(KakaoMemberInfoRequest kakaoUserInfo) {
		// DB 에 중복된 Kakao email 가 있는지 확인
		Long kakaoId = kakaoUserInfo.getId();
		Member kakaoUser = memberRepository.findByKakaoId(kakaoId)
			.orElse(null);

		if (kakaoUser == null) {
			//카카오 사용자 email 동일한 email 가진 회원이 있는지 확인
			String kakaoEmail = kakaoUserInfo.getEmail();
			Member sameEmailUser = memberRepository.findByEmail(kakaoEmail).orElse(null);
			if (sameEmailUser != null) {
				kakaoUser = sameEmailUser;
				// 기존 회원정보에 카카오 Id 추가
				kakaoUser = kakaoUser.kakaoIdUpdate(kakaoId);
			} else {
				// 신규 회원가입
				String email = kakaoUserInfo.getEmail();

				int leftLimit = 97; // letter 'a'
				int rightLimit = 122; // letter 'z'
				int targetStringLength = 8;
				Random random = new Random();
				String nickname = random.ints(leftLimit, rightLimit + 1)
					.limit(targetStringLength)
					.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
					.toString();
				
				String password = UUID.randomUUID().toString();
				String encodedPassword = passwordEncoder.encode(password);

				kakaoUser = new Member(email, encodedPassword, nickname, kakaoId, MemberRoleEnum.USER);
				memberRepository.save(kakaoUser);
			}
		}

		return kakaoUser;
	}
}
