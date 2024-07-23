package com.snowhub.server.dummy.repository;

import com.snowhub.server.dummy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User,Integer> {

    User findByDisplayName(String username);// fireBase용
    User findByEmail(String email);
    User findByRefreshToken(String refreshtoken);
}
