-- ============================================================
-- Enums
-- ============================================================

CREATE TYPE household_member_role AS ENUM (
    'OWNER',
    'MEMBER'
);

CREATE TYPE household_member_status AS ENUM (
    'ACTIVE',
    'INACTIVE'
);

CREATE TYPE duty_status AS ENUM (
    'PENDING_REVIEW',
    'RESOLVED'
);

CREATE TYPE duty_outcome AS ENUM (
    'COMPLETED',
    'COVERED',
    'PAID',
    'SWITCHED',
    'MISSED'
);

CREATE TYPE duty_report_type AS ENUM (
    'COVERED',
    'PAID',
    'SWITCHED'
);


-- ============================================================
-- Users
-- ============================================================

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT users_auth_user_fk
        FOREIGN KEY (id)
        REFERENCES auth.users(id)
        ON DELETE CASCADE
);


-- ============================================================
-- Households
-- ============================================================

CREATE TABLE households (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    timezone VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- ============================================================
-- Household members
-- ============================================================

CREATE TABLE household_members (
    id UUID PRIMARY KEY,
    household_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role household_member_role NOT NULL,
    status household_member_status NOT NULL,
    rotation_position INTEGER NOT NULL,

    CONSTRAINT household_members_household_fk
        FOREIGN KEY (household_id)
        REFERENCES households(id)
        ON DELETE CASCADE,

    CONSTRAINT household_members_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT household_members_household_user_unique
        UNIQUE (household_id, user_id),

    CONSTRAINT household_members_rotation_unique
        UNIQUE (household_id, rotation_position)
);


-- ============================================================
-- Duties
-- ============================================================

CREATE TABLE duties (
    id UUID PRIMARY KEY,
    household_id UUID NOT NULL,
    date DATE NOT NULL,
    scheduled_member_id UUID NOT NULL,
    status duty_status NOT NULL,
    outcome duty_outcome,
    completed_by_member_id UUID,
    resolved_at TIMESTAMPTZ,

    CONSTRAINT duties_household_fk
        FOREIGN KEY (household_id)
        REFERENCES households(id)
        ON DELETE CASCADE,

    CONSTRAINT duties_scheduled_member_fk
        FOREIGN KEY (scheduled_member_id)
        REFERENCES household_members(id),

    CONSTRAINT duties_completed_by_member_fk
        FOREIGN KEY (completed_by_member_id)
        REFERENCES household_members(id),

    CONSTRAINT duties_household_date_unique
        UNIQUE (household_id, date)
);


-- ============================================================
-- Duty reports
-- ============================================================

CREATE TABLE duty_reports (
    id UUID PRIMARY KEY,
    duty_id UUID NOT NULL UNIQUE,
    reported_by_member_id UUID NOT NULL,
    type duty_report_type NOT NULL,
    other_member_id UUID,
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT duty_reports_duty_fk
        FOREIGN KEY (duty_id)
        REFERENCES duties(id)
        ON DELETE CASCADE,

    CONSTRAINT duty_reports_reported_by_member_fk
        FOREIGN KEY (reported_by_member_id)
        REFERENCES household_members(id),

    CONSTRAINT duty_reports_other_member_fk
        FOREIGN KEY (other_member_id)
        REFERENCES household_members(id)
);