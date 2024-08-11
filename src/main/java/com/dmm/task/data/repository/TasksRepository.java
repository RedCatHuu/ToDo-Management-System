package com.dmm.task.data.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmm.task.data.entity.Tasks;

public interface TasksRepository extends JpaRepository<Tasks, Long> {
	List<Tasks> findByName(String name);
	
	// 期間とユーザー名でフィルタリング↓タスクを取得する
    List<Tasks> findByNameAndDateBetween(String name, LocalDateTime start, LocalDateTime end);
    
    // 期間でフィルタリングして、全てのタスクを取得する (管理者用)
    List<Tasks> findByDateBetween(LocalDateTime start, LocalDateTime end);
}
