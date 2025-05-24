package com.time.tracealibility.services;

import com.time.tracealibility.dto.PivotedSatDiffDTO;
import com.time.tracealibility.entity.SatCommonViewDifference;
import com.time.tracealibility.repository.SatCommonViewDifferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Service
public class SatCommonViewDifferenceService {

    @Autowired
    private SatCommonViewDifferenceRepository repository;

    public List<PivotedSatDiffDTO> getPivotedSatDifferences(LocalDateTime startDate, LocalDateTime endDate, String source1) {
        List<SatCommonViewDifference> filteredData = repository.findByFilters(startDate, endDate, source1);

        return filteredData.stream()
                .collect(Collectors.groupingBy(
                        d -> List.of(d.getSatLetter(), d.getMjd(), d.getMjdDateTime(), d.getSttime()),
                        LinkedHashMap::new,
                        Collectors.toMap(
                                SatCommonViewDifference::getSource2,
                                SatCommonViewDifference::getAvgRefsysDifference,
                                (v1, v2) -> v1 // handle duplicate source2 entries
                        )
                ))
                .entrySet().stream()
                .map(entry -> {
                    List<?> key = entry.getKey();
                    String satLetter = (String) key.get(0);
                    Integer mjd = (Integer) key.get(1);
                    String mjdDateTime = key.get(2).toString();
                    String sttime = (String) key.get(3);
                    Map<String, Double> diffsByLoc = entry.getValue();

                    return new PivotedSatDiffDTO(
                            satLetter,
                            mjd,
                            mjdDateTime,
                            sttime,
                            diffsByLoc
                    );
                })
                .collect(Collectors.toList());
    }
}
