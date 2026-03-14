package com.example.server.repository;

import com.example.server.entity.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long> {

    List<ItemCategory> findAllByOrderBySortOrderAsc();
}
