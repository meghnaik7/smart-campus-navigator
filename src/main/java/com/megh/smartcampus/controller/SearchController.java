package com.megh.smartcampus.controller;

import com.megh.smartcampus.algorithm.trie.SearchSuggestion;
import com.megh.smartcampus.algorithm.trie.TrieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Provides Trie-based autocomplete search.
 * Type 3+ characters → instant suggestions from the in-memory Trie.
 * No database hit — O(prefix length) time.
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search")
@SecurityRequirement(name = "bearerAuth")
public class SearchController {

    private final TrieService trieService;

    @GetMapping("/suggest")
    @Operation(summary = "Autocomplete suggestions from Trie",
               description = "Type 'lib' → returns ['Central Library', ...] instantly without DB query.")
    public ResponseEntity<List<SearchSuggestion>> suggest(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(trieService.suggest(q, limit));
    }
}
