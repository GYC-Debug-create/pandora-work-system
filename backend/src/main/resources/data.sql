-- Data Init Script
INSERT INTO "PUBLIC"."ROLES" VALUES
                                 (1, 'ADMIN', 'Administrator with full system access'),
                                 (2, 'FOUNDER', 'Founder with full information access'),
                                 (3, 'DEPARTMENT_HEAD', 'Department Head with team view access'),
                                 (4, 'TEAM_LEAD', 'Team Lead with team view access'),
                                 (5, 'EMPLOYEE', 'Regular employee with basic access');

INSERT INTO "PUBLIC"."DEPARTMENTS" VALUES
                                       (1, U&'\6280\672f\90e8', NULL),
                                       (2, U&'\5e02\573a\90e8', NULL),
                                       (3, U&'\8bbe\8ba1\90e8', NULL),
                                       (4, U&'\884c\653f\90e8', NULL);

INSERT INTO "PUBLIC"."USERS" VALUES
                                 ('admin-uuid-placeholder', 'admin', '$2a$12$9eUrTf/kqVmXp7AlpMjBjuR43SI1nuMRmkKDG7hxBTku1TRKj96pu', NULL, U&'\7ba1\7406\5458', 1, NULL, 'ENFJ', U&'\6d3b\8dc3', TIMESTAMP '2025-11-14 14:10:45.289262'),
                                 ('575bb55af27cb8d5fb6d31b3aec95acd', 'xm', '$2a$10$/uuFNhwZbaZ4MEP4Oq9ukOAG.DVAlCC5yaLp0OJsWzmVXWJ.LIe62', 'user-f2ee3018@example.invalid', U&'\5c0f\660e', 5, 1, 'INTP', U&'\6d3b\8dc3', TIMESTAMP '2025-11-14 14:10:45.291325'),
                                 ('547309c2503d6dd1bec16a200ea14458', 'xz', '$2a$10$/uuFNhwZbaZ4MEP4Oq9ukOAG.DVAlCC5yaLp0OJsWzmVXWJ.LIe62', 'user-236d5cf4@example.invalid', U&'\5c0f\5f20', 4, 1, 'INTJ', U&'\6d3b\8dc3', TIMESTAMP '2025-11-14 14:10:45.292337');

INSERT INTO "PUBLIC"."TASKS" VALUES
                                 (1, U&'\4e3b\4efb\52a1 - \7f51\7ad9\91cd\6784', U&'\91cd\65b0\8bbe\8ba1\516c\53f8\5b98\65b9\7f51\7ad9', 'admin-uuid-placeholder', 'admin-uuid-placeholder', 'TEAM', 'HIGH', 'PENDING_TEAM', DATE '2025-12-01', DATE '2026-01-15',NULL, TIMESTAMP '2025-12-14 14:10:45.292337', TIMESTAMP '2025-12-14 14:10:45.292337'),
                                 (2, U&'\4e3b\4efb\52a1 - \79fb\52a8\7aef\9002\914d', U&'\4f18\5316\79fb\52a8\7aef\7528\6237\4f53\9a8c', 'admin-uuid-placeholder', '547309c2503d6dd1bec16a200ea14458', 'TEAM', 'MEDIUM', 'PENDING_TEAM', DATE '2025-12-05', DATE '2025-12-25',NULL, TIMESTAMP '2025-12-14 14:10:45.293534', TIMESTAMP '2025-12-14 14:10:45.293534'),
                                 (3, U&'\4e3b\4efb\52a1 - Q4 \6280\672f\5206\4eab', U&'\51c6\5907\5e76\7ec4\7ec7\7b2c\56db\5b63\5ea6\6280\672f\5206\4eab\4f1a', '547309c2503d6dd1bec16a200ea14458', '547309c2503d6dd1bec16a200ea14458', 'TEAM', 'MEDIUM', 'IN_PROGRESS', DATE '2025-12-10', DATE '2025-12-30', NULL,TIMESTAMP '2025-12-14 14:10:45.293534', TIMESTAMP '2025-12-14 14:10:45.293534'),
                                 (4, U&'\4e3b\4efb\52a1 - \79fb\52a8\7aef\9002\914d', NULL, '547309c2503d6dd1bec16a200ea14458', '547309c2503d6dd1bec16a200ea14458', 'TEAM', 'MEDIUM', 'IN_PROGRESS', DATE '2025-12-05', DATE '2025-12-25', NULL,TIMESTAMP '2025-12-14 14:23:42.215689', TIMESTAMP '2025-12-14 14:31:45.596461');

INSERT INTO "PUBLIC"."SUB_TASKS" VALUES
                                     (1, 3, NULL, '547309c2503d6dd1bec16a200ea14458', U&'\5b50\4efb\52a1 - \51c6\5907\5206\4eab PPT', 'TODO', '575bb55af27cb8d5fb6d31b3aec95acd', TIMESTAMP '2025-12-11 09:00:00', TIMESTAMP '2025-12-15 18:00:00', NULL, NULL, TIMESTAMP '2025-12-14 14:10:45.294551', NULL, TIMESTAMP '2025-12-14 14:10:45.294551'),
                                     (2, 3, NULL, '547309c2503d6dd1bec16a200ea14458', U&'\5b50\4efb\52a1 - \9884\5b9a\4f1a\8bae\5ba4', 'PENDING_APPROVAL', '547309c2503d6dd1bec16a200ea14458', TIMESTAMP '2025-12-10 14:00:00', TIMESTAMP '2025-12-10 15:00:00', U&'[{"uniqueFileName":"booking_confirm.pdf","originalFileName":"\4f1a\8bae\5ba4\9884\5b9a\786e\8ba4\5355.pdf"}]', NULL, TIMESTAMP '2025-12-14 14:10:45.295554', NULL, TIMESTAMP '2025-12-14 14:10:45.295554'),
                                     (3, 2, NULL, '547309c2503d6dd1bec16a200ea14458', U&'\64b0\5199\8c03\7814\7ade\54c1 App\6587\6863', 'TODO', '575bb55af27cb8d5fb6d31b3aec95acd', TIMESTAMP '2025-12-06 10:00:00', TIMESTAMP '2025-12-08 18:00:00', NULL, NULL, TIMESTAMP '2025-12-14 14:10:45.295554', NULL, TIMESTAMP '2025-12-14 14:10:45.295554'),
                                     (4, NULL, NULL, '575bb55af27cb8d5fb6d31b3aec95acd', U&'\8bc4\5ba1\4e0a\5468\4ee3\7801', 'TODO', '575bb55af27cb8d5fb6d31b3aec95acd', TIMESTAMP '2025-12-01 09:00:00', TIMESTAMP '2025-12-01 12:00:00', NULL, NULL, TIMESTAMP '2025-12-14 14:10:45.295554', NULL, TIMESTAMP '2025-12-14 14:10:45.295554'),
                                     (5, 2, 3, '575bb55af27cb8d5fb6d31b3aec95acd', U&'\5f00\53d1\65e5\5fd7\6a21\5757', 'TODO', '575bb55af27cb8d5fb6d31b3aec95acd', TIMESTAMP '2025-12-07 10:00:00', TIMESTAMP '2025-12-07 12:00:00', NULL, NULL, TIMESTAMP '2025-12-14 14:10:45.296556', NULL, TIMESTAMP '2025-12-14 14:10:45.296556'),
                                     (6, 4, NULL, '547309c2503d6dd1bec16a200ea14458', U&'\5b50\4efb\52a1 - \8c03\7814\7ade\54c1 App', 'TODO', '575bb55af27cb8d5fb6d31b3aec95acd', TIMESTAMP '2025-12-25 10:00:00', TIMESTAMP '2025-12-25 18:00:00', NULL, NULL, TIMESTAMP '2025-12-14 14:45:51.22841', NULL, TIMESTAMP '2025-12-14 14:45:51.22841'),
                                     (7, 4, 6, '575bb55af27cb8d5fb6d31b3aec95acd', U&'\8c03\7814', 'PENDING_APPROVAL', '575bb55af27cb8d5fb6d31b3aec95acd', TIMESTAMP '2025-12-25 10:00:00', TIMESTAMP '2025-12-25 18:00:00', '[{"uniqueFileName":"uuid-xyz.zip","originalFileName":"screenshots.zip"}]', NULL, TIMESTAMP '2025-12-14 14:47:03.730943', NULL, TIMESTAMP '2025-12-14 14:53:40.262112');

INSERT INTO "PUBLIC"."TASK_MEMBERS" VALUES
    (4, '575bb55af27cb8d5fb6d31b3aec95acd', TIMESTAMP '2025-12-14 14:31:45.588262');

INSERT INTO "PUBLIC"."LOGS" VALUES
                                 (2, '547309c2503d6dd1bec16a200ea14458', 4, NULL, 'MEMBERS_ADDED', U&'\5c0f\5f20 \4e3a\4efb\52a1\6dfb\52a0\4e86 1 \540d\6210\5458 \4e3b\4efb\52a1 ''\4e3b\4efb\52a1 - \79fb\52a8\7aef\9002\914d''', '江苏',TIMESTAMP '2025-11-14 14:31:45.608492'),
                                (3, '575bb55af27cb8d5fb6d31b3aec95acd', 4, 7, 'SUBTASK_SUBMITTED', U&'\5c0f\660e \63d0\4ea4\4e86 \5b50\4efb\52a1 ''\8c03\7814''','江苏', TIMESTAMP '2025-12-14 14:53:40.269672');

INSERT INTO "PUBLIC"."COMPANY_MATTERS" VALUES
                                           (1, U&'2025\5e74\5ea6\6218\7565\89c4\5212\53d1\5e03', U&'\516c\53f8\6b63\5f0f\53d1\5e032025\5e74\5ea6\6574\4f53\6218\7565\89c4\5212\ff0c\5c06\91cd\70b9\805a\7126\4e8eAI\6280\672f\5728\4e1a\52a1\573a\666f\4e2d\7684\6df1\5ea6\5e94\7528\4ee5\53ca\6d77\5916\5e02\573a\7684\62d3\5c55\3002\5168\4f53\5458\5de5\9700\8ba4\771f\5b66\4e60\3002', 'admin-uuid-placeholder', 10, TIMESTAMP '2025-11-14 14:10:45.297559'),
                                           (2, U&'Q3 \5b63\5ea6\5168\5458\5927\4f1a\901a\77e5', U&'\5b9a\4e8e11\670815\65e5\4e0b\534814:00\5728\5927\4f1a\8bae\5ba4\4e3e\884cQ3\5b63\5ea6\5168\5458\5927\4f1a\3002\4f1a\8bae\5c06\603b\7ed3\4e0a\4e00\5b63\5ea6\7684\5de5\4f5c\6210\679c\ff0c\5206\6790\5b58\5728\7684\95ee\9898\ff0c\5e76\5bf9\4f18\79c0\5458\5de5\8fdb\884c\8868\5f70\3002', '547309c2503d6dd1bec16a200ea14458', 8, TIMESTAMP '2025-11-14 14:10:45.297559'),
                                           (3, U&'\65b0\529e\516c\5ba4\642c\8fc1\9884\544a', U&'\968f\7740\516c\53f8\89c4\6a21\6269\5927\ff0c\6211\4eec\5c06\4e8e\4e0b\4e2a\6708\5e95\642c\8fc1\81f3\9ad8\65b0\79d1\6280\56ed\533a\7684\65b0\529e\516c\697c\3002\884c\653f\90e8\5c06\5728\8fd1\671f\53d1\5e03\5177\4f53\7684\5de5\4f4d\5b89\6392\548c\642c\8fc1\6307\5357\ff0c\8bf7\5404\90e8\95e8\63d0\524d\505a\597d\7269\8d44\6574\7406\3002', 'admin-uuid-placeholder', 9, TIMESTAMP '2025-11-14 14:10:45.297559'),
                                           (4, U&'\5173\4e8e\52a0\5f3a\4fe1\606f\5b89\5168\7684\7d27\6025\901a\77e5', U&'\8fd1\671f\7f51\7edc\5b89\5168\5f62\52bf\4e25\5cfb\ff0c\4e3a\4fdd\969c\516c\53f8\6570\636e\5b89\5168\ff0c\8bf7\5404\4f4d\540c\4e8b\7acb\5373\68c0\67e5\5e76\66f4\65b0\529e\516c\7cfb\7edf\5bc6\7801\ff0c\5f00\542f\53cc\91cd\8ba4\8bc1\ff0c\4e25\7981\5728\975e\516c\53f8\8bbe\5907\4e0a\5904\7406\654f\611f\6570\636e\3002', 'admin-uuid-placeholder', 10, TIMESTAMP '2025-11-14 14:10:45.297559'),
                                           (5, U&'\5e74\5ea6\5458\5de5\4f53\68c0\5b89\6392', U&'2025\5e74\5ea6\5458\5de5\514d\8d39\5065\5eb7\4f53\68c0\5c06\4e8e\672c\670820\65e5\5f00\59cb\3002\8bf7\5927\5bb6\767b\5f55\5185\7f51HR\7cfb\7edf\9884\7ea6\4f53\68c0\65f6\95f4\ff0c\5173\6ce8\8eab\4f53\5065\5eb7\3002', 'admin-uuid-placeholder', 6, TIMESTAMP '2025-11-14 14:10:45.297559'),
                                           (6, U&'\65b0\4e00\4ee3\4ea7\54c1\7814\53d1\542f\52a8\4f1a', U&'\4ee3\53f7\4e3a\201c\6f58\591a\62c9\201d\7684\65b0\4e00\4ee3\667a\80fd\534f\4f5c\5e73\53f0\7814\53d1\9879\76ee\6b63\5f0f\542f\52a8\3002\9879\76ee\7ec4\6b63\9762\5411\5168\516c\53f8\62db\52df\6280\672f\9aa8\5e72\548c\4ea7\54c1\4e13\5bb6\ff0c\6b22\8fce\8e0a\8dc3\62a5\540d\3002', '547309c2503d6dd1bec16a200ea14458', 9, TIMESTAMP '2025-11-14 14:10:45.297559');



-- --- 关键修复：重置所有表的自增 ID 序列 (H2 2.x 语法) ---
-- (在所有 INSERT 完成后执行)

ALTER TABLE roles ALTER COLUMN role_id RESTART WITH (SELECT COALESCE(MAX(role_id) + 1, 1) FROM roles);
ALTER TABLE departments ALTER COLUMN department_id RESTART WITH (SELECT COALESCE(MAX(department_id) + 1, 1) FROM departments);
--ALTER TABLE tasks ALTER COLUMN task_id RESTART WITH (SELECT COALESCE(MAX(task_id) + 1, 1) FROM tasks);
--ALTER TABLE sub_tasks ALTER COLUMN sub_task_id RESTART WITH (SELECT COALESCE(MAX(sub_task_id) + 1, 1) FROM sub_tasks);
ALTER TABLE logs ALTER COLUMN log_id RESTART WITH (SELECT COALESCE(MAX(log_id) + 1, 1) FROM logs);
ALTER TABLE company_matters ALTER COLUMN matter_id RESTART WITH (SELECT COALESCE(MAX(matter_id) + 1, 1) FROM company_matters);
--ALTER TABLE deliverables ALTER COLUMN deliverable_id RESTART WITH (SELECT COALESCE(MAX(deliverable_id) + 1, 1) FROM deliverables);
-- 注意：最后一条 DELIVERABLES 如果是空的NULL，H2有时候会导出一行全NULL作为占位，
-- 如果你DELIVERABLES表里本来就没数据，上面的INSERT DELIVERABLES可以删掉，
-- 你的脚本里显示 "0 +/- SELECT COUNT" 所以理论上不该有 Insert。
-- 我上面写了一个占位符，如果你运行报错，直接把 INSERT INTO DELIVERABLES 这一整段删掉即可。

-- 重置 company_matters 表的自增序列（修复创建新事项时的主键冲突问题）
ALTER TABLE company_matters ALTER COLUMN matter_id RESTART WITH 7;