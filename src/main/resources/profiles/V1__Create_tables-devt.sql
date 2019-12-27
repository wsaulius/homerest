drop table if exists authority;
drop table if exists bank_account;
drop table if exists user;
drop table if exists user_authority;

create table authority (id integer not null auto_increment, name varchar(255), primary key (id));
create table bank_account (account_id binary(255) not null, account_number varchar(255) not null, account_status integer, currency_unit varchar(3) not null, current_balance decimal(19,2), date_updated datetime, primary key (account_id));
create table user (id integer not null auto_increment, date_created datetime, password varchar(255), username varchar(255) not null, primary key (id));
create table user_authority (user_id integer not null, authority_id integer not null, primary key (user_id, authority_id));

alter table bank_account add constraint UK_mb8kv2x9143o96jgxbv6mahcq unique (account_number);
alter table user add constraint UK_sb8bbouer5wak8vyiiy4pf2bx unique (username);
alter table user_authority add constraint FKgvxjs381k6f48d5d2yi11uh89 foreign key (authority_id) references authority (id);
alter table user_authority add constraint FKpqlsjpkybgos9w2svcri7j8xy foreign key (user_id) references user (id);

