package com.megh.smartcampus.analytics;

import com.megh.smartcampus.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {
    private final SearchHistoryRepository searchRepo;
    private final RouteHistoryRepository  routeRepo;
    private final UserRepository          userRepo;
    private final BuildingRepository      buildingRepo;
    private final FacultyRepository       facultyRepo;
    private final ClassroomRepository     classroomRepo;

    public Map<String, Object> dashboard() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalUsers",      userRepo.count());
        m.put("totalBuildings",  buildingRepo.count());
        m.put("totalFaculty",    facultyRepo.count());
        m.put("totalClassrooms", classroomRepo.count());
        m.put("totalSearches",   searchRepo.count());
        m.put("totalRoutes",     routeRepo.count());
        return m;
    }

    public List<Map<String, Object>> topSearches(int limit) {
        return searchRepo.findTopQueries(PageRequest.of(0, limit)).stream()
            .map(r -> Map.<String, Object>of("query", r[0], "count", r[1]))
            .toList();
    }

    public List<Map<String, Object>> topDestinations(int limit) {
        return routeRepo.findTopDestinations(PageRequest.of(0, limit)).stream()
            .map(r -> Map.<String, Object>of("destination", r[0], "count", r[1]))
            .toList();
    }
}
