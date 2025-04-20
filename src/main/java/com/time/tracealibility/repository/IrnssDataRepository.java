package com.time.tracealibility.repository;

import com.time.tracealibility.dto.SourceSessionStatusDTO;
import com.time.tracealibility.entity.IrnssData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IrnssDataRepository extends JpaRepository<IrnssData, Long> {

    boolean existsByMjdAndSource(int mjd, String source);


    @Query("""
    SELECT new com.time.tracealibility.dto.SourceSessionStatusDTO(
        d.source, CAST(d.mjd AS long), COUNT(DISTINCT d.sttime), CAST(90 AS long)
    )
    FROM IrnssData d
    WHERE d.mjd = :mjd
    GROUP BY d.source, d.mjd
""")
    List<SourceSessionStatusDTO> findSessionCountsByMjd(@Param("mjd") String mjd);




}

