package com.snowhub.server.dummy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


// User만 조회했는데 자꾸 쓸데없이 같이 조회됨 <- 지연로딩할 필요가 있다.
@Getter
@Setter
@Entity
public class TmpBoard {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private String category;

}
