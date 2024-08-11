package com.dmm.task.data.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmm.task.data.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
	List<Task> findByName(String name);
	
	// 期間とユーザー名でフィルタリング↓タスクを取得する
    List<Task> findByNameAndDateBetween(String name, LocalDateTime start, LocalDateTime end);
    
    // 期間でフィルタリングして、全てのタスクを取得する (管理者用)
    List<Task> findByDateBetween(LocalDateTime start, LocalDateTime end);
}
