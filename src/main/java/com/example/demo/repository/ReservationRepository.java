package com.example.demo.repository;

import com.example.demo.dto.ReservationResponseDto;
import com.example.demo.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserIdAndItemId(Long userId, Long itemId);

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByItemId(Long itemId);

    @Query("SELECT new com.example.demo.dto.ReservationResponseDto(r.id, u.nickname, i.name, r.startAt, r.endAt) FROM Reservation r " +
            "JOIN r.user u " +
            "JOIN r.item i ")
    List<ReservationResponseDto> findAllDto();

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.item.id = :id " +
            "AND NOT (r.endAt <= :startAt OR r.startAt >= :endAt) " +
            "AND r.status = 'APPROVED'")
    List<Reservation> findConflictingReservations(
            @Param("id") Long id,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}
