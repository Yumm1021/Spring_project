<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
<meta charset="UTF-8">
<title>Chating</title>
<style>
	* {
		margin: 0;
		padding: 0;
	}
	.container {
		width: 500px;
		margin: 0 auto;
		padding: 25px;
	}
	.container h1 {
		text-align: left;
		padding: 5px 5px 5px 15px;
		color: #FFBB00;
		border-left: 3px solid #FFBB00;
		margin-bottom: 20px;
	}
	.chating {
		background-color: #000;
		width: 500px;
		height: 500px;
		overflow: auto;
	}
	.chating .me {
		color: #F6F6F6;
		text-align: right;
	}
	.chating .others {
		color: #FFE400;
		text-align: left;
	}
	input {
		width: 330px;
		height: 25px;
	}
	#yourMsg {
		display: none;
	}
</style>
</head>


<body>
	<div id="container" class="container">
		<h1>${roomName}의 채팅방</h1>
		<input type="hidden" id="sessionId" value="">
		<input type="hidden" id="roomNumber" value="${roomNumber}">
		<input type="hidden" id="h_userName" value="">
		
		
		<div id="chating" class="chating"></div>
		
		<div id="yourName">
			<table class="inputTable">
				<tr>
					<th>사용자명</th>
					<th><input type="text" name="userName" id="userName"></th>
					<th><button onclick="chatName()" id="startBtn">이름 등록</button></th>
				</tr>
			</table>
		</div>
		
		<div id="yourMsg">
			<table class="inputTable">
				<tr>
					<th>메세지</th>
					<th><input id="chatting" placeholder="보내실 메세지를 입력하세요"></th>
					<th><button onclick="send()" id="sendBtn">보내기</button></th>
					<th><button onclick="end()" id="endBtn">채팅종료</button></th>
				</tr>
			</table>
		</div>
	</div>
	
<script type="text/javascript">
	var ws; // ws 변수 선언
	
	function wsOpen() {
		//웹소켓 전송시 현재 방의 번호를 넘겨서 보냄
		ws = new WebSocket("ws://"+location.host+"/chating/"+$("#roomNumber").val()); // 원래는 WebSocket ws = new WebSocket 인거지 그치만 다른데서도 ws를 쓰기 위해서 위에 선언 해주는 거야
		wsEvt(); // 웹소켓 활성화되면 발생하는 이벤트
	} 
	
	function wsEvt() { // 그 함수 
		
		ws.onopen = function(data) {
			// 소켓이 열리면 동작
		}

		ws.onmessage = function(data) {
			// 메세지를 받으면 동작
			var msg = data.data;
			if(msg != null && msg.trim() != '') { // trim() > 문자열 좌우 공백 제거
				var d = JSON.parse(msg); // "msg" String 객체 > JSON 형태로 변환
				if(d.type == "getId") {
					var si = d.sessionId != null ? d.sessionId : ""; // msg에 sessionId가 null이 아니라면 sessionId
					if(si != '') {
						$("#sessionId").val(si); // sessionId > 상대와 나를 구분하기 위해 사용
					}
				} else if(d.type == "message") {
					if(d.sessionId == $("#sessionId").val()) {
						$("#chating").append("<p class='me'>"+d.userName+" : " + d.msg + "</p>");
					} else {
						$("#chating").append("<p class='others'>"+d.userName+ ":" + d.msg + "</p>");
					}
				} else {
					console.warn("알 수 없음!") // 웹 콘솔에 "알 수 없음" 경고 메세지 출력
				
			}
		}
		
	 	 document.addEventListener("keypress", function() { 
			if(e.keyCode == 13) { //enter press
				send();
			}
		}); 
	}
}	
		
	function chatName() {
		var userName = $("#userName").val();
		if(userName == null || userName.trim() == "") {
			alert("사용자 이름을 입력해주세요");
			$("#userName").focus();
		} else {
			$("#h_userName").val(userName);
			wsOpen();
			$("#yourName").hide();
			$("#yourMsg").show();
		}
}
	
	 function send() {
			var option = {
					type: "message",
					roomNumber: $("#roomNumber").val(),
					sessionId: $("#sessionId").val(),
					userName: $("#h_userName").val(),
					msg: $("#chatting").val()
			}
			console.log(option.type);
			console.log(option.roomNumber);
			console.log(option.sessionId);
			console.log(option.userName);
			
			console.log(option.msg);
			ws.send(JSON.stringify(option));
			 $("#chatting").val("");
	 }
	 
	 function end() {
		 $("#container").remove();
	 }
	 
</script>
</body>
</html>