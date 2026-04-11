SET @seed_tag = 'SEED_BATCH_20260411';
SET @pwd_hash = '$2a$10$7EqJtq98hPqEX7fNZaFWoOaW8fW8QWGWpZJYG1YB7sCv0xOsyjz6.';
SET @resume_url = 'https://res.cloudinary.com/dhi0u31ia/raw/upload/v1775710835/resumes/byt5xsxfc3s7xmwhbpcq.pdf';
SET @resume_public_id = 'resumes/byt5xsxfc3s7xmwhbpcq.pdf';
SET @offer_letter_url = 'https://res.cloudinary.com/dhi0u31ia/raw/upload/v1775710868/offer-letters/s0hukjbxj1kzbig7bqxf.pdf';
SET @offer_letter_public_id = 'offer-letters/s0hukjbxj1kzbig7bqxf.pdf';

USE job_portal_db;

DROP TEMPORARY TABLE IF EXISTS seed_users;
CREATE TEMPORARY TABLE seed_users (
    seq INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role ENUM('RECRUITER', 'JOB_SEEKER') NOT NULL
);

INSERT INTO seed_users (seq, name, email, phone, role) VALUES
    (1,  'Lordgabbar Devansh', 'lordgabbardevansh@gmail.com', '9000000001', 'RECRUITER'),
    (2,  'Aarav Singh',   'aarav.singh@gmail.com',   '9000000002', 'RECRUITER'),
    (3,  'Ishita Sharma', 'ishita.sharma@gmail.com', '9000000003', 'RECRUITER'),
    (4,  'Karan Verma',   'karan.verma@gmail.com',   '9000000004', 'RECRUITER'),
    (5,  'Neha Kapoor',   'neha.kapoor@gmail.com',   '9000000005', 'RECRUITER'),
    (6,  'Vikram Rao',    'vikram.rao@gmail.com',    '9000000006', 'RECRUITER'),
    (7,  'Priya Nair',    'priya.nair@gmail.com',    '9000000007', 'RECRUITER'),
    (8,  'Devansh Kumar Pal', 'devanshkumarpal@gmail.com', '9000000008', 'JOB_SEEKER'),
    (9,  'Rahul Jain',    'rahul.jain@gmail.com',    '9000000009', 'JOB_SEEKER'),
    (10, 'Simran Kaur',   'simran.kaur@gmail.com',   '9000000010', 'JOB_SEEKER'),
    (11, 'Arjun Patel',   'arjun.patel@gmail.com',   '9000000011', 'JOB_SEEKER'),
    (12, 'Meera Iyer',    'meera.iyer@gmail.com',    '9000000012', 'JOB_SEEKER'),
    (13, 'Dev Malhotra',  'dev.malhotra@gmail.com',  '9000000013', 'JOB_SEEKER'),
    (14, 'Pooja Das',     'pooja.das@gmail.com',     '9000000014', 'JOB_SEEKER'),
    (15, 'Nikhil Bansal', 'nikhil.bansal@gmail.com', '9000000015', 'JOB_SEEKER'),
    (16, 'Ritika Sinha',  'ritika.sinha@gmail.com',  '9000000016', 'JOB_SEEKER'),
    (17, 'Sahil Arora',   'sahil.arora@gmail.com',   '9000000017', 'JOB_SEEKER'),
    (18, 'Kavya Menon',   'kavya.menon@gmail.com',   '9000000018', 'JOB_SEEKER'),
    (19, 'Yash Khanna',   'yash.khanna@gmail.com',   '9000000019', 'JOB_SEEKER'),
    (20, 'Tanya Roy',     'tanya.roy@gmail.com',     '9000000020', 'JOB_SEEKER');

INSERT INTO job_portal_db.users (name, email, password, phone, role, created_at)
SELECT su.name, su.email, @pwd_hash, su.phone, su.role, NOW()
FROM seed_users su
LEFT JOIN job_portal_db.users u ON u.email = su.email
WHERE u.id IS NULL;

DROP TEMPORARY TABLE IF EXISTS recruiter_ids;
CREATE TEMPORARY TABLE recruiter_ids AS
SELECT
    su.seq,
    u.id AS recruiter_id,
    su.name,
    su.email
FROM seed_users su
JOIN job_portal_db.users u ON u.email = su.email
WHERE su.role = 'RECRUITER'
ORDER BY su.seq;

SET @target_recruiter_id = (
    SELECT recruiter_id
    FROM recruiter_ids
    WHERE email = 'lordgabbardevansh@gmail.com'
    LIMIT 1
);

DROP TEMPORARY TABLE IF EXISTS seeker_ids;
CREATE TEMPORARY TABLE seeker_ids AS
SELECT
    su.seq,
    u.id AS user_id,
    su.name,
    su.email
FROM seed_users su
JOIN job_portal_db.users u ON u.email = su.email
WHERE su.role = 'JOB_SEEKER'
ORDER BY su.seq;

DROP TEMPORARY TABLE IF EXISTS seed_job_templates;
CREATE TEMPORARY TABLE seed_job_templates (
    seq INT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    salary DOUBLE NOT NULL,
    experience INT NOT NULL,
    description VARCHAR(2000) NOT NULL,
    internship_duration_months INT NULL,
    job_type ENUM('FULL_TIME', 'PART_TIME', 'INTERNSHIP') NOT NULL,
    status ENUM('ACTIVE', 'DRAFT', 'CLOSED') NOT NULL
);

INSERT INTO seed_job_templates (seq, title, company_name, location, salary, experience, description, internship_duration_months, job_type, status) VALUES
    (1,  'Senior Java Backend Engineer', 'TechNova Solutions', 'Bangalore', 1800000, 4, 'Design and maintain scalable Java microservices using Spring Boot, improve API latency, and drive code quality through unit/integration testing in a product engineering team.', NULL, 'FULL_TIME', 'ACTIVE'),
    (2,  'Frontend Engineer (Angular)', 'CloudAxis Digital', 'Hyderabad', 1400000, 3, 'Build responsive Angular modules, integrate REST APIs, optimize Core Web Vitals, and collaborate with design and QA to ship polished user-facing features.', NULL, 'FULL_TIME', 'ACTIVE'),
    (3,  'Data Analyst', 'DataBridge Analytics', 'Pune', 1100000, 2, 'Create dashboards in Power BI/Tableau, write SQL for product and business insights, and present weekly KPI trends to stakeholders with clear recommendations.', NULL, 'FULL_TIME', 'ACTIVE'),
    (4,  'DevOps Engineer', 'NexaWorks Systems', 'Mumbai', 1500000, 3, 'Own CI/CD pipelines, automate infrastructure with IaC, monitor production reliability, and support secure deployments across staging and production environments.', NULL, 'FULL_TIME', 'ACTIVE'),
    (5,  'QA Automation Engineer', 'TalentForge Labs', 'Chennai', 1200000, 3, 'Develop automation suites using Selenium/Cypress, integrate tests into pipelines, and improve release confidence by reducing manual regression effort.', NULL, 'FULL_TIME', 'ACTIVE'),
    (6,  'Product Manager', 'TechNova Solutions', 'Delhi', 2000000, 5, 'Define product roadmap, gather customer requirements, prioritize backlog, and align engineering, design, and business teams to deliver measurable outcomes.', NULL, 'FULL_TIME', 'ACTIVE'),
    (7,  'Technical Support Specialist', 'CloudAxis Digital', 'Bangalore', 700000, 1, 'Handle L2 customer issues, troubleshoot application defects, escalate incidents with clear diagnostics, and maintain SLA-compliant response timelines.', NULL, 'FULL_TIME', 'DRAFT'),
    (8,  'Full Stack Developer', 'DataBridge Analytics', 'Hyderabad', 1600000, 4, 'Develop end-to-end features with Java and Angular, design database schema changes, and participate in architecture discussions for scalable platform modules.', NULL, 'FULL_TIME', 'ACTIVE'),
    (9,  'HR Operations Executive', 'NexaWorks Systems', 'Pune', 650000, 2, 'Manage onboarding/offboarding, maintain HRIS records, coordinate payroll inputs, and ensure policy compliance across hiring and employee lifecycle workflows.', NULL, 'FULL_TIME', 'ACTIVE'),
    (10, 'Software Engineering Intern', 'TalentForge Labs', 'Mumbai', 35000, 0, 'Work with mentors on API and UI enhancements, write clean code, participate in code reviews, and deliver intern sprint tasks with documentation and demo notes.', 6, 'INTERNSHIP', 'ACTIVE'),
    (11, 'Business Development Associate', 'TechNova Solutions', 'Chennai', 800000, 2, 'Generate qualified leads, run product demos, maintain CRM hygiene, and support enterprise deal closure by coordinating with pre-sales and product teams.', NULL, 'FULL_TIME', 'ACTIVE'),
    (12, 'UX Designer', 'CloudAxis Digital', 'Delhi', 1050000, 3, 'Create user journeys, wireframes, and high-fidelity designs, run usability testing, and partner with frontend teams for pixel-accurate implementation.', NULL, 'FULL_TIME', 'ACTIVE'),
    (13, 'Android Developer', 'DataBridge Analytics', 'Bangalore', 1450000, 3, 'Build and maintain Android applications in Kotlin, optimize app startup and crash metrics, and integrate secure APIs and analytics instrumentation.', NULL, 'FULL_TIME', 'ACTIVE'),
    (14, 'Content Marketing Specialist', 'NexaWorks Systems', 'Hyderabad', 720000, 2, 'Plan and execute content calendars, produce SEO-focused blogs and campaigns, and measure conversion impact across digital channels.', NULL, 'FULL_TIME', 'CLOSED'),
    (15, 'Site Reliability Engineer', 'TalentForge Labs', 'Pune', 1900000, 5, 'Improve service reliability through observability, incident response, and capacity planning while driving automation for alerting and remediation workflows.', NULL, 'FULL_TIME', 'ACTIVE'),
    (16, 'Finance Analyst', 'TechNova Solutions', 'Mumbai', 980000, 2, 'Support budgeting and forecasting, build monthly variance reports, and provide finance insights to leadership on revenue, cost, and margin trends.', NULL, 'FULL_TIME', 'ACTIVE'),
    (17, 'Recruitment Specialist', 'CloudAxis Digital', 'Chennai', 780000, 2, 'Handle full-cycle hiring, coordinate interview panels, manage offer rollouts, and maintain strong candidate communication throughout the hiring funnel.', NULL, 'FULL_TIME', 'ACTIVE'),
    (18, 'Cybersecurity Analyst', 'DataBridge Analytics', 'Delhi', 1650000, 4, 'Monitor security events, run vulnerability assessments, support incident triage, and enforce security controls aligned to organizational standards.', NULL, 'FULL_TIME', 'ACTIVE'),
    (19, 'Database Administrator', 'NexaWorks Systems', 'Bangalore', 1550000, 4, 'Administer MySQL/PostgreSQL clusters, tune query performance, handle backup and disaster recovery drills, and ensure database availability and integrity.', NULL, 'FULL_TIME', 'ACTIVE'),
    (20, 'Data Science Intern', 'TalentForge Labs', 'Hyderabad', 40000, 0, 'Assist in model experimentation, clean and prepare datasets, build evaluation scripts, and present findings to data science leads in weekly reviews.', 6, 'INTERNSHIP', 'ACTIVE'),
    (21, 'Network Engineer', 'TechNova Solutions', 'Pune', 1150000, 3, 'Maintain enterprise network infrastructure, troubleshoot routing/switching issues, and support secure VPN and firewall operations across offices.', NULL, 'FULL_TIME', 'ACTIVE'),
    (22, 'Sales Operations Analyst', 'CloudAxis Digital', 'Mumbai', 900000, 2, 'Maintain sales dashboards, improve pipeline reporting accuracy, and drive process improvements in forecasting, territory planning, and quota tracking.', NULL, 'FULL_TIME', 'ACTIVE'),
    (23, 'Machine Learning Engineer', 'DataBridge Analytics', 'Chennai', 2200000, 5, 'Build ML pipelines for training and deployment, monitor model drift, and collaborate with product teams to ship production-grade AI features.', NULL, 'FULL_TIME', 'ACTIVE'),
    (24, 'Technical Writer', 'NexaWorks Systems', 'Delhi', 850000, 2, 'Develop API docs, release notes, and user guides with engineering teams, ensuring technical accuracy and clear structure for developers and end users.', NULL, 'FULL_TIME', 'DRAFT'),
    (25, 'React Developer', 'TalentForge Labs', 'Bangalore', 1350000, 3, 'Implement reusable React components, manage state effectively, integrate backend services, and maintain UI quality through tests and code reviews.', NULL, 'FULL_TIME', 'ACTIVE'),
    (26, 'Customer Success Manager', 'TechNova Solutions', 'Hyderabad', 1250000, 4, 'Own post-sales customer engagement, drive product adoption, conduct QBRs, and reduce churn through proactive account health management.', NULL, 'FULL_TIME', 'ACTIVE'),
    (27, 'Cloud Architect', 'CloudAxis Digital', 'Pune', 2800000, 8, 'Design cloud-native architecture, lead migration programs, set platform standards, and guide engineering teams on cost, reliability, and security best practices.', NULL, 'FULL_TIME', 'ACTIVE'),
    (28, 'BI Developer', 'DataBridge Analytics', 'Mumbai', 1180000, 3, 'Build reliable ETL pipelines and semantic models, optimize BI performance, and deliver executive-ready reporting for business decision support.', NULL, 'FULL_TIME', 'ACTIVE'),
    (29, 'Operations Manager', 'NexaWorks Systems', 'Chennai', 1700000, 6, 'Oversee daily operations, improve team productivity with process controls, and own SLA/compliance metrics across delivery and support functions.', NULL, 'FULL_TIME', 'ACTIVE'),
    (30, 'UI/UX Design Intern', 'TalentForge Labs', 'Delhi', 30000, 0, 'Support design system updates, create wireframes and prototypes, and assist in usability testing for web and mobile experiences.', 4, 'INTERNSHIP', 'ACTIVE');

DROP TEMPORARY TABLE IF EXISTS old_seed_jobs;
CREATE TEMPORARY TABLE old_seed_jobs AS
SELECT id AS job_id
FROM jobdb.job
WHERE description LIKE CONCAT('%', @seed_tag, '%');

DELETE a
FROM applicationdb.applications a
JOIN old_seed_jobs osj ON osj.job_id = a.job_id;

DELETE j
FROM jobdb.job j
JOIN old_seed_jobs osj ON osj.job_id = j.id;

INSERT INTO jobdb.job (
    title,
    company_name,
    location,
    salary,
    experience,
    description,
    recruiter_id,
    created_at,
    internship_duration_months,
    job_type,
    status
)
SELECT
    t.title,
    t.company_name,
    t.location,
    t.salary,
    t.experience,
    CONCAT(t.description, ' | Internal ref: ', @seed_tag),
    CASE
        WHEN t.seq <= 12 THEN @target_recruiter_id
        ELSE ri_default.recruiter_id
    END,
    NOW() - INTERVAL t.seq DAY,
    t.internship_duration_months,
    t.job_type,
    t.status
FROM seed_job_templates t
JOIN recruiter_ids ri_default ON ri_default.seq = ((t.seq - 1) % 7) + 1;

DROP TEMPORARY TABLE IF EXISTS seed_jobs;
CREATE TEMPORARY TABLE seed_jobs AS
SELECT
    j.id AS job_id,
    j.title,
    j.company_name,
    ROW_NUMBER() OVER (ORDER BY j.id) AS job_seq
FROM jobdb.job j
WHERE j.description LIKE CONCAT('%', @seed_tag, '%');

DELETE a
FROM applicationdb.applications a
JOIN seeker_ids s ON s.user_id = a.user_id
JOIN seed_jobs j ON j.job_id = a.job_id;

INSERT INTO applicationdb.applications (
    applied_at,
    job_id,
    resume_url,
    resume_public_id,
    status,
    user_id,
    applicant_email,
    company,
    job_title,
    applicant_name,
    interview_link,
    interview_date,
    interview_time,
    interview_time_zone,
    offer_letter_url,
    offer_letter_public_id
)
WITH ordered_pairs AS (
    SELECT
        si.user_id,
        si.name AS applicant_name,
        si.email AS applicant_email,
        sj.job_id,
        sj.title AS job_title,
        sj.company_name AS company,
        ROW_NUMBER() OVER (ORDER BY si.seq, sj.job_seq) AS rn
    FROM seeker_ids si
    CROSS JOIN seed_jobs sj
)
SELECT
    NOW() - INTERVAL ordered_pairs.rn HOUR,
    ordered_pairs.job_id,
    @resume_url,
    @resume_public_id,
    ELT((ordered_pairs.rn % 6) + 1, 'APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'OFFERED', 'REJECTED'),
    ordered_pairs.user_id,
    ordered_pairs.applicant_email,
    ordered_pairs.company,
    ordered_pairs.job_title,
    ordered_pairs.applicant_name,
    CASE
        WHEN ELT((ordered_pairs.rn % 6) + 1, 'APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'OFFERED', 'REJECTED') = 'INTERVIEW_SCHEDULED'
            THEN 'www.google.com'
        ELSE NULL
    END,
    CASE
        WHEN ELT((ordered_pairs.rn % 6) + 1, 'APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'OFFERED', 'REJECTED') = 'INTERVIEW_SCHEDULED'
            THEN CURDATE()
        ELSE NULL
    END,
    CASE
        WHEN ELT((ordered_pairs.rn % 6) + 1, 'APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'OFFERED', 'REJECTED') = 'INTERVIEW_SCHEDULED'
            THEN '10:00:00'
        ELSE NULL
    END,
    CASE
        WHEN ELT((ordered_pairs.rn % 6) + 1, 'APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'OFFERED', 'REJECTED') = 'INTERVIEW_SCHEDULED'
            THEN 'Asia/Calcutta'
        ELSE NULL
    END,
    CASE
        WHEN ELT((ordered_pairs.rn % 6) + 1, 'APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'OFFERED', 'REJECTED') = 'OFFERED'
            THEN @offer_letter_url
        ELSE NULL
    END,
    CASE
        WHEN ELT((ordered_pairs.rn % 6) + 1, 'APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'OFFERED', 'REJECTED') = 'OFFERED'
            THEN @offer_letter_public_id
        ELSE NULL
    END
FROM ordered_pairs
LEFT JOIN applicationdb.applications a
    ON a.user_id = ordered_pairs.user_id
   AND a.job_id = ordered_pairs.job_id
WHERE a.id IS NULL
ORDER BY ordered_pairs.rn
LIMIT 100;

SELECT
    SUM(CASE WHEN u.role = 'RECRUITER' THEN 1 ELSE 0 END) AS seeded_recruiters,
    SUM(CASE WHEN u.role = 'JOB_SEEKER' THEN 1 ELSE 0 END) AS seeded_job_seekers
FROM job_portal_db.users u
JOIN seed_users su ON su.email = u.email;

SELECT COUNT(*) AS seeded_jobs
FROM jobdb.job
WHERE description LIKE CONCAT('%', @seed_tag, '%');

SELECT COUNT(*) AS jobs_for_target_recruiter
FROM jobdb.job j
JOIN job_portal_db.users u ON u.id = j.recruiter_id
WHERE u.email = 'lordgabbardevansh@gmail.com'
  AND j.description LIKE CONCAT('%', @seed_tag, '%');

SELECT COUNT(*) AS applications_for_target_seeker
FROM applicationdb.applications a
JOIN job_portal_db.users u ON u.id = a.user_id
JOIN seed_jobs j ON j.job_id = a.job_id
WHERE u.email = 'devanshkumarpal@gmail.com';

SELECT COUNT(*) AS seeded_applications
FROM applicationdb.applications a
JOIN seed_jobs j ON j.job_id = a.job_id;
