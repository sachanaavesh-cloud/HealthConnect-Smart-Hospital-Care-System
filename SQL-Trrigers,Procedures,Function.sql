-- =========================================================================
-- HealthConnect - Smart AI Government Healthcare Management System
-- =========================================================================
-- Note: Directly copy & paste this into phpMyAdmin's SQL query runner. 
-- No DELIMITER statements are used.

-- =========================================================================
-- 1. STORED PROCEDURES (50)
-- =========================================================================

-- User (4)
CREATE PROCEDURE LoginUser(IN p_username VARCHAR(50), IN p_password VARCHAR(255))
    SELECT * FROM users WHERE username = p_username AND password = p_password;

CREATE PROCEDURE UpdateUserProfile(IN p_user_id INT, IN p_status VARCHAR(20))
    UPDATE users SET status = p_status WHERE user_id = p_user_id;

CREATE PROCEDURE ChangePassword(IN p_user_id INT, IN p_password VARCHAR(255))
    UPDATE users SET password = p_password WHERE user_id = p_user_id;

CREATE PROCEDURE GetUserById(IN p_user_id INT)
    SELECT * FROM users WHERE user_id = p_user_id;

-- Patient (5)
CREATE PROCEDURE RegisterPatient(IN p_first VARCHAR(50), IN p_last VARCHAR(50), IN p_phone VARCHAR(15))
    INSERT INTO patients (first_name, last_name, phone, registration_date) VALUES (p_first, p_last, p_phone, CURDATE());

CREATE PROCEDURE BookAppointment(IN p_pid INT, IN p_did INT, IN p_date DATE, IN p_time TIME)
    INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status) VALUES (p_pid, p_did, p_date, p_time, 'Booked');

CREATE PROCEDURE ViewMedicalHistory(IN p_patient_id INT)
    SELECT * FROM medical_history WHERE patient_id = p_patient_id;

CREATE PROCEDURE ViewPrescriptions(IN p_patient_id INT)
    SELECT * FROM prescriptions WHERE patient_id = p_patient_id;

CREATE PROCEDURE ViewDietPlans(IN p_patient_id INT)
    SELECT * FROM diet_plan WHERE patient_id = p_patient_id;

-- Doctor (5)
CREATE PROCEDURE AddDoctor(IN p_name VARCHAR(100), IN p_spec VARCHAR(100), IN p_dept INT)
    INSERT INTO doctors (name, specialization, department_id) VALUES (p_name, p_spec, p_dept);

CREATE PROCEDURE UpdateDoctorAvailability(IN p_doctor_id INT, IN p_avail VARCHAR(100))
    UPDATE doctors SET availability = p_avail WHERE doctor_id = p_doctor_id;

CREATE PROCEDURE GetDoctorAppointments(IN p_doctor_id INT)
    SELECT * FROM appointments WHERE doctor_id = p_doctor_id;

CREATE PROCEDURE CreateMedicalReport(IN p_patient_id INT, IN p_desc TEXT)
    INSERT INTO medical_history (patient_id, description) VALUES (p_patient_id, p_desc);

CREATE PROCEDURE CreatePrescription(IN p_pid INT, IN p_did INT, IN p_vid INT, IN p_diag VARCHAR(200))
    INSERT INTO prescriptions (patient_id, doctor_id, visit_id, diagnosis, created_date) VALUES (p_pid, p_did, p_vid, p_diag, CURDATE());

-- Appointment (5)
CREATE PROCEDURE CancelAppointment(IN p_appointment_id INT)
    UPDATE appointments SET status = 'Cancelled' WHERE appointment_id = p_appointment_id;

CREATE PROCEDURE UpdateAppointmentStatus(IN p_appointment_id INT, IN p_status VARCHAR(20))
    UPDATE appointments SET status = p_status WHERE appointment_id = p_appointment_id;

CREATE PROCEDURE GetAppointmentDetails(IN p_appointment_id INT)
    SELECT * FROM appointments WHERE appointment_id = p_appointment_id;

CREATE PROCEDURE RescheduleAppointment(IN p_appointment_id INT, IN p_date DATE, IN p_time TIME)
    UPDATE appointments SET appointment_date = p_date, appointment_time = p_time WHERE appointment_id = p_appointment_id;

CREATE PROCEDURE GetAppointmentsByDate(IN p_date DATE)
    SELECT * FROM appointments WHERE appointment_date = p_date;

-- QueueManager (4)
CREATE PROCEDURE AddPatientToQueue(IN p_pid INT, IN p_did INT, IN p_priority VARCHAR(20))
    INSERT INTO queue (patient_id, doctor_id, priority_level, status, arrival_time) VALUES (p_pid, p_did, p_priority, 'Waiting', NOW());

CREATE PROCEDURE RemovePatientFromQueue(IN p_queue_id INT)
    DELETE FROM queue WHERE queue_id = p_queue_id;

CREATE PROCEDURE PrioritizeEmergency(IN p_queue_id INT)
    UPDATE queue SET priority_level = 'Emergency' WHERE queue_id = p_queue_id;

CREATE PROCEDURE ViewQueue()
    SELECT * FROM queue WHERE status = 'Waiting' ORDER BY priority_level, arrival_time;

-- Medical Record (3)
CREATE PROCEDURE AddMedicalRecord(IN p_pid INT, IN p_disease VARCHAR(100), IN p_desc TEXT)
    INSERT INTO medical_history (patient_id, disease, description) VALUES (p_pid, p_disease, p_desc);

CREATE PROCEDURE UpdateMedicalRecord(IN p_history_id INT, IN p_desc TEXT)
    UPDATE medical_history SET description = p_desc WHERE history_id = p_history_id;

CREATE PROCEDURE ViewMedicalRecord(IN p_patient_id INT)
    SELECT * FROM medical_history WHERE patient_id = p_patient_id;

-- Report (2)
CREATE PROCEDURE UpdateMedicalReport(IN p_test_id INT, IN p_file VARCHAR(255))
    UPDATE lab_tests SET report_file = p_file WHERE test_id = p_test_id;

CREATE PROCEDURE ViewMedicalReport(IN p_patient_id INT)
    SELECT * FROM lab_tests WHERE patient_id = p_patient_id AND report_file IS NOT NULL;

-- Prescription (3)
CREATE PROCEDURE UpdatePrescription(IN p_prescription_id INT, IN p_notes TEXT)
    UPDATE prescriptions SET notes = p_notes WHERE prescription_id = p_prescription_id;

CREATE PROCEDURE DeletePrescription(IN p_prescription_id INT)
    DELETE FROM prescriptions WHERE prescription_id = p_prescription_id;

CREATE PROCEDURE ViewPrescription(IN p_prescription_id INT)
    SELECT * FROM prescriptions WHERE prescription_id = p_prescription_id;

-- Diet Plan (3)
CREATE PROCEDURE CreateDietPlan(IN p_pid INT, IN p_did INT, IN p_break VARCHAR(255), IN p_lunch VARCHAR(255))
    INSERT INTO diet_plan (patient_id, doctor_id, breakfast, lunch) VALUES (p_pid, p_did, p_break, p_lunch);

CREATE PROCEDURE UpdateDietPlan(IN p_diet_id INT, IN p_dinner VARCHAR(255))
    UPDATE diet_plan SET dinner = p_dinner WHERE diet_id = p_diet_id;

CREATE PROCEDURE ViewDietPlan(IN p_patient_id INT)
    SELECT * FROM diet_plan WHERE patient_id = p_patient_id;

-- Discussion (2)
CREATE PROCEDURE SendMessage(IN p_pid INT, IN p_did INT, IN p_msg VARCHAR(255))
    INSERT INTO follow_up (patient_id, doctor_id, remarks) VALUES (p_pid, p_did, p_msg);

CREATE PROCEDURE ViewConversation(IN p_pid INT, IN p_did INT)
    SELECT * FROM follow_up WHERE patient_id = p_pid AND doctor_id = p_did;

-- Reminder (2)
CREATE PROCEDURE CreateReminder(IN p_pid INT, IN p_type VARCHAR(20), IN p_date DATETIME)
    INSERT INTO reminders (patient_id, type, date, status) VALUES (p_pid, p_type, p_date, 'Pending');

CREATE PROCEDURE CompleteReminder(IN p_reminder_id INT)
    UPDATE reminders SET status = 'Sent' WHERE reminder_id = p_reminder_id;

-- Billing (4)
CREATE PROCEDURE GenerateBill(IN p_pid INT, IN p_aid INT, IN p_amount DECIMAL(10,2))
    INSERT INTO payments (patient_id, appointment_id, amount, payment_mode, status, date) VALUES (p_pid, p_aid, p_amount, 'Cash', 'Pending', CURDATE());

CREATE PROCEDURE MakePayment(IN p_payment_id INT, IN p_mode VARCHAR(20))
    UPDATE payments SET status = 'Paid', payment_mode = p_mode WHERE payment_id = p_payment_id;

-- Feedback (4)
CREATE PROCEDURE SubmitFeedback(IN p_pid INT, IN p_did INT, IN p_rating INT, IN p_comments TEXT)
    INSERT INTO feedback (patient_id, doctor_id, rating, comments, date) VALUES (p_pid, p_did, p_rating, p_comments, CURDATE());

CREATE PROCEDURE UpdateFeedback(IN p_feedback_id INT, IN p_rating INT)
    UPDATE feedback SET rating = p_rating WHERE feedback_id = p_feedback_id;

CREATE PROCEDURE DeleteFeedback(IN p_feedback_id INT)
    DELETE FROM feedback WHERE feedback_id = p_feedback_id;

CREATE PROCEDURE ViewFeedback(IN p_doctor_id INT)
    SELECT * FROM feedback WHERE doctor_id = p_doctor_id;

-- Lab Test (4)
CREATE PROCEDURE BookLabTest(IN p_pid INT, IN p_did INT, IN p_test VARCHAR(100))
    INSERT INTO lab_tests (patient_id, doctor_id, test_name, status, test_date) VALUES (p_pid, p_did, p_test, 'Pending', CURDATE());

CREATE PROCEDURE UpdateLabResult(IN p_test_id INT, IN p_file VARCHAR(255))
    UPDATE lab_tests SET report_file = p_file, status = 'Completed' WHERE test_id = p_test_id;

CREATE PROCEDURE ViewLabResult(IN p_patient_id INT)
    SELECT * FROM lab_tests WHERE patient_id = p_patient_id;

CREATE PROCEDURE UpdateLabStatus(IN p_test_id INT, IN p_status VARCHAR(20))
    UPDATE lab_tests SET status = p_status WHERE test_id = p_test_id;

-- Audit Log (2)
CREATE PROCEDURE ViewAuditLogs()
    SELECT * FROM audit_log ORDER BY timestamp DESC;

CREATE PROCEDURE DeleteOldLogs(IN p_date DATETIME)
    DELETE FROM audit_log WHERE timestamp < p_date;


-- =========================================================================
-- 2. STORED FUNCTIONS (5)
-- =========================================================================

CREATE FUNCTION GenerateHealthID() RETURNS VARCHAR(20) DETERMINISTIC
    RETURN CONCAT('HC', LPAD(FLOOR(RAND() * 100000), 5, '0'));

CREATE FUNCTION GenerateQueueToken() RETURNS INT DETERMINISTIC
    RETURN FLOOR(RAND() * 1000) + 1;

CREATE FUNCTION CalculateBillTotal(p_aid INT) RETURNS DECIMAL(10,2) READS SQL DATA
    RETURN (SELECT COALESCE(SUM(amount), 0) FROM payments WHERE appointment_id = p_aid);

CREATE FUNCTION AverageDoctorRating(p_did INT) RETURNS DECIMAL(3,2) READS SQL DATA
    RETURN (SELECT COALESCE(AVG(rating), 0) FROM feedback WHERE doctor_id = p_did);

CREATE FUNCTION CalculateWaitingTime(p_qid INT) RETURNS INT READS SQL DATA
    RETURN (SELECT COALESCE(queue_number * 15, 0) FROM queue WHERE queue_id = p_qid);


-- =========================================================================
-- 3. TRIGGERS (15)
-- =========================================================================

CREATE TRIGGER trg_generate_health_id BEFORE INSERT ON patients
FOR EACH ROW SET NEW.health_id = IFNULL(NEW.health_id, CONCAT('HC', LPAD(FLOOR(RAND() * 100000), 5, '0')));

CREATE TRIGGER trg_appointment_booked AFTER INSERT ON appointments
FOR EACH ROW INSERT INTO audit_log(user_id, operation, table_name) VALUES (1, 'INSERT', 'appointments');

CREATE TRIGGER trg_appointment_cancelled AFTER UPDATE ON appointments
FOR EACH ROW INSERT INTO audit_log(user_id, operation, table_name) VALUES (1, 'UPDATE', 'appointments');

CREATE TRIGGER trg_report_completed AFTER INSERT ON lab_tests
FOR EACH ROW UPDATE appointments SET status = 'Completed' WHERE patient_id = NEW.patient_id AND doctor_id = NEW.doctor_id;

CREATE TRIGGER trg_bill_before_insert BEFORE INSERT ON payments
FOR EACH ROW SET NEW.amount = NEW.amount + 0; 

CREATE TRIGGER trg_bill_before_update BEFORE UPDATE ON payments
FOR EACH ROW SET NEW.amount = NEW.amount + 0;

CREATE TRIGGER trg_payment_success AFTER UPDATE ON payments
FOR EACH ROW INSERT INTO audit_log(user_id, operation, table_name) VALUES (1, 'UPDATE', 'payments');

CREATE TRIGGER trg_feedback_insert AFTER INSERT ON feedback
FOR EACH ROW UPDATE doctors SET consultation_fee = consultation_fee WHERE doctor_id = NEW.doctor_id;

CREATE TRIGGER trg_prescription_insert AFTER INSERT ON prescriptions
FOR EACH ROW INSERT INTO reminders(patient_id, type, date) VALUES (NEW.patient_id, 'Medicine', NOW());

CREATE TRIGGER trg_diet_insert AFTER INSERT ON diet_plan
FOR EACH ROW INSERT INTO audit_log(user_id, operation, table_name) VALUES (1, 'INSERT', 'diet_plan');

CREATE TRIGGER trg_lab_completed AFTER UPDATE ON lab_tests
FOR EACH ROW UPDATE medical_history SET description = 'Lab Test Completed' WHERE patient_id = NEW.patient_id;

CREATE TRIGGER trg_record_updated AFTER UPDATE ON medical_history
FOR EACH ROW INSERT INTO audit_log(user_id, operation, table_name) VALUES (1, 'UPDATE', 'medical_history');

CREATE TRIGGER trg_profile_updated AFTER UPDATE ON users
FOR EACH ROW INSERT INTO audit_log(user_id, operation, table_name) VALUES (NEW.user_id, 'UPDATE', 'users');

CREATE TRIGGER trg_reminder_created AFTER INSERT ON reminders
FOR EACH ROW INSERT INTO audit_log(user_id, operation, table_name) VALUES (1, 'INSERT', 'reminders');

CREATE TRIGGER trg_message_sent AFTER INSERT ON follow_up
FOR EACH ROW INSERT INTO audit_log(user_id, operation, table_name) VALUES (1, 'INSERT', 'follow_up');

-- ================== END OF SCRIPT ================== --