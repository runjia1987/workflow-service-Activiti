create table wf_process (
	innerProcessId varchar(64) primary key not null,
	outerSequenceId varchar(128) not null,
	sourceSystem varchar(64) not null,
	processModelKey varchar(255) not null,
	acceptTimeStamp datetime not null,
	status varchar(32) not null,
	initUser varchar(255),
	processDetail varchar(4096) not null
);
