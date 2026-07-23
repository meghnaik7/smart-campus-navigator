package com.megh.smartcampus.algorithm.trie;

import com.megh.smartcampus.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TrieService — builds an in-memory Trie from campus buildings for instant autocomplete.
 *
 * On startup, all active building names and codes are inserted into the Trie.
 * When a user types "lib", the Trie returns "Central Library" in O(prefix length)
 * time — no database query needed.
 */
@Service
@RequiredArgsConstructor
public class TrieService {

    private static final Logger log = LoggerFactory.getLogger(TrieService.class);

    private final CampusTrie       trie = new CampusTrie();
    private final BuildingRepository buildingRepo;

    /** Called once after the full Spring context starts — @Transactional works here. */
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

        // Index all active buildings by name and short code
        buildingRepo.findByIsActiveTrue().forEach(b -> {
            trie.insert(b.getName(), b.getId(), "BUILDING", b.getType().name());
            if (b.getCode() != null && !b.getCode().isBlank())
                trie.insert(b.getCode(), b.getId(), "BUILDING", b.getName());
        });

        log.info("Trie built with {} words", trie.getWordCount());
    }

    public List<SearchSuggestion> suggest(String prefix, int limit) {
        return trie.getSuggestions(prefix, Math.min(limit, 20));
    }
}
