package com.time.tracealibility.repository;

import com.time.tracealibility.dto.SourceSessionStatusDTO;
import com.time.tracealibility.entity.IrnssData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IrnssDataRepository extends JpaRepository<IrnssData, Long> {

    boolean existsByMjdAndSource(int mjd, String source);


    @Query("""
    SELECT new com.time.tracealibility.dto.SourceSessionStatusDTO(
        d.source, CAST(d.mjd AS long), COUNT(DISTINCT d.sttime), CAST(90 AS long)
    )
    FROM IrnssData d
    WHERE (:mjd IS NULL OR d.mjd = :mjd)
    GROUP BY d.source, d.mjd
""")
    List<SourceSessionStatusDTO> findSessionCountsByMjd(@Param("mjd") String mjd);

    @Query("""
    SELECT new com.time.tracealibility.dto.SourceSessionStatusDTO(
        d.source, CAST(d.mjd AS long), COUNT(DISTINCT d.sttime), CAST(90 AS long)
    )
    FROM IrnssData d
    WHERE (:source IS NULL OR d.source = :source)
      AND (:mjd IS NULL OR d.mjd = :mjd)
    GROUP BY d.source, d.mjd
""")
    List<SourceSessionStatusDTO> findSessionCountsByFilters(
            @Param("source") String source,
            @Param("mjd") String mjd,
            @Param("currentSessionCount") Integer currentSessionCount, // Not directly filterable from the grouped query
            @Param("expectedSessionCount") Integer expectedSessionCount // Not directly filterable from the grouped query
    );


}

