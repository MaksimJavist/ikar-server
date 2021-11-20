package com.ikar.ikarserver.repository.jpa;

import com.ikar.ikarserver.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

}
