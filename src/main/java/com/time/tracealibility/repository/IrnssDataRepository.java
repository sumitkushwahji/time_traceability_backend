package com.time.tracealibility.repository;

import com.time.tracealibility.entity.IrnssData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IrnssDataRepository extends JpaRepository<IrnssData, Long> {

    boolean existsByMjdAndSource(int mjd, String source);

}

