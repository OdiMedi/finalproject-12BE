package com.example.finalproject12be.domain.bookmark.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.finalproject12be.domain.bookmark.entity.Bookmark;
import com.example.finalproject12be.domain.bookmark.repository.BookmarkRepository;
import com.example.finalproject12be.domain.member.entity.Member;
import com.example.finalproject12be.domain.member.repository.MemberRepository;
import com.example.finalproject12be.domain.store.entity.Store;
import com.example.finalproject12be.domain.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkService {

	private final BookmarkRepository bookmarkRepository;
	private final StoreRepository storeRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public void bookmarkStore(Long storeId, int userId) { //test 용도로 userId 하드코딩

		//TODO: custom Exception 사용하기
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new IllegalArgumentException("해당 약국이 존재하지 않습니다."));

		//TODO: user 검사 로직 추가
		Member member = memberRepository.findById(Long.valueOf(userId))
			.orElseThrow(() -> new IllegalArgumentException("멤버 없음"));

		Optional<Bookmark> bookmarkOptional = bookmarkRepository.findByStoreId(storeId);

		int isPresent = 0;

		if(bookmarkOptional.isPresent()) {
			Bookmark bookmark = bookmarkOptional.get();
			if(bookmark.getMembers().contains(member)){
				bookmarkRepository.delete(bookmark);
				isPresent = -1;
				bookmark.bookmarkStore(isPresent);
				bookmark.deleteBookmark(member);
				member.setBookmark(null);
				if(bookmark.getMembers().size() == 0){
					store.setBookmark(null);
				}else {
					store.setBookmark(bookmark);
					bookmarkRepository.saveAndFlush(bookmark);
				}

			}else{
				bookmarkRepository.delete(bookmark);
				isPresent = 1;
				Bookmark newBookmark = new Bookmark(store, member);
				bookmark.bookmarkStore(isPresent);
				store.setBookmark(bookmark);
				member.setBookmark(bookmark);
				bookmarkRepository.saveAndFlush(bookmark);
			}
		}else{
			isPresent = 1;
			Bookmark bookmark = new Bookmark(store, member);
			bookmark.bookmarkStore(isPresent);
			store.setBookmark(bookmark);
			member.setBookmark(bookmark);
			bookmarkRepository.saveAndFlush(bookmark);
		}

		//TODO: user 다수에도 돌아가는 로직인지 테스트
	}
}
