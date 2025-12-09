package es.wrapitup.wrapitup_planner.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import es.wrapitup.wrapitup_planner.security.jwt.JwtRequestFilter;
import es.wrapitup.wrapitup_planner.security.jwt.UnauthorizedHandlerJwt;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	private final JwtRequestFilter jwtRequestFilter;
	private final UnauthorizedHandlerJwt unauthorizedHandlerJwt;
	private final RepositoryUserDetailsService userDetailService;
	
	public WebSecurityConfig(JwtRequestFilter jwtRequestFilter, UnauthorizedHandlerJwt unauthorizedHandlerJwt,
							 RepositoryUserDetailsService userDetailService) {
		this.jwtRequestFilter = jwtRequestFilter;
		this.unauthorizedHandlerJwt = unauthorizedHandlerJwt;
		this.userDetailService = userDetailService;
	}

	@Bean
	public static PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin("http://localhost:4200");
		config.addAllowedOrigin("http://localhost:4201");
		config.addAllowedOrigin("http://localhost:9876");
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

	@Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) 
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

		authProvider.setUserDetailsService(userDetailService);
		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}

	@Bean
	@Order(1)
	public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
		
		http.authenticationProvider(authenticationProvider());
		
		http
			.securityMatcher("/api/**")
			.exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandlerJwt));
		
		http
			.authorizeHttpRequests(authorize -> authorize
                    // PUBLIC ENDPOINTS
					.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
					.requestMatchers(HttpMethod.POST, "/api/v1/auth/user").permitAll()
					.requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
					
					.requestMatchers(HttpMethod.GET, "/api/v1/notes/*").permitAll()
					.requestMatchers(HttpMethod.GET, "/api/v1/users/profile-image/*").permitAll()

					// PRIVATE ENDPOINTS
					.requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").hasAnyRole("USER", "ADMIN")
					
					.requestMatchers(HttpMethod.GET, "/api/v1/users").hasAnyRole("USER", "ADMIN")
					.requestMatchers(HttpMethod.GET, "/api/v1/users/*").hasAnyRole("USER", "ADMIN")
					.requestMatchers(HttpMethod.PUT, "/api/v1/users").hasAnyRole("USER", "ADMIN")
					.requestMatchers(HttpMethod.POST, "/api/v1/users/upload-image").hasAnyRole("USER", "ADMIN")
					
					// Notes - USER only (admins cannot create/edit notes)
					.requestMatchers(HttpMethod.GET, "/api/v1/notes").hasRole("USER")
					.requestMatchers(HttpMethod.GET, "/api/v1/notes/shared").hasRole("USER")
					.requestMatchers(HttpMethod.POST, "/api/v1/notes").hasRole("USER")
					.requestMatchers(HttpMethod.PUT, "/api/v1/notes/*").hasRole("USER")
					.requestMatchers(HttpMethod.PUT, "/api/v1/notes/*/share").hasRole("USER")
					
					// Delete notes - both USER (own notes) and ADMIN (any note)
					.requestMatchers(HttpMethod.DELETE, "/api/v1/notes/*").hasAnyRole("USER", "ADMIN")
					
					.requestMatchers(HttpMethod.GET, "/api/v1/notes/*/comments").hasAnyRole("USER", "ADMIN")
					.requestMatchers(HttpMethod.POST, "/api/v1/notes/*/comments").hasAnyRole("USER", "ADMIN")
					.requestMatchers(HttpMethod.DELETE, "/api/v1/notes/*/comments/*").hasAnyRole("USER", "ADMIN")

					.anyRequest().permitAll()
					);
		
        // Disable Form login Authentication
        http.formLogin(formLogin -> formLogin.disable());

        // Disable CSRF protection (it is difficult to implement in REST APIs)
        http.csrf(csrf -> csrf.disable());

        // Disable Basic Authentication
        http.httpBasic(httpBasic -> httpBasic.disable());

        // Stateless session
        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// Add JWT Token filter
		http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

}
