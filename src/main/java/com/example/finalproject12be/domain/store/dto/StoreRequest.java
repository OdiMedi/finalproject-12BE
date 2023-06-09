package com.example.finalproject12be.domain.store.dto;

import javax.persistence.Column;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoreRequest {

	private String address;

	private String name;

	private String callNumber;

	private String weekdaysTime;

	private String saturdayTime;

	private String sundayTime;

	private String holidayTime;

	private Double longitude;

	private Double latitude;

	private Integer foreignLanguage;

	private Integer english;

	private Integer chinese;

	private Integer japanese;

	private Integer nightPharmacy;

	public StoreRequest(String address, String name, String callNumber, String weekdaysTime, String saturdayTime,
		String sundayTime, String holidayTime, Double longitude, Double latitude, Integer foreignLanguage,
		Integer english,
		Integer chinese, Integer japanese, Integer nightPharmacy) {
		this.address = address;
		this.name = name;
		this.callNumber = callNumber;
		this.weekdaysTime = weekdaysTime;
		this.saturdayTime = saturdayTime;
		this.sundayTime = sundayTime;
		this.holidayTime = holidayTime;
		this.longitude = longitude;
		this.latitude = latitude;
		this.foreignLanguage = foreignLanguage;
		this.english = english;
		this.chinese = chinese;
		this.japanese = japanese;
		this.nightPharmacy = nightPharmacy;
	}
}
