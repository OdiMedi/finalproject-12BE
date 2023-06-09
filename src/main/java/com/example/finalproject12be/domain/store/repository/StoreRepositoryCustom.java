package com.example.finalproject12be.domain.store.repository;

import static com.example.finalproject12be.domain.store.entity.QStore.*;

import java.util.List;

import com.example.finalproject12be.domain.store.entity.Store;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;

	public List<Store> searchTest(Double baseRadius, Double baseLatitude, Double baseLongitude) {

			NumberExpression<Double> distance = distance(baseLatitude, baseLongitude, store.latitude, store.longitude);

			return jpaQueryFactory
				.select(store)
				.from(store)
				.where(
					withinDistance(baseLatitude, baseLongitude, store.latitude, store.longitude)
				)
				.orderBy(distance.asc())
				.limit(30)
				.fetch();
		}

	private BooleanExpression eqAddress(String address) {
		return address != null ? store.address.like("%" + address + "%") : null;
	}

	private BooleanExpression withinDistance(Double baseLatitude, Double baseLongitude, NumberPath<Double> latitude, NumberPath<Double> longitude) {
		if (baseLatitude == null) {return null;}
		else {
			NumberExpression<Double> distance = distance(baseLatitude, baseLongitude, latitude, longitude);
			return distance.loe(baseLongitude);
		}
	}

	private NumberExpression<Double> distance(double baseLatitude, double baseLongitude, NumberPath<Double> latitude, NumberPath<Double> longitude) {
		double earthRadius = 6371; // 지구 반지름 (단위: km)
		double baseLatitudeRad = Math.toRadians(baseLatitude);
		double baseLongitudeRad = Math.toRadians(baseLongitude);

		return Expressions.numberTemplate(Double.class,
			"({0} * acos(cos({1}) * cos(radians({3})) * cos(radians({4})-{2}) + sin({1}) * sin(radians({3}))))",
			earthRadius, baseLatitudeRad, baseLongitudeRad, latitude, longitude);
	}

}


