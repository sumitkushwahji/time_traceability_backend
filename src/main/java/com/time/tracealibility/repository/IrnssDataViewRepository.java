package com.time.tracealibility.repository;

import com.time.tracealibility.entity.IrnssDataView;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface IrnssDataViewRepository extends JpaRepository<IrnssDataView, Long> {
    // You can add custom query methods here if needed
    // e.g. List<IrnssDataView> findBySource(String source);
}
