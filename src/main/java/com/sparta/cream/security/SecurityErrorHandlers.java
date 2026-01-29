package com.sparta.cream.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.cream.exception.CommonErrorResponse;
import com.sparta.cream.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Spring Security 단계에서 발생하는 401/403 응답을 공통 에러 포맷으로 내려주기 위한 핸들러입니다.
 *
 * @author 오정빈
 * @version 1.0
 */
@Component
public class SecurityErrorHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

	public static final String ATTR_AUTH_ERROR_CODE = "AUTH_ERROR_CODE";

	private final ObjectMapper objectMapper;

	public SecurityErrorHandlers(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(
		HttpServletRequest request,
		HttpServletResponse response,
		org.springframework.security.core.AuthenticationException authException
	) throws IOException, ServletException {

		ErrorCode code = ErrorCode.AUTH_UNAUTHORIZED;
		Object attr = request.getAttribute(ATTR_AUTH_ERROR_CODE);
		if (attr instanceof ErrorCode) {
			code = (ErrorCode)attr;
		}

		response.setStatus(code.getStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), CommonErrorResponse.of(code));
	}

	@Override
	public void handle(
		HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException accessDeniedException
	) throws IOException, ServletException {
		response.setStatus(ErrorCode.ACCESS_DENIED.getStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), CommonErrorResponse.of(ErrorCode.ACCESS_DENIED));
	}
}


