Class next_day_component:
next_day *u 		// a pointer to the next_day object
voting *v 			// a pointer to the voting object

void next_day(in: int month, int day, int year; out: int month1, int day1, int year1) {
	Date[] is a {int, int, int} list 		// {integer month1, integer day1, integer year1}
	next_day d[]
	d[1] = new next_day_1()
	d[2] = new next_day_2()
	d[3] = new next_day_3()
	For i = 1 to 3
		d[i]->next_day(month, day, year, month1, day1, year1)
		Date[i] = {month1, day1, year1}
	End For
	{month1, day1, year1} = v->vote(Date)
}

Class voting:
{int, int, int} vote(in: Date){
	If Date[1] == Date[2] Then
		return Date[1] 
	Else If Date[2] == Date[3] Then
		return Date[2]
	Else If Date[1] == Date[3] Then
		return Date[3] 
	ENDIF
	r = Random(1,3) 	// generate random number between 1 to 3
	return Date[r] 		// randomly select one from LU and return
}



























Class next_day_component:
next_day *u 			// a pointer to the next_day object
acceptance_test *at 	// a pointer to the acceptance_test object

void next_day(in: int month, int day, int year; out: int month1, int day1, int year1) {
	Date[] is a {int, int, int} list 		// {integer month1, integer day1, integer year1}
	next_day d[]
	
	d[1] = new next_day_1()
	d[1]->next_day(month, day, year, month1, day1, year1)
	Date[1] = {month1, day1, year1}
	testResult = at->test(month, day, year, month1, day1, year1)
	If testResult == true Then
		exit; 
	End If
	
	d[2] = new next_day_2()
	d[2]->next_day(month, day, year, month1, day1, year1)
	Date[2] = {month1, day1, year1}
	testResult = at->test(month, day, year, month1, day1, year1)
	If testResult == true Then
		exit; 
	End If
	
	d[3] = new next_day_3()
	d[3]->next_day(month, day, year, month1, day1, year1)
	Date[3] = {month1, day1, year1}
	testResult = at->test(month, day, year, month1, day1, year1)
	If testResult == true Then
		exit; 
	End If
	
	// if all tests are false
	r = Random(1,3) 		// generate random number between 1 to 3
	{month1, day1, year1} = Date[r] 		//randomly select one from LU
}

Class acceptance_test:
boolean test(in: int month, int day, int year; out: int month1, int day1, int year1) {

	// determine whether it is a leap year
    boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
    boolean isLeapYear1 = (year1 % 4 == 0 && year1 % 100 != 0) || year1 % 400 == 0

    // store the number of days in each month
    int[] daysInMonth = {31, isLeapYear ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}
    int[] daysInMonth1 = {31, isLeapYear1 ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}

    // convert a date to a number of days
    int days = day
    For i = 0 to month - 2
        days += daysInMonth[i]
	End For
	
    int days1 = day1
    For i = 0 to month1 - 2
        days1 += daysInMonth1[i]
    End For

    // calculate date difference
    int diff = (year1 - year) * 365 + (days1 - days)
    If isLeapYear && month > 2 Then
        diff++;
	End If
	
    If isLeapYear1 && month1 <= 2 Then
        diff++;
    End If

    return diff == 1
}





