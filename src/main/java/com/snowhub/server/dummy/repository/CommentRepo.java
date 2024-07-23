package com.snowhub.server.dummy.repository;

import com.snowhub.server.dummy.model.Comment;
import com.snowhub.server.dummy.model.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepo extends JpaRepository<Comment,Integer> {

    List<Comment> findByReply(Reply reply);
}
