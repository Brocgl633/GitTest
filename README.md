------------------------------------------- Centralized

Class "Account"
S  // points to current state object
LS[0]  // points to "Start" Object
LS[1]  // points to "Idle" Object
LS[2]  // points to "CheckPin" Object
LS[3]  // points to "Ready" Object
LS[4]  // points to "Overdrawn" Object

S = LS[0]  // initialize state object to "Start"

Operations
open(int x, int y, int z) {
  S->open(int x, int y, int z)
  IF (S->getStateId() == 0) THEN
    S = LS[1]
  ENDIF
}

login(int x) {
  S->login(int x)
  IF (S->getStateId() == 1) THEN
    IF (S->getLoginFlag()) THEN
      S = LS[2]
    ENDIF
  ENDIF
}

logout() {
  S->logout()
  stateId = S->getStateId()
  IF (stateId == 2) || (stateId == 3) || (stateId == 4) THEN
    S = LS[1]
  ENDIF
}

pin(int x) {
  S->pin(int x)
  IF (S->getStateId() == 2) THEN
    IF (S->getPinFlagToIdle()) THEN
      S = LS[1]
    ELSE IF (S->getPinFlagToReady()) THEN
      S = LS[3]
    ELSE IF (S->getPinFlagToOverdrawn()) THEN
      S = LS[4]
    ENDIF
  ENDIF
}

deposit(int d) {
  S->deposit(int d)
  IF (S->getStateId() == 4) THEN
    IF (S->getDepositFlag()) THEN
      S = LS[3]
    ENDIF
  ENDIF
}

withdraw(int w) {
  S->withdraw(int w)
  IF (S->getStateId() == 3) THEN
    IF (S->getWithdrawFlag()) THEN
      S = LS[4]
    ENDIF
  ENDIF
}

balance() {
  S->balance()
}





Class "AccountData"
b    // balance value
pn  // pin number
id   // account id
attempts // attemptation times
loginFlag  // whether change the state from "Idle" to "CheckPin"
depositFlag  // whether change the state from "Overdrawn" to "Ready"
withdrawFlag  // whether change the state from "Ready" to "Overdrawn"
pinFlagToIdle  // whether change the state from "CheckPin" to "Idle"
pinFlagToReady  // whether change the state from "CheckPin" to "Ready"
pinFlagToOverdrawn  // whether change the state from "CheckPin" to "Overdrawn"

getB() { return b }
setB(x) { b = x }
getPn() { return pn }
setPn(x) { pn = x }
getId() { return id }
setId(x) { id = x }
getAttempts() { return attempts }
setAttempts(x) { attempts = x }
getLoginFlag() { return loginFlag }
setLoginFlag(flag) { loginFlag = flag }
getDepositFlag() { return depositFlag }
setDepositFlag(flag) { depositFlag = flag }
getWithdrawFlag() { return withdrawFlag }
setWithdrawFlag(flag) { withdrawFlag = flag }
getPinFlagToIdle() { return pinFlagToIdle }
setPinFlagToIdle(flag) { pinFlagToIdle = flag }
getPinFlagToReady() { return pinFlagToReady }
setPinFlagToReady(flag) { pinFlagToReady = flag }
getPinFlagToOverdrawn() { return pinFlagToOverdrawn }
setPinFlagToOverdrawn(flag) { pinFlagToOverdrawn = flag }





Class "AccountState"
Operations
open(int x, int y, int z), login(int x), logout(), pin(int x), deposit(int d), withdraw(int w), balance(), getStateId() are abstract operatiosn.



Class "Start"
stateId = 0	// state of "Start" is 0

Operations
open(int x, int y, int z) {
  data->setB(x)
  data->setPn(y)
  data->setId(z)
  data->setLoginFlag(true)
  data->setDepositFlag(false)
  data->setWithdrawFlag(false)
  data->setPinFlagToIdle(false)
  data->setPinFlagToReady(false)
  data->setPinFlagToOverdrawn(false)
}

getStateId() {
  return stateId
}



Class "Idle"
stateId = 1	// state of "Idle" is 1

Operations
login(int x) {
  IF (x == data->getId()) THEN
    data->setAttempts(0)
    data->setLoginFlag(true)
  ENDIF
}

getStateId() {
  return stateId
}




Class "CheckPin"
stateId = 2	// state of "CheckPin" is 2

Operations
pin(int x) {
  IF (x != data->getPn())) THEN
    numbersOfAttempts = data->getAttempts()
    IF (numbersOfAttempts < 2) THEN
      numbersOfAttempts = numbersOfAttempts + 1
      data->setAttempts(numbersOfAttempts)
    ELSE IF (numbersOfAttempts == 2) THEN
      data->setPinFlagToIdle(true)
  ELSE
    IF ((data->getB() < 100) THEN
      data->setPinFlagToOverdrawn(true)
    ELSE
      data->setPinFlagToReady(true)
  ENDIF
}

logout() {
  
}

getStateId() {
  return stateId
}




Class "Ready"
stateId = 3	// state of "Ready" is 3

Operations
deposit(int d) {
  numbersOfBalance = data->getB()
  numbersOfBalance = numbersOfBalance + d
  data->setB(numbersOfBalance)
}

withdraw(int w) {
  numbersOfBalance = data->getB()
  IF (numbersOfBalance - w >= 100) THEN
    numbersOfBalance = numbersOfBalance - w
  ELSE
    IF (numbersOfBalance - w > 0) THEN
      numbersOfBalance = numbersOfBalance - w - 10
      data->setWithdrawFlag( true)
  data->setB(numbersOfBalance)
}

balance() {
  return data->getB()
}

logout() {
  
}

getStateId() {
  return stateId
}


Class "Ready"
stateId = 3	// state of "Ready" is 3

Operations
deposit(int d) {
  numbersOfBalance = data->getB()
  numbersOfBalance = numbersOfBalance + d
  data->setB(numbersOfBalance)
}

withdraw(int w) {
  numbersOfBalance = data->getB()
  IF (numbersOfBalance - w >= 100) THEN
    numbersOfBalance = numbersOfBalance - w
  ELSE
    IF (numbersOfBalance - w > 0) THEN
      numbersOfBalance = numbersOfBalance - w - 10
      data->setWithdrawFlag( true)
  data->setB(numbersOfBalance)
}

balance() {
  return data->getB()
}

logout() {
  
}

getStateId() {
  return stateId
}




AccountData
-b
-pn
-id
-attempts
-loginFlag
-depositFlag
-withdrawFlag
-pinFlagToIdle
-pinFlagToReady
-pinFlagToOverdrawn

+getB()
+setB(x)
+getPn()
+setPn(x)
+getId()
+setId(x)
+getAttempts()
+setAttempts(x)
+getLoginFlag()
+setLoginFlag(flag)
+getDepositFlag()
+setDepositFlag(flag)
+getWithdrawFlag()
+setWithdrawFlag(flag)
+getPinFlagToIdle()
+setPinFlagToIdle(flag)
+getPinFlagToReady()
+setPinFlagToReady(flag)
+getPinFlagToOverdrawn()
+setPinFlagToOverdrawn(flag)












------------------------------------------- de-Centralized
Class "Account"
S  // points to current state object
LS[0]  // points to "Start" Object
LS[1]  // points to "Idle" Object
LS[2]  // points to "CheckPin" Object
LS[3]  // points to "Ready" Object
LS[4]  // points to "Overdrawn" Object

S = LS[0]  // initialize state object to "Start"

Operations
changeState(ID) {
  S = LS[ID]
}

open(int x, int y, int z) {
  S->open(int x, int y, int z)
}

login(int x) {
  S->login(int x)
}

logout() {
  S->logout()
}

pin(int x) {
  S->pin(int x)
}

deposit(int d) {
  S->deposit(int d)
}

withdraw(int w) {
  S->withdraw(int w)
}

balance() {
  S->balance()
}




Class "AccountData"
b    // balance value
pn  // pin number
id   // account id
attempts // attemptation times

getB() {
  return b
}

setB(x) {
  b = x
}

getPn() {
  return pn
}

setPn(x) {
  pn = x
}

getId() {
  return id
}

setId(x) {
  id = x
}

getAttempts() {
  return attempts
}

setAttempts(x) {
  attempts = x
}




Class "AccountState"
Operations
open(int x, int y, int z), login(int x), logout(), pin(int x), deposit(int d), withdraw(int w), balance() are abstract operatiosn.




Class "Start"
Operations
open(int x, int y, int z) {
  data->setB(x)
  data->setPn(y)
  data->setId(z)
  account->changeState(1)
}



Class "Idle"
Operations
login(int x) {
  IF (x == data->getId()) THEN
    data->setAttempts(0)
    account->changeState(2)
  ENDIF
}




Class "CheckPin"
Operations
pin(int x) {
  IF (x != data->getPn())) THEN
    numbersOfAttempts = data->getAttempts()
    IF (numbersOfAttempts < 2) THEN
      numbersOfAttempts = numbersOfAttempts + 1
      data->setAttempts(numbersOfAttempts)
    ELSE IF (numbersOfAttempts == 2) THEN
      account->changeState(1)
  ELSE
    IF ((data->getB() < 100) THEN
      account->changeState(4)
    ELSE
      account->changeState(3)
  ENDIF
}

logout() {
  account->changeState(1)
}




Class "Ready"
Operations
deposit(int d) {
  numbersOfBalance = data->getB()
  numbersOfBalance = numbersOfBalance + d
  data->setB(numbersOfBalance)
}

withdraw(int w) {
  numbersOfBalance = data->getB()
  IF (numbersOfBalance - w >= 100) THEN
    numbersOfBalance = numbersOfBalance - w
  ELSE
    IF (numbersOfBalance - w > 0) THEN
      numbersOfBalance = numbersOfBalance - w - 10
      account->changeState(4)
  data->setB(numbersOfBalance)
}

balance() {
  return data->getB()
}

logout() {
  account->changeState(1)
}



Class "OverDrawn"
Operations
deposit(int d) {
  numbersOfBalance = data->getB()
  IF (numbersOfBalance + d >= 100) THEN
    numbersOfBalance = numbersOfBalance + d
    account->changeState(3)
  ELSE
    numbersOfBalance = numbersOfBalance + d - 10
  data->setB(numbersOfBalance)
}

balance() {
  return data->getB()
}

logout() {
  account->changeState(1)
}



AccountData
-b
-pn
-id
-attempts

+getB()
+setB(x)
+getPn()
+setPn(x)
+getId()
+setId(x)
+getAttempts()
+setAttempts(x)
