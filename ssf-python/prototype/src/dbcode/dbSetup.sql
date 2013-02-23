create database SSFDB;

use SSFDB

create table player(playerID INT NOT NULL AUTO_INCREMENT,playerName VARCHAR(30) NOT NULL,playerEmail VARCHAR(50),playerWins INT,playerLosses INT,Primary Key (playerID));
create table game(gameID INT NOT NULL AUTO_INCREMENT,p1PID INT NOT NULL,p2PID INT NOT NULL,gameDate CHAR(10) NOT NULL,winner INT,Primary Key (gameID),Foreign Key (p1PID) references player(playerID),Foreign Key (p2PID) references player(playerID));
create table moves(moveID INT NOT NULL AUTO_INCREMENT,gameID INT NOT NULL,gestureID INT NOT NULL,playerNumber INT NOT NULL,gestureTime DECIMAL(6,2) NOT NULL, Primary Key (moveID), Foreign Key (gameID) references game(gameID));
insert into player (playerName,playerEmail,playerWins,playerLosses) value('Guest', 'No-Email@No-Email.com', 0, 0);
insert into player (playerName,playerEmail,playerWins,playerLosses) value('Ryu', 'Ryu@SuperStreetFire.com', 0, 0);
insert into player (playerName,playerEmail,playerWins,playerLosses) value('Ken', 'Ken@SuperStreetFire.com', 0, 0);
