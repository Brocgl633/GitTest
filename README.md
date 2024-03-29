A list of meta events for the MDA-EFSM:
Activate()
Start()
PayCredit()
PayCash()
Approved()
Reject()
Cancel()
Pump()
StartPump()
StopPump()
SelectGas()
Receipt()
NoReceipt()



A list of meta actions for the MDA-EFSM:
StoreData()			// store the price of the gas per liter from temporary data store to price in data store
PayMsg()				// display payment method information
RejectMsg()			// display information about refusing to pay with credit
EjectCard()			// unable to pay with credit
CancelMsg()			// display cancellation message with two payment methods
StoreCash() 		// store the cash from temporary data store to cash in data store
DisplayMenu()			// display a menu with a list of transactions
StorePrice()		// store the unit final price of specific gas type from temporary data store to price in data store
SetInitialValue()	// initialize the variable liter and total price
StopMsg()			// display stop message
PrintReceipt()			// print transaction invoice information
ReturnCash()			// return remaining amount







