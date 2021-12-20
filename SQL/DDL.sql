create table publisher
	(ID				varchar(8),
	 name			varchar(15) not null,
	 email			varchar(30) not null,
	 province		varchar(2) not null
		check (province in ('NL', 'PE', 'NS', 'NB', 'QC', 'ON','MB', 'SK', 'AB', 'BC', 'YT', 'NT', 'NU')),
	 city 			varchar(20) not null,
	 address		varchar(50) not null,
	 postal_code	varchar(6) not null,
	 bank_num		varchar(20) not null,
	 primary key(ID)
	);

create table book
	(ISBN			numeric(13,0),
	 title			varchar(50) not null,
	 price			numeric(6,2) not null,
	 num_pages		int not null,
	 genre			varchar(15) not null,
	 in_stock		int not null,
	 num_sold		int not null,
	 pub_id			varchar(8),
	 percent_sales	numeric(3,0),
	 primary key(ISBN),
	 foreign key (pub_id) references publisher(ID)
		on delete set null
	);

create table author
	(ID 			varchar(8),
	 name 			varchar(30) not null,
	 num_sold		int not null,
	 primary key(ID)
	);

create table wrote
	(ISBN			numeric(13, 0),
	 author_id		varchar(8),
	 primary key(ISBN, author_id),
	 foreign key(ISBN) references book(ISBN)
		on delete cascade,
	 foreign key(author_id) references author(ID)
		on delete cascade
	);

create table users
	(username		varchar(15),
	 password		varchar(15) not null,
	 province		varchar(2) not null
		check (province in ('NL', 'PE', 'NS', 'NB', 'QC', 'ON','MB', 'SK', 'AB', 'BC', 'YT', 'NT', 'NU')),
	 city 			varchar(20) not null,
	 address		varchar(50) not null,
	 postal_code	varchar(6) not null,
	 is_admin		boolean not null,
	 primary key(username)
	);

create table orders
	(ID				varchar(8),
	 username		varchar(15) not null,
	 tracking_num	varchar(10) not null, 
	 province		varchar(2) not null
		check (province in ('NL', 'PE', 'NS', 'NB', 'QC', 'ON', 'MB', 'SK', 'AB', 'BC', 'YT', 'NT', 'NU')),
	 city 			varchar(20) not null,
	 address		varchar(50) not null,
	 postal_code	varchar(6) not null,
	 order_total	numeric(8, 2) not null, --Assuming that no one spends over $999 999.99 at a book store...
	 month			numeric(2, 0) not null, check(month > 1 and month < 13),
	 year			numeric(4, 0) not null, check(year >= 2021),
	 primary key(ID),
	 foreign key(username) references users(username)
		on delete cascade
	);

create table cart
	(ISBN		numeric(13, 0),
	 order_id	varchar(8),
	 quantity	int not null,
	 primary key(ISBN, order_id),
	 foreign key(ISBN) references book(ISBN)
		on delete cascade,
	 foreign key(order_id) references orders(ID)
		on delete cascade
	);
