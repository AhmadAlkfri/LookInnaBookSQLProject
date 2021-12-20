insert into publisher
values(1, 'HARPERCOLLINS', 'harper.collins@hc.com', 'ON', 'Ottawa', '11 Publisher St.', 'K2K3X3', 1313131313);

insert into book 
values(9780261102354, 'The Fellowship Of The Ring', 10.99, 488, 'Fantasy', 10, 0, 1, 15);

insert into author
values(1, 'J. R. R. Tolkein', 0);

insert into wrote
values(9780261102354, 1);

insert into publisher
values(2, 'Faber & Faber', 'faber.faber@ff.com', 'BC', 'Victoria', '16 Publisher St.', 'V0V1X1', 1212121212);

insert into book 
values(9780571084838, 'Lord Of The Flies', 14.50, 240, 'Fiction', 10, 0, 2, 25);

insert into author
values(2, 'William Golding', 0);

insert into wrote
values(9780571084838, 2);

insert into users
values('user1', 'user1', 'ON', 'Ottawa', '188 User Crecent', 'K2K3S3', false);

insert into users
values('admin', 'admin', 'ON', 'Ottawa', '188 Admin Crecent', 'K2K3S3', true);