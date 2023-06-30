package com.example.finalproject12be.domain.store.repository;

import static com.example.finalproject12be.domain.store.entity.QSecond.*;
import static com.example.finalproject12be.domain.store.entity.QThird.*;
import static com.example.finalproject12be.domain.store.entity.QLanguage.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.example.finalproject12be.domain.store.dto.MappedSearchForeignRequest;
import com.example.finalproject12be.domain.store.entity.QLanguage;
import com.example.finalproject12be.domain.store.entity.QThird;
import com.example.finalproject12be.domain.store.entity.Second;
import com.example.finalproject12be.domain.store.entity.Store;
import com.example.finalproject12be.domain.store.entity.Third;
import com.example.finalproject12be.security.UserDetailsImpl;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryCustom_3Case {

	private final JPAQueryFactory jpaQueryFactory;

	public Page<Third> searchForeignStoreWith3Filtertest(MappedSearchForeignRequest request, UserDetailsImpl userDetails) {
		int page = request.getPage();
		int size = request.getSize();

		QueryResults<Third> results = jpaQueryFactory
			.select(third)
			.from(third)
			.leftJoin(third.languages, language)
			.fetchJoin()
			.distinct()
			.where(
				withinDistance(request.getLatitude(), request.getLongitude(), third.latitude, third.longitude),
				eqAddress(request.getGu()),
				eqStoreName(request.getStoreName()),
				checkOpen(request.isOpen()),
				checkHolidayOpen(request.isHolidayBusiness()),
				checkNightOpen(request.isNightBusiness()),
				checkEnglish(request.getEnglish()),
				checkChinese(request.getChinese()),
				checkJapanese(request.getJapanese())
			)
			.fetchResults();

		return new PageImpl<>(results.getResults(), PageRequest.of(page, size), results.getTotal());
	}

	// public List<Third> searchForeignStoreWith3Filtertest(MappedSearchForeignRequest request, UserDetailsImpl userDetails) {
	// 	int page = request.getPage();
	// 	int size = request.getSize();
	//
	// 	List<Third> results = jpaQueryFactory
	// 		.select(third)
	// 		.from(third)
	// 		.leftJoin(third.languages, language)
	// 		.fetchJoin()
	// 		.distinct()
	// 		.where(
	// 			withinDistance(request.getLatitude(), request.getLongitude(), second.latitude, second.longitude),
	// 			eqAddress(request.getGu()),
	// 			eqStoreName(request.getStoreName()),
	// 			checkOpen(request.isOpen()),
	// 			checkHolidayOpen(request.isHolidayBusiness()),
	// 			checkNightOpen(request.isNightBusiness()),
	// 			checkEnglish(request.getEnglish()),
	// 			checkChinese(request.getChinese()),
	// 			checkJapanese(request.getJapanese())
	// 		)
	// 		.limit(100)
	// 		.fetch();
	//
	// 	return results;
	// }



	private BooleanExpression eqAddress(String gu) {
		return gu != null ? third.gu.eq(gu) : null;
	}

	private BooleanExpression eqStoreName(String storeName) {
		return storeName != null ? third.name.like("%" + storeName + "%") : null;
	}

	private BooleanExpression checkOpen(boolean open) {
		if (open) {
			LocalDate currentDate = LocalDate.now();
			DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
			String currentDateTime = LocalDateTime.now().format(timeFormatter);

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
		return holidayBusiness == true ? third.holidayTime.isNotNull() : null;
	}

	private BooleanExpression checkNightOpen(boolean nightBusiness) {
		return nightBusiness == true ? third.nightPharmacy.eq(1) : null;
	}

	private BooleanExpression checkEnglish(int english) {

		if (english == 1) {
			return Expressions.booleanTemplate(
				"language.forlanguage = 'english'"
			);
		} else {
			return null;
		}
	}

	private BooleanExpression checkChinese(int chinese) {
		if (chinese == 1) {
			return Expressions.booleanTemplate(
				"language.forlanguage = 'chinese'"
			);
		}else {
			return null;
		}
	}

	private BooleanExpression checkJapanese(int japanese) {
		if (japanese == 1) {
			return Expressions.booleanTemplate(
				"language.forlanguage = 'japanese'"
			);
		} else {
			return null;
		}
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
