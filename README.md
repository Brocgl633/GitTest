Class "Timer"
hour  // points to current hour
minute  // points to current minute
second  // points to current second
observerList  // Class "Observer" list
precisionMap  // key:observer;value:observer's precesion
formatMap  // key:observer;value:observer's format

Operations
Trick() {
  second = second + 1
  IF (second == 60) THEN
    second = 0
    minute = minute + 1
    IF (minute == 60) THEN
      minute = 0
      hour = hour + 1
      IF (hour == 24) THEN
        hour = 0
      ENDIF
    ENDIF
  ENDIF
}

GetHour() {
  return hour
}

GetMinute() {
  return minute
}

GetSecond() {
  return second
}

AMPM() {
  hour = GetHour()
  IF (hour > 12) && (hour < 24) THEN
    return "PM"
  ELSE 
    return "AM"
  ENDIF
}

registerCurrentTime(observer) {
  precision = precisionMap.get(observer)
  format = formatMap.get(observer)
  result = getPrecisionTime(precision, format)
  observer.setTime(result)
}

getPrecisionTime(precision, format) {
  result = ""
  hour = getRealHour(format)
  IF (precision == "h") THEN
    result = hour
  ElSE IF (precision == "m") THEN
    result = hour + ":" + GetMinute()
  ELSE 
    result = hour + ":" + GetMinute() + ":" + GetSecond()
  ENDIF
  
  IF (format == "12") THEN
    result = result + AMPM()
  ENDIF
  return result
}

getRealHour(format) {
  hour = GetHour()
  IF (format == "12") THEN
    IF (hour > 12) && (hour < 24) THEN
      hour = hour - 12
    ENDIF
  ENDIF
  return hour
}

register(observer, precision, format) {
  observerList.add(observer)
  precisionMap.put(observer, precision)
  formatMap.put(observer, format)
}

unregister(observer) {
  observerList.remove(observer)
  precisionMap.remove(observer)
  formatMap.remove(observer)
}

notify() {
  i = 1
  n = observerList.size()
  while i <= n do
  begin
    observer = observerList.get(i)
    registerCurrentTime(observer)
    observer.DisplayTime()
    i = i + 1
  end
}




Class "Subject"
Operations
regester(observe, precision, format), unregester(observer), notify() are abstract operatiosn.




Class "Observer"
time  // current time of input format and precision

Operations
DisplayTime() is abstract operation.




Class "DigitalClock"
Operations
DisplayTime() {
  return time
}


Class "AnalogClock"
Operations
DisplayTime() {
  return time
}


Class "AlarmClock"
Operations
DisplayTime() {
  return time
}










