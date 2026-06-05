create table audit_log (
    audit_log_id varchar(255) not null,
    action_type varchar(255) check (
        (action_type in (
            'MEMBERS_BULK_IMPORTED',
            'MEMBER_PROMOTED_TO_LEAD',
            'MEMBER_DEPARTMENT_CHANGED',
            'GENERAL_TASK_ASSIGNED',
            'OWNERSHIP_TASK_ASSIGNED',
            'OWNERSHIP_TASK_DIRECTLY_ASSIGNED',
            'TASK_SUBMISSION_REVIEWED',
            'TASK_SUBMITTED',
            'OWNERSHIP_BID_PLACED',
            'OWNERSHIP_BID_UPDATED',
            'OWNERSHIP_BIDDING_AUTO_ASSIGNED',
            'OWNERSHIP_BIDDING_CLOSED_WITHOUT_VALID_BIDS'
        ))
    ),
    actor_user_id varchar(255),
    actor_name varchar(255),
    actor_role varchar(255),
    department_id varchar(255),
    target_id varchar(255),
    target_type varchar(255),
    summary varchar(500),
    details varchar(4000),
    occurred_at timestamp(6),
    primary key (audit_log_id)
);
