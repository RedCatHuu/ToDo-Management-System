package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dmm.task.data.entity.Task;
import com.dmm.task.data.entity.Users;
import com.dmm.task.data.repository.TaskRepository;
import com.dmm.task.form.TaskForm;
import com.dmm.task.service.AccountUserDetails;


@Controller
public class TaskController {
	
	@Autowired
	private TaskRepository repo;

	@GetMapping("/main/create/{date}")
	public String newPost(@PathVariable String date, Model model) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate parsedDate = LocalDate.parse(date, formatter);
		LocalDateTime dateTime = parsedDate.atTime(0, 0);
		
		model.addAttribute("date", dateTime);
	    model.addAttribute("taskForm", new TaskForm());
	    
	    return "create";
	}


	
	@PostMapping("/main/create")
	public String create(@Validated TaskForm taskForm, BindingResult bindingResult,
			@AuthenticationPrincipal AccountUserDetails user, Model model) {
		// バリデーションの結果、エラーがあるかどうかチェック
		if (bindingResult.hasErrors()) {
			// エラーがある場合は投稿登録画面を返す
			List<Task> list = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
			model.addAttribute("task", list);
	        model.addAttribute("taskForm", taskForm);
			return "/create";
		}
		LocalDate date = taskForm.getDate();
		LocalDateTime dateTime = date.atTime(0, 0);

		Task task = new Task();
		task.setTitle(taskForm.getTitle());
		task.setText(taskForm.getText());
		task.setName(user.getName());
		task.setDate(dateTime);

		repo.save(task);
		
	    return "redirect:/main";
	}


	@GetMapping("/main/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model) {
	    Task task = repo.findById(id)
	                   .orElseThrow(() -> new IllegalArgumentException("Invalid task Id:" + id));
	    model.addAttribute("task", task);
        return "edit";
    }
	
	@PostMapping("/main/edit/{id}")
	public String updateTask(@PathVariable("id") Long id, @ModelAttribute TaskForm taskForm, BindingResult bindingResult) {
	    if (bindingResult.hasErrors()) {
	        return "edit";
	    }

	    Task existingTask = repo.findById(id)
	        .orElseThrow(() -> new IllegalArgumentException("Invalid task Id:" + id));
	    
		LocalDate date =taskForm.getDate();
		LocalDateTime dateTime = date.atTime(0,0);
		
	    existingTask.setTitle(taskForm.getTitle());
	    existingTask.setDate(dateTime);
	    existingTask.setText(taskForm.getText());
	    existingTask.setDone(taskForm.isDone());

	    repo.save(existingTask);

	    return "redirect:/main";
	}


    @RequestMapping("/main")
    public String main(@RequestParam(value = "date", required = false) String date, Model model, @AuthenticationPrincipal AccountUserDetails user) {
        LocalDate currentDate;

        // 矢印で月を変えたときに使用。valueでdateに日付が格納されている。
        if (date != null && !date.isEmpty()) {
            currentDate = LocalDate.parse(date);
        } else {
            // ログイン後のmain画面で使用
            currentDate = LocalDate.now();
        }
        
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");
        String month = currentDate.format(monthFormatter);

        // 設定した日付の前月と翌月を計算
        LocalDate prev = currentDate.minusMonths(1);
        LocalDate next = currentDate.plusMonths(1);

        model.addAttribute("month", month);
        model.addAttribute("prev", prev);
        model.addAttribute("next", next);
        
        // ログインユーザー情報
        Users currentUser = user.getUser();
        String roleName = "ROLE_" + currentUser.getRoleName();
        
        // admingかuserか判別
        List<Task> tasksList;
        if(roleName.equals("ROLE_ADMIN")) {
        	tasksList = repo.findAll();
        } else {
        	tasksList = repo.findByName(currentUser.getName());
        }
        
        // タスクを取得し、日付をキーにしたMapに変換
		LinkedMultiValueMap<LocalDate, Task> tasksMap = new LinkedMultiValueMap<>();
		tasksList.forEach(task -> tasksMap.add(task.getDate().toLocalDate(), task));
        model.addAttribute("tasks", tasksMap);
        
        // カレンダー用のデータを生成
        // 1. 2次元表になるので、ListのListを用意する
        List<List<LocalDate>> weeks = new ArrayList<>();

        // 2. 1週間分のLocalDateを格納するListを用意する
        List<LocalDate> week = new ArrayList<>();

        // 3. その月の1日のLocalDateを取得する
        YearMonth currentMonth = YearMonth.from(currentDate);
        LocalDate firstDayOfMonth = currentMonth.atDay(1);

        // 4. 曜日を表すDayOfWeekを取得し、前月分の日付を求める
        DayOfWeek firstDayOfWeek = firstDayOfMonth.getDayOfWeek();
        LocalDate startDay;
        // 初日が日曜日なら前月の日付を追加しない。
        if (firstDayOfWeek == DayOfWeek.SUNDAY) {
            startDay = firstDayOfMonth;
        } else {
            startDay = firstDayOfMonth.minusDays(firstDayOfWeek.getValue());
        }

        // 5. 1日ずつ増やしてLocalDateを求めていき、1週間分詰めたら1．のリストへ格納する
        LocalDate day = startDay;
        while (day.isBefore(firstDayOfMonth.plusMonths(1))) {
            week.add(day);
            if (week.size() == 7) {
                weeks.add(new ArrayList<>(week));
                week.clear();
            }
            day = day.plusDays(1);
        }

        // 6. 次月の日付を追加して行数を揃える
        DayOfWeek lastDayOfMonth = currentMonth.atEndOfMonth().getDayOfWeek();
        if(lastDayOfMonth != DayOfWeek.SATURDAY ) {
	        while (week.size() < 7) {
	            week.add(day);
	            day = day.plusDays(1);
	        }
	        weeks.add(week);
        }

        model.addAttribute("matrix", weeks);
        


        return "main";
    }
}
