package com.dmm.task.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmm.task.data.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

}
