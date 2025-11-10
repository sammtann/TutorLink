package com.csy.springbootauthbe.admin.entity;

public enum Permissions {

    // Student Management
    VIEW_STUDENTS,
    SUSPEND_STUDENT,
    DELETE_STUDENT,

    // Tutor Management
    VIEW_TUTORS,
    APPROVE_TUTOR,
    REJECT_TUTOR,
    SUSPEND_TUTOR,
    DELETE_TUTOR,

    // Admin Management
    VIEW_ADMIN,
    CREATE_ADMIN,
    EDIT_ADMIN_ROLES,
    SUSPEND_ADMIN,
    DELETE_ADMIN,
    SUPER_ADMIN,

    // Booking Management
    DELETE_BOOKING

}