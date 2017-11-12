
create table books(booknumber varchar2(20) primary key,title varchar2(20),author varchar2(20),price number,amount int);

create table student(studentnumber varchar2(20) primary key,name varchar2(20),gender varchar2(10),major varchar2(20),discountlevel number default 0,totalPrice number default 0);

create table orders(orderid varchar2(50),studentnumber varchar2(20),orderdate date,totalprice number,paymentmethod varchar2(20),cardno varchar2(50));

create table orderbook(orderbookid varchar2(50),orderid varchar2(50),booknumber varchar2(20),deliverydate date,delivered integer);

CREATE OR REPLACE TRIGGER TR1
AFTER INSERT ON ORDERBOOK 
FOR EACH ROW
BEGIN
UPDATE BOOKS  SET AMOUNT = AMOUNT-1 WHERE BOOKNUMBER = :new.BOOKNUMBER;
END;
/

CREATE OR REPLACE TRIGGER TR2
AFTER DELETE ON ORDERBOOK 
FOR EACH ROW
BEGIN
UPDATE BOOKS  SET AMOUNT = AMOUNT+1 WHERE BOOKNUMBER = :new.BOOKNUMBER;
END;
/

CREATE OR REPLACE TRIGGER TR3
BEFORE INSERT ON ORDERS 
FOR EACH ROW
DECLARE 
ORDER_TOTAL_PRICE NUMBER;
DISCOUNT NUMBER;
BEGIN
dbms_output.put_line('tr3');
SELECT DISCOUNTLEVEL INTO DISCOUNT FROM STUDENT WHERE STUDENTNUMBER = :new.STUDENTNUMBER;
SELECT SUM(a.PRICE) INTO ORDER_TOTAL_PRICE FROM BOOKS a,ORDERBOOK b WHERE a.BOOKNUMBER=b.BOOKNUMBER and b.ORDERID=:new.ORDERID;
:new.TOTALPRICE := ORDER_TOTAL_PRICE * (1-DISCOUNT );
update student set totalPrice = totalPrice + :new.TOTALPRICE where STUDENTNUMBER = :new.STUDENTNUMBER;
END;
/

CREATE OR REPLACE TRIGGER TR4
AFTER INSERT OR DELETE ON ORDERS 
FOR EACH ROW
DECLARE 
ALL_IN_CURRENT_YEAR NUMBER;
BEGIN
dbms_output.put_line('tr4');
SELECT totalPrice INTO ALL_IN_CURRENT_YEAR FROM student WHERE STUDENTNUMBER=:new.STUDENTNUMBER;
IF(ALL_IN_CURRENT_YEAR>1000 and ALL_IN_CURRENT_YEAR<=2000) THEN
    UPDATE STUDENT  SET DISCOUNTLEVEL = 0.1 WHERE STUDENTNUMBER = :new.STUDENTNUMBER;
END IF;
IF(ALL_IN_CURRENT_YEAR>2000) THEN
    UPDATE STUDENT  SET DISCOUNTLEVEL = 0.2 WHERE STUDENTNUMBER = :new.STUDENTNUMBER;
END IF;
END;
/

CREATE OR REPLACE TRIGGER TR5
BEFORE INSERT ON ORDERS
FOR EACH ROW
BEGIN
dbms_output.put_line('tr5');
 IF (:new.PAYMENTMETHOD='by credit card' AND (:new.CARDNO='' OR :new.CARDNO is NULL)) THEN
     RAISE_APPLICATION_ERROR(-20001, ' the card no. cannot be empty if the payment method is by credit card!');
 END IF;
END;
/

insert into books VALUES ('7802117321','Pride and Prejudice','Jane Austen',399,3);
insert into books VALUES ('7802117322','THE GREAT GATSBY','F. Scott Fitzgerald',1699,4);
insert into books VALUES ('7802117323','Beartown: A Novel','Fredrik Backman',300,3);
insert into books VALUES ('7802117324','Harry Potter I','JK Roling',300,10);
insert into books VALUES ('7802117325','Harry Potter II','JK Roling',300,0);
insert into books VALUES ('7802117326','Harry Potter III','JK Roling',400,20);

insert into orders VALUES('debf95ac-236d-11e7-93ae-92361f002671','0002',sysdate,399,'card','123');
insert into orders VALUES('debf95ac-236d-11e7-93ae-92361f002672','0002',sysdate-3,600,'card','123');
insert into orders VALUES('601f38aa-236e-11e7-93ae-92361f002671','0001',sysdate,1699,'card','1234');
insert into orders VALUES('601f38aa-236e-11e7-93ae-92361f002672','0001',sysdate-8,300,'card','1234');
insert into orders VALUES('718c7f44-236e-11e7-93ae-92361f002671','0003',sysdate,300,'card','12345');

insert into orderbook VALUES('902c0f28-236e-11e7-93ae-92361f002671','debf95ac-236d-11e7-93ae-92361f002671','7802117321',sysdate,1);
insert into orderbook VALUES('902c0f28-236e-11e7-93ae-92361f002672','debf95ac-236d-11e7-93ae-92361f002672','7802117324',sysdate-3,1);
insert into orderbook VALUES('902c0f28-236e-11e7-93ae-92361f002673','debf95ac-236d-11e7-93ae-92361f002672','7802117325',null,0);
insert into orderbook VALUES('abbc8254-236e-11e7-93ae-92361f002671','601f38aa-236e-11e7-93ae-92361f002671','7802117322',sysdate,1);
insert into orderbook VALUES('abbc8254-236e-11e7-93ae-92361f002672','601f38aa-236e-11e7-93ae-92361f002672','7802117325',null,0);
insert into orderbook VALUES('bd591be4-236e-11e7-93ae-92361f002671','718c7f44-236e-11e7-93ae-92361f002671','7802117323',sysdate,1);


insert into student(studentnumber,name,discountlevel,totalPrice) values('0001','Kurt',0.1,1999);
insert into student(studentnumber,name,discountlevel,totalPrice) values('0002','Rex',0,999);
insert into student(studentnumber,name,discountlevel,totalPrice) values('0003','Jerry',0,300);