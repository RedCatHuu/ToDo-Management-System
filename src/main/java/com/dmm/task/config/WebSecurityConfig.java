package com.dmm.task.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dmm.task.service.AccountUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	private AccountUserDetailsService userDetailsService;

	public PasswordEncoder passwordEncoder() {
		// BCryptアルゴリズムを使用してパスワードのハッシュ化を行う
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		// AuthenticationManagerBuilderに、実装したUserDetailsServiceを設定する
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
		super.configure(auth);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// 認可の設定
		http.exceptionHandling().accessDeniedPage("/accessDeniedPage").and().authorizeRequests()
				.antMatchers("/login", "/css/**").permitAll() // loginは、全ユーザからのアクセスを許可
				.anyRequest().authenticated(); // login以外は、認証を求める

		// ログイン設定
		http.formLogin() // フォーム認証の有効化
				.loginPage("/login") // ログインフォームを表示するパス
				.loginProcessingUrl("/authenticate") // フォーム認証処理のパス
				.usernameParameter("userName") // ユーザ名のリクエストパラメータ名
				.passwordParameter("password") // パスワードのリクエストパラメータ名
				.defaultSuccessUrl("/main") // 認証成功時の遷移先
				.failureUrl("/login?error=true"); // 認証失敗時に遷移するパス

		// ログアウト設定
		http.logout().logoutSuccessUrl("/login") // ログアウト成功時に遷移するパス
				.permitAll(); // 全ユーザに対して許可
	}

}