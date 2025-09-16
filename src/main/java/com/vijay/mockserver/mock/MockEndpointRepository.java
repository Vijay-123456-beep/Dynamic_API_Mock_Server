package com.vijay.mockserver.mock;

import com.vijay.mockserver.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MockEndpointRepository extends JpaRepository<MockEndpoint, Long> {

    List<MockEndpoint> findByUser(User user);

    Optional<MockEndpoint> findTopByUserAndEndpointAndMethodOrderByVersionDesc(User user, String endpoint, String method);
}
