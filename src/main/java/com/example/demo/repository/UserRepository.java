package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    List<User> findByIdIn(Collection<Long> ids);

    @Modifying
    @Query("UPDATE User u set u.status='BLOCKED' WHERE u.id IN :userIds")
    void updateStatusToBlockedIn(List<Long> userIds);

}
