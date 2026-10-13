-- Demo organisation. Accounts are created locked ('!'); at start-up the API sets the demo
-- password on them from DEMO_USER_PASSWORD (see DemoAccountsInitializer).

INSERT INTO departments (code, name) VALUES
    ('ENG', 'Engineering'),
    ('FIN', 'Finance');

INSERT INTO users (username, display_name, password_hash, role, department_code) VALUES
    ('alice',       'Alice Morgan', '!', 'EMPLOYEE', 'ENG'),
    ('priya.raman', 'Priya Raman',  '!', 'EMPLOYEE', 'FIN'),
    ('mark',        'Mark Ellis',   '!', 'MANAGER',  'ENG'),
    ('nina',        'Nina Varga',   '!', 'MANAGER',  'FIN'),
    ('fiona',       'Fiona Clarke', '!', 'FINANCE',  'FIN');

INSERT INTO cost_centres (code, name, department_code) VALUES
    ('ENG',        'Engineering',          'ENG'),
    ('ENG-CLOUD',  'Engineering - Cloud',  'ENG'),
    ('FIN',        'Finance',              'FIN'),
    ('FIN-TRAVEL', 'Finance - Travel',     'FIN');

INSERT INTO claims (owner_id, owner_username, title, amount, expense_date, notes, status, approver_id, created_at)
SELECT u.id, u.username, c.title, c.amount, c.expense_date, c.notes, c.status, m.id, c.created_at
FROM (VALUES
    ('alice',       'Taxi to customer site',      42.50, DATE '2026-09-02', 'Return trip, Bosch site visit',     'APPROVED',  'mark', TIMESTAMPTZ '2026-09-02 17:10:00+00'),
    ('alice',       'Cloud certification exam',  150.00, DATE '2026-09-15', 'AWS Solutions Architect Associate', 'SUBMITTED', 'mark', TIMESTAMPTZ '2026-09-15 09:30:00+00'),
    ('priya.raman', 'Team lunch - month close',  186.40, DATE '2026-09-30', 'Six people, quarter close',         'APPROVED',  'nina', TIMESTAMPTZ '2026-09-30 13:45:00+00'),
    ('priya.raman', 'Printer toner',              64.99, DATE '2026-10-01', NULL,                                'REJECTED',  'nina', TIMESTAMPTZ '2026-10-01 08:20:00+00')
) AS c (owner, title, amount, expense_date, notes, status, manager, created_at)
JOIN users u ON u.username = c.owner
JOIN users m ON m.username = c.manager;

INSERT INTO claim_allocations (claim_id, cost_centre_code, amount)
SELECT c.id, CASE WHEN c.owner_username = 'alice' THEN 'ENG' ELSE 'FIN' END, c.amount
FROM claims c;

INSERT INTO claim_events (claim_id, action, actor_username, comment, occurred_at)
SELECT c.id, 'SUBMITTED', c.owner_username, NULL, c.created_at
FROM claims c;

INSERT INTO claim_events (claim_id, action, actor_username, comment, occurred_at)
SELECT c.id, c.status, m.username,
       CASE WHEN c.status = 'REJECTED' THEN 'Order supplies through procurement' ELSE NULL END,
       c.created_at + INTERVAL '1 day'
FROM claims c
JOIN users m ON m.id = c.approver_id
WHERE c.status IN ('APPROVED', 'REJECTED');
