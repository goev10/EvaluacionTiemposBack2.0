create table if not exists change_log
(
    id               int auto_increment
    primary key,
    field            varchar(30)  null,
    original         varchar(255) null,
    updated          varchar(255) null,
    num_empleado     varchar(20)  null,
    evaluation_id    int          null,
    editor_user_name varchar(50)  null,
    updated_on       datetime     null,
    sociedad         varchar(250) null,
    area_nomina      varchar(250) null,
    editor_name      varchar(250) null,
    empleado_name    varchar(250) null,
    user_id          int          null
    )
    charset = utf8;

create table if not exists employee
(
    id           char(36)    not null
    primary key,
    num_employee varchar(11) null,
    name         varchar(50) null,
    grouper_1    varchar(50) null,
    grouper_2    varchar(50) null,
    grouper_3    varchar(50) null,
    grouper_4    varchar(50) null,
    grouper_5    varchar(50) null,
    start_date   date        null
    );

create table if not exists evaluation
(
    id                      int auto_increment
    primary key,
    fecha                   date                  null,
    hora_entrada            time                  null,
    hora_pausa              time                  null,
    hora_regreso_pausa      time                  null,
    hora_salida             time                  null,
    resultado_entrada       varchar(5)            null,
    resultado_pausa         varchar(5)            null,
    resultado_regreso_pausa varchar(5)            null,
    resultado_salida        varchar(5)            null,
    resultado_general       varchar(250)          null,
    status_registro         varchar(20)           null,
    num_empleado            varchar(20)           null,
    horario                 varchar(8)            null,
    comentario              varchar(80)           null,
    enlace                  varchar(255)          null,
    tipo_incidencia         int                   null,
    horas_extra             smallint default 0    null,
    horas_tomadas           smallint default 0    null,
    payload                 json                  null,
    area_nomina             varchar(20)           null,
    sociedad                varchar(20)           null,
    tipo_hrs_extra          varchar(20)           null,
    referencia              varchar(255)          null,
    consecutivo1            varchar(255)          null,
    consecutivo2            varchar(255)          null,
    approbation_level       int                   null,
    aprobado                bit      default b'0' null,
    turn                    int                   null,
    employee_name           varchar(350)          null,
    payroll                 varchar(100)          null
    )
    charset = utf8;

create table if not exists festive_days
(
    id          bigint unsigned auto_increment
    primary key,
    day         int          not null,
    month       int          not null,
    name        varchar(100) not null,
    description varchar(255) null,
    constraint id
    unique (id)
    );

create table if not exists groupers_configurations
(
    id         bigint unsigned auto_increment
    primary key,
    name       varchar(255) not null,
    short_name varchar(50)  not null,
    visible    tinyint(1)   not null,
    constraint id
    unique (id)
    );

create table if not exists permission
(
    id          int auto_increment
    primary key,
    key_name    varchar(30)  not null,
    description varchar(100) not null
    )
    charset = utf8;

create table if not exists profile
(
    id          int auto_increment
    primary key,
    description varchar(100) not null
    )
    charset = utf8;

create table if not exists profile_permission
(
    profile_id    int not null,
    permission_id int not null,
    primary key (profile_id, permission_id),
    constraint permission_id_permission_id
    foreign key (permission_id) references permission (id),
    constraint profile_id_profile_id
    foreign key (profile_id) references profile (id)
    )
    charset = utf8;

create table if not exists seq
(
    next_val bigint null
);

create table if not exists timesheets
(
    id                   char(36)     not null
    primary key,
    timesheet_identifier char(5)      not null,
    description          varchar(255) null,
    days_of_the_week     smallint     not null,
    entry_time           time         null,
    break_departure_time time         null,
    break_return_time    time         null,
    departure_time       time         null
    );

create table if not exists employee_timesheet
(
    id           char(36) not null
    primary key,
    employee_id  char(36) not null,
    timesheet_id char(36) not null,
    from_date    date     not null,
    to_date      date     not null,
    constraint fk_employee_timesheet_employee
    foreign key (employee_id) references employee (id),
    constraint fk_employee_timesheet_timesheet
    foreign key (timesheet_id) references timesheets (id)
    );

create table if not exists user
(
    id         int auto_increment
    primary key,
    username   varchar(255) not null,
    name       varchar(255) not null,
    email      varchar(255) not null,
    password   varchar(255) not null,
    active     bit          not null,
    user_level int          null,
    constraint UK_user_email
    unique (email),
    constraint UK_user_username
    unique (username)
    )
    charset = utf8;

create table if not exists impersonations
(
    id             bigint auto_increment
    primary key,
    user_id        int null,
    target_user_id int null,
    constraint impersonations_target_user_id_user_id
    foreign key (target_user_id) references user (id),
    constraint impersonations_user_id_user_id
    foreign key (user_id) references user (id)
    )
    charset = utf8;

create table if not exists user_employee
(
    user_id         int         not null,
    employee_number varchar(20) not null,
    primary key (user_id, employee_number),
    constraint fk_user
    foreign key (user_id) references user (id)
    );

create table if not exists user_groupers
(
    id        bigint unsigned auto_increment
    primary key,
    user_id   int         not null,
    grouper_1 varchar(50) null,
    grouper_2 varchar(50) null,
    grouper_3 varchar(50) null,
    grouper_4 varchar(50) null,
    grouper_5 varchar(50) null,
    constraint id
    unique (id),
    constraint fk_user_groupers_user
    foreign key (user_id) references user (id)
    );

create table if not exists user_profile
(
    user_id    int not null,
    profile_id int not null,
    primary key (profile_id, user_id),
    constraint user_id_user_id
    foreign key (user_id) references user (id),
    constraint user_profile_profile_id_profile_id
    foreign key (profile_id) references profile (id)
    )
    charset = utf8;

create table if not exists work_lock
(
    id             int          not null
    primary key,
    area_nomina    varchar(255) null,
    evaluation_id  int          null,
    lock_ends_on   datetime(6)  null,
    lock_starts_on datetime(6)  null,
    locked_on      datetime(6)  null,
    sociedad       varchar(255) null,
    user_id        int          null,
    constraint FKm53c4ohfjo943we7vi8v3uux
    foreign key (user_id) references user (id)
    );
