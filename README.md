A list of meta events for the MDA-EFSM:
Activate()
Start()
PayCredit()
PayCash()
Approved()
Reject()
Cancel()
StartPump()
StopPump()
BelowCurrentPrice()
AboveCurrentPrice()
SelectGas()
Receipt()
NoReceipt()



A list of meta actions for the MDA-EFSM:
StoreData()			// store the price of the gas per liter from temporary data store to price in data store
PayMsg()				// display payment method information
ejectMsg()			// display information about refusing to pay with credit
CancelMsg()			// display cancellation message with two payment methods
EjectCard()			// unable to pay with credit
DisplayMenu()			// display a menu with a list of transactions
PrintReceipt()			// print transaction invoice information
ReturnCash()			// return remaining amount
