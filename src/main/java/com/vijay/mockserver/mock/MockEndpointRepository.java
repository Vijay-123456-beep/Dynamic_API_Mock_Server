package com.vijay.mockserver.mock;

import com.vijay.mockserver.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MockEndpointRepository extends JpaRepository<MockEndpoint, Long> {

    List<MockEndpoint> findByUser(User user);

    Page<MockEndpoint> findByUser(User user, Pageable pageable);

    Optional<MockEndpoint> findTopByUserAndEndpointAndMethodOrderByVersionDesc(User user, String endpoint, String method);

    List<MockEndpoint> findByUserAndEndpointAndMethodOrderByPriorityDesc(User user, String endpoint, String method);

    @Query("SELECT m FROM MockEndpoint m WHERE m.user = :user AND "
            + "(:endpoint IS NULL OR m.endpoint LIKE %:endpoint%) AND "
            + "(:method IS NULL OR m.method = :method) AND "
            + "(:isActive IS NULL OR m.isActive = :isActive)")
    Page<MockEndpoint> findByUserAndFilters(@Param("user") User user,
            @Param("endpoint") String endpoint,
            @Param("method") String method,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("SELECT m FROM MockEndpoint m WHERE m.user = :user AND "
            + "(:searchTerm IS NULL OR "
            + "m.endpoint LIKE %:searchTerm% OR "
            + "m.method LIKE %:searchTerm% OR "
            + "m.responseJson LIKE %:searchTerm%)")
    Page<MockEndpoint> findByUserAndSearchTerm(@Param("user") User user,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    long countByIsActiveTrue();

    long countByUser(User user);
}
