CREATE TABLE users(
	user_id						varchar(30) not null,
    username					varchar(30) not null,
    constraint					pk_user primary key (user_id)
);

CREATE TABLE user_details(
	id							int not null auto_increment,
    user_id						varchar(30) not null,
    firstname					varchar(50) not null,
    lastname					varchar(50),
    email						varchar(100) not null,
    dob							date,
    age							int,
    occupation					varchar(50),
    country						varchar(50),
    image_url                   varchar(200),
    constraint					pk_user_details primary key (id),
    constraint					fk_user_details foreign key (user_id) references users(user_id)
									on update cascade on delete cascade
);

CREATE TABLE accounts(
	account_id					varchar(50) not null,
    user_id						varchar(30) not null,
    salt_string					varchar(30) not null,
    hashed_string				varchar(100) not null,
    is_valid					boolean,
    deletion_token				varchar(100),
    constraint					pk_account primary key (account_id),
    constraint					fk_account foreign key (user_id) references users(user_id)
);

CREATE TABLE activities(
	id							int not null auto_increment,
    user_id						varchar(30),
    category					enum('income', 'saving', 'spending'),
    item						varchar(100),
    item_date					date,
    amount						float default 0.0,
    currency					varchar(10) default 'SGD',
    constraint					pk_activity primary key (id),
    constraint					fk_activity foreign key (user_id) references users(user_id)
									on update cascade on delete set null
);