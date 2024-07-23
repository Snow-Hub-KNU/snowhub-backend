package com.snowhub.server.dummy.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reply { // Reply : Board = N : 1

    // 최신순 = 2024-05-22
    // 등록순 = 2024-03-13
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    private Board board;

    private String reply;

    @CreationTimestamp
    private Timestamp createDate; // 작성일
}
