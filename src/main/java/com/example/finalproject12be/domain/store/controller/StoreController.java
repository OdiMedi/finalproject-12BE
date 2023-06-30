package com.example.finalproject12be.domain.store.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.finalproject12be.domain.store.dto.SearchOptionRequest;
import com.example.finalproject12be.domain.store.entity.Second;
import com.example.finalproject12be.domain.store.entity.Store_2;
import com.example.finalproject12be.domain.store.entity.Third;
import com.example.finalproject12be.domain.store.service.StoreService;
import com.example.finalproject12be.domain.store.dto.ForeignOneStoreResponse;
import com.example.finalproject12be.domain.store.dto.ForeignStoreResponse;
import com.example.finalproject12be.domain.store.dto.OneStoreResponseDto;
import com.example.finalproject12be.domain.store.dto.SearchForeignOptionRequest;
import com.example.finalproject12be.domain.store.dto.StoreRequest;
import com.example.finalproject12be.domain.store.dto.StoreResponseDto;
import com.example.finalproject12be.domain.store.entity.Store;

import com.example.finalproject12be.security.UserDetailsImpl;
import com.querydsl.core.Tuple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StoreController {

	private final StoreService storeService;

	//약국 전체보기
	@GetMapping("/api/store")
	public ResponseEntity<List<StoreResponseDto>> getAllStores(@AuthenticationPrincipal UserDetailsImpl userDetails){

		List<StoreResponseDto> storeResponseDtos = storeService.getAllStores(userDetails);
		return ResponseEntity.status(HttpStatus.OK).body(storeResponseDtos);
	}

	//약국 상세보기
	@GetMapping("/api/store/{id}")
	public ResponseEntity<OneStoreResponseDto> getStore(
		@PathVariable(name = "id") Long storeId,
		@AuthenticationPrincipal UserDetailsImpl userDetails){

		OneStoreResponseDto oneStoreResponseDto = storeService.getStore(storeId, userDetails);
		return ResponseEntity.status(HttpStatus.OK).body(oneStoreResponseDto);
	}

	//일반 약국 검색하기
	//@GetMapping("/api/store/search")
	public ResponseEntity<Page<StoreResponseDto>> searchStore(
		@RequestParam("page") int page,
		@RequestParam("size") int size,
		@RequestParam("storeName") String storeName,
		@RequestParam("gu") String gu,
		@RequestParam("open") boolean open,
		@RequestParam("holidayBusiness") boolean holidayBusiness,
		@RequestParam("nightBusiness") boolean nightBusiness,
		@RequestParam("radius") String radius,
		@RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude,
		@AuthenticationPrincipal UserDetailsImpl userDetails){

		Page<StoreResponseDto> storeResponseDtos = storeService.searchStore(page, size, storeName, gu, open, holidayBusiness, nightBusiness, radius, latitude, longitude,userDetails);
		return ResponseEntity.status(HttpStatus.OK).body(storeResponseDtos);
	}

	//외국어 가능 약국 상세보기
	@GetMapping("/api/store/foreign/{store-id}")
	public ResponseEntity<ForeignOneStoreResponse> getForeignStore(
		@PathVariable(name = "store-id") Long storeId,
		@AuthenticationPrincipal UserDetailsImpl userDetails){

		ForeignOneStoreResponse foreignOneStoreResponse = storeService.getForeignStore(storeId, userDetails);
		return ResponseEntity.status(HttpStatus.OK).body(foreignOneStoreResponse);
	}

	//외국어 가능 약국 검색하기
	//@GetMapping("/api/store/foreign/search")
	public ResponseEntity<Page<ForeignStoreResponse>> searchForeignStore(
		@RequestParam("page") int page,
		@RequestParam("size") int size,
		@RequestParam("storeName") String storeName,
		@RequestParam("gu") String gu,
		@RequestParam("open") boolean open,
		@RequestParam("holidayBusiness") boolean holidayBusiness,
		@RequestParam("nightBusiness") boolean nightBusiness,
		@RequestParam("english") boolean english,
		@RequestParam("chinese") boolean chinese,
		@RequestParam("japanese") boolean japanese,
		@RequestParam("radius") String radius,
		@RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude,
		@AuthenticationPrincipal UserDetailsImpl userDetails){

		Page<ForeignStoreResponse> foreignStoreResponses = storeService.searchForeignStore(page, size, storeName, gu, open, holidayBusiness, nightBusiness, english, chinese, japanese, radius, latitude, longitude, userDetails);
		return ResponseEntity.status(HttpStatus.OK).body(foreignStoreResponses);
	}

	//위치 불러오기
	@GetMapping("/api/store/location")
	public List<Store> getLocation(
		@RequestParam("radius") String radius,
		@RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude,
		@RequestParam("address") String address,
		@AuthenticationPrincipal UserDetailsImpl userDetails){

		return storeService.searchLocation(radius,latitude, longitude);
		//return storeService.getLocation(baseRadius,baseLatitude, baseLongitude, address);
	}

	//관리자 약국 등록하기
	@PostMapping("/api/store")
	public ResponseEntity<String> createStore (
		@RequestBody StoreRequest storeRequest,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {

		storeService.createStore(storeRequest, userDetails.getMember());
		return ResponseEntity.ok("약국이 등록되었습니다.");
	}

	//관리자 약국 수정하기
	@PutMapping("/api/store/{store-id}")
	public ResponseEntity<String> updateStore(
		@PathVariable("store-id") Long storeId,
		@RequestBody StoreRequest storeRequest,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {

		storeService.updateStore(storeId, storeRequest, userDetails.getMember());
		return ResponseEntity.ok("약국이 수정되었습니다.");
	}

	//관리자 약국 삭제하기
	@DeleteMapping("/api/store/{store-id}")
	public ResponseEntity<String> deleteStore(
		@PathVariable("store-id") Long storeId,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {
		storeService.deleteStore(storeId, userDetails.getMember());
		return ResponseEntity.ok("약국이 삭제되었습니다.");
	}

	// private final OpenApiManager openApiManager;
	//
	// //!!사용하면 안됨!!
	// //api db에 저장하기
	// @GetMapping("api/store/open-api")
	// public void fetch() {
	// 	openApiManager.fetch();
	// }

	//일반 약국 검색 QueryDSL
	@GetMapping("/api/store/search")
	//@GetMapping("/test")
	public ResponseEntity<Page<StoreResponseDto>> searchStoreWithFilter(SearchOptionRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails){

		Page<StoreResponseDto> result =  storeService.searchStoreWithFilter(request,userDetails);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	//@GetMapping("/testFor")
	@GetMapping("/api/store/foreign/search")
	public ResponseEntity<Page<ForeignStoreResponse>> searchForeignStoreWithFilter(SearchForeignOptionRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails){
		Page<ForeignStoreResponse> result = storeService.searchForeignStoreWithFilter(request,userDetails);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/test1")
	//@GetMapping("/api/store/foreign/search")
	public ResponseEntity<Page<ForeignStoreResponse>> fortes_1(SearchForeignOptionRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails){
		Page<ForeignStoreResponse> result = storeService.fortes_1(request,userDetails);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	//일반 약국 검색 test
	@GetMapping("/test2")
	public ResponseEntity<Page<Second>> fortes_2(SearchForeignOptionRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails){

		Page<Second> result =  storeService.fortes_2(request,userDetails);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/test3")
	public ResponseEntity<Page<Third>> fortes_3(SearchForeignOptionRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails){
		Page<Third> result = storeService.fortes_3(request,userDetails);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}


}