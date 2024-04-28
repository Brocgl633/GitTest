Class Filter#1
Pipe#1 *p1

ReadStudentAnswer() {
	reads student's test answers together with student's IDs into SA/ID
	p1.write(SA/ID)
}



Class Pipe#1
buffer

SA/ID read() {
	If buffer != NULL Then
		buffer.pop()
	End If
}

write(SA/ID) {
	buffer.push(SA)
}



Class Filter#2
Pipe#2 *p2

ReadStudentAnswer() {
	reads correct answers for the test into CA
	p2.write(CA)
}


Class Pipe#2
buffer

CA read() {
	If buffer != NULL Then
		buffer.pop()
	End If
}

write(CA) {
	buffer.push(CA)
}



Class Filter#3
SA	// student's test answers together with student's IDs
CA 	// correct answers for the test
SC/ID	// student's test scores with student's ID
flagCA = false
flagSA = false
Pipe#2 *p2
Pipe#3 *p3

writeSA(SA) {
	SA = p2.read()
	If flagCA == true Then
		computeStudentTestScore(SA,CA)
		flagSA = false
		flagCA = false
	Else
		flagSA = true
	End If
}

writeCA(CA) {
	SA = p2.read()
	If flagSA == true Then
		computeStudentTestScore(SA,CA)
		flagSA = false
		flagCA = false
	Else
		flagCA = true
	End If
}

computeStudentTestScore(SA,CA) {
	grade student answers with student ID into SC/ID
	p3.process(SC/ID)
}



Class Pipe#3
Filter#4 *p4

process(SC/ID) {
	p4.write(SC/ID)
}



Class Filter#4
SC/ID	// student's test scores with student's ID

wrtie(SC/ID) {
	store into SC/ID
}

sortScores(SC/ID) {
	sort student scores with ID
}

printTestScores() {
	sortScores(SC/ID)
	If SC/ID != NULL Then
		print SC/ID
	End If
}

















Class searching:
search *s // a pointer to the search object
acceptance_test *at // a pointer to the acceptance_test object

void search(in int n, int L[], int x, int y; out int m) {
	LS[] is a { int } list // {the number of valid elements in L}
	search s[]
	
	s[1] = new search_1()
	s[1]-> search(n, L[], x, y, m)
	LS[1] = {m}
	testResult = at->test(n, L[], x, y, m)
	If testResult == true Then
		exit; 
	End If
	
	s[2] = new search_2()
	s[2]-> search(n, L[], x, y, m)
	LS[2] = {m}
	testResult = at->test(n, L[], x, y, m)
	If testResult == true Then
		exit; 
	End If
	
	s[3] = new search_3()
	s[3]-> search(n, L[], x, y, m)
	LS[3] = {m}
	testResult = at->test(n, L[], x, y, m)
	If testResult == true Then
		exit; 
	End If
	
	// if all tests are false
	r = Random(1,3) // generate random number between 1 to 3
	{m} = LS[r] //randomly select one from LU
}

Class acceptance_test:
boolean test(in int n, int L[], int x, int y; out int m) {
	IF x > y Then
		return false
	End If
	
	int count = 0
	For i=0 to n-1:
		IF x <= L[i] and L[i] <= y Then
			count++
		End If
	End For
	
	IF count == m Then
		return true
	Else
		return false
	End If
}



















