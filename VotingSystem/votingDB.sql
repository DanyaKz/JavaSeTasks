create database votingDataBase;
use votingDataBase;

CREATE TABLE users(
	id integer primary key auto_increment,
    name varchar (50),
    login varchar (50),
    us_password varchar (50));
CREATE TABLE selections(
	id integer primary key auto_increment,
	name varchar (200),
    start_time datetime, 
    end_time datetime);
CREATE TABLE candidates ( 
	id integer primary key auto_increment, 
    name varchar (150), 
    voices integer, 
    selection_id integer,
	foreign key (selection_id) references selection(id));
CREATE TABLE winner(
	id integer primary key auto_increment,
    candidate_id integer, 
    selection_id integer,
	foreign key (candidate_id) references candidates (id), 
    foreign key (selection_id) references selections(id));
    
alter table selections 
modify column start_time datetime default now();


drop trigger setSelectEndTime;
delimiter $$
create trigger setSelectEndTime
	before update on selections
	for each row 
	begin  
		if not (new.isDone <=> old.isDone) then
			set new.end_time = now();
		end if;
end $$ delimiter ; 

alter table selections 
add column isDone bool default false;
alter table users 
modify column login varchar(50) unique;
alter table candidates
modify column voices int default 0;  
alter table users 
add column isSuperUser bool default 0;
alter table users 
add column isVoted bool default 0;

delimiter $$
create procedure setVote(candidateName varchar(150), selectionId int, userLogin varchar(50))
begin  
	set @updVoices =  (
		select c1.voices + 1 
			from candidates c1 
			where c1.name = candidateName and c1.selection_id = selectionId);
	update candidates c 
		set c.voices = @updVoices
		where c.name = candidateName and c.selection_id = selectionId;
        
	update users u
		set u.isVoted = 1
		where u.login = userLogin;
end $$ delimiter ;

delimiter $$
create procedure endVoting()
begin 
	update selections s , (select id from selections
	order by id desc limit 1) sub  set s.isDone = 1 
	where s.id = sub.id;
    update users set isVoted = 0 where isVoted = 1;
end $$ delimiter ;

