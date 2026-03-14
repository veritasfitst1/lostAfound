package com.example.server.repository;

import com.example.server.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    List<Item> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, Integer type);

    @Query("SELECT i FROM Item i WHERE i.status = 0 AND (LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.location) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Item> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.status = 0")
    Page<Item> findAllActive(Pageable pageable);

    long countByStatus(Integer status);

    long countByType(Integer type);
}
