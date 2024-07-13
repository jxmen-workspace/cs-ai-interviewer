CREATE TABLE member
(
    id         BIGINT                     NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255)               NOT NULL,
    email      VARCHAR(255)               NOT NULL,
    login_type VARCHAR(255)               NOT NULL,
    created_at TIMESTAMP(6) DEFAULT now() NOT NULL,
    updated_at TIMESTAMP(6) DEFAULT NOW() NOT NULL ON UPDATE NOW(),

    PRIMARY KEY (id),
    UNIQUE KEY unique_email (email)
) character set utf8;;
