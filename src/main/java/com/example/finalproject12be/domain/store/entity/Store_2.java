package com.example.finalproject12be.domain.store.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.example.finalproject12be.domain.store.dto.StoreRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Store_2 {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "STORE_ID")
	private Long id;

	@Column
	private String gu;

	@Column
	private String detailAddress;

	@Column
	private String name;

	@Column
	private String callNumber;

	@Column
	private String businessHours;

	@Column
	private Double longitude;

	@Column
	private Double latitude;

	@Column
	private String language;

	@Column
	private Integer nightPharmacy;


	public Store_2(String address, String name, String callNumber, String weekdaysTime, String saturdayTime, String sundayTime, String holidayTime, Double longitude, Double latitude){
		//this.address = address;
		this.name = name;
		this.callNumber = callNumber;
		// this.weekdaysTime = storeRequest.getWeekdaysTime();
		// this.saturdayTime = storeRequest.getSaturdayTime();
		// this.sundayTime = storeRequest.getSundayTime();
		// this.holidayTime = storeRequest.getHolidayTime();
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public Store_2(StoreRequest storeRequest) {
		//this.address = storeRequest.getAddress();
		this.name = storeRequest.getName();
		this.callNumber = storeRequest.getCallNumber();
		//this.weekdaysTime = storeRequest.getWeekdaysTime();
		//this.saturdayTime = storeRequest.getSaturdayTime();
		//this.sundayTime = storeRequest.getSundayTime();
		//this.holidayTime = storeRequest.getHolidayTime();
		this.longitude = storeRequest.getLongitude();
		this.latitude = storeRequest.getLatitude();
		// this.foreignLanguage = storeRequest.getForeignLanguage();
		// this.english = storeRequest.getEnglish();
		// this.chinese =storeRequest.getChinese();
		// this.japanese = storeRequest.getJapanese();
		this.nightPharmacy = storeRequest.getNightPharmacy();
	}

	// public void setForeign(int foreignLanguage, int english, int chinese, int japanese){
	// 	this.foreignLanguage = foreignLanguage;
	// 	this.english = english;
	// 	this.japanese = japanese;
	// 	this.chinese = chinese;
	// }

	public void updateStore(StoreRequest storeRequest) {
		//his.address = storeRequest.getAddress();
		this.name = storeRequest.getName();
		this.callNumber = storeRequest.getCallNumber();
		//this.weekdaysTime = storeRequest.getWeekdaysTime();
		//this.saturdayTime = storeRequest.getSaturdayTime();
		//this.sundayTime = storeRequest.getSundayTime();
		//this.holidayTime = storeRequest.getHolidayTime();
		this.longitude = storeRequest.getLongitude();
		this.latitude = storeRequest.getLatitude();
		// this.foreignLanguage = storeRequest.getForeignLanguage();
		// this.english = storeRequest.getEnglish();
		// this.chinese =storeRequest.getChinese();
		// this.japanese = storeRequest.getJapanese();
		this.nightPharmacy = storeRequest.getNightPharmacy();
	}

	// public boolean isForeignLanguage() {
	// 	return foreignLanguage != null && foreignLanguage == 1;
	// }
}


