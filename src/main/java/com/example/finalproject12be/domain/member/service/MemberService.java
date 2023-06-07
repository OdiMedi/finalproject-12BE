package com.example.finalproject12be.domain.member.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.finalproject12be.domain.member.dto.response.MemberLoginResponse;
import com.example.finalproject12be.domain.member.dto.response.MemberNewNameResponse;
import com.example.finalproject12be.exception.MemberErrorCode;
import com.example.finalproject12be.exception.RestApiException;
import com.example.finalproject12be.security.UserDetailsImpl;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.finalproject12be.domain.member.dto.request.MemberLoginRequest;
import com.example.finalproject12be.domain.member.dto.request.MemberSignupRequest;
import com.example.finalproject12be.domain.member.dto.TokenDto;
import com.example.finalproject12be.domain.member.entity.Member;
import com.example.finalproject12be.domain.member.entity.RefreshToken;
import com.example.finalproject12be.domain.member.repository.MemberRepository;
import com.example.finalproject12be.domain.member.repository.RefreshTokenRepository;
import com.example.finalproject12be.security.jwt.JwtUtil;
//import com.example.finalproject12be.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final JwtUtil jwtUtil;
	private final PasswordEncoder passwordEncoder;
	private final RefreshTokenRepository refreshTokenRepository;
	private final EmailService emailService;

	@Transactional
	public void signup(final MemberSignupRequest memberSignupRequest) {

		throwIfExistOwner(memberSignupRequest.getEmail(), memberSignupRequest.getNickname());
		String password = passwordEncoder.encode(memberSignupRequest.getPassword());
		Member member = MemberSignupRequest.toEntity(memberSignupRequest, password);
		memberRepository.save(member);
	}

	@Transactional
	public MemberLoginResponse login(final MemberLoginRequest memberLoginRequest, final HttpServletResponse response) {

		String email = memberLoginRequest.getEmail();
		String password = memberLoginRequest.getPassword();
		Member searchedMember = memberRepository.findByEmail(email).orElseThrow(
			() -> new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND));

		System.out.println(password);
		System.out.println(searchedMember.getPassword());

		if(!passwordEncoder.matches(password, searchedMember.getPassword())){
			throw new RestApiException(MemberErrorCode.INVALID_PASSWORD);
		}

		TokenDto tokenDto = jwtUtil.createAllToken(email);
		Optional<RefreshToken> refreshToken = refreshTokenRepository.findByEmail(email);

		if(refreshToken.isPresent()) {
			RefreshToken updateToken = refreshToken.get().updateToken(tokenDto.getRefreshToken().substring(7));
			refreshTokenRepository.save(updateToken);
		} else {
			RefreshToken newToken = new RefreshToken(tokenDto.getRefreshToken().substring(7), memberLoginRequest.getEmail());
			refreshTokenRepository.save(newToken);
		}

		response.addHeader(jwtUtil.ACCESS_KEY, tokenDto.getAccessToken());
		response.addHeader(jwtUtil.REFRESH_KEY, tokenDto.getRefreshToken());

		MemberLoginResponse loginResult = new MemberLoginResponse(searchedMember.getEmail(), searchedMember.getNickname());
		return loginResult;

	}

	public void logout(HttpServletRequest request, HttpServletResponse response) {

		String accessToken = jwtUtil.resolveToken(request, JwtUtil.ACCESS_KEY);
		String kakaoAccessToken = request.getHeader("Authorization");

		if (kakaoAccessToken == null){
			if (accessToken != null) {
				boolean isAccessTokenExpired = jwtUtil.validateToken(accessToken);
				if (!isAccessTokenExpired) {
					String username = jwtUtil.getUserInfoFromToken(accessToken);
					// 액세스 토큰을 무효화하는 작업 수행
					Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
					if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
						UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
						if (username.equals(userDetails.getUsername())) {
							SecurityContextHolder.clearContext();
						}
					}
				}
			}

			String refreshToken = jwtUtil.resolveToken(request, JwtUtil.REFRESH_KEY);
			if (refreshToken != null) {
				boolean isRefreshTokenValid = jwtUtil.refreshTokenValidation(refreshToken);
				if (isRefreshTokenValid) {
					String username = jwtUtil.getUserInfoFromToken(refreshToken);
					// 리프레시 토큰을 무효화하는 작업 수행
					// 여기에 리프레시 토큰을 저장하는 로직 또는 DB에서 삭제하는 로직을 추가해야 합니다.
				}
			}

			// 로그아웃 후 필요한 작업 수행
			response.setHeader(jwtUtil.ACCESS_KEY, null);
			response.setHeader(jwtUtil.REFRESH_KEY, null);
		}
		else { //소셜 회원 로그아웃 로직
			String reqURL = "https://kapi.kakao.com/v1/user/logout";
			try {
				URL url = new URL(reqURL);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Authorization", kakaoAccessToken);

				int responseCode = conn.getResponseCode();
				System.out.println("responseCode : " + responseCode);

				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				String result = "";
				String line = "";

				while ((line = br.readLine()) != null) {
					result += line;
				}
				System.out.println(result);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// HttpHeaders headers = new HttpHeaders();
			// headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
			// headers.add("Authorization", kakaoAccessToken);
			//
			// // HTTP Body 생성
			// MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
			// body.add("message", "로그아웃");
			//
			// HttpEntity<MultiValueMap<String, String>> kakaoTokenForLogout =
			// 	new HttpEntity<>(body, headers);
			// RestTemplate rt = new RestTemplate();
			//
			// ResponseEntity<String> responseLogout = rt.exchange(
			// 	URI.create("https://kapi.kakao.com/v1/user/logout"),
			// 	HttpMethod.POST,
			// 	kakaoTokenForLogout,
			// 	String.class
			// );
			// System.out.println("로그아웃 완료!!!!!!!!!!!!!!!!!!!!");
		}

	}

	@Transactional
	public void signout(String email, final HttpServletRequest request) {

		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() ->  new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND));

		String kakaoAccessToken = request.getHeader("authorization");
		System.out.println(kakaoAccessToken + "!!!!!!!!!!!!!!!!!!!kakao 토큰!!!!!!!!!!!!!!!!!!!");
		System.out.println("토큰토큰 확인확인" + kakaoAccessToken);
		if (member.getKakaoId() == null) {
			// 카카오 소셜 로그인이 아닌 일반 가입 회원의 경우 직접 삭제
			memberRepository.delete(member);
		} else {
			// 카카오 소셜 로그인 회원의 경우 카카오 계정 연결 해제 후 삭제
			disconnectKakaoAccount(kakaoAccessToken);
			memberRepository.delete(member);
		}
	}

	private void disconnectKakaoAccount(String kakaoAccessToken) {
		// *** 카카오 API를 사용하여 카카오 계정 연결 해제 로직 구현해주셔야합니다 ***
		// *** 카카오 계정 연결 해제 작업 수행 ***
		// HTTP Header 생성

		String reqURL = "https://kapi.kakao.com/v1/user/unlink";
		System.out.println(kakaoAccessToken + "탈퇴 시작 로직 시작 !!!!!!!!! ");
		try {
			URL url = new URL(reqURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", kakaoAccessToken);

			int responseCode = conn.getResponseCode();
			System.out.println("responseCode : " + responseCode);

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
			String result = "";
			String line = "";

			while ((line = br.readLine()) != null) {
				result += line;
			}
			System.out.println(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(kakaoAccessToken + "탈퇴 시작 로직 마무리 !!!!!!!!! ");
		// HttpHeaders headers = new HttpHeaders();
		// headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		// headers.add("Authorization", kakaoAccessToken);
		//
		// // HTTP Body 생성
		// MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
		// body.add("message", "탈퇴 완료");
		// HttpEntity<MultiValueMap<String, String>> kakaoTokenDisconnect =
		// 	new HttpEntity<>(body, headers);
		// RestTemplate rt = new RestTemplate();
		// System.out.println("HTTP!!!!!!!!!!~");
		//
		// ResponseEntity<String> signOut = rt.exchange(
		// 	"https://kapi.kakao.com/v1/user/unlink",
		// 	HttpMethod.POST,
		// 	kakaoTokenDisconnect,
		// 	String.class
		// );
		// System.out.println("소셜 계정 삭제 실행 완료!!!!!!!!!!~");
	}

	private void throwIfExistOwner(String loginEmail, String loginNickName) {

		Optional<Member> searchedEmail = memberRepository.findByEmail(loginEmail);
		Optional<Member> searchedNickName = memberRepository.findByNickname(loginNickName);

		if (searchedEmail.isPresent()) {
			throw new RestApiException(MemberErrorCode.DUPLICATED_EMAIL);
		}

		if(searchedNickName.isPresent()){
			throw new RestApiException(MemberErrorCode.DUPLICATED_MEMBER);
		}
	}

	// @Transactional
	public MemberNewNameResponse changeNickname(String newName, Member member) {
		Optional<Member> memberOptional = memberRepository.findByNickname(newName);

		if(memberOptional.isPresent()){
			throw new RestApiException(MemberErrorCode.DUPLICATED_MEMBER);
		}

		member.updateName(newName);
		memberRepository.save(member);

		return new MemberNewNameResponse(newName);
	}

	//ing
	public void findPassword(String email) {

		Optional<Member> memberOptional = memberRepository.findByEmail(email);

		//로그인 한 유저의 이메일과 요청받은 이메일이 같은가?
		//TODO: 예외 던지기
		if(memberOptional.isPresent()){
			Member member = memberOptional.get();

			int leftLimit = 97; // letter 'a'
			int rightLimit = 122; // letter 'z'
			int targetStringLength = 10;
			Random random = new Random();
			String newPassword = random.ints(leftLimit, rightLimit + 1)
				.limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();

			String encodePw = passwordEncoder.encode(newPassword);
			member.updatePassword(encodePw);
			memberRepository.save(member);

			emailService.sendMail(newPassword, email);
		}else{
			throw new RestApiException(MemberErrorCode.INACTIVE_MEMBER);
		}
	}

	public void changePassword(String password, Member member) {

		String encodePw = passwordEncoder.encode(password);
		member.updatePassword(encodePw);
		memberRepository.save(member);

	}

	// @Override

	// public void mailSend() {
	//
	// 	@Autowired
	// 	MailSender mailSender;
	//
	//
	//
	// 	// System.out.println("전송 완료!");
	// 	SimpleMailMessage message = new SimpleMailMessage();
	// 	message.setTo("kmskes1125@gmail.com"); //수신자 설정
	// 	message.setSubject("오디약! 비밀번호 변경"); //메일 제목
	// 	message.setText("이건 메일 내용이고 임시 비밀번호를 보낼 예정입니다."); //메일 내용 설정
	// 	message.setFrom("kmskes0917@naver.com"); //발신자 설정
	// 	// message.setReplyTo("보낸이@naver.com");
	// 	// System.out.println("message"+message);
	// 	mailSender.send(message);
	// }
}
