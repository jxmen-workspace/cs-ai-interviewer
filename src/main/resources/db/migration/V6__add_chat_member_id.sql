# NOTE: memberId 저장 시 sessionId가 빈 값이 되므로 sessionId를 nullable로 설정한다.

# 1. userSessionId set to nullable
alter table chat modify column user_session_id varchar(255) comment '유저 세션 식별자' null;

# 2. add memberId to chat
# NOTE: sessionId 컬럼을 삭제한 후에는 nullable 해제 예정
alter table chat add member_id bigint null comment '멤버 식별자' after subject_id;
