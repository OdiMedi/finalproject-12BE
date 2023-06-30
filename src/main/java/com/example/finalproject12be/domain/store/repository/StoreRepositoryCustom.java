package com.example.finalproject12be.domain.store.repository;

import static com.example.finalproject12be.domain.bookmark.entity.QBookmark.*;
import static com.example.finalproject12be.domain.store.entity.QStore.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.example.finalproject12be.domain.bookmark.repository.BookmarkRepository;
import com.example.finalproject12be.domain.store.dto.MappedSearchRequest;
import com.example.finalproject12be.domain.member.entity.Member;
import com.example.finalproject12be.domain.store.dto.ForeignStoreResponse;
import com.example.finalproject12be.domain.store.dto.MappedSearchForeignRequest;
import com.example.finalproject12be.domain.store.dto.StoreResponseDto;
import com.example.finalproject12be.domain.store.entity.Store;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.example.finalproject12be.security.UserDetailsImpl;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;
	private final BookmarkRepository bookmarkRepository;

	public List<Store> searchStoreWithinDistance(String baseRadius, String baseLatitude, String baseLongitude) {

			NumberExpression<Double> distance = distance(baseLatitude, baseLongitude, store.latitude, store.longitude);

			return jpaQueryFactory
				.select(store)
				.from(store)
				.where(
					withinDistance(baseLatitude, baseLongitude, store.latitude, store.longitude)

				)
				.orderBy(distance.asc())
				.fetch();

	}

	public List<StoreResponseDto> searchStoreWithinDistancee(String baseRadius, String baseLatitude, String baseLongitude) {

		NumberExpression<Double> distance = distance(baseLatitude, baseLongitude, store.latitude, store.longitude);

		return  jpaQueryFactory
			.select(Projections.constructor(StoreResponseDto.class,
				store.id, store.address, store.name, store.callNumber,
				store.weekdaysTime, store.longitude, store.latitude
			))
			.from(store)
			.where(
				withinDistance(baseLatitude, baseLongitude, store.latitude, store.longitude)
			)
			.orderBy(distance.asc())
			.fetch();
	}

	public Page<StoreResponseDto> searchStoreWithFilter(MappedSearchRequest request, UserDetailsImpl userDetails) {

		int page = request.getPage();
		int size = request.getSize();

		NumberExpression<Double> distance = distance(request.getLatitude(), request.getLongitude(), store.latitude, store.longitude);

		if (distance == null) {

			SubQueryExpression<Long> bookmarkCountSubquery = JPAExpressions.select(bookmark.id.count())
				.from(bookmark)
				.where(bookmark.store.eq(store));

			QueryResults<StoreResponseDto> results = jpaQueryFactory
				.select(Projections.constructor(StoreResponseDto.class,
					store.id, store.address, store.name, store.callNumber,
					store.weekdaysTime, store.longitude, store.latitude,
					bookmarkCountSubquery))
				.from(store)
				.where(
					eqAddress(request.getGu()),
					eqStoreName(request.getStoreName()),
					checkOpen(request.isOpen()),
					checkHolidayOpen(request.isHolidayBusiness()),
					checkNightdOpen(request.isNightBusiness())
				)
				.orderBy(store.name.asc())
				.offset(page * size)
				.limit(size)
				.fetchResults();

				// 로그인한 유저의 북마크 정보 가져오기
				List<Long> bookmarkedStoreIds = new ArrayList<>();

				if (userDetails != null) {
					Member member = userDetails.getMember();
					bookmarkedStoreIds = bookmarkRepository.findStoreIdsByMember(member);
				}

				List<StoreResponseDto> storeResponseDtos = results.getResults();

				// 북마크 여부 체크하여 StoreResponseDto에 설정
				for (StoreResponseDto result : storeResponseDtos) {
					if (bookmarkedStoreIds.contains(result.getStoreId())) {
						result.setBookmark(true);
					}
				}

			return new PageImpl<>(results.getResults(), PageRequest.of(page, size), results.getTotal());
		}
		else {
			SubQueryExpression<Long> bookmarkCountSubquery = JPAExpressions.select(bookmark.id.count())
				.from(bookmark)
				.where(bookmark.store.eq(store));

			QueryResults<StoreResponseDto> results = jpaQueryFactory
				.select(Projections.constructor(StoreResponseDto.class,
					store.id, store.address, store.name, store.callNumber,
					store.weekdaysTime, store.longitude, store.latitude,
					bookmarkCountSubquery))
				.from(store)
				.where(
					withinDistance(request.getLatitude(), request.getLongitude(), store.latitude, store.longitude),
					eqAddress(request.getGu()),
					eqStoreName(request.getStoreName()),
					checkOpen(request.isOpen()),
					checkHolidayOpen(request.isHolidayBusiness()),
					checkNightdOpen(request.isNightBusiness())
				)
				.orderBy(distance.asc(), store.name.asc())
				.offset(page * size)
				.limit(size)
				.fetchResults();

			// 로그인한 유저의 북마크 정보 가져오기
			List<Long> bookmarkedStoreIds = new ArrayList<>();

			if (userDetails != null) {
				Member member = userDetails.getMember();
				bookmarkedStoreIds = bookmarkRepository.findStoreIdsByMember(member);
			}

			List<StoreResponseDto> storeResponseDtos = results.getResults();
			// 북마크 여부 체크하여 StoreResponseDto에 설정
			for (StoreResponseDto result : storeResponseDtos) {
				if (bookmarkedStoreIds.contains(result.getStoreId())) {
					result.setBookmark(true);

				}
			}

			return new PageImpl<>(results.getResults(), PageRequest.of(page, size), results.getTotal());
		}

	}

	public Page<ForeignStoreResponse> searchForeignStoreWithFilter(MappedSearchForeignRequest request, UserDetailsImpl userDetails) {

		int page = request.getPage();
		int size = request.getSize();
		NumberExpression<Double> distance = distance(request.getLatitude(), request.getLongitude(), store.latitude, store.longitude);

		if (distance == null) {
			SubQueryExpression<Long> bookmarkCountSubquery = JPAExpressions.select(bookmark.id.count())
				.from(bookmark)
				.where(bookmark.store.eq(store));

			QueryResults<ForeignStoreResponse> results = jpaQueryFactory
				.select(Projections.constructor(
					ForeignStoreResponse.class,
					store.id, store.address, store.name, store.callNumber,
					store.weekdaysTime, store.longitude, store.latitude,
					store.english, store.chinese, store.japanese,
					bookmarkCountSubquery))
				.from(store)
				.where(
					eqAddress(request.getGu()),
					eqStoreName(request.getStoreName()),
					checkOpen(request.isOpen()),
					checkHolidayOpen(request.isHolidayBusiness()),
					checkNightdOpen(request.isNightBusiness()),
					eqEnglish(request.getEnglish()),
					eqChinese(request.getChinese()),
					eqJapanese(request.getJapanese())
				)
				.orderBy(store.name.asc())
				.offset(page * size)
				.limit(size)
				.fetchResults();
			// 로그인한 유저의 북마크 정보 가져오기
			List<Long> bookmarkedStoreIds = new ArrayList<>();

			if (userDetails != null) {
				Member member = userDetails.getMember();
				bookmarkedStoreIds = bookmarkRepository.findStoreIdsByMember(member);
			}

			List<ForeignStoreResponse> foreignStoreResponses = results.getResults();

			// 북마크 여부 체크하여 StoreResponseDto에 설정
			for (ForeignStoreResponse result : foreignStoreResponses) {
				if (bookmarkedStoreIds.contains(result.getStoreId())) {
					result.setBookmark(true);
				}
			}

			return new PageImpl<>(results.getResults(), PageRequest.of(page, size), results.getTotal());
		}
		else {
			SubQueryExpression<Long> bookmarkCountSubquery = JPAExpressions.select(bookmark.id.count())
				.from(bookmark)
				.where(bookmark.store.eq(store));

			QueryResults<ForeignStoreResponse> results = jpaQueryFactory
				.select(Projections.constructor(
					ForeignStoreResponse.class,
					store.id, store.address, store.name, store.callNumber,
					store.weekdaysTime, store.longitude, store.latitude,
					store.english, store.chinese, store.japanese,
					bookmarkCountSubquery))
				.from(store)
				.where(
					withinDistance(request.getLatitude(), request.getLongitude(), store.latitude, store.longitude),
					eqAddress(request.getGu()),
					eqStoreName(request.getStoreName()),
					checkOpen(request.isOpen()),
					checkHolidayOpen(request.isHolidayBusiness()),
					checkNightdOpen(request.isNightBusiness()),
					eqEnglish(request.getEnglish()),
					eqChinese(request.getChinese()),
					eqJapanese(request.getJapanese())
				)
				.orderBy(distance.asc(), store.name.asc())
				.offset(page * size)
				.limit(size)
				.fetchResults();
			// 로그인한 유저의 북마크 정보 가져오기
			List<Long> bookmarkedStoreIds = new ArrayList<>();

			if (userDetails != null) {
				Member member = userDetails.getMember();
				bookmarkedStoreIds = bookmarkRepository.findStoreIdsByMember(member);
			}

			List<ForeignStoreResponse> foreignStoreResponses = results.getResults();

			// 북마크 여부 체크하여 StoreResponseDto에 설정
			for (ForeignStoreResponse result : foreignStoreResponses) {
				if (bookmarkedStoreIds.contains(result.getStoreId())) {
					result.setBookmark(true);
				}
			}

			return new PageImpl<>(results.getResults(), PageRequest.of(page, size), results.getTotal());
		}
	}

	public Page<ForeignStoreResponse> fortes_1(MappedSearchForeignRequest request, UserDetailsImpl userDetails) {

		int page = request.getPage();
		int size = request.getSize();

			QueryResults<ForeignStoreResponse> results = jpaQueryFactory
				.select(Projections.constructor(
					ForeignStoreResponse.class,
					store.id, store.address, store.name, store.callNumber,
					store.weekdaysTime, store.longitude, store.latitude,
					store.english, store.chinese, store.japanese))
				.from(store)
				.where(
					withinDistance(request.getLatitude(), request.getLongitude(), store.latitude, store.longitude),
					eqAddressTest(request.getGu()),
					eqStoreName(request.getStoreName()),
					checkOpen(request.isOpen()),
					checkHolidayOpen(request.isHolidayBusiness()),
					checkNightdOpen(request.isNightBusiness()),
					eqEnglish(request.getEnglish()),
					eqChinese(request.getChinese()),
					eqJapanese(request.getJapanese())
				)
				.offset(page * size)
				.limit(size)
				.fetchResults();

			return new PageImpl<>(results.getResults(), PageRequest.of(page, size), results.getTotal());

	}


	private BooleanExpression eqAddress(String gu) {

		if (gu == null) {
			return null;
		}
		if(gu.equals("gangnam-gu")){
			gu = "강남구";
		}else if(gu.equals("gangdong-gu")){
			gu = "강동구";
		}else if(gu.equals("gangbuk-gu")){
			gu = "강북구";
		}else if(gu.equals("gangseo-gu")){
			gu = "강서구";
		}else if(gu.equals("gwanak-gu")){
			gu = "관악구";
		}else if(gu.equals("gwangjin-gu")){
			gu = "광진구";
		}else if(gu.equals("guro-gu")){
			gu = "구로구";
		}else if(gu.equals("geumcheon-gu")){
			gu = "금천구";
		}else if(gu.equals("nowon-gu")){
			gu = "노원구";
		}else if(gu.equals("dobong-gu")){
			gu = "도봉구";
		}else if(gu.equals("dongdaemun-gu")){
			gu = "동대문구";
		}else if(gu.equals("dongjak-gu")){
			gu = "동작구";
		}else if(gu.equals("Mapo-gu")){
			gu = "마포구";
		}else if(gu.equals("seodaemun-gu")){
			gu = "서대문구";
		}else if(gu.equals("seocho-gu")){
			gu = "서초구";
		}else if(gu.equals("seongdong-gu")){
			gu = "성동구";
		}else if(gu.equals("seongbuk-gu")){
			gu = "성북구";
		}else if(gu.equals("songpa-gu")){
			gu = "송파구";
		}else if(gu.equals("yeongdeungpo-gu")){
			gu = "영등포구";
		}else if(gu.equals("yangcheon-gu")){
			gu = "양천구";
		}else if(gu.equals("yongsan-gu")){
			gu = "용산구";
		}else if(gu.equals("eunpyeong-gu")){
			gu = "은평구";
		}else if(gu.equals("jongno-gu")){
			gu = "종로구";
		}else if(gu.equals("jung-gu")){
			gu = "중구";
		}else if(gu.equals("jungnang-gu")){
			gu = "중랑구";
		}

		return gu != null ? store.address.like("%" + gu + "%") : null;
	}

	private BooleanExpression eqAddressTest(String gu) {

		if (gu == null) {
			return null;
		}

		return gu != null ? store.address.like("%" + gu + "%") : null;
	}

	private BooleanExpression eqStoreName(String storeName) {
		return storeName != null ? store.name.like("%" + storeName + "%") : null;
	}


	private BooleanExpression checkOpen(boolean open) {
		if (open) {
			LocalDate currentDate = LocalDate.now();
			DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
			String currentDateTime = LocalDateTime.now().format(timeFormatter);
			//String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("17:00"));
			if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {

				BooleanExpression startCondition = Expressions.booleanTemplate(
					"TIME({0}) <= TIME_FORMAT(SUBSTRING_INDEX(weekdays_time, ' ', -1), '%H:%i')",
					currentDateTime
				);

				BooleanExpression endCondition = Expressions.booleanTemplate(
					"TIME({0}) >= TIME_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(weekdays_time, ' ~', 1), ' ', -1), '%H:%i')",
					currentDateTime
				);

				BooleanExpression equalCondition = Expressions.booleanTemplate(
					"TIME_FORMAT(SUBSTRING_INDEX(weekdays_time, ' ', -1), '%H:%i') = TIME_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(weekdays_time, ' ~', 1), ' ', -1), '%H:%i')"
				);

				return startCondition.and(endCondition).or(equalCondition);
			} else if (dayOfWeek == DayOfWeek.SATURDAY) {

				BooleanExpression startCondition = Expressions.booleanTemplate(
					"TIME({0}) <= TIME_FORMAT(SUBSTRING_INDEX(saturday_time, ' ', -1), '%H:%i')",
					currentDateTime
				);

				BooleanExpression endCondition = Expressions.booleanTemplate(
					"TIME({0}) >= TIME_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(saturday_time, ' ~', 1), ' ', -1), '%H:%i')",
					currentDateTime
				);

				BooleanExpression equalCondition = Expressions.booleanTemplate(
					"TIME_FORMAT(SUBSTRING_INDEX(saturday_time, ' ', -1), '%H:%i') = TIME_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(saturday_time, ' ~', 1), ' ', -1), '%H:%i')"
				);

				return startCondition.and(endCondition).or(equalCondition);
			} else if (dayOfWeek == DayOfWeek.SUNDAY) {


				BooleanExpression startCondition = Expressions.booleanTemplate(
					"TIME({0}) <= TIME_FORMAT(SUBSTRING_INDEX(sunday_time, ' ', -1), '%H:%i')",
					currentDateTime
				);

				BooleanExpression endCondition = Expressions.booleanTemplate(
					"TIME({0}) >= TIME_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(sunday_time, ' ~', 1), ' ', -1), '%H:%i')",
					currentDateTime
				);

				BooleanExpression equalCondition = Expressions.booleanTemplate(
					"TIME_FORMAT(SUBSTRING_INDEX(sunday_time, ' ', -1), '%H:%i') = TIME_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(sunday_time, ' ~', 1), ' ', -1), '%H:%i')"
				);

				return startCondition.and(endCondition).or(equalCondition);
			}
		}

		return null;
	}


	private BooleanExpression checkHolidayOpen(boolean holidayBusiness) {
		return holidayBusiness == true ? store.holidayTime.isNotNull() : null;
	}

	private BooleanExpression checkNightdOpen(boolean nightBusiness) {
		return nightBusiness == true ? store.nightPharmacy.eq(1) : null;
	}

	private BooleanExpression eqEnglish(int english) { return english == 1 ? store.english.eq(1) : null;
	}

	private BooleanExpression eqChinese(Integer chinese) {
		return chinese == 1 ? store.chinese.eq(1) : null;
	}

	private BooleanExpression eqJapanese(Integer japanese) {
		return japanese == 1 ? store.japanese.eq(1) : null;
	}

	private BooleanExpression withinDistance(String baseLatitude, String baseLongitude, NumberPath<Double> latitude, NumberPath<Double> longitude) {
		if (baseLatitude == null) {

			return null;}
		else {
			// Double b_latitude = Double.parseDouble(baseLatitude);
			// Double b_longitude = Double.parseDouble(baseLongitude);
			Double baseRadius = 1.0;

			NumberExpression<Double> distance = distance(baseLatitude, baseLongitude, latitude, longitude);
			return distance.loe(baseRadius);
		}
	}

	private NumberExpression<Double> distance(String baseLatitude, String baseLongitude, NumberPath<Double> latitude, NumberPath<Double> longitude) {

		if (baseLatitude == null) {
			return null;
		} else {
			double earthRadius = 6371; // 지구 반지름 (단위: km)
			double baseLatitudeRad = Math.toRadians(Double.parseDouble(baseLatitude));
			double baseLongitudeRad = Math.toRadians(Double.parseDouble(baseLongitude));

			return Expressions.numberTemplate(Double.class,
				"({0} * acos(cos({1}) * cos(radians({3})) * cos(radians({4})-{2}) + sin({1}) * sin(radians({3}))))",
				earthRadius, baseLatitudeRad, baseLongitudeRad, latitude, longitude);
		}
	}

}


