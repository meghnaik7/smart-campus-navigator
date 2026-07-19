package com.megh.smartcampus.algorithm.trie;

import java.util.ArrayList;
import java.util.List;

public class CampusTrie {
    private final TrieNode root = new TrieNode();
    private int wordCount = 0;

    public void insert(String word, Long id, String type, String subtitle) {
        if (word == null || word.isBlank()) return;
        String original = word.trim();
        String lower    = original.toLowerCase();
        TrieNode cur    = root;
        for (char c : lower.toCharArray()) cur = cur.addChild(c);
        if (!cur.isEndOfWord()) wordCount++;
        cur.setEndOfWord(true);
        cur.setOriginalWord(original);
        cur.setEntityId(id);
        cur.setEntityType(type);
        cur.setSubtitle(subtitle);
    }

    public List<SearchSuggestion> getSuggestions(String prefix, int limit) {
        List<SearchSuggestion> result = new ArrayList<>();
        if (prefix == null || prefix.isBlank()) return result;
        TrieNode cur = root;
        for (char c : prefix.toLowerCase().trim().toCharArray()) {
            if (!cur.hasChild(c)) return result;
            cur = cur.getChild(c);
        }
        collect(cur, result, limit);
        return result;
    }

    private void collect(TrieNode node, List<SearchSuggestion> res, int limit) {
        if (res.size() >= limit) return;
        if (node.isEndOfWord())
            res.add(new SearchSuggestion(node.getOriginalWord(), node.getEntityId(),
                                         node.getEntityType(), node.getSubtitle()));
        for (TrieNode child : node.getChildren().values()) {
            if (res.size() >= limit) break;
            collect(child, res, limit);
        }
    }

    public void clear()        { root.getChildren().clear(); wordCount = 0; }
    public int  getWordCount() { return wordCount; }
}
