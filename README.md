Class State
MDA_EFSM *mda

Activate(), Start(), PayType(int t), Reject(), Cancel(), Approved(), SelectGas(int g), StartPump(), Pump(), StopPump(), Receipt(), NoReceipt(), Continue() are abstract operations.

--------------------------------------------------------------------------------------------------------------------

Each state of the class State is shown below. According to MDA-EFSM, only the functions unique to each state need to be implemented.

--------------------------------------------------------------------------------------------------------------------

Class S0
MDA_EFSM *mda		// Output object in MDA-EFSM

Operations
Activate() {
	mda->op->storeData(); // point to data class
    mda->changeState(0); // change the state to S0
}

Start() {
	mda->op->payMsg(); // point to payMsg
    mda->changeState(1); // change the state to s1
}

--------------------------------------------------------------------------------------------------------------------

Class S1
MDA_EFSM *mda		// Output object in MDA-EFSM

Operations
PayType(int t) {
    IF t == 0 THEN            // cash
        mda->changeState(3);
        mda->op->storeCash();
        mda->op->setPayTypeFlag(0);
        mda->op->displayMenu();
    ELSE IF t == 1 THEN    // credit card
        mda->changeState(2);	// change the state to s2
    ELSE
        print("Please choose cash(0) and credit card(1) again !!!");
    END IF
}

--------------------------------------------------------------------------------------------------------------------

Class S2
MDA_EFSM *mda		// Output object in MDA-EFSM

Operations
Reject() {
    mda->op->rejectMsg();
    mda->op->ejectCard();
    mda->changeState(0);		// change the state to s0
}
	
Approved() {
    mda->op->setPayTypeFlag(1);
    mda->op->displayMenu();
    mda->changeState(3);		// change the state to s3
}

--------------------------------------------------------------------------------------------------------------------

Class S3
MDA_EFSM *mda		// Output object in MDA-EFSM

Operations
Cancel() {
    mda->op->cancelMsg();
    mda->op->returnCash();
    mda->changeState(0);		// change the state to s0
}

SelectGas(int g) {
    IF g == 1 THEN
        mda->op->setPrice(1);
        print("You chose Regular gas !!!");
    ELSE IF g == 2 THEN
        mda->op->setPrice(2);
        print("You chose Diesel gas !!!");
    ELSE IF g == 3 THEN
        mda->op->setPrice(3);
        print("You chose Premium gas !!!");
    ELSE
        print("Please select a valid gas type again !!!");
    END IF
}
	
Continue() {
    mda->changeState(4);		// change the state to s4
}

--------------------------------------------------------------------------------------------------------------------

Class S4
MDA_EFSM *mda		// Output object in MDA-EFSM

Operations
StartPump() {
    mda->op->setInitialValue();
    mda->changeState(5);		// change the state to s5
}

--------------------------------------------------------------------------------------------------------------------

Class S5
MDA_EFSM *mda		// Output object in MDA-EFSM

Operations
Pump() {
    mda->op->pumpGasUnit();
    mda->op->gasPumpedMsg();
}

StopPump() {
    mda->changeState(6);		// change the state to s6
}

--------------------------------------------------------------------------------------------------------------------

Class S6
MDA_EFSM *mda		// Output object in MDA-EFSM

Operations
Receipt() {
    mda->op->printReceipt();
    IF GasStation->ds1->w == 0 THEN		// GasStation is Driven
        mda->op->returnCash();
    END IF
    mda->changeState(0);		// change the state to s0
}

NoReceipt() {
    IF GasStation.ds1.w == 0 THEN
        mda->op->returnCash();
    END IF
	mda->changeState(0);		// change the state to s0
}

--------------------------------------------------------------------------------------------------------------------

Class PumpAbstractFactory1
Operations
StorePrices doStorePrices() {
    return new StorePrices1();
}

PayMsg doPayMsg() {
    return new PayMsg1();
}

StoreCash doStoreCash() {
    return new StoreCash1();
}

DisplayMenu doDisplayMenu() {
    return new DisplayMenu1();
}

RejectMsg doRejectMsg() {
    return new RejectMsg1();
}

SetPrice doSetPrice() {
    return new SetPrice1();
}

SetInitialValue doSetInitialValue() {
    return new SetInitialValue1();
}

PumpGasUnit doPumpGasUnit() {
    return new PumpGasUnit1();
}

GasPumpedMsg doGasPumpedMsg() {
    return new GasPumpedMsg1();
}

PrintReceipt doPrintReceipt() {
    return new PrintReceipt1();
}

CancelMsg doCancelMsg() {
    return new CancelMsg1();
}

ReturnCash doReturnCash() {
    return new ReturnCash1();
}

SetPayType doSetPayType() {
    return new SetPayType1();
}


EjectCard doEjectCard() {
    return new EjectCard1();
}

--------------------------------------------------------------------------------------------------------------------

Class PumpAbstractFactory2
Operations
StorePrices doStorePrices() {
    return new StorePrices2();
}

PayMsg doPayMsg() {
    return new PayMsg2();
}

StoreCash doStoreCash() {
    return new StoreCash2();
}

DisplayMenu doDisplayMenu() {
    return new DisplayMenu2();
}

RejectMsg doRejectMsg() {
    return new RejectMsg2();
}

SetPrice doSetPrice() {
    return new SetPrice2();
}

SetInitialValue doSetInitialValue() {
    return new SetInitialValue2();
}

PumpGasUnit doPumpGasUnit() {
    return new PumpGasUnit2();
}

GasPumpedMsg doGasPumpedMsg() {
    return new GasPumpedMsg2();
}

PrintReceipt doPrintReceipt() {
    return new PrintReceipt2();
}

CancelMsg doCancelMsg() {
    return new CancelMsg2();
}

ReturnCash doReturnCash() {
    return new ReturnCash2();
}

SetPayType doSetPayType() {
    return new SetPayType2();
}


EjectCard doEjectCard() {
    return new EjectCard2();
}

--------------------------------------------------------------------------------------------------------------------

// Class GasStation contains the PumpAbstractFactory object. When the user selects the corresponding GasPump, the PumpAbstractFactory object is referenced to the specific PumpFactory. For example, the user selects GasPump1, then paf = new PumpFactory1().
// After the GasStation points to a specific factory object, the paf here also points to the same specific factory object.
// Invoke the method of the specific factory object to obtain different specific strategy classes. Then, the specific code in the strategy class can be executed.

Class Output
PumpAbstractFactory *paf

Operations
storeData() {
    paf = GasStation->paf;
    paf->doStorePrices()->saveData();
}

payMsg() {
    paf = GasStation->paf;
    paf->doPayMsg()->showPayMsg();
}

storeCash() {
    paf = GasStation->paf;
    paf->doStoreCash()->showStoreCash();
}

setPayTypeFlag(int w) {
    paf = GasStation->paf;
    paf->doSetPayType()->showSetPayTypeFlag(w);
}

displayMenu() {
    paf = GasStation->paf;
    paf->doDisplayMenu()->showMenu();
}

rejectMsg() {
    paf = GasStation->paf;
    paf->doRejectMsg()->showRejectMsg();
}

ejectCard() {
    paf = GasStation->paf;
    paf->doEjectCard()->showEjectCard();
}

setPrice(int g) {
    paf = GasStation->paf;
    paf->doSetPrice()->showSetPrice(g);
}

setInitialValue() {
    paf = GasStation->paf;
    paf->doSetInitialValue()->showSetInitialValue();
}

pumpGasUnit() {
    paf = GasStation->paf;
    paf->doPumpGasUnit()->showPumpGasUnit();
}

gasPumpedMsg() {
    paf = GasStation->paf;
    paf->doGasPumpedMsg()->showGasPumpedMsg();
}

printReceipt() {
    paf = GasStation->paf;
    paf->doPrintReceipt()->showPrintReceipt();
}

cancelMsg() {
    paf = GasStation->paf;
    paf->doCancelMsg()->showCancelMsg();
}

returnCash() {
    paf = GasStation->paf;
    paf->doReturnCash()->showReturnCash();
}

--------------------------------------------------------------------------------------------------------------------

Class MDA_EFSM
Output *op
State *s
State[] sList = new State[7]

Operations
{
    sList[0] = new S0(this);
    sList[1] = new S1(this);
    sList[2] = new S2(this);
    sList[3] = new S3(this);
    sList[4] = new S4(this);
    sList[5] = new S5(this);
    sList[6] = new S6(this);
    s = sList[0]
}

changeState(int state) {
    s = sList[state];
}

Activate() {
    s->Activate();
}

Start() {
    s->Start();
}

PayType(int t) {
    s->PayType(t);
}

Reject() {
    s->Reject();
}

Cancel() {
    s->Cancel();
}

Approved() {
    s->Approved();
}

SelectGas(int g) {
    s->SelectGas(g);
}

StartPump() {
    s->StartPump();
}

Pump() {
    s->Pump();
}

StopPump() {
    s->StopPump();
}

Receipt() {
    s->Receipt();
}

NoReceipt() {
    s->NoReceipt();
}

Continue() {
    s->Continue();
}

--------------------------------------------------------------------------------------------------------------------

Class GasPump1
MDA_EFSM *m
DS1 *ds1

Operations
Activate(int a) {
    IF a > 0 THEN
        ds1->setTemp_a(a);
        m->Activate();
    END IF
}

Start() {
    m->Start();
}

PayCash(int c) {
    IF c > 0 THEN
        ds1->setTemp_c(c);
        m->PayType(0);
    END IF
}

PayCredit() {
    m->PayType(1);
}

Reject() {
    m->Reject();
}

Cancel() {
    m->Cancel();
}

Approved() {
    m->Approved();
}

StartPump() {
    m->Continue();
    m->StartPump();
}

Pump() {
    IF GasStation->ds1->getW() == 1 THEN
        m->Pump();
    ELSE IF GasStation->ds1->getCash() < GasStation->ds1->getPrice() * (GasStation->ds1->getL() + 1) THEN
        m->StopPump();
        m->Receipt();
    ELSE
        m->Pump();
    END IF
}

StopPump() {
    m->StopPump();
    m->Receipt();
}

--------------------------------------------------------------------------------------------------------------------

Class GasPump2
MDA_EFSM *m
DS2 *ds2

Operations
Activate(float a, float b, float c) {
    IF a > 0 && b > 0 && c > 0 THEN
        ds2->setTemp_a(a);
        ds2->setTemp_b(b);
        ds2->setTemp_c(c);
        m->Activate();
    END IF
}

PayCash(int c) {
    IF c > 0 THEN
        ds2->setTemp_cash(c);
        m->PayType(0);
    END IF
}

Start() {
    m->Start();
}

Cancel() {
    m->Cancel();
}

Diesel() {
    m->SelectGas(2);
    m->Continue();
}

Premium() {
    m->SelectGas(3);
    m->Continue();
}

Regular() {
    m->SelectGas(1);
    m->Continue();
}

StartPump() {
    m->StartPump();
}

PumpGallon() {
    IF GasStation->ds2->getCash() < GasStation->ds2->getPrice() * (GasStation->ds2->getG() + 1) THEN
        m->StopPump();
    ELSE
        m->Pump();
    END IF
}

Stop() {
    m->StopPump();
}

Receipt() {
    m->Receipt();
}

NoReceipt() {
    m->NoReceipt();
}

--------------------------------------------------------------------------------------------------------------------

The interfaces and implementation classes in the Strategy package are invoked by different specific factories. Each implementation class implements different execution logic corresponding to GasPump1 and GasPump2. Due to excessive content, see the source-code folder for details.



