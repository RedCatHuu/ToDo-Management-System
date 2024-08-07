package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String main(@RequestParam(value = "date", required = false) String date, Model model) {
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
