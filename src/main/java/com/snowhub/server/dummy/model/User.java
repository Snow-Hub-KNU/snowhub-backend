package com.snowhub.server.dummy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.database.annotations.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;

import java.util.List;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
public class User {

    public User(){

    }

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    // 한 명의 User는 여러개의 board를 가질 수 있다.
    @OneToMany(mappedBy = "user")
    private List<Board> boardList;

    // 이메일 = 식별가능한 이름. 어차피 중복x
    private String  email;

    // Authentication을 만들기 위함 <- security에서 필수, firebase 등록을 위해서 필수
    private String password;

    //private String phoneNumber;

    @Column(name = "username")
    private String displayName;

    // enum => String으로 자동변환
    // 사용자 역할 식별을 위해서 필요할 듯.
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    // 연속적인 서비스 이용을 위해서 필수!
    @Column(length = 1000)
    private String refreshToken;

    private String uid;// 로그아웃시 RefeshToken을 만료시키기 위해서 로컬 DB에 따로 저장. 나중에 회원탈퇴시 필요할 듯 firebase에.


    // User는 TmpBoard에 자신의 글을 임시 저장을 할 수 있다.
    // Join을 안하면 select 2번, 하면 1번
    @OneToOne
    @JoinColumn(name = "tmpboard_id")
    private TmpBoard tmpBoard;

    //
    public void addBoards(Board board){
        boardList.add(board);
    }

    public List<Board> getBoards(){
        return boardList;
    }



}
