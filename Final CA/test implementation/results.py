import re
import math

def createLogDataStructure():
    log = dict()
    log['maxFrame'] = 0
    log['sendStrikerPosition'] = []
    log['sendBallCollision'] = []
    log['sendBallCollisionAck'] = []
    log['sendGoalScoredAck'] = []
    log['sendGoalScored'] = []
    log['receivePositionMessage'] = []
    log['receiveBallCollisionMessage'] = []
    log['receiveGoalScoredMessage'] = []
    log['receiveGoalScoredAckMessage'] = []
    log['receiveCollisionAckMessage'] = []
    return log

def createStrikerPositionLog(frame,x,y,id):
    return {
        "type" : "StrikerPosition",
        "frame" : float(frame),
        "msgId" : float(id),
        "position" : (float(x),float(y))
    }

def createBallCollisionLog(frame,x,y,vx,vy,id):
    return {
        "type" : "BallCollision",
        "frame" : float(frame),
        "msgId" : float(id),
        "position" : (float(x),float(y)),
        "velocity" : (float(vx),float(vy))
    }

def createBallCollisionAckLog(frame,id):
    return {
        "type" : "BallCollisionAck",
        "msgId" : float(id),
        "frame" : float(frame)
    }

def createGoalScoredAckLog(frame,goal,id):
    return {
        "type" : "GoalScoredAck",
        "msgId" : float(id),
        "goal" : float(goal),
        "frame" : float(frame)
    }

def createGoalScoredLog(frame,goal,id):
    return {
        "type" : "GoalScored",
        "msgId" : float(id),
        "goal" : float(goal),
        "frame" : float(frame)
    }

def is_digit(str):
    return str.lstrip('-').replace('.', '').isdigit()

def parseLine(line : str):
    items = re.split("[ #@:\n]",line)
    ret = []
    for item in items:
        if item:
            if is_digit(item):
                ret.append(float(item))
            elif is_digit(item[1:]):
                ret.append(float(item[1:]))
            else:
                ret.append(item)
    return ret

def readFile(file):
    lines = file.readlines()
    logs = createLogDataStructure()
    for line_old in lines:
        line = parseLine(line_old)
        try:
            logs['maxFrame'] = max(logs['maxFrame'],line[1])
            if line[0] == "sendStrikerPosition":
                logs[line[0]].append(createStrikerPositionLog(line[1],line[2],line[3],line[4]))
            if line[0] == "sendBallCollision":
                logs[line[0]].append(createBallCollisionLog(line[1],line[2],line[3],line[4],line[5],line[6]))
            if line[0] == "sendBallCollisionAck":
                logs[line[0]].append(createBallCollisionAckLog(line[1],line[2]))
            if line[0] == "sendGoalScoredAck":
                logs[line[0]].append(createGoalScoredAckLog(line[1],line[2],line[3]))
            if line[0] == "sendGoalScored":
                logs[line[0]].append(createGoalScoredLog(line[1],line[2],line[3]))  
            if line[0] == "receivePositionMessage":
                logs[line[0]].append(createStrikerPositionLog(line[1],line[2],line[3],line[4]))
            if line[0] == "receiveBallCollisionMessage":
                logs[line[0]].append(createBallCollisionLog(line[1],line[2],line[3],line[4],line[5],line[6]))
            if line[0] == "receiveGoalScoredMessage":
                logs[line[0]].append(createGoalScoredLog(line[1],line[2],line[3]))  
            if line[0] == "receiveGoalScoredAckMessage":
                logs[line[0]].append(createGoalScoredAckLog(line[1],line[2],line[3]))
            if line[0] == "receiveCollisionAckMessage":
                logs[line[0]].append(createBallCollisionAckLog(line[1],line[2]))
        except Exception as e:
            # print(e)
            pass
    return logs
         
def checkBallSync(logs1,logs2):
    sends1 = logs1['sendBallCollision']
    recv1 = logs1['receiveBallCollisionMessage']
    sends2 = logs2['sendBallCollision']
    recv2 = logs2['receiveBallCollisionMessage']
    maxFrame1 = logs1['maxFrame']
    dropCount1 = 0
    nonSyncTime1 = set()
    for sendMsg in sends1:
        foundMsg = None
        for recvMsg in recv2:
            if recvMsg['msgId'] == sendMsg['msgId']:
                foundMsg = recvMsg
                break
        if foundMsg == None:
            dropCount1 += 1
            continue
        nonSyncTime1 = nonSyncTime1.union(set(range(int(sendMsg['frame']),int(foundMsg['frame']))))
    maxFrame2 = logs1['maxFrame']
    dropCount2 = 0
    nonSyncTime2 = set()
    for sendMsg in sends2:
        foundMsg = None
        for recvMsg in recv1:
            if recvMsg['msgId'] == sendMsg['msgId']:
                foundMsg = recvMsg
                break
        if foundMsg == None:
            dropCount2 += 1
            continue
        nonSyncTime2 = nonSyncTime2.union(set(range(int(sendMsg['frame']),int(foundMsg['frame']))))
    nonSyncTime = nonSyncTime2.union(nonSyncTime1)
    maxFrame = maxFrame1 + maxFrame2
    dropCount = dropCount1 + dropCount2
    totalMsg = len(sends1) + len(sends2)
    print("sync perecentage = ",1 - len(nonSyncTime)/maxFrame)
    print("packet receive rate = ",1 - dropCount/totalMsg)

def checkDelay(logs1,logs2):
    sends1 = logs1['sendGoalScored']
    recv1 = logs1['receiveGoalScoredAckMessage']
    sends2 = logs2['sendGoalScored']
    recv2 = logs2['receiveGoalScoredAckMessage']
    total_delay1 = 0
    total_received1 = 0
    max_delay1 = 0
    for sendMsg in sends1:
        foundMsg = None
        for recvMsg in recv1:
            if recvMsg['goal'] == sendMsg['goal']:
                foundMsg = recvMsg
                break
        if foundMsg != None:
            total_received1 += 1
        else:
            continue
        total_delay1 += (foundMsg['frame'] - sendMsg['frame'])/2
        max_delay1 = max(max_delay1,(foundMsg['frame'] - sendMsg['frame'])/2)
    total_delay2 = 0
    total_received2 = 0
    max_delay2 = 0
    for sendMsg in sends2:
        foundMsg = None
        for recvMsg in recv2:
            if recvMsg['goal'] == sendMsg['goal']:
                foundMsg = recvMsg
                break
        if foundMsg != None:
            total_received2 += 1
        else:
            continue
        total_delay2 += (foundMsg['frame'] - sendMsg['frame'])/2
        max_delay2 = max(max_delay2,(foundMsg['frame'] - sendMsg['frame'])/2)
    print("average delay(ms) = ",(total_delay1+total_delay2)/(total_received1+total_received2)*17)
    print("max delay(ms) = ",max(max_delay2,max_delay1)*17)
    

def showResults(address):
    with open(address+"/logs.log") as file:
        logs1 = readFile(file)
    with open(address+"/logs2.log") as file:
        logs2 = readFile(file)
    checkBallSync(logs1,logs2)
    checkDelay(logs1,logs2)

print("0.5m:")
showResults("0")
print("5m:")
showResults("5")
print("10m:")
showResults("10")