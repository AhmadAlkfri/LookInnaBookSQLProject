create view bestselling_authors as
select name, num_sold
from author
order by num_sold DESC;

create view bestselling_books as
select title, num_sold
from book
order by num_sold DESC;