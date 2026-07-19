package com.megh.smartcampus.controller;

import com.megh.smartcampus.algorithm.trie.SearchSuggestion;
import com.megh.smartcampus.algorithm.trie.TrieService;
import com.megh.smartcampus.entity.SearchHistory;
import com.megh.smartcampus.repository.SearchHistoryRepository;
import com.megh.smartcampus.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search")
@SecurityRequirement(name = "bearerAuth")
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    private final TrieService              trieService;
    private final UserRepository           userRepo;
    private final SearchHistoryRepository  searchRepo;

    @GetMapping("/suggest")
    @Operation(summary = "Autocomplete suggestions using Trie (instant)",
               description = "Returns suggestions as you type. Type 'lib' → ['Library', 'Lab 1', ...]")
    public ResponseEntity<List<SearchSuggestion>> suggest(
            @RequestParam String q,
            @RequestParam(defaultValue="10") int limit,
            @AuthenticationPrincipal UserDetails ud) {
        List<SearchSuggestion> suggestions = trieService.suggest(q, limit);
        saveSearch(ud, q);
        return ResponseEntity.ok(suggestions);
    }

    @PostMapping("/trie/reload")
    @Operation(summary = "[ADMIN] Reload search index")
    public ResponseEntity<String> reload() {
        trieService.reload();
        return ResponseEntity.ok("Search index reloaded. Words indexed: " + trieService.count());
    }

    private void saveSearch(UserDetails ud, String q) {
        if (ud == null || q == null || q.isBlank()) return;
        try {
            userRepo.findByEmail(ud.getUsername()).ifPresent(user ->
                searchRepo.save(SearchHistory.builder()
                    .user(user).query(q)
                    .searchType(SearchHistory.SearchType.GENERAL).build()));
        } catch (Exception ex) {
            log.warn("Failed to save search history for user '{}': {}",
                ud.getUsername(), ex.getMessage());
        }
    }
}
