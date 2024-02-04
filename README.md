-------------------- 6 --------------------
Multi-process and multi-thread are used to improve the concurrency and efficiency of the program, but they have their own advantages and disadvantages.

Multi-process:
Advantages:
1. High security. The memory space of different processes is independent. If a process breaks down, other processes are not affected.
2. Multiple cores can be used, and each process can be on a different CPU, thereby improving the concurrency and efficiency of the program.
3. Data can be exchanged through inter-process communication, such as using pipes and message queues.
Disadvantages:
1. Switching between processes is expensive because the context of the process needs to be saved and restored.
2. Inter-process communication is more expensive because data needs to be copied from one process to another.
3. Creating and destroying processes is expensive because system calls are required.

Multi-thread:
Advantages:
1. The memory space is shared between threads, which facilitates data exchange.
2. Thread switching is less expensive than process switching because only the context of the thread needs to be saved and restored.
3. Multiple CPU cores can be used, and each thread can be executed in a different CPU, thereby improving the concurrency and efficiency of the program.
Disadvantages:
1. The shared memory between threads is prone to contention and deadlock problems, and requires synchronization and mutual exclusion operations.
2. A single thread crash can cause the entire process to crash.
3. The overhead of communication between threads is high because mechanisms such as locking are required to ensure data consistency.

-------------------- 7 --------------------
Preemptive scheduling:
Advantages:
1.  This feature ensures system responsiveness. When a high-priority task appears, it can immediately preempt the resources of a low-priority task that is being executed, ensuring timely response to the high-priority task.
2.  Better utilization of system resources because preemptive scheduling provides finer control over the execution time of processes, thereby avoiding situations in which some processes occupy CPU resources for long periods of time.
Disadvantages:
1.  Increasing the complexity of the system because it needs to monitor the priority changes of processes and the allocation of time slices in real time.
2.  Increasing the overhead of the system because context switching is required, which causes a certain time and resource overhead.

Non-preemptive scheduling:
Advantages:
1.  Simple and reliable. Non-preemptive scheduling does not require real-time monitoring of process priority changes and time slice allocation, which reduces system complexity and overhead.
2.  Non-preemptive scheduling is applicable to scenarios that do not have high requirements on response time. And it ensures that the current process always occupies CPU resources, thereby avoiding the delay caused by context switching.
Disadvantages:
1.  The system response cannot be ensured. When a task with a higher priority appears, the task with a lower priority that is being executed occupies CPU resources. As a result, the task with a higher priority cannot be responded in time.
2.  System resources cannot be fully used because the execution time of processes cannot be precisely controlled. As a result, some processes may occupy CPU resources for a long time.

-------------------- 8 --------------------
Not always. Although multi-threaded can improve processing speed and efficiency, it also brings some potential problems. For example, contention between threads can lead to deadlocks, starvation, and performance degradation. In addition, multi-threaded programming requires additional overhead to manage threads and synchronize data. Therefore, in some cases, a single thread may be more efficient, especially for simple tasks or in resource-constrained environments. Ultimately, the performance advantage of the multi-threaded approach depends on the specific application scenario and implementation mode.

-------------------- 9 --------------------
Two distinctions:
1. User-level threads are managed by the user-space thread library, while kernel-level threads are managed by the operating system kernel.
2. User-level threads switching is implemented by the user program itself, and kernel-level threads switching is implemented by the operating system kernel.
Kernel-level threads are a better choice when higher concurrency and better responsiveness are required. However, when more flexibility and control are required, user-level threads are a better choice.

-------------------- 10 --------------------


-------------------- 11 --------------------
The difference between a process and a thread is that a process is an independent execution environment and has its own memory space and system resources, while a thread is an execution unit created inside a process and shares the memory space and system resources of the process. A process can contain multiple threads that can perform different tasks in parallel. In contrast, a process uses more system resources because it requires separate memory space and system resources. Threads share the memory and system resources of the process, so they use fewer resources.
The process has the following resources:
1. Independent address space.
2. Global variable.
3. File descriptor.
4. Runtime stack.
5. Process control block.
The thread has the following resources:
1. Independent execution stack.
2. Register set.
3. Shared address space.
4. Thread control block.
