-- Indexing for Users table
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_email ON users(email);

-- Indexing for Bid table
CREATE INDEX idx_bid_user_id ON bid(user_id);
CREATE INDEX idx_bid_task_id ON bid(task_id);
CREATE INDEX idx_bid_status ON bid(status);

-- Indexing for Department table
CREATE INDEX idx_department_user_id ON department(user_id);

-- Indexing for Member table
CREATE INDEX idx_member_department_id ON member(department_id);

-- Indexing for Task table
CREATE INDEX idx_task_department_id ON task(department_id);
CREATE INDEX idx_task_type ON task(task_type);

-- Indexing for Task Ownership table
CREATE INDEX idx_task_ownership_owner_user_id ON task_ownership(owner_user_id);
CREATE INDEX idx_task_ownership_task_id ON task_ownership(task_id);
CREATE INDEX idx_task_ownership_transferred_to ON task_ownership(transferred_to_user_id);

-- Indexing for Task Submission table
CREATE INDEX idx_task_submission_member_id ON task_submission(member_id);
CREATE INDEX idx_task_submission_task_id ON task_submission(task_id);
CREATE INDEX idx_task_submission_status ON task_submission(status);

-- Indexing for Audit Log table
CREATE INDEX idx_audit_log_actor_user_id ON audit_log(actor_user_id);
CREATE INDEX idx_audit_log_occurred_at ON audit_log(occurred_at);
CREATE INDEX idx_audit_log_target_id ON audit_log(target_id);
CREATE INDEX idx_audit_log_action_type ON audit_log(action_type);
