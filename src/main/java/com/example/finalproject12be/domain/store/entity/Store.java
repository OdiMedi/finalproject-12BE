package com.example.finalproject12be.domain.store.entity;

import javax.persistence.*;

import com.example.finalproject12be.domain.bookmark.entity.Bookmark;

import com.example.finalproject12be.domain.comment.entity.Comment;
import com.example.finalproject12be.domain.store.dto.StoreRequest;
import com.example.finalproject12be.domain.store.repository.StoreRepository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Store {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "STORE_ID")
	private Long id;

	@Column
	private String address;

	@Column
	private String name;

	@Column
	private String callNumber;

	@Column
	private String weekdaysTime;

	@Column
	private String saturdayTime;

	@Column
	private String sundayTime;

	@Column
	private String holidayTime;

	@Column
	private Double longitude;

	@Column
	private Double latitude;

	@Column
	private Integer foreignLanguage;

	@Column
	private Integer english;

	@Column
	private Integer chinese;

	@Column
	private Integer japanese;

	@Column
	private Integer nightPharmacy;

	@OneToMany(mappedBy = "store", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<Comment> commentList = new ArrayList<>();

	@OneToMany(mappedBy = "store", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<Bookmark> bookmarks;

	public Store(String address, String name, String callNumber, String weekdaysTime, String saturdayTime, String sundayTime, String holidayTime, Double longitude, Double latitude){
		this.address = address;
		this.name = name;
		this.callNumber = callNumber;
		this.weekdaysTime = weekdaysTime;
		this.saturdayTime = saturdayTime;
		this.sundayTime = sundayTime;
		this.holidayTime = holidayTime;
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public Store(StoreRequest storeRequest) {
		this.address = storeRequest.getAddress();
		this.name = storeRequest.getName();
		this.callNumber = storeRequest.getCallNumber();
		this.weekdaysTime = storeRequest.getWeekdaysTime();
		this.saturdayTime = storeRequest.getSaturdayTime();
		this.sundayTime = storeRequest.getSundayTime();
		this.holidayTime = storeRequest.getHolidayTime();
		this.longitude = storeRequest.getLongitude();
		this.latitude = storeRequest.getLatitude();
		this.foreignLanguage = storeRequest.getForeignLanguage();
		this.english = storeRequest.getEnglish();
		this.chinese =storeRequest.getChinese();
		this.japanese = storeRequest.getJapanese();
		this.nightPharmacy = storeRequest.getNightPharmacy();
	}

	public void setForeign(int foreignLanguage, int english, int chinese, int japanese){
		this.foreignLanguage = foreignLanguage;
		this.english = english;
		this.japanese = japanese;
		this.chinese = chinese;
	}

	public void deleteBookmark(Bookmark bookmark){
		this.bookmarks.remove(bookmark);
	}

	public void addBookmark(Bookmark bookmark){
		this.bookmarks.add(bookmark);
	}

	public void updateStore(StoreRequest storeRequest) {
		this.address = storeRequest.getAddress();
		this.name = storeRequest.getName();
		this.callNumber = storeRequest.getCallNumber();
		this.weekdaysTime = storeRequest.getWeekdaysTime();
		this.saturdayTime = storeRequest.getSaturdayTime();
		this.sundayTime = storeRequest.getSundayTime();
		this.holidayTime = storeRequest.getHolidayTime();
		this.longitude = storeRequest.getLongitude();
		this.latitude = storeRequest.getLatitude();
		this.foreignLanguage = storeRequest.getForeignLanguage();
		this.english = storeRequest.getEnglish();
		this.chinese =storeRequest.getChinese();
		this.japanese = storeRequest.getJapanese();
		this.nightPharmacy = storeRequest.getNightPharmacy();
	}

    public boolean isForeignLanguage() {
		return foreignLanguage != null && foreignLanguage == 1;
    }

	private int getBookmarkCount(Store store) {
		// 북마크 개수를 조회하는 로직을 구현합니다.
		// 예를 들어, 다음과 같이 적절한 코드로 대체해야 합니다:
		return store.getBookmarks().size();
	}

}
