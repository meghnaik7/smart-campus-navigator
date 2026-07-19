package com.megh.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "search_history",
    indexes = @Index(name = "idx_search_user", columnList = "user_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SearchHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "query", nullable = false, length = 255)
    private String query;

    @Enumerated(EnumType.STRING)
    @Column(name = "search_type", length = 20)
    private SearchType searchType;

    @Column(name = "result_id")
    private Long resultId;

    public enum SearchType { BUILDING, FACULTY, CLASSROOM, EVENT, GENERAL }
}
