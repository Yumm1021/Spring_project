package com.spring.chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

// 웹소켓 구현체 등록할 SocketHandler
@Component
public class SocketHandler extends TextWebSocketHandler{ 
	// 상속받는 TextWebSocketHandler > HandlerTextMessage 실행

	//HashMap<String, WebSocketSession> sessionMap = new HashMap<>(); //웹소켓 세션을 담아둘 맵
	List <HashMap<String, Object>> ris = new ArrayList<>(); //웹소켓 세션을 담아둘 리스트 - roomListSessions , ris : 방정보 + 세션정보 관리
	
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		// 클라이언트에서 send 이용해서 메세지 발송한 경우 이벤트 > 메세지 발송
		String msg = message.getPayload(); // getPayload() > 메세지에 담긴 텍스트 값 얻어옴
		JSONObject obj = JsonToObjectParser(msg);
		
		String rN = (String) obj.get("roomNumber");
		HashMap<String, Object> temp = new HashMap<String, Object>();
		if(ris.size() > 0) {  // 방정보 + 세션정보 관리하는 rls 리스트
			for(int i = 0; i < ris.size(); i++) {
				String roomNumber = (String) ris.get(i).get("roomNumber"); //세션리스트의 저장된 방번호를 가져와서
				if(roomNumber.equals(rN)) { // 같은 값의 방이 존재한다면
					temp = ris.get(i);	// 방번호의 세션리스트의 존재하는 모든 object 값을 가져옴
					break;
				}
			}
			
			// 해당 방의 세션들만 찾아서 메세지 발송
			for(String k : temp.keySet()) { // temp에 담긴 모든 key값을 set으로 리턴
				if(k.equals("roomNumber")) { // 방번호 일 경우엔 건너뜀
					continue;
				}
				
				WebSocketSession wss = (WebSocketSession) temp.get(k); // 원래의 자료형으로 형변환
				if(wss != null) {
					try {
						wss.sendMessage(new TextMessage(obj.toJSONString()));
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
				
				/* 현재의 방번호를 가져오고 방정보+세션정보를 관리하는 rls리스트 컬랙션에서
				 * 데이터를 조회한 후에 해당 Hashmap을 임시 맵에 파싱하여
				 * roomNumber의 키값을 제외한 모든 세션키값들을 웹소켓을 통해 메시지를 보내줌
				 */
			}
		}
	}
	
	@SuppressWarnings("unchecked") // 검증되지 않은 연산자 관련 경고를 표시안함
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		// 클라이언트에서 접속하여 성공할 경우 발생하는 이벤트 > 소켓 연결
		super.afterConnectionEstablished(session); //after..메소드 참조하여 session 호출
		boolean flag = false;
		String url = session.getUri().toString();
		System.out.println(url);
		String roomNumber = url.split("/chating/")[1];
		int idx = ris.size(); // 방의 사이즈 조사
		if(ris.size() > 0) { // size() > 데이터 개수
			for(int i=0; i<ris.size(); i++) {
				String rN = (String) ris.get(i).get("roomNumber");
				if(rN.equals(roomNumber)) {
					flag = true;
					idx = i;
					break;
				}
			}
		}
		
		if(flag) { // 존재하는 방이라면 세션만 추가
			HashMap<String, Object> map = ris.get(idx);
			map.put(session.getId(), session);
		} else { // 최초 생성하는 방이라면 방번호와 세션 추가
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("roomNumber", roomNumber);
			map.put(session.getId(), session);
			ris.add(map);
		}
		
		// 세션등록이 끝나면 발급받은 세션ID 값의 메세지 발송
		JSONObject obj = new JSONObject();
		obj.put("type", "getId");
		obj.put("sessionId", session.getId());
		session.sendMessage(new TextMessage(obj.toJSONString()));
		
		/* 생성된 세션을 저장하면 발신메시지의 타입은 getId라고 명시 후 생성된 세션ID값을 클라이언트단으로 발송
		 * 클라이언트단에서는 type값을 통해 메시지와 초기 설정값을 구분
		 * 메시지 전송시 JSON파싱을 위해 message.getPayload()를 통해 
		 * 받은 문자열을 만든 함수 jsonToObjectParser에 넣어서
		 * JSONObject값으로 받아서 강제 문자열 형태로 보내줌
		 */
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		// 클라이언트에서 연결 종료할 경우 발생하는 이벤트 > 소켓 종료
		
		if(ris.size() > 0) { // 소켓이 종료되면 해당 세션값들을 찾아서 지움
			for(int i=0; i<ris.size(); i++) { 
				ris.get(i).remove(session.getId());
			}
		}
		super.afterConnectionClosed(session, status);
	}
	
	private static JSONObject JsonToObjectParser(String jsonStr) {
		JSONParser parser = new JSONParser(); 
		JSONObject obj = null; 
		try {
			obj = (JSONObject) parser.parse(jsonStr);
			//json형태의 문자열을 파라미터로 받아서 SimpleJson의 파서를 활용하여 JSONObject로 파싱처리
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	// 최종 > 방구분을 하고 해당 방에 존재하는 session값들에게만 메시지를 발송하여 구분
}
