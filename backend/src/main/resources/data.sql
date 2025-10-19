-- Insert initial roles for the system
-- Insert a default administrator user
-- The raw password is 'adminpassword'
INSERT INTO users (user_id, username, password_hash, role_id) VALUES
    ('admin-uuid-placeholder', 'admin', '$2a$12$9eUrTf/kqVmXp7AlpMjBjuR43SI1nuMRmkKDG7hxBTku1TRKj96pu', (SELECT role_id FROM roles WHERE role_name = 'ADMIN'));
-- Insert initial departments for selection
INSERT INTO departments (department_name) VALUES ('技术部'), ('市场部'), ('设计部'), ('行政部');
INSERT INTO roles (role_name, description) VALUES
                                               ('ADMIN', 'Administrator with full system access'),
                                               ('FOUNDER', 'Founder with full information access'),
                                               ('DEPARTMENT_HEAD', 'Department Head with team view access'), -- Assuming '部门老总' maps to this
                                               ('TEAM_LEAD', 'Team Lead with team view access'),          -- Assuming '团队长' maps to this
                                               ('EMPLOYEE', 'Regular employee with basic access');