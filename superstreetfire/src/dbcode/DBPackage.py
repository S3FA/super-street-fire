import MySQLdb
import datetime

#Simple Payer Class for passing user data
class Player:
    def __init__ (self, _PlayerID, _PlayerName, _PlayerEmail, _PlayerWins, _PlayerLosses):
        self.playerID = _PlayerID
        self.playerName = _PlayerName
        self.playerEmail = _PlayerEmail
        self.playerWins = _PlayerWins
        self.playerLosses = _PlayerLosses
        
    def WinPercent (self):
    	total = self.playerWins + self.playerLosses
    	if total > 0:
    	    percent = self.playerWins / float(total)*100.0
    	    return percent
    	else:
            return 0.00
        
#Create Player takes a character name and an email address, creates a player with 0'ed stats.  Returns a player object    
def CreatePlayer(_PlayerName, _PlayerEmail):
    conn = MySQLdb.connect (host="localhost",user="root", passwd="SuperStreetFire",db="SSFDB")
    cursor = conn.cursor ()
    cursor.execute ("INSERT INTO player (playerName, playerEmail, playerWins, playerLosses) VALUES ('%s','%s',0,0)" % (_PlayerName,_PlayerEmail))
    conn.commit ()
    cursor.execute("SELECT * FROM player WHERE playerEmail = '%s'" % _PlayerEmail)
    row = cursor.fetchone()
    newPlayer = Player(row[0],row[1],row[2],row[3],row[4])
    cursor.close ()
    conn.close ()
    return newPlayer

#Get Player find player by email address. Returns player object
def GetPlayer(_PlayerEmail):
    conn = MySQLdb.connect (host="localhost",user="root", passwd="SuperStreetFire",db="SSFDB")
    cursor = conn.cursor ()
    cursor.execute("SELECT * FROM player WHERE playerEmail = '%s'" % _PlayerEmail)
    row = cursor.fetchone()
    foundPlayer = Player(row[0],row[1],row[2],row[3],row[4])
    cursor.close ()
    conn.close ()
    return foundPlayer

#Start Game, takes the player IDs for player 1 and 2.  0 is the guest account.  Returns game ID    
def StartGame(_P1ID,_P2ID):
    conn = MySQLdb.connect (host="localhost",user="root", passwd="SuperStreetFire",db="SSFDB")
    cursor = conn.cursor ()
    now = datetime.datetime.now()
    date = "%d-%02d-%02d" % (now.year,now.month,now.day)
    cursor.execute ("INSERT INTO game (p1PID,p2PID,gameDate) VALUES ('%s','%s',%s)" % (_P1ID,_P2ID,date))
    conn.commit ()
    cursor.execute("SELECT * FROM game ORDER BY gameID DESC LIMIT 1")
    row = cursor.fetchone()
    gameNumber= row[0]
    cursor.close ()
    conn.close ()
    return gameNumber

#Player Win increments the playerWins column for the specified Player ID, no return
def PlayerWin(_PID):
    conn = MySQLdb.connect (host="localhost",user="root", passwd="SuperStreetFire",db="SSFDB")
    cursor = conn.cursor ()
    cursor.execute ("UPDATE player SET playerWins = playerWins+1 WHERE playerID = %s" % (_PID))
    conn.commit ()
    cursor.close ()
    conn.close ()

#Player Fail increments the playerLosses column for the specified Player ID, no return
def PlayerFail(_PID):
    conn = MySQLdb.connect (host="localhost",user="root", passwd="SuperStreetFire",db="SSFDB")
    cursor = conn.cursor ()
    cursor.execute ("UPDATE player SET playerLosses = playerLosses+1 WHERE playerID = %s" % (_PID))
    conn.commit ()
    cursor.close ()
    conn.close ()

#End Game set the winner field in the game table, and calls the player win/player fail methods appropriately
#TODO: Flush move list to move DB
def EndGame(_GameID,_WinningPlayer):
    conn = MySQLdb.connect (host="localhost",user="root", passwd="SuperStreetFire",db="SSFDB")
    cursor = conn.cursor ()
    cursor.execute ("UPDATE game SET winner = %s WHERE gameID = %s" % (_WinningPlayer,_GameID))
    conn.commit ()
    cursor.execute("SELECT * FROM game WHERE gameID = '%s'" % _GameID)
    row = cursor.fetchone()
    P1 = row[1]
    P2 = row[2]
    cursor.close ()
    conn.close ()
    if _WinningPlayer == 1:
        PlayerWin(P1)
        PlayerFail(P2)
    else:
        PlayerWin(P2)
        PlayerFail(P1)

##
##TODO: Create record move method to store moves in a list
##

#Small main unit test for the above functions This will be pulled out into it's own file in later revisions
if __name__ == '__main__':
    PName = "Blanka"
    PEmail = "Blanka@SuperStreetFire.Com"
    nPlayer = CreatePlayer(PName,PEmail)
    print "Player Name: %s\nPlayer Email: %s\nWins: %s Losses: %s Win Prct: %.2f" % (nPlayer.playerName,nPlayer.playerEmail,nPlayer.playerWins,nPlayer.playerLosses,nPlayer.WinPercent())
    p2Player = GetPlayer("Ryu@SuperStreetFire.com")
    print "Player Name: %s\nPlayer Email: %s\nWins: %s Losses: %s Win Prct: %.2f" % (p2Player.playerName,p2Player.playerEmail,p2Player.playerWins,p2Player.playerLosses,p2Player.WinPercent())
    gameID = StartGame(nPlayer.playerID,p2Player.playerID)
    print "Game %s has started" % (gameID)
    EndGame(gameID,2)
    p2Player = GetPlayer("Ryu@SuperStreetFire.com")
    print "Player Name: %s\nPlayer Email: %s\nWins: %s Losses: %s Win Prct: %.2f" % (p2Player.playerName,p2Player.playerEmail,p2Player.playerWins,p2Player.playerLosses,p2Player.WinPercent())
    nPlayer = GetPlayer("Blanka@SuperStreetFire.com")
    print "Player Name: %s\nPlayer Email: %s\nWins: %s Losses: %s Win Prct: %.2f" % (nPlayer.playerName,nPlayer.playerEmail,nPlayer.playerWins,nPlayer.playerLosses,nPlayer.WinPercent())
