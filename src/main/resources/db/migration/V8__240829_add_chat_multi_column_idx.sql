# 미사용 userSessionId 인덱스 및 컬럼 삭제
DROP INDEX idx_subject_userSessionId ON chat;

ALTER TABLE chat
    DROP COLUMN user_session_id;

# 채팅방 조회 성능을 위한 인덱스 추가
CREATE INDEX idx_chat_subject_id_member_id ON chat (subject_id, member_id, created_at);

# 기존 sessionId를 사용하던 데이터 삭제
DELETE
FROM chat
WHERE member_id IS NULL;

# 기존에 userSessionId 때문에 nullable 로 설정되어 있던 member_id 컬럼을 NOT NULL 로 변경
ALTER TABLE chat
    MODIFY COLUMN member_id BIGINT NOT NULL AFTER subject_id;
