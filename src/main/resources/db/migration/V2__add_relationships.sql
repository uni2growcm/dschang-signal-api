ALTER TABLE reports ADD COLUMN created_by BIGINT;
ALTER TABLE medias ADD COLUMN created_by BIGINT;
ALTER TABLE medias ADD COLUMN report_id BIGINT;
ALTER TABLE categories ADD COLUMN created_by BIGINT;

ALTER TABLE reports ADD CONSTRAINT fk_reports_created_by FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE medias ADD CONSTRAINT fk_medias_created_by FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE medias ADD CONSTRAINT fk_medias_report_id FOREIGN KEY (report_id) REFERENCES reports(id);
ALTER TABLE categories ADD CONSTRAINT fk_categories_created_by FOREIGN KEY (created_by) REFERENCES users(id);

CREATE TABLE report_category (
    report_id BIGINT NOT NULL,
    category_id INTEGER NOT NULL,
    PRIMARY KEY (report_id, category_id),
    FOREIGN KEY (report_id) REFERENCES reports(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);