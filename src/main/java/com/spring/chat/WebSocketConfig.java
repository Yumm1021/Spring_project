package com.spring.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

//생성했던 구현체 등록
@Configuration // 이 클래스가 Bean 설정할 것을 의미
@EnableWebSocket // WebSocket 서버 활성화
public class WebSocketConfig implements WebSocketConfigurer{
	// implements WebSocketConfigurer > 웹 소켓 연결 구성하기 위한 메소드 구현, 제공
	
	// WebSocketConfig 만들고 WebSocketConfigurer 구현하여
	// 만들어 놓은 socketHandler 사용할 수 있게 registry에 등록
	
	@Autowired
	SocketHandler socketHandler;
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(socketHandler, "/chating/{roomNumber}");
		// url에서 chating/이후 들어오는 {roomNumber}값 > 방 구분 값
	}

}
