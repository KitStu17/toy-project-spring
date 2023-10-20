# WebKit640 개인 프로젝트(백엔드)

## 토이 프로젝트 멤버 모집 사이트

### 개발 및 운영 환경

- OS
  - Windows 11 Home
- Middleware
  - Apache Tomcat, Redis
- DB
  - MariaDB
- Framework
  - JDK, Spring, Spring boot
- Language
  - Java
- Tool
  - VS Code, DBeaver

### 요구 사항

- 사이트 이용자의 회원 정보 관리를 위한 CRUD API 구현
- 모집 글 관리를 위한 CRUD API 구현
- 참여 요청 관리를 위한 CRUD API 구현
- 이메일 인증, 참여 요청 알림을 위한 Redis, JavaMailSender 설정
- 로그인 여부에 따른 사용 가능 API 설정

### E-R 다이어그램

![개인 프로젝트 테이블.png](<WebKit640%20%E1%84%80%E1%85%A2%E1%84%8B%E1%85%B5%E1%86%AB%20%E1%84%91%E1%85%B3%E1%84%85%E1%85%A9%E1%84%8C%E1%85%A6%E1%86%A8%E1%84%90%E1%85%B3(%E1%84%87%E1%85%A2%E1%86%A8%E1%84%8B%E1%85%A6%E1%86%AB%E1%84%83%E1%85%B3)%20f67a7cf05aa941a68a34d8839e156d72/%25EA%25B0%259C%25EC%259D%25B8_%25ED%2594%2584%25EB%25A1%259C%25EC%25A0%259D%25ED%258A%25B8_%25ED%2585%258C%25EC%259D%25B4%25EB%25B8%2594.png>)

- 회원 정보가 담기는 MEMBER 테이블
- 모집 글 정보가 담기는 MEMBER_POST 테이블
- 회원의 관심 기술 스택, 모집 글의 사용 기술 스택이 담기는 MEMBER_SKILL, POST_SKILL 테이블
- 모집 참여 요청 정보가 담기는 POST_APPLICANT 테이블
- MEMBER_SKILL, POST_SKILL 테이블의 경우, 참조 만을 위한 테이블이다.
- 설계 단계에서 제작한 E-R 다이어그램 링크
  [WebKit640 개인 프로젝트-2](https://brash-draw-d7b.notion.site/WebKit640-2-5502e0dc5c664e3080f8fdd920d5f186?pvs=4)
- 설계 단계에 존재하는 MEMBER_RATING 테이블의 경우, 참여 멤버 평가 기능을 설계에 반영하지 않아 삭제되었다.

### 구현 API 설명

- 회원 가입
  - 회원 가입을 위해 사용
  - 로그인하지 않는 사용자도 이용 가능
  - REST API : /auth/signup
  - 메소드 : POST
- 로그인
  - 회원 가입 후 이용 가능
  - 사이트 로그인 기능
  - 로그인 성공 시 사용자 정보가 담긴 JWT 토큰 반환
  - REST API : /auth/signin
  - 메소드 : POST
- 회원 정보 수정(비밀번호)
  - 로그인 후 사용 가능
  - 로그인한 사용자의 회원 정보 수정
  - DB 저장 시 DB 암호화가 필요하여 비밀번호 수정 API 따로 구현
  - REST API : /auth/updateMemberPassword
  - 메소드 : PUT
- 회원 정보 수정(비밀번호 외)
  - 로그인 후 사용 가능
  - 로그인한 사용자의 회원 정보 수정
  - REST API : /auth/updateMember
  - 메소드 : PUT
- 회원 정보 삭제
  - 로그인 후 사용 가능
  - 로그인한 사용자의 회원 정보 삭제
  - REST API : /auth/deleteMember
  - 메소드 : DELETE
- 모집 글 작성
  - 로그인 후 사용 가능
  - 모집 글 작성 시 사용
  - REST API : /post/addPost
  - 메소드 : POST
- 모집 글 목록 가져오기(로그인 O)
  - 로그인 후 사용 가능
  - 모집 글의 사용 기술 스택이 사용자의 관심 기술 스택과 겹치는 모집 글만 가져옴
  - REST API : /post/retrieveLoginPost
  - 메소드 : GET
- 모집 글 목록 가져오기(로그인 X)
  - 로그인 없이 사용 가능
  - 모집 글 중 상태가 “모집 중”인 모집 글을 전부 가져옴
  - REST API : /post/retrievePost
  - 메소드 : GET
- 모집 글 상세 보기
  - 로그인 없이 사용 가능
  - PathVariable로 받은 하나의 모집 글 정보만 반환
  - REST API : /post/retrieveDetail/{postId}
  - 메소드 : GET
- 참여 요청 추가
  - 로그인 후 사용 가능
  - PathVariable로 받은 postId의 모집 글에 요청 추가
  - REST API : /apply/{postId}
  - 메소드 : POST
- 참여 요청 가져오기(관리자)
  - 로그인 후 사용 가능
  - 사용자가 작성한 모집 글의 참여 요청 목록 가져오기
  - 참여 요청 정보 변경 이전에 사용
  - REST API : /apply/applyListWriter/{postId}
  - 메소드 : GET
- 참여 요청 가져오기(참여 요청자)
  - 로그인 후 사용 가능
  - 현재까지 사용자가 요청한 참여 요청 목록 가져오기
  - 참여 요청 삭제 이전에 사용
  - REST API : /apply/applyListRequest
  - 메소드 : GET
- 참여 요청 정보 변경
  - 로그인 후 사용 가능
  - 모집 글 작성자만 사용 가능
  - 자신이 작성한 모집 글의 참여 요청 정보 변경
  - REST API : /apply/updateApply/{postId}
  - 메소드 : PUT
- 참여 요청 삭제
  - 로그인 후 사용 가능
  - 참여 요청자만 사용 가능
  - 자신이 작성한 참여 요청 삭제
  - REST API : /apply/deleteApply
  - 메소드 : DELETE
- 인증 코드 전송
  - 회원 가입 시 사용
  - 요청 발생 시 서버에 인증 코드 생성 후 3분 간 저장
  - 사용자가 입력한 이메일로 인증 코드를 전송
  - REST API : /mail/send
  - 메소드 : POST
- 인증 코드 확인
  - 회원 가입 시 사용
  - 전송된 인증 코드를 서버에 저장된 인증 코드와 비교
  - 인증 코드 일치 시 인증 성공 메세지 전송
  - REST API : /mail/check
  - 메소드 : POST

### 추후 참고 사항 및 느낀 점

구현 이후 테스트 도중 아래의 문제점을 찾게 되었다.

![문제 발생.png](<WebKit640%20%E1%84%80%E1%85%A2%E1%84%8B%E1%85%B5%E1%86%AB%20%E1%84%91%E1%85%B3%E1%84%85%E1%85%A9%E1%84%8C%E1%85%A6%E1%86%A8%E1%84%90%E1%85%B3(%E1%84%87%E1%85%A2%E1%86%A8%E1%84%8B%E1%85%A6%E1%86%AB%E1%84%83%E1%85%B3)%20f67a7cf05aa941a68a34d8839e156d72/%25EB%25AC%25B8%25EC%25A0%259C_%25EB%25B0%259C%25EC%2583%259D.png>)

![모집 글 출력.png](<WebKit640%20%E1%84%80%E1%85%A2%E1%84%8B%E1%85%B5%E1%86%AB%20%E1%84%91%E1%85%B3%E1%84%85%E1%85%A9%E1%84%8C%E1%85%A6%E1%86%A8%E1%84%90%E1%85%B3(%E1%84%87%E1%85%A2%E1%86%A8%E1%84%8B%E1%85%A6%E1%86%AB%E1%84%83%E1%85%B3)%20f67a7cf05aa941a68a34d8839e156d72/%25EB%25AA%25A8%25EC%25A7%2591_%25EA%25B8%2580_%25EC%25B6%259C%25EB%25A0%25A5.png>)

모집 글을 게시일 기준으로 내림차순으로 정렬하여 가져오도록 하였지만 해당 정보를 프론트엔드로 전송하여 출력한 결과 정렬이 이루어지지 않았다.

확인 결과 DB에 요청한 쿼리도 정상적으로 ORDER BY가 사용된 것을 확인하였지만 DB에서 백엔드로 전송될 때, 해당 쿼리의 정렬이 수행되지 않았음을 알 수 있었다.

Spring boot에서 제공하는 JPA에 의존하여 해당 기능을 구현하였기 때문에 백엔드 환경에서 다시 한번 정렬이 필요하였다.

이 과정에서 여러 의존성을 사용할 경우 개발 편의성이 높아지지만 해당 의존성에 종속되어 프로그램의 유지 보수에 어려움을 겪을 수 있다는 문제점이 발생한다는 것을 알게 되었다.
