package com.dmm.task.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmm.task.data.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
	List<Task> findByName(String name);
}
