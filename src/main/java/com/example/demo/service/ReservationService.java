package com.example.demo.service;

import com.example.demo.dto.ReservationResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.ReservationConflictException;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.example.demo.entity.Reservation.Status.*;


@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final RentalLogService rentalLogService;
    private final JPAQueryFactory jpaQueryFactory;

    public ReservationService(ReservationRepository reservationRepository,
                              ItemRepository itemRepository,
                              UserRepository userRepository,
                              RentalLogService rentalLogService,
                              JPAQueryFactory jpaQueryFactory) {
        this.reservationRepository = reservationRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.rentalLogService = rentalLogService;
        this.jpaQueryFactory = jpaQueryFactory;
    }
    @Transactional
    // TODO: 1. 트랜잭션 이해
    public void createReservation(Long itemId, Long userId, LocalDateTime startAt, LocalDateTime endAt) {
        // 쉽게 데이터를 생성하려면 아래 유효성검사 주석 처리
        List<Reservation> haveReservations = reservationRepository.findConflictingReservations(itemId, startAt, endAt);
        if(!haveReservations.isEmpty()) {
            throw new ReservationConflictException("해당 물건은 이미 그 시간에 예약이 있습니다.");
        }

        Item item = itemRepository.findByIdOrElseThrow(itemId);
        User user = userRepository.findByIdOrElseThrow(userId);
        Reservation reservation = new Reservation(item, user, PENDING, startAt, endAt);
        Reservation savedReservation = reservationRepository.save(reservation);

        RentalLog rentalLog = new RentalLog(savedReservation, "로그 메세지", "CREATE");
        rentalLogService.save(rentalLog);
    }

    // TODO: 3. N+1 문제
    public List<ReservationResponseDto> getReservations() {
        return reservationRepository.findAllDto();
    }

    // TODO: 5. QueryDSL 검색 개선
    public List<ReservationResponseDto> searchAndConvertReservations(Long userId, Long itemId) {

        List<Reservation> reservations = searchReservations(userId, itemId);

        return convertToDto(reservations);
    }

    public List<Reservation> searchReservations(Long userId, Long itemId) {

        QReservation reservation = QReservation.reservation;
        return jpaQueryFactory.selectFrom(reservation)
                .leftJoin(reservation.user).fetchJoin()
                .leftJoin(reservation.item).fetchJoin()
                .where(
                        userIdEq(userId),
                        itemIdEq(itemId)
                )
                .fetch();
    }

    private BooleanExpression userIdEq (Long userId) {
        QReservation reservation = QReservation.reservation;
        return Objects.nonNull(userId) ? reservation.user.id.eq(userId) : null;
    }

    private BooleanExpression itemIdEq (Long itemId) {
        QReservation reservation = QReservation.reservation;
        return Objects.nonNull(itemId) ? reservation.item.id.eq(itemId) : null;
    }

    private List<ReservationResponseDto> convertToDto(List<Reservation> reservations) {
        return reservations.stream()
                .map(reservation -> new ReservationResponseDto(
                        reservation.getId(),
                        reservation.getUser().getNickname(),
                        reservation.getItem().getName(),
                        reservation.getStartAt(),
                        reservation.getEndAt()
                ))
                .toList();
    }

    // TODO: 7. 리팩토링
    @Transactional
    public void updateReservationStatus(Long reservationId, String status) {
        Reservation reservation = reservationRepository.findByIdOrElseThrow(reservationId);

        if (APPROVED.getName().equals(status)) {
            if (!PENDING.equals(reservation.getStatus())) {
                throw new IllegalArgumentException("PENDING 상태만 APPROVED로 변경 가능합니다.");
            }
            reservation.updateStatus(APPROVED);
            return;
        }

        if (CANCELED.getName().equals(status)) {
            if (EXPIRED.equals(reservation.getStatus())) {
                throw new IllegalArgumentException("EXPIRED 상태인 예약은 취소할 수 없습니다.");
            }
            reservation.updateStatus(CANCELED);
            return;
        }

        if (EXPIRED.getName().equals(status)) {
            if (!PENDING.equals(reservation.getStatus())) {
                throw new IllegalArgumentException("PENDING 상태만 EXPIRED로 변경 가능합니다.");
            }
            reservation.updateStatus(Reservation.Status.EXPIRED);
            return;
        }

            throw new IllegalArgumentException("올바르지 않은 상태: " + status);
    }
}
