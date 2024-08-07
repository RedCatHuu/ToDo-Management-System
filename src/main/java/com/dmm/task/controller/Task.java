package com.dmm.task.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Task {

	@RequestMapping("/create")
	public String index() {
		return "create";
	}

	@RequestMapping("/edit")
	public String test() {
		return "edit";
	}

	@RequestMapping("/main")
	public String main(String date, Model model) {
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
		
		
		return "main";
	}

}
