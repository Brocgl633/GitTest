Class Request
string op_signature



Class Request1
string p1
int p2
int p3



Class Request2
string p1
int p2



Class Request3
string p1
int result



Class Request4
string p1
float result



Class ClientProxy_A
Operations
void Service1(string x, int y, int z) {
	r = new Request1
	r->p1 = x
	r->p2 = y
	r->p3 = z
	r->op_signature = "void Service1(string, int, int)"
	brk->ForwardService(r)
}

void Service2(string x, int y) {
	r = new Request2
	r->p1 = x
	r->p2 = y
	r->op_signature = "void Service2(string, int)"
	brk->ForwardService(r)
}

int Service3(string x) {
	r = new Request3
	r->p1 = x
	r->op_signature = "int Service3(string)"
	brk->ForwardService(r)
	return r->result
}

float Service4(string x) {
	r = new Request4
	r->p1 = x
	r->op_signature = "float Service4(string)"
	brk->ForwardService(r)\
	return r->result
}



Class Broker
ServerProxy *sp
ServerProxy spList[][]
Operations
Register(ServerProxy sp, string signature) {
	add sp to spList
	add signature to spList[sp]
}

Unregister(ServerProxy sp) {
	remove sp from spList
}

FindServerProxy(string op_signature) {
	For every svrproxy in spList
		If spList[svrproxy] contains op_signature Then
			return svrproxy
		End If
	End For
}

ForwardService(Request r) {
	sp = FindServerProxy(r->op_signature)
	If sp != NULL Then
		sp->callService(r)
	End If
}



Class ServerProxy_S1
server-S1 *svr
Operations
callService(Request r) {
	switch(r->op_signature)
		case "void Service1(string, int, int)"
			svr->Service1(r->p1, r->p2, r->p3)
		case "void Service2(string, int)"
			svr->Service2(r->p1, r->p2)
		case "int Service3(string)"
			r->result = svr->Service3(r->p1)
		case "float Service4(string)"
			r->result = svr->Service4(r->p1)
}

Class ServerProxy
Broker *brk
Operations
callService(Request r) is an abstract operation


