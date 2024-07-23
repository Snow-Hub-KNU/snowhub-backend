package com.snowhub.server.dummy.repository;

import com.snowhub.server.dummy.model.TmpBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TmpBoardRepo extends JpaRepository<TmpBoard,Integer> {
}
