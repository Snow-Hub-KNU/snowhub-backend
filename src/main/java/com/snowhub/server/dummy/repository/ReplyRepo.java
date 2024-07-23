package com.snowhub.server.dummy.repository;

import com.snowhub.server.dummy.model.Board;
import com.snowhub.server.dummy.model.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplyRepo extends JpaRepository<Reply,Integer> {

    List<Reply> findAllByBoard(Board board);
}
