package com.example.finalproject12be.domain.store.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.finalproject12be.domain.bookmark.entity.Bookmark;
import com.example.finalproject12be.domain.member.entity.Member;
import com.example.finalproject12be.domain.member.entity.MemberRoleEnum;
import com.example.finalproject12be.domain.store.dto.ForeignOneStoreResponse;
import com.example.finalproject12be.domain.store.dto.ForeignStoreResponse;
import com.example.finalproject12be.domain.store.dto.MappedSearchForeignRequest;
import com.example.finalproject12be.domain.store.dto.MappedSearchRequest;
import com.example.finalproject12be.domain.store.dto.OneStoreResponseDto;
import com.example.finalproject12be.domain.store.dto.SearchForeignOptionRequest;
import com.example.finalproject12be.domain.store.dto.SearchOptionRequest;
import com.example.finalproject12be.domain.store.dto.StoreRequest;
import com.example.finalproject12be.domain.store.dto.StoreResponseDto;
import com.example.finalproject12be.domain.store.entity.Second;
import com.example.finalproject12be.domain.store.entity.Store;
import com.example.finalproject12be.domain.store.entity.Store_2;
import com.example.finalproject12be.domain.store.entity.Third;
import com.example.finalproject12be.domain.store.repository.StoreRepository;
import com.example.finalproject12be.domain.store.repository.StoreRepositoryCustom;
import com.example.finalproject12be.domain.store.repository.StoreRepositoryCustom_2Case;
import com.example.finalproject12be.domain.store.repository.StoreRepositoryCustom_3Case;
import com.example.finalproject12be.exception.CommonErrorCode;
import com.example.finalproject12be.exception.MemberErrorCode;
import com.example.finalproject12be.exception.RestApiException;
import com.example.finalproject12be.security.UserDetailsImpl;
import com.querydsl.core.Tuple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

	private final StoreRepository storeRepository;
	private final StoreRepositoryCustom storeRepositoryCustom;
	private final StoreRepositoryCustom_2Case storeRepositoryCustom_2Case;
	private final StoreRepositoryCustom_3Case storeRepositoryCustom_3Case;

	//약국 전체보기
	public List<StoreResponseDto> getAllStores(UserDetailsImpl userDetails) {

		List<Store> stores = storeRepository.findAll();
		List<StoreResponseDto> storeResponseDtos = new ArrayList<>();

		if(userDetails != null){
			Member member = userDetails.getMember();
			checkBookmark(stores, storeResponseDtos, member);

		}else{
			Iterator<Store> storeIterator = stores.iterator();
			while (storeIterator.hasNext()) {
				Store store = storeIterator.next();
				storeResponseDtos.add(new StoreResponseDto(store));
			}
		}

		return storeResponseDtos;
	}

	//외국어 가능, 북마크 확인 로직
	private List<ForeignStoreResponse> checkForeignBookmark(List<Store> stores, List<ForeignStoreResponse> foreignStoreResponses, UserDetailsImpl userDetails){

		int bookmarkCheck = 0;
		boolean english = false;
		boolean chinese = false;
		boolean japanese = false;

		for(Store store : stores){

			if(userDetails != null){
				Member member = userDetails.getMember();

				if(store.getBookmarks().size() != 0){
					List<Bookmark> bookmarks = store.getBookmarks();

					for(Bookmark bookmark : bookmarks){

						if(bookmark.getMember().getId().equals(member.getId())){
							bookmarkCheck = 1;
						}
					}
				}
			}

			if(store.getForeignLanguage() != null){

				if(store.getEnglish() == 1){
					english = true;
				}

				if(store.getChinese() == 1){
					chinese = true;
				}

				if(store.getJapanese() == 1){
					japanese = true;
				}
			}

			ForeignStoreResponse foreignStoreResponse = new ForeignStoreResponse(store);

			if(bookmarkCheck == 1){
				foreignStoreResponse.setBookmark(true);
			}

			foreignStoreResponse.setLanguage(english, chinese, japanese);

			foreignStoreResponses.add(foreignStoreResponse);
			bookmarkCheck = 0;
			english = false;
			chinese = false;
			japanese = false;
		}

		return foreignStoreResponses;
	}

	//북마크 확인 로직
	private List<StoreResponseDto> checkBookmark(List<Store> stores, List<StoreResponseDto> storeResponseDtos, Member member){

		int check = 0;

		// for(Store store : stores){
		Iterator<Store> storeIterator = stores.iterator();
		while(storeIterator.hasNext()){
			Store store = storeIterator.next();

			if(store.getBookmarks().size() != 0){
				List<Bookmark> bookmarks = store.getBookmarks();

				// for(Bookmark bookmark : bookmarks){
				Iterator<Bookmark> bookmarkIterator = bookmarks.iterator();
				while(bookmarkIterator.hasNext()){
					Bookmark bookmark = bookmarkIterator.next();

					if(bookmark.getMember().getId().equals(member.getId())){
						check = 1;
					}
				}
			}

			StoreResponseDto storeResponseDto = new StoreResponseDto(store);

			if(check == 1){
				storeResponseDto.setBookmark(true);
			}

			storeResponseDtos.add(storeResponseDto);
			check = 0;
		}

		return storeResponseDtos;
	}

	//일반 약국 검색하기
	public Page<StoreResponseDto> searchStore(int page, int size, String storeName, String gu, boolean open, boolean holidayBusiness, boolean nightBusiness, String radius, String latitude, String longitude, UserDetailsImpl userDetails) {

		int progress = 0; //stores 리스트가 null일 때 0, 반대는 1
		List<StoreResponseDto> storeResponseDtos = new ArrayList<>();
		List<Store> stores = new ArrayList<>();

		//내 위치 기반 가까운 약국 검색
		if (latitude != "") {
			progress = 1;

			// stores = storeRepository.findByDistanceWithinRadius(baseRadius, baseLatitude, baseLongitude);
			stores = storeRepositoryCustom.searchStoreWithinDistance(radius, latitude, longitude);
		}

		//약국 이름 검색
		if(storeName != ""){

			if(progress == 0){ //저장된 stores가 없을 때
				progress = 1;
				stores = storeRepository.findAllByNameContaining(storeName);

			}else{ //저장된 stores가 있을 때
				List<Store> testStores = new ArrayList<>();

				for(Store store: stores){
					testStores.add(store);
				}

				// for(Store testStore : testStores){
				Iterator<Store> testStoreIterator = testStores.iterator();
				while (testStoreIterator.hasNext()){

					Store testStore = testStoreIterator.next();

					if(!testStore.getName().contains(storeName)){
						stores.remove(testStore);
					}
				}
			}
		}

		//구 검색
		if(gu != "") {

			if (progress == 0) { //저장된 stores가 없을 때

				progress = 1;
				stores = storeRepository.findAllByAddressContaining(gu);

			} else { //저장된 stores가 있을 때
				// List<Store> testStores = new ArrayList<>();
				//
				// for (Store store : stores) {
				// 	testStores.add(store);
				// }

				// for (Store testStore : testStores) {
				// Iterator<Store> testStoreIterator = testStores.iterator();

				// while (testStoreIterator.hasNext()){

				for(Iterator<Store> storeIterator = stores.iterator(); storeIterator.hasNext();){
					Store testStore = storeIterator.next();

					if (!testStore.getAddress().contains(gu)) {
						// stores.remove(testStore);
						storeIterator.remove();
					}
				}
			}
		}else if(progress == 0){
			Pageable pageable = PageRequest.of(page, size);

			final int start = (int)pageable.getOffset();
			final int end = Math.min((start + pageable.getPageSize()), storeResponseDtos.size());
			final Page<StoreResponseDto> storeResponsePage = new PageImpl<>(storeResponseDtos.subList(start, end), pageable, storeResponseDtos.size());
			return storeResponsePage;
		}

		//filter
		if(open == true){ // 영업중 필터

			if(progress == 1){ //저장된 stores가 있을 때만 실행 가능함

				stores = openCheck(stores);

			}

		}else if(holidayBusiness == true){

			if(progress == 0){
				progress = 1;
				stores = storeRepository.findAllByHolidayTimeIsNotNull();

			}else{
				List<Store> restStores = new ArrayList<>();

				for(Store store: stores){
					restStores.add(store);
				}

				for(Store restStore : restStores){

					if(restStore.getHolidayTime() == null){
						stores.remove(restStore);
					}
				}
			}

		}else if (nightBusiness == true){

			if(progress == 0){
				progress = 1;
				stores = storeRepository.findAllByNightPharmacy(1);

			}else if(progress == 1){
				List<Store> restStores = new ArrayList<>();

				for(Store store: stores){
					restStores.add(store);
				}

				for(Store restStore : restStores){

					if(restStore.getNightPharmacy() != 1){
						stores.remove(restStore);
					}
				}
			}
		}

		if(userDetails != null){
			Member member = userDetails.getMember();
			storeResponseDtos = checkBookmark(stores, storeResponseDtos, member);

		}else{

			for(Store store : stores){
				storeResponseDtos.add(new StoreResponseDto(store));
			}
		}

		Pageable pageable = PageRequest.of(page, size);

		final int start = (int)pageable.getOffset();
		final int end = Math.min((start + pageable.getPageSize()), storeResponseDtos.size());
		final Page<StoreResponseDto> storeResponsePage = new PageImpl<>(storeResponseDtos.subList(start, end), pageable, storeResponseDtos.size());

		return storeResponsePage;
	}

	//약국 상세보기
	public OneStoreResponseDto getStore(Long storeId, UserDetailsImpl userDetails) {

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new RestApiException(CommonErrorCode.RESOURCE_NOT_FOUND));

		OneStoreResponseDto oneStoreResponseDto = new OneStoreResponseDto(store);

		if(store.getSaturdayTime() != null){
			oneStoreResponseDto.setSaturdayTime(store.getSaturdayTime());
		}

		if(store.getSundayTime() != null){
			oneStoreResponseDto.setSundayTime(store.getSundayTime());
		}

		if(store.getHolidayTime() != null){
			oneStoreResponseDto.setHolidayTime(store.getHolidayTime());
		}

		if(userDetails != null){
			Member member = userDetails.getMember();

			if(store.getBookmarks().size() != 0){
				List<Bookmark> bookmarks = store.getBookmarks();

				// for(Bookmark bookmark : bookmarks){
				Iterator<Bookmark> bookmarkIterator = bookmarks.iterator();
				while(bookmarkIterator.hasNext()){
					Bookmark bookmark = bookmarkIterator.next();

					if(bookmark.getMember().getId().equals(member.getId())){

						oneStoreResponseDto.setBookmark(true);
						oneStoreResponseDto.setTotalBookmark(store.getBookmarks().size());
					}
				}
			}
		}

		if(store.getNightPharmacy() == 1){
			oneStoreResponseDto.setNightBusiness(true);
		}

		if(store.getBookmarks().size() != 0){
			oneStoreResponseDto.setTotalBookmark(store.getBookmarks().size());
		}

		return oneStoreResponseDto;
	}

	//영업중 필터 검사 로직
	private List<Store> openCheck(List<Store> stores){

		// List<Store> restStores = new ArrayList<>();
		// for(Store store: stores){
		// 	restStores.add(store);
		// }

		LocalDate now = LocalDate.now();
		int dayOfWeek = now.getDayOfWeek().getValue();

		LocalTime nowTime = LocalTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

		// 포맷 적용하기
		String formatedNow = nowTime.format(formatter);
		int nowHour = Integer.parseInt(formatedNow.substring(0, 2));
		int nowMin = Integer.parseInt(formatedNow.substring(3, 5));

		int openHour = 0;
		int openMin = 0;
		int closeHour = 0;
		int closeMin = 0;

		// for (Store restStore : restStores) {
		for(Iterator<Store> storeIterator = stores.iterator(); storeIterator.hasNext();){
			Store store = storeIterator.next();

			int status = 0; //시간 null이면 1, 아니면 0

			if (dayOfWeek > 0 && dayOfWeek < 6) { //평일

				String storeTime = store.getWeekdaysTime();

				if(storeTime != null && !storeTime.contains("nu")){ //TODO: nu:ll 로 시간 들어가 있는 객체 골라서 작업하기

					String[] storeTimes = storeTime.split("~");

					openHour = Integer.parseInt(storeTimes[0].substring(3, 5));
					openMin = Integer.parseInt(storeTimes[0].substring(6, 8));

					closeHour = Integer.parseInt(storeTimes[1].substring(1, 3));
					closeMin = Integer.parseInt(storeTimes[1].substring(4, 6));

				}else {
					status = 1;
					// stores.remove(restStore);
					storeIterator.remove();
				}

			}else if (dayOfWeek == 6){ // 토요일 TODO: 일요일이랑 합치기

				String storeTime = store.getSaturdayTime();

				if (storeTime != null && !storeTime.contains("nu")){

					String[] storeTimes = storeTime.split("~");

					openHour = Integer.parseInt(storeTimes[0].substring(2, 4));
					openMin = Integer.parseInt(storeTimes[0].substring(5, 7));

					closeHour = Integer.parseInt(storeTimes[1].substring(1, 3));
					closeMin = Integer.parseInt(storeTimes[1].substring(4, 6));

				}else{
					status = 1;
					// stores.remove(restStore);
					storeIterator.remove();
				}

			}else if( dayOfWeek == 7){ // 일요일

				String storeTime = store.getSundayTime();

				if(storeTime != null && !storeTime.contains("nu")){

					String[] storeTimes = storeTime.split("~");

					openHour = Integer.parseInt(storeTimes[0].substring(2, 4));
					openMin = Integer.parseInt(storeTimes[0].substring(5, 7));

					closeHour = Integer.parseInt(storeTimes[1].substring(1, 3));
					closeMin = Integer.parseInt(storeTimes[1].substring(4, 6));

				}else {
					status = 1;
					// stores.remove(restStore);
					storeIterator.remove();
				}
			}

			if(status != 1){

				if((openHour < nowHour) && (closeHour > nowHour)){
					continue;
				}else if((openHour == nowHour) && (openMin < nowMin)){
					continue;
				}else if((closeHour == nowHour) && (closeMin > nowMin)){
					continue;
				}else if((closeHour == openHour)){
					continue;
				}else{
					// stores.remove(restStore);
					storeIterator.remove();
				}
			}
		}

		return stores;
	}

	//위치 불러오기
	public List<Store> getLocation(Double baseRadius,Double baseLatitude, Double baseLongitude) {

		List<Store> result = storeRepository.findByDistanceWithinRadius(baseLatitude, baseLongitude, baseRadius);
		return result;
	}

	//외국어 가능 약국 검사
	public Page<ForeignStoreResponse> searchForeignStore(int page, int size, String storeName, String gu, boolean open, boolean holidayBusiness, boolean nightBusiness, boolean english, boolean chinese, boolean japanese, String radius, String latitude, String longitude, UserDetailsImpl userDetails) {

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

		int progress = 0; //stores 리스트가 null일 때 0, 반대는 1
		List<ForeignStoreResponse> foreignStoreResponses = new ArrayList<>();
		List<Store> stores = new ArrayList<>();

		//내 위치 기반 가까운 약국 검색
		if (latitude != "") {
			progress = 1;
			//stores = storeRepository.findByDistanceWithinRadius(baseRadius, baseLatitude, baseLongitude);
			stores = storeRepositoryCustom.searchStoreWithinDistance(radius, latitude, longitude);
		}

		//약국 이름 검색하기
		if(storeName != ""){

			if(progress == 0){
				progress = 1;
				stores = storeRepository.findAllByNameContaining(storeName);

			}else{
				List<Store> testStores = new ArrayList<>();

				for(Store store: stores){
					testStores.add(store);
				}

				for(Store testStore : testStores){

					if(!testStore.getName().contains(storeName)){
						stores.remove(testStore);
					}
				}
			}
		}

		//구 검색하기
		if(!gu.equals("")){

			if(progress == 0){ //저장된 stores가 없을 때
				progress = 1;
				stores = storeRepository.findAllByAddressContaining(gu);

			}else{ //저장된 stores가 있을 때
				List<Store> testStores = new ArrayList<>();

				for(Store store: stores){
					testStores.add(store);
				}

				for(Store testStore : testStores){

					if(!testStore.getAddress().contains(gu)){
						stores.remove(testStore);
					}
				}
			}
		}else if(progress == 0){
			Pageable pageable = PageRequest.of(page, size);

			final int start = (int)pageable.getOffset();
			final int end = Math.min((start + pageable.getPageSize()), foreignStoreResponses.size());
			final Page<ForeignStoreResponse> foreignStoreResponsePage = new PageImpl<>(foreignStoreResponses.subList(start, end), pageable, foreignStoreResponses.size());

			return foreignStoreResponsePage;
		}

		//각종 필터
		if(open){ // 영업중 필터

			if(progress == 1){ //저장된 stores가 있을 때만 실행 가능함
				stores = openCheck(stores);
			}

		}else if(holidayBusiness){

			if(progress == 0){
				progress = 1;
				stores = storeRepository.findAllByHolidayTimeIsNotNull();

			}else{
				List<Store> restStores = new ArrayList<>();

				for(Store store: stores){
					restStores.add(store);
				}

				for(Store restStore : restStores){

					if(restStore.getHolidayTime() == null){
						stores.remove(restStore);
					}
				}
			}

		}else if (nightBusiness){

			if(progress == 0){
				progress = 1;
				stores = storeRepository.findAllByNightPharmacy(1);

			}else {
				List<Store> restStores = new ArrayList<>();
				for(Store store: stores){
					restStores.add(store);
				}

				for(Store restStore : restStores){

					if(restStore.getNightPharmacy() != 1){
						stores.remove(restStore);
					}
				}
			}
		}

		if(english){

			if(progress == 0){
				progress = 1;
				stores = storeRepository.findAllByEnglish(1);

			} else{
				// List<Store> restStores = new ArrayList<>();
				//
				// for(Store store: stores){
				// 	restStores.add(store);
				// }

				// for(Store restStore : restStores){
				for(Iterator<Store> storeIterator = stores.iterator(); storeIterator.hasNext();){
					Store store = storeIterator.next();

					if(store.getEnglish() == null || store.getEnglish() == 0){
						// stores.remove(restStore);
						storeIterator.remove();
					}
				}
			}

		}else if(chinese){

			if(progress == 0){
				progress = 1;
				stores = storeRepository.findAllByChinese(1);

			} else{
				List<Store> restStores = new ArrayList<>();

				for(Store store: stores){
					restStores.add(store);
				}

				for(Store restStore : restStores){

					if(restStore.getChinese() == null || restStore.getChinese() == 0){
						stores.remove(restStore);
					}
				}
			}

		}else if(japanese){

			if(progress == 0){
				progress = 1;
				stores = storeRepository.findAllByJapanese(1);

			} else{
				List<Store> restStores = new ArrayList<>();

				for(Store store: stores){
					restStores.add(store);
				}

				for(Store restStore : restStores){

					if(restStore.getJapanese() == null || restStore.getJapanese() == 0){
						stores.remove(restStore);
					}
				}
			}
		}

		foreignStoreResponses = checkForeignBookmark(stores, foreignStoreResponses, userDetails);

		Pageable pageable;

		if(latitude.equals("")){
			// pageable = PageRequest.of(page, size, Sort.by("name").ascending());
			pageable = PageRequest.of(page, size, Sort.Direction.DESC, "name");
		}else {
			pageable = PageRequest.of(page, size);
		}

		final int start = (int)pageable.getOffset();
		final int end = Math.min((start + pageable.getPageSize()), foreignStoreResponses.size());
		final Page<ForeignStoreResponse> foreignStoreResponsePage = new PageImpl<>(foreignStoreResponses.subList(start, end), pageable, foreignStoreResponses.size());

		return foreignStoreResponsePage;
	}

	//외국어 약국 상세보기
	public ForeignOneStoreResponse getForeignStore(Long storeId, UserDetailsImpl userDetails) {

		boolean english = false;
		boolean chinese = false;
		boolean japanese = false;

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new RestApiException(CommonErrorCode.RESOURCE_NOT_FOUND));

		ForeignOneStoreResponse foreignOneStoreResponse = new ForeignOneStoreResponse(store);

		if(store.getSaturdayTime() != null){
			foreignOneStoreResponse.setSaturdayTime(store.getSaturdayTime());
		}

		if(store.getSundayTime() != null){
			foreignOneStoreResponse.setSundayTime(store.getSundayTime());
		}

		if(store.getHolidayTime() != null){
			foreignOneStoreResponse.setHolidayTime(store.getHolidayTime());
		}

		if(userDetails != null){
			Member member = userDetails.getMember();

			if(store.getBookmarks().size() != 0){
				List<Bookmark> bookmarks = store.getBookmarks();

				// for(Bookmark bookmark : bookmarks){
				Iterator<Bookmark> bookmarkIterator = bookmarks.iterator();
				while(bookmarkIterator.hasNext()){
					Bookmark bookmark = bookmarkIterator.next();
					if(bookmark.getMember().getId().equals(member.getId())){

						foreignOneStoreResponse.setBookmark(true);
						long totalBookmarks = store.getBookmarks().size();
						foreignOneStoreResponse.setTotalBookmark(totalBookmarks);
					}
				}
			}
		}

		if(store.getNightPharmacy() == 1){
			foreignOneStoreResponse.setNightBusiness(true);
		}

		if(store.getBookmarks().size() != 0){
			foreignOneStoreResponse.setTotalBookmark(store.getBookmarks().size());
		}

		if(store.getForeignLanguage() != null){

			if(store.getEnglish() == 1){
				english = true;
			}

			if (store.getChinese() == 1){
				chinese = true;
			}

			if(store.getJapanese() == 1){
				japanese = true;
			}

			foreignOneStoreResponse.setLanguage(english, chinese, japanese);
		}

		return foreignOneStoreResponse;
	}

	public List<Store> searchLocation(String baseRadius,String baseLatitude, String baseLongitude) {
		List<Store> result = storeRepositoryCustom.searchStoreWithinDistance(baseRadius, baseLatitude, baseLongitude);
		// List<Store> result = storeRepository.findByDistanceWithinRadius(baseRadius, baseLatitude, baseLongitude);

		return result;
	}
	@Transactional
	public Store createStore (StoreRequest storeRequest, Member member) {

		MemberRoleEnum memberRoleEnum =  member.getRole();
		if (memberRoleEnum != MemberRoleEnum.ADMIN) {
			throw new RestApiException(MemberErrorCode.ADMIN_ERROR);
		}
		Store store = new Store(storeRequest);
		return storeRepository.save(store);

	}

	@Transactional
	public void updateStore (Long storeId, StoreRequest storeRequest, Member member) {
		MemberRoleEnum memberRoleEnum =  member.getRole();
		if (memberRoleEnum != MemberRoleEnum.ADMIN) {
			throw new RestApiException(MemberErrorCode.INACTIVE_MEMBER);
		}

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new RestApiException(MemberErrorCode.STORE_NOT_FOUND));

		store.updateStore(storeRequest);

	}

	@Transactional
	public void deleteStore (Long storeId, Member member) {
		MemberRoleEnum memberRoleEnum =  member.getRole();
		if (memberRoleEnum != MemberRoleEnum.ADMIN) {
			throw new RestApiException(MemberErrorCode.INACTIVE_MEMBER);
		}

		storeRepository.deleteStoresById(storeId);

	}


	public Page<StoreResponseDto> searchStoreWithFilter(SearchOptionRequest request, UserDetailsImpl userDetails) {
		MappedSearchRequest mappedRequest = request.toMappedSearchRequest();

		if (request.getGu()=="" & request.getLongitude() =="" & request.getStoreName() =="") {

			int page = request.getPage();
			int size = request.getSize();
			return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
		}

		Page<StoreResponseDto> result = storeRepositoryCustom.searchStoreWithFilter(mappedRequest, userDetails);


		return result;
	}

	public Page<ForeignStoreResponse> searchForeignStoreWithFilter(SearchForeignOptionRequest request, UserDetailsImpl userDetails) {

		MappedSearchForeignRequest mappedRequest = request.toMappedSearchRequest();

		if (request.getGu()=="" & request.getLongitude() =="" & request.getStoreName() =="") {

			int page = request.getPage();
			int size = request.getSize();
			return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
		}
		Page<ForeignStoreResponse> result = storeRepositoryCustom.searchForeignStoreWithFilter(mappedRequest, userDetails);

		return result;
	}


	@Transactional(readOnly = true)
	public Page<ForeignStoreResponse> fortes_1(SearchForeignOptionRequest request, UserDetailsImpl userDetails) {

		MappedSearchForeignRequest mappedRequest = request.toMappedSearchRequest();

		Page<ForeignStoreResponse> result = storeRepositoryCustom.fortes_1(mappedRequest, userDetails);

		return result;
	}

	@Transactional(readOnly = true)
	public Page<Second> fortes_2(SearchForeignOptionRequest request, UserDetailsImpl userDetails) {

		MappedSearchForeignRequest mappedRequest = request.toMappedSearchRequest();

		Page<Second> test = storeRepositoryCustom_2Case.searchForeignStoreWithFiltertest(mappedRequest, userDetails);

		return test;
	}

	@Transactional(readOnly = true)
	public Page<Third> fortes_3(SearchForeignOptionRequest request, UserDetailsImpl userDetails) {

		MappedSearchForeignRequest mappedRequest = request.toMappedSearchRequest();

		Page<Third> test = storeRepositoryCustom_3Case.searchForeignStoreWith3Filtertest(mappedRequest, userDetails);

		return test;
	}

}