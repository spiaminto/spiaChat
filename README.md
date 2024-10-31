# 채팅 with GPT
## 개요
### [프로젝트 바로가기](http://spia.ap-northeast-2.elasticbeanstalk.com/spiachat/lobby)    
___샘플 ID: sample / PW: sample123___ (오전 8시 ~ 오후 10시 까지 이용가능합니다.)

웹 소켓을 이용한 채팅 서비스에 Chat GPT 를 적용한 프로젝트입니다.<br>
[이전 프로젝트](https://github.com/spiamint/chatApp) 를 JPA 기술을 사용하여 리팩토링 하였습니다.<br>
Mybatis 에서 JPA 로 전환하면서 필요없는 쿼리가 나가지 않도록, 또 기존 쿼리 횟수를 줄이기 위해 노력하였습니다.<br>

### 사용 기술

* Spring(Boot), Gradle
* Spring Data Jpa, QueryDSL
* MySql
* Thymeleaf
* HTML, CSS(BootStrap), JavaScript
* AWS ElasticBeanstalk

## 1. ERD
<img width="75%" alt="ERD" src="https://github.com/spiamint/boardJpa/assets/122969954/02eaabc5-2ef7-446d-9bc8-133b5401d6a2">

## 2. 일반채팅
#### 2.0 데모 영상 (유튜브)
[![Video Label](http://img.youtube.com/vi/_226vM3Djz8/0.jpg)](https://youtu.be/_226vM3Djz8)
#### 2.1 채팅
<img width="48%" alt='normalChat' src='https://github.com/spiamint/chatApp/assets/122969954/107bc772-df30-41bd-ad9e-69f2720d54bc'>
<img width="48%" alt='normalChatStatus' src='https://github.com/spiamint/chatApp/assets/122969954/40d93cfc-a093-412e-9215-8b8495ef4975'>

채팅방에 접속하면, 접속자 상태를 확인할 수 있으며 접속한 유저들과 자유롭게 채팅할 수 있습니다.

#### 2.2 GPT 활성화 및 질문하기
<img alt='normalChatGpt' src='https://github.com/spiamint/chatApp/assets/122969954/60e69308-78ee-4cdf-b0c4-6c7349e7ea7e'>

필요한 경우 GPT 를 활성화 하여, GPT 에게 질문 할 수 있습니다.  

GPT 를 종료할 권한은 활성화 한 유저가 가지며, 활성화 하지 않은 유저도 질문할 수 있습니다.

GPT 요청 질문 목록은 이전 질문을 포함하도록 설정되어 있습니다.

## 3. 스무고개
#### 3.0 데모 영상 (유튜브)
[![Video Label](http://img.youtube.com/vi/YqRH595rA_0/0.jpg)](https://youtu.be/YqRH595rA_0)
#### 3.1 준비하기
플레이어는 '준비하기' 버튼을 눌러 게임에 참가 할 수 있습니다.
#### 3.2 시작하기
방장 은 모든 플레이어가 준비 상태일때 게임을 시작할 수 있습니다.
#### 3.3 게임진행 및 종료
한 플레이어가 정답을 맞추거나, 다른 플레이어가 퇴장하면 게임이 종료됩니다.  
게임 종료 후, 다시 모든 플레이어가 준비 상태일때 게임을 새로 시작할 수 있습니다.
#### 3.4 플레이어 강퇴
<img width="48%" alt='banPlayer' src='https://github.com/spiamint/chatApp/assets/122969954/515831bb-a377-4f46-b5d4-732fdf6b25a3'>
<img width="48%" alt='banPlayerConfirm' src='https://github.com/spiamint/chatApp/assets/122969954/7e6c3cee-3ec1-4a89-bc13-f2f1516b2eff'>

방장은 게임 시작 전, 임의의 플레이어 를 강퇴할 수 있습니다.

#### 3.5 관전
<img alt='twentyObserve' src='https://github.com/spiamint/chatApp/assets/122969954/aea313c7-7fa7-4264-bfe1-8f770714221b'>

게임 도중에 입장한 유저는 자동으로 관전자가 되며, GPT 질문을 제외한 일반채팅을 할 수 있습니다.

관전자는 해당 게임이 종료되면 플레이어로서 다음 게임에 참가 가능합니다.

## 4. 후기
블로그 링크에 그동안 개발하면서 생긴 문제점, 이에 대한 고민과 해결방법에 대해 기록하였습니다.  
[블로그 링크](https://spiaminto.tistory.com/category/%ED%95%99%EC%8A%B5%EC%A0%95%EB%A6%AC%28%EA%B3%B5%EA%B0%9C%29)





