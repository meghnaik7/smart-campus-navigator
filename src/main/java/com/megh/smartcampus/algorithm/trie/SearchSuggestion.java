package com.megh.smartcampus.algorithm.trie;

public class SearchSuggestion {
    private final String displayText;
    private final Long entityId;
    private final String entityType;
    private final String subtitle;

    public SearchSuggestion(String displayText, Long entityId, String entityType, String subtitle) {
        this.displayText = displayText;
        this.entityId    = entityId;
        this.entityType  = entityType;
        this.subtitle    = subtitle;
    }

    public String getDisplayText() { return displayText; }
    public Long   getEntityId()    { return entityId; }
    public String getEntityType()  { return entityType; }
    public String getSubtitle()    { return subtitle; }
}
