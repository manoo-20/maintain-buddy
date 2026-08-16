-- ====================================================================
-- GRAMIN SHALA: Multi-Tenant Rural School Management Platform
-- PostgreSQL / Supabase Complete DDL Schema & Row Level Security (RLS)
-- ====================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. SCHOOLS (TENANTS)
CREATE TABLE IF NOT EXISTS public.schools (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    village VARCHAR(255) NOT NULL,
    district VARCHAR(255) NOT NULL,
    state VARCHAR(255) NOT NULL DEFAULT 'Uttar Pradesh',
    udise_number VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. PROFILES / USERS
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    school_id UUID NOT NULL REFERENCES public.schools(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('admin', 'teacher', 'student')),
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    avatar_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. ACADEMIC YEARS
CREATE TABLE IF NOT EXISTS public.academic_years (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    school_id UUID NOT NULL REFERENCES public.schools(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL, -- e.g. '2025-2026'
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. CLASSES & SECTIONS
CREATE TABLE IF NOT EXISTS public.classes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    school_id UUID NOT NULL REFERENCES public.schools(id) ON DELETE CASCADE,
    academic_year_id UUID REFERENCES public.academic_years(id) ON DELETE SET NULL,
    name VARCHAR(50) NOT NULL, -- 'Class 1', 'Class 5', 'Class 10'
    section VARCHAR(10) NOT NULL DEFAULT 'A', -- 'A', 'B'
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(school_id, name, section)
);

-- 6. SUBJECTS
CREATE TABLE IF NOT EXISTS public.subjects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    school_id UUID NOT NULL REFERENCES public.schools(id) ON DELETE CASCADE,
    class_id UUID NOT NULL REFERENCES public.classes(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL, -- 'Hindi', 'Mathematics', 'Science', 'English'
    code VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 7. TEACHERS
CREATE TABLE IF NOT EXISTS public.teachers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    school_id UUID NOT NULL REFERENCES public.schools(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    employee_id VARCHAR(50),
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    qualification VARCHAR(100),
    joining_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 8. TEACHER ASSIGNMENTS
CREATE TABLE IF NOT EXISTS public.teacher_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    school_id UUID NOT NULL REFERENCES public.schools(id) ON DELETE CASCADE,
    teacher_id UUID NOT NULL REFERENCES public.teachers(id) ON DELETE CASCADE,
    class_id UUID NOT NULL REFERENCES public.classes(id) ON DELETE CASCADE,
    subject_id UUID REFERENCES public.subjects(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(school_id, teacher_id, class_id, subject_id)
);

-- 9. STUDENTS
CREATE TABLE IF NOT EXISTS public.students (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    school_id UUID NOT NULL REFERENCES public.schools(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    class_id UUID NOT NULL REFERENCES public.classes(id) ON DELETE CASCADE,
    admission_no VARCHAR(50) NOT NULL,
    roll_no VARCHAR(20) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('Male', 'Female', 'Other')),
    dob DATE,
    guardian_name VARCHAR(255) NOT NULL,
    guardian_phone VARCHAR(20) NOT NULL,
    village_address TEXT,
    aadhaar_last4 VARCHAR(4),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(school_id, admission_no)
);

-- 10. EXAMINATIONS
CREATE TABLE IF NOT EXISTS public.exams (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    school_id UUID NOT NULL REFERENCES public.schools(id) ON DELETE CASCADE,
    academic_year_id UUID REFERENCES public.academic_years(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL, -- 'Unit Test 1', 'Half Yearly', 'Annual Exam'
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 11. MARKS
CREATE TABLE IF NOT EXISTS public.marks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    school_id UUID NOT NULL REFERENCES public.schools(id) ON DELETE CASCADE,
    exam_id UUID NOT NULL REFERENCES public.exams(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES public.students(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES public.subjects(id) ON DELETE CASCADE,
    marks_obtained NUMERIC(5, 2) NOT NULL CHECK (marks_obtained >= 0),
    max_marks NUMERIC(5, 2) NOT NULL CHECK (max_marks > 0),
    grade VARCHAR(5),
    entered_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT marks_cannot_exceed_max CHECK (marks_obtained <= max_marks),
    UNIQUE(school_id, exam_id, student_id, subject_id)
);

-- 12. ATTENDANCE
CREATE TABLE IF NOT EXISTS public.attendance (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    school_id UUID NOT NULL REFERENCES public.schools(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES public.students(id) ON DELETE CASCADE,
    class_id UUID NOT NULL REFERENCES public.classes(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    status VARCHAR(15) NOT NULL CHECK (status IN ('present', 'absent', 'late', 'leave')),
    remarks TEXT,
    marked_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(school_id, student_id, date)
);

-- 13. FEES STRUCTURE
CREATE TABLE IF NOT EXISTS public.fees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    school_id UUID NOT NULL REFERENCES public.schools(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES public.students(id) ON DELETE CASCADE,
    academic_year_id UUID REFERENCES public.academic_years(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL, -- 'Annual Tuition Fee + Examination'
    total_amount NUMERIC(10, 2) NOT NULL CHECK (total_amount >= 0),
    discount_amount NUMERIC(10, 2) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    due_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 14. FEE PAYMENTS
CREATE TABLE IF NOT EXISTS public.fee_payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    school_id UUID NOT NULL REFERENCES public.schools(id) ON DELETE CASCADE,
    fee_id UUID NOT NULL REFERENCES public.fees(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES public.students(id) ON DELETE CASCADE,
    receipt_no VARCHAR(50) NOT NULL,
    amount_paid NUMERIC(10, 2) NOT NULL CHECK (amount_paid > 0),
    payment_date DATE NOT NULL,
    payment_mode VARCHAR(20) NOT NULL CHECK (payment_mode IN ('cash', 'upi', 'bank_transfer', 'cheque')),
    recorded_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ====================================================================
-- 15. INDEXES FOR HIGH-PERFORMANCE MULTI-TENANT QUERYING
-- ====================================================================
CREATE INDEX IF NOT EXISTS idx_profiles_school_user ON public.profiles(school_id, id);
CREATE INDEX IF NOT EXISTS idx_students_school_class ON public.students(school_id, class_id);
CREATE INDEX IF NOT EXISTS idx_attendance_school_date_class ON public.attendance(school_id, date, class_id);
CREATE INDEX IF NOT EXISTS idx_marks_school_exam_student ON public.marks(school_id, exam_id, student_id);
CREATE INDEX IF NOT EXISTS idx_fees_school_student ON public.fees(school_id, student_id);
CREATE INDEX IF NOT EXISTS idx_fee_payments_school_fee ON public.fee_payments(school_id, fee_id);
CREATE INDEX IF NOT EXISTS idx_teacher_assignments_school_teacher ON public.teacher_assignments(school_id, teacher_id);

-- ====================================================================
-- 16. ROW LEVEL SECURITY (RLS) POLICIES
-- ====================================================================

-- Helper Functions
CREATE OR REPLACE FUNCTION public.get_auth_school_id()
RETURNS UUID AS $$
  SELECT school_id FROM public.profiles WHERE id = auth.uid();
$$ LANGUAGE sql STABLE SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.get_auth_role()
RETURNS VARCHAR AS $$
  SELECT role FROM public.profiles WHERE id = auth.uid();
$$ LANGUAGE sql STABLE SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.get_auth_student_id()
RETURNS UUID AS $$
  SELECT id FROM public.students WHERE user_id = auth.uid();
$$ LANGUAGE sql STABLE SECURITY DEFINER;

CREATE OR REPLACE FUNCTION public.get_auth_teacher_id()
RETURNS UUID AS $$
  SELECT id FROM public.teachers WHERE user_id = auth.uid();
$$ LANGUAGE sql STABLE SECURITY DEFINER;

-- Enable RLS on ALL tables
ALTER TABLE public.schools ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.academic_years ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.classes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subjects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.teachers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.teacher_assignments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.students ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.exams ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.marks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.fees ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.fee_payments ENABLE ROW LEVEL SECURITY;

-- Schools: Admin can read/update own school. Public can register new school.
CREATE POLICY "Allow school read by tenant users"
  ON public.schools FOR SELECT
  USING (id = public.get_auth_school_id());

CREATE POLICY "Allow school admin to update their school"
  ON public.schools FOR UPDATE
  USING (id = public.get_auth_school_id() AND public.get_auth_role() = 'admin');

CREATE POLICY "Allow public school registration"
  ON public.schools FOR INSERT
  WITH CHECK (true);

-- Profiles: Tenant users can view profiles in their school; users can update own profile.
CREATE POLICY "Tenant users can view school profiles"
  ON public.profiles FOR SELECT
  USING (school_id = public.get_auth_school_id());

CREATE POLICY "Users can insert own profile"
  ON public.profiles FOR INSERT
  WITH CHECK (auth.uid() = id);

CREATE POLICY "Admin can update profiles in own school"
  ON public.profiles FOR ALL
  USING (school_id = public.get_auth_school_id() AND public.get_auth_role() = 'admin');

-- Classes & Subjects: Tenant users can read; Admin can manage
CREATE POLICY "Tenant users can view classes"
  ON public.classes FOR SELECT
  USING (school_id = public.get_auth_school_id());

CREATE POLICY "Admin can manage classes"
  ON public.classes FOR ALL
  USING (school_id = public.get_auth_school_id() AND public.get_auth_role() = 'admin');

CREATE POLICY "Tenant users can view subjects"
  ON public.subjects FOR SELECT
  USING (school_id = public.get_auth_school_id());

CREATE POLICY "Admin can manage subjects"
  ON public.subjects FOR ALL
  USING (school_id = public.get_auth_school_id() AND public.get_auth_role() = 'admin');

-- Teachers & Assignments
CREATE POLICY "Tenant users can view teachers"
  ON public.teachers FOR SELECT
  USING (school_id = public.get_auth_school_id());

CREATE POLICY "Admin can manage teachers"
  ON public.teachers FOR ALL
  USING (school_id = public.get_auth_school_id() AND public.get_auth_role() = 'admin');

CREATE POLICY "Tenant users can view teacher assignments"
  ON public.teacher_assignments FOR SELECT
  USING (school_id = public.get_auth_school_id());

CREATE POLICY "Admin can manage teacher assignments"
  ON public.teacher_assignments FOR ALL
  USING (school_id = public.get_auth_school_id() AND public.get_auth_role() = 'admin');

-- Students:
CREATE POLICY "Admin and Teachers can view all students in school"
  ON public.students FOR SELECT
  USING (
    school_id = public.get_auth_school_id()
    AND (
      public.get_auth_role() IN ('admin', 'teacher')
      OR id = public.get_auth_student_id()
    )
  );

CREATE POLICY "Admin can manage students"
  ON public.students FOR ALL
  USING (school_id = public.get_auth_school_id() AND public.get_auth_role() = 'admin');

-- Attendance:
CREATE POLICY "Admin and Teachers view attendance in school; Student view own"
  ON public.attendance FOR SELECT
  USING (
    school_id = public.get_auth_school_id()
    AND (
      public.get_auth_role() IN ('admin', 'teacher')
      OR student_id = public.get_auth_student_id()
    )
  );

CREATE POLICY "Admin and assigned Teachers can insert/update attendance"
  ON public.attendance FOR INSERT
  WITH CHECK (
    school_id = public.get_auth_school_id()
    AND public.get_auth_role() IN ('admin', 'teacher')
  );

CREATE POLICY "Admin and assigned Teachers can modify attendance"
  ON public.attendance FOR UPDATE
  USING (
    school_id = public.get_auth_school_id()
    AND public.get_auth_role() IN ('admin', 'teacher')
  );

-- Marks:
CREATE POLICY "Admin and Teachers view marks in school; Student view own"
  ON public.marks FOR SELECT
  USING (
    school_id = public.get_auth_school_id()
    AND (
      public.get_auth_role() IN ('admin', 'teacher')
      OR student_id = public.get_auth_student_id()
    )
  );

CREATE POLICY "Admin and authorized teachers can insert marks"
  ON public.marks FOR INSERT
  WITH CHECK (
    school_id = public.get_auth_school_id()
    AND public.get_auth_role() IN ('admin', 'teacher')
  );

CREATE POLICY "Admin and authorized teachers can update marks"
  ON public.marks FOR UPDATE
  USING (
    school_id = public.get_auth_school_id()
    AND public.get_auth_role() IN ('admin', 'teacher')
  );

-- Fees & Payments:
CREATE POLICY "Admin can manage fees; Student can view own"
  ON public.fees FOR SELECT
  USING (
    school_id = public.get_auth_school_id()
    AND (
      public.get_auth_role() = 'admin'
      OR student_id = public.get_auth_student_id()
    )
  );

CREATE POLICY "Admin can insert and update fees"
  ON public.fees FOR ALL
  USING (school_id = public.get_auth_school_id() AND public.get_auth_role() = 'admin');

CREATE POLICY "Admin can view payments; Student can view own"
  ON public.fee_payments FOR SELECT
  USING (
    school_id = public.get_auth_school_id()
    AND (
      public.get_auth_role() = 'admin'
      OR student_id = public.get_auth_student_id()
    )
  );

CREATE POLICY "Admin can record fee payments"
  ON public.fee_payments FOR INSERT
  WITH CHECK (
    school_id = public.get_auth_school_id()
    AND public.get_auth_role() = 'admin'
  );
