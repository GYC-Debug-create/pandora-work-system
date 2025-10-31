-- Insert initial roles for the system
INSERT INTO roles (role_name, description) VALUES
                                               ('ADMIN', 'Administrator with full system access'),
                                               ('FOUNDER', 'Founder with full information access'),
                                               ('DEPARTMENT_HEAD', 'Department Head with team view access'), -- Assuming '部门老总' maps to this
                                               ('TEAM_LEAD', 'Team Lead with team view access'),          -- Assuming '团队长' maps to this
                                               ('EMPLOYEE', 'Regular employee with basic access');

-- Insert initial departments for selection
INSERT INTO departments (department_name) VALUES ('技术部'), ('市场部'), ('设计部'), ('行政部');


-- Insert a default administrator user (password: adminpassword)
INSERT INTO users (user_id, username, password_hash, email, full_name, role_id, department_id, mbti_type) VALUES
    ('admin-uuid-placeholder', 'admin', '$2a$12$9eUrTf/kqVmXp7AlpMjBjuR43SI1nuMRmkKDG7hxBTku1TRKj96pu', null, '管理员', (SELECT role_id FROM roles WHERE role_name = 'ADMIN'), null, null);

-- Insert an employee user 'xm' (password: 1)
INSERT INTO users (user_id, username, password_hash, email, full_name, role_id, department_id, mbti_type) VALUES
    ('575bb55af27cb8d5fb6d31b3aec95acd', 'xm', '$2a$10$/uuFNhwZbaZ4MEP4Oq9ukOAG.DVAlCC5yaLp0OJsWzmVXWJ.LIe62', 'user-f2ee3018@example.invalid', '小明', (SELECT role_id FROM roles WHERE role_name = 'EMPLOYEE'), (SELECT department_id FROM departments WHERE department_name = '技术部'), 'INTP');

-- Insert a team lead user 'xz' (password: 1)
INSERT INTO users (user_id, username, password_hash, email, full_name, role_id, department_id, mbti_type) VALUES
    ('547309c2503d6dd1bec16a200ea14458', 'xz', '$2a$10$/uuFNhwZbaZ4MEP4Oq9ukOAG.DVAlCC5yaLp0OJsWzmVXWJ.LIe62', 'user-236d5cf4@example.invalid', '小张', (SELECT role_id FROM roles WHERE role_name = 'TEAM_LEAD'), (SELECT department_id FROM departments WHERE department_name = '技术部'), 'INTJ');

-- --- 新增测试数据 ---

-- 主任务 1: 由 admin 创建并负责，状态 PENDING_TEAM (用于测试添加成员)
INSERT INTO tasks (task_name, description, creator_id, responsible_person_id, task_type, priority, status, start_date, end_date)
VALUES ('主任务 - 网站重构', '重新设计公司官方网站', 'admin-uuid-placeholder', 'admin-uuid-placeholder', 'TEAM', 'HIGH', 'PENDING_TEAM', '2025-11-01', '2025-12-15');

-- 主任务 2: 由 admin 创建，由 xz (Team Lead) 负责，状态 PENDING_TEAM (用于测试团队长添加成员和分解)
INSERT INTO tasks (task_name, description, creator_id, responsible_person_id, task_type, priority, status, start_date, end_date)
VALUES ('主任务 - 移动端适配', '优化移动端用户体验', 'admin-uuid-placeholder', '547309c2503d6dd1bec16a200ea14458', 'TEAM', 'MEDIUM', 'PENDING_TEAM', '2025-11-05', '2025-11-25');

-- 主任务 3: 由 xz (Team Lead) 创建并负责，状态 IN_PROGRESS (用于测试子任务创建和完成)
INSERT INTO tasks (task_name, description, creator_id, responsible_person_id, task_type, priority, status, start_date, end_date)
VALUES ('主任务 - Q4 技术分享', '准备并组织第四季度技术分享会', '547309c2503d6dd1bec16a200ea14458', '547309c2503d6dd1bec16a200ea14458', 'TEAM', 'MEDIUM', 'IN_PROGRESS', '2025-11-10', '2025-11-30');

-- 子任务 1.1: 属于主任务 3，由 xz 创建，分配给 xm，状态 TODO (用于测试提交交付物)
INSERT INTO sub_tasks (parent_task_id, creator_id, sub_task_name, status, assigned_to_id, start_time, end_time)
VALUES (3, '547309c2503d6dd1bec16a200ea14458', '子任务 - 准备分享 PPT', 'TODO', '575bb55af27cb8d5fb6d31b3aec95acd', '2025-11-11T09:00:00', '2025-11-15T18:00:00');

-- 子任务 1.2: 属于主任务 3，由 xz 创建，分配给 xz 自己，状态 PENDING_APPROVAL (用于测试审批)
INSERT INTO sub_tasks (parent_task_id, creator_id, sub_task_name, status, assigned_to_id, start_time, end_time, deliverable_content)
VALUES (3, '547309c2503d6dd1bec16a200ea14458', '子任务 - 预定会议室', 'PENDING_APPROVAL', '547309c2503d6dd1bec16a200ea14458', '2025-11-10T14:00:00', '2025-11-10T15:00:00', '[{"uniqueFileName":"booking_confirm.pdf","originalFileName":"会议室预定确认单.pdf"}]');

-- 子任务 2.1: 属于主任务 2 (移动端适配)，由 xz 创建，分配给 xm，状态 TODO (用于测试二次分解)
INSERT INTO sub_tasks (parent_task_id, creator_id, sub_task_name, status, assigned_to_id, start_time, end_time)
VALUES (2, '547309c2503d6dd1bec16a200ea14458', '子任务 - 调研竞品 App', 'TODO', '575bb55af27cb8d5fb6d31b3aec95acd', '2025-11-06T10:00:00', '2025-11-08T18:00:00');


-- 个人任务 1: 由 xm 创建并分配给自己 (无父任务)
INSERT INTO sub_tasks (creator_id, sub_task_name, status, assigned_to_id, start_time, end_time)
VALUES ('575bb55af27cb8d5fb6d31b3aec95acd', '个人任务 - 学习 Vuex', 'TODO', '575bb55af27cb8d5fb6d31b3aec95acd', '2025-11-01T09:00:00', '2025-11-01T12:00:00');

-- 在 H2 Console 执行 (如果重启后数据被清空了，需要重新插入):
INSERT INTO sub_tasks (parent_sub_task_id, parent_task_id, creator_id, sub_task_name, status, assigned_to_id, start_time, end_time)
VALUES (3, 2, '575bb55af27cb8d5fb6d31b3aec95acd', '孙任务 - 截图整理', 'TODO', '575bb55af27cb8d5fb6d31b3aec95acd', '2025-11-07T10:00:00', '2025-11-07T12:00:00');
-- 在 H2 Console 执行:
INSERT INTO sub_tasks (parent_sub_task_id, parent_task_id, creator_id, sub_task_name, status, assigned_to_id, start_time, end_time)
VALUES (5, 2, '575bb55af27cb8d5fb6d31b3aec95acd', '曾孙任务 - 标记重点', 'TODO', '575bb55af27cb8d5fb6d31b3aec95acd', '2025-11-07T10:30:00', '2025-11-07T11:00:00');