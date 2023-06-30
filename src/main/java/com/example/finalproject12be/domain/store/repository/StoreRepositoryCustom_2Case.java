package com.example.finalproject12be.domain.store.repository;

import static com.example.finalproject12be.domain.store.entity.QStore.*;
import static com.example.finalproject12be.domain.store.entity.QSecond.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.example.finalproject12be.domain.store.dto.MappedSearchForeignRequest;
import com.example.finalproject12be.domain.store.entity.Second;
import com.example.finalproject12be.domain.store.entity.Store;
import com.example.finalproject12be.domain.store.entity.Store_2;
import com.example.finalproject12be.security.UserDetailsImpl;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryCustom_2Case {

	private final JPAQueryFactory jpaQueryFactory;

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

	public Page<Second> searchForeignStoreWithFiltertest(MappedSearchForeignRequest request, UserDetailsImpl userDetails) {

		int page = request.getPage();
		int size = request.getSize();

		QueryResults<Second> results = jpaQueryFactory
			.selectFrom(second)
			.where(
				withinDistance(request.getLatitude(), request.getLongitude(), second.latitude, second.longitude),
				eqAddress(request.getGu()),
				eqStoreName(request.getStoreName()),
				checkOpen(request.isOpen()),
				checkHolidayOpen(request.isHolidayBusiness()),
				checkNightdOpen(request.isNightBusiness()),
				checkEnglish(request.getEnglish()),
				checkChinese(request.getChinese()),
				checkJapanese(request.getJapanese())
			)
			.offset(page * size)
			.limit(size)
			.fetchResults();

		return new PageImpl<>(results.getResults(), PageRequest.of(page, size), results.getTotal());
	}

	private BooleanExpression eqAddress(String gu) {
		return gu != null ? second.gu.eq(gu) : null;
	}

	private BooleanExpression eqStoreName(String storeName) {
		return storeName != null ? second.name.like("%" + storeName + "%") : null;
	}

	private BooleanExpression checkOpen(boolean open) {
		if (open) {
			LocalDate currentDate = LocalDate.now();
			DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

			if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
				String currentDateTime = LocalDateTime.now().format(timeFormatter);

				BooleanExpression startCondition = Expressions.booleanTemplate(
					"TIME({0}) <= TIME_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(business_hours, '|', 1), ' ', -1), '%H:%i')",
					LocalDateTime.now().format(timeFormatter)
				);

				BooleanExpression endCondition = Expressions.booleanTemplate(
					"TIME({0}) >= TIME_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(SUBSTRING_INDEX(business_hours, '|', 1), ' ~', 1), ' ', -1), '%H:%i')",
					LocalDateTime.now().format(timeFormatter)
				);

				BooleanExpression equalCondition = Expressions.booleanTemplate(
					"TIME_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(SUBSTRING_INDEX(business_hours, '|', 1), ' ~', 1), ' ', -1), '%H:%i') = TIME_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(business_hours, '|', 1), ' ', -1), '%H:%i')"
				);

				return startCondition.and(endCondition).or(equalCondition);
			} else {
				return null;
			}
		}
		return null;
	}

	private BooleanExpression checkHolidayOpen(boolean holidayBusiness) {

		if (holidayBusiness == true) {
			return Expressions.booleanTemplate(
				" '' <> SUBSTRING_INDEX(SUBSTRING_INDEX(business_hours, '|', 4), '|', -1)"
			);
		}
		else  {
			return null;
		}
	}

	private BooleanExpression checkNightdOpen(boolean nightBusiness) {
		return nightBusiness == true ? second.nightPharmacy.eq(1) : null;
	}

	private BooleanExpression checkEnglish(int english) {

		if (english == 1) {
			System.out.println("!!!!!!!!!!!!!잉글리쉬!!!!!!!!!!!!!!!!!!!!!!!!");
			return Expressions.booleanTemplate(
				"SUBSTRING(language, 1, 1) = '1'"
			);
		} else {
			return null;
		}
	}

	private BooleanExpression checkChinese(int chinese) {
		if (chinese == 1) {
			return Expressions.booleanTemplate(
				"SUBSTRING(language, 2, 1) = '1'"
			);
		}else {
			return null;
		}
	}

	private BooleanExpression checkJapanese(int japanese) {
		if (japanese == 1) {
			return Expressions.booleanTemplate(
				"SUBSTRING(language, 3, 1) = '1'"
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
