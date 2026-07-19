package com.megh.smartcampus.algorithm.trie;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {
    private final Map<Character, TrieNode> children = new HashMap<>();
    private boolean endOfWord;
    private String originalWord;
    private Long entityId;
    private String entityType;
    private String subtitle;

    public Map<Character, TrieNode> getChildren() { return children; }
    public boolean isEndOfWord()                  { return endOfWord; }
    public void setEndOfWord(boolean v)           { endOfWord = v; }
    public String getOriginalWord()               { return originalWord; }
    public void setOriginalWord(String v)         { originalWord = v; }
    public Long getEntityId()                     { return entityId; }
    public void setEntityId(Long v)               { entityId = v; }
    public String getEntityType()                 { return entityType; }
    public void setEntityType(String v)           { entityType = v; }
    public String getSubtitle()                   { return subtitle; }
    public void setSubtitle(String v)             { subtitle = v; }
    public boolean hasChild(char c)               { return children.containsKey(c); }
    public TrieNode getChild(char c)              { return children.get(c); }
    public TrieNode addChild(char c)              { return children.computeIfAbsent(c, k -> new TrieNode()); }
}
