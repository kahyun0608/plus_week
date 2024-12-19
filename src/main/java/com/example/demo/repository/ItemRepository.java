package com.example.demo.repository;

import com.example.demo.entity.Item;
import com.example.demo.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    default Item findByIdOrElseThrow (Long itemId) {
        return findById(itemId).orElseThrow(() -> new IllegalArgumentException("해당 ID에 맞는 데이터가 존재하지 않습니다."));
    }
}
