CREATE TABLE chat_archive
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_id BIGINT       NOT NULL,
    member_id  BIGINT       NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX chat_archive_member_id_subject_id_idx (member_id, subject_id)
) ENGINE = InnoDB,
  CHARACTER SET utf8;

CREATE TABLE chat_archive_content
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_archive_id BIGINT                     NOT NULL COMMENT '채팅 아카이브 아이디',
    message          TEXT                       NOT NULL COMMENT '채팅', -- text: 65,535 bytes
    score            INTEGER COMMENT '점수', # NOTE: default 0 설정하지 않음.
    chat_type        VARCHAR(255)               NOT NULL COMMENT '채팅 유형',
    created_at       TIMESTAMP(6) DEFAULT now() NOT NULL COMMENT '생성일',
    updated_at       TIMESTAMP(6) DEFAULT now() NOT NULL ON UPDATE now() COMMENT '수정일',

    INDEX chat_archive_content_chat_archive_id_idx (chat_archive_id)
) ENGINE = InnoDB,
  CHARACTER SET utf8;

# default 0 제거
ALTER TABLE chat MODIFY COLUMN score INTEGER COMMENT '점수';
