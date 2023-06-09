package com.example.finalproject12be.domain.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.finalproject12be.domain.comment.entity.Comment;
import com.example.finalproject12be.domain.store.entity.Store;


public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findAllByAddressContaining(String gu);

    List<Store> findAllByHolidayTimeIsNotNull();

    List<Store> findAllByWeekdaysTimeContaining(String time);

    List<Store> findAllBysaturdayTimeContaining(String time);

    List<Store> findAllBysundayTimeContaining(String time);

    List<Store> findAllByNameContaining(String storeName);

    Optional<Store> findByCallNumber(Object callNumber);

    @Query(value = "SELECT *, "
        + "(6371 * acos(cos(radians(:baseLatitude)) * cos(radians(s.latitude)) * cos(radians(s.longitude) - radians(:baseLongitude)) + sin(radians(:baseLatitude)) * sin(radians(s.latitude)))) AS distance "
        + "FROM store s "
        //+ "WHERE s.address LIKE %:address% "
        + "HAVING distance <= :radius "
        + "ORDER BY distance ASC", nativeQuery = true)
    List<Store> findByDistanceWithinRadius(@Param("baseLatitude") Double baseLatitude, @Param("baseLongitude") Double baseLongitude, @Param("radius") Double radius);

	List<Store> findAllByEnglish(int english);

    List<Store> findAllByChinese(int chinese);

    List<Store> findAllByJapanese(int japanese);

	List<Store> findAllByNightPharmacy(int nightPharmacy);

    Optional<Store> deleteStoresById(Long storeId);

    Optional<Store> findById(Long storeId);

}