package com.snowhub.server.dummy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne // Comment:reply = N:1
    private Reply reply;

    @Column(columnDefinition = "LONGTEXT") // DB에 글자 충분히 저장할 수 있게.
    private String comment;
}
