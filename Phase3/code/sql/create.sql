DROP TABLE IF EXISTS Customer CASCADE;--OK
DROP TABLE IF EXISTS Mechanic CASCADE;--OK
DROP TABLE IF EXISTS Car CASCADE;--OK
DROP TABLE IF EXISTS Owns CASCADE;--OK
DROP TABLE IF EXISTS Service_Request CASCADE;--OK
DROP TABLE IF EXISTS Closed_Request CASCADE;--OK


-------------
---DOMAINS---
-------------
CREATE DOMAIN us_postal_code AS TEXT CHECK(VALUE ~ '^\d{5}$' OR VALUE ~ '^\d{5}-\d{4}$');
CREATE DOMAIN _STATUS CHAR(1) CHECK (value IN ( 'W' , 'C', 'R' ) );
CREATE DOMAIN _GENDER CHAR(1) CHECK (value IN ( 'F' , 'M' ) );
CREATE DOMAIN _CODE CHAR(2) CHECK (value IN ( 'MJ' , 'MN', 'SV' ) ); --Major, Minimum, Service
CREATE DOMAIN _PINTEGER AS int4 CHECK(VALUE > 0);
CREATE DOMAIN _PZEROINTEGER AS int4 CHECK(VALUE >= 0);
CREATE DOMAIN _YEARS AS int4 CHECK(VALUE >= 0 AND VALUE < 100);
CREATE DOMAIN _YEAR AS int4 CHECK(VALUE >= 1970);

------------
---TABLES---
------------
CREATE TABLE Customer
(
	id INTEGER NOT NULL,
	fname CHAR(32) NOT NULL,
	lname CHAR(32) NOT NULL,
	phone CHAR(13) NOT NULL,
	address CHAR(256) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE Mechanic
(
	id INTEGER NOT NULL,
	fname CHAR(32) NOT NULL,
	lname CHAR(32) NOT NULL,
	experience _YEARS NOT NULL,
	PRIMARY KEY (id) 
);

CREATE TABLE Car
(
	vin VARCHAR(16) NOT NULL,
	make VARCHAR(32) NOT NULL,
	model VARCHAR(32) NOT NULL,
	year _YEAR NOT NULL,
	PRIMARY KEY (vin)
);
---------------
---RELATIONS---
---------------
CREATE TABLE Owns
(
	ownership_id INTEGER NOT NULL,
	customer_id INTEGER NOT NULL,
	car_vin VARCHAR(16) NOT NULL,
	PRIMARY KEY (ownership_id),
	FOREIGN KEY (customer_id) REFERENCES Customer(id),
	FOREIGN KEY (car_vin) REFERENCES Car(vin)
);

CREATE TABLE Service_Request
(
	rid INTEGER NOT NULL,
	customer_id INTEGER NOT NULL,
	car_vin VARCHAR(16) NOT NULL,
	date DATE NOT NULL,
	odometer _PINTEGER NOT NULL,
	complain TEXT,
	PRIMARY KEY (rid),
	FOREIGN KEY (customer_id) REFERENCES Customer(id),
	FOREIGN KEY (car_vin) REFERENCES Car(vin)
);

CREATE TABLE Closed_Request
(
	wid INTEGER NOT NULL,
	rid INTEGER NOT NULL,
	mid INTEGER NOT NULL,
	date DATE NOT NULL,
	comment TEXT,
	bill _PINTEGER NOT NULL,
	PRIMARY KEY (wid),
	FOREIGN KEY (rid) REFERENCES Service_Request(rid),
	FOREIGN KEY (mid) REFERENCES Mechanic(id)
);

----------------------------
-- INSERT DATA STATEMENTS --
----------------------------

COPY Customer (
	id,
	fname,
	lname,
	phone,
	address
)
FROM 'customer.csv'
WITH DELIMITER ',';

COPY Mechanic (
	id,
	fname,
	lname,
	experience
)
FROM 'mechanic.csv'
WITH DELIMITER ',';

COPY Car (
	vin,
	make,
	model,
	year
)
FROM 'car.csv'
WITH DELIMITER ',';

COPY Owns (
	ownership_id,
	customer_id,
	car_vin
)
FROM 'owns.csv'
WITH DELIMITER ',';

COPY Service_Request (
	rid,
	customer_id,
	car_vin,
	date,
	odometer,
	complain
)
FROM 'service_request.csv'
WITH DELIMITER ',';

COPY Closed_Request (
	wid,
	rid,
	mid,
	date,
	comment,
	bill
)
FROM 'closed_request.csv'
WITH DELIMITER ',';


CREATE SEQUENCE rid_gen START WITH 30001;
CREATE SEQUENCE cid_gen START WITH 500;
CREATE SEQUENCE mid_gen START WITH 250;
CREATE SEQUENCE wid_gen START WITH 30001;
--Trigger for InsertServiceRequest function 

CREATE LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION createRID()
RETURNS TRIGGER AS $createRID$
BEGIN
NEW.rid := nextval('rid_gen');
RETURN NEW;
END; 
$createRID$
LANGUAGE plpgsql VOLATILE; 

DROP TRIGGER IF exists createRID on Service_Request;

CREATE TRIGGER createRID BEFORE INSERT
ON Service_Request FOR EACH ROW
EXECUTE PROCEDURE createRID(); 

--Trigger for AddCustomer function 

CREATE LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION createCID()
RETURNS TRIGGER AS $createCID$
BEGIN
NEW.id := nextval('cid_gen');
RETURN NEW;
END; 
$createCID$
LANGUAGE plpgsql VOLATILE; 

DROP TRIGGER IF exists createCID on Customer;

CREATE TRIGGER createCID BEFORE INSERT
ON Customer FOR EACH ROW
EXECUTE PROCEDURE createCID(); 

--Trigger for AddMechanic Function 

CREATE LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION createMID()
RETURNS TRIGGER AS $createMID$
BEGIN
NEW.id := nextval('mid_gen');
RETURN NEW;
END; 
$createMID$
LANGUAGE plpgsql VOLATILE; 

DROP TRIGGER IF exists createMID on Mechanic;

CREATE TRIGGER createMID BEFORE INSERT
ON Mechanic FOR EACH ROW
EXECUTE PROCEDURE createMID(); 

--Trigger for WID

CREATE LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION createWID()
RETURNS TRIGGER AS $createWID$
BEGIN
NEW.wid := nextval('wid_gen');
RETURN NEW;
END; 
$createWID$
LANGUAGE plpgsql VOLATILE; 

DROP TRIGGER IF exists createWID on Closed_Request;

CREATE TRIGGER createWID BEFORE INSERT
ON Closed_Request FOR EACH ROW
EXECUTE PROCEDURE createWID(); 