package com.megh.smartcampus.algorithm.trie;

import com.megh.smartcampus.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TrieService - builds the search autocomplete index.
 *
 * Uses ApplicationContext ContextRefreshedEvent instead of @PostConstruct
 * so that @Transactional works correctly (Spring proxies are fully active).
 */
@Service
@RequiredArgsConstructor
public class TrieService {

    private static final Logger log = LoggerFactory.getLogger(TrieService.class);

    private final CampusTrie trie = new CampusTrie();
    private final BuildingRepository    buildingRepo;
    private final ClassroomRepository   classroomRepo;
    private final FacultyRepository     facultyRepo;
    private final CampusEventRepository eventRepo;

    /**
     * Triggered once after the entire Spring context is fully started.
     * At this point all proxies and transactions work correctly.
     */
    @EventListener(ContextRefreshedEvent.class)
    @Transactional(readOnly = true)
    public void onContextRefreshed() {
        build();
    }

    @Transactional(readOnly = true)
    public void reload() {
        build();
    }

    private void build() {
        trie.clear();

        // Index buildings
        buildingRepo.findByIsActiveTrue().forEach(b -> {
            trie.insert(b.getName(), b.getId(), "BUILDING", b.getType().name());
            if (b.getCode() != null && !b.getCode().isBlank())
                trie.insert(b.getCode(), b.getId(), "BUILDING", b.getName());
        });

        // Index classrooms (building is eagerly loaded via JOIN FETCH query)
        classroomRepo.findAllActiveWithBuilding().forEach(c -> {
            String bld = (c.getBuilding() != null) ? c.getBuilding().getName() : "";
            trie.insert(c.getRoomNumber(), c.getId(), "CLASSROOM", bld);
            if (c.getName() != null && !c.getName().isBlank())
                trie.insert(c.getName(), c.getId(), "CLASSROOM", bld);
        });

        // Index faculty (department eagerly loaded)
        facultyRepo.findAllActiveWithDepartment().forEach(f -> {
            String dept = (f.getDepartment() != null) ? f.getDepartment().getName() : "";
            trie.insert(f.getName(), f.getId(), "FACULTY", dept);
        });

        // Index upcoming events
        try {
            eventRepo.findUpcoming(LocalDateTime.now()).forEach(e ->
                trie.insert(e.getTitle(), e.getId(), "EVENT",
                    e.getVenueName() != null ? e.getVenueName() : ""));
        } catch (Exception ignored) {}

        log.info("Trie built with {} words", trie.getWordCount());
    }

    public List<SearchSuggestion> suggest(String prefix, int limit) {
        return trie.getSuggestions(prefix, Math.min(limit, 20));
    }

    public int count() { return trie.getWordCount(); }
}
