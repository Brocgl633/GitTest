### CS587 - Midterm Exam Answers

**Q1. What is the purpose of the work package? Who will create it?**

A work package is a detailed deliverable or project work component at the lowest level of the Work Breakdown Structure (WBS). It serves multiple purposes:
- **Breaks down tasks:** It divides the overall project into manageable sections.
- **Defines scope:** It provides a clear description of what is to be done.
- **Facilitates scheduling and budgeting:** Each work package has its own timeline and budget, helping in the overall project planning.
- **Assigns responsibilities:** It identifies who will perform the work, often specifying the project team members or departments responsible.

Typically, work packages are created by the project manager in collaboration with team leads and subject matter experts to ensure accurate and feasible task breakdowns.

**Q2. Explain the difference between reviews and audits within the context of software project management and the software development process.**

- **Reviews:** These are evaluations of project activities or deliverables conducted internally within the project team. Reviews aim to identify defects, verify conformance to requirements, and ensure that the project is on track. Types include peer reviews, design reviews, code reviews, and formal inspections.

- **Audits:** These are formal examinations conducted by external parties (e.g., quality assurance teams or external auditors) to ensure compliance with standards, policies, and procedures. Audits assess the effectiveness of the processes and their implementation within the project.

**Q3. When building the project network diagram, can we schedule the testing phase to start after the design phase? Explain.**

Yes, we can schedule the testing phase to start after the design phase. The project network diagram typically follows the sequence of project phases:
- **Design Phase:** This includes high-level design (HLD) and low-level design (LLD) where the architecture and detailed system components are defined.
- **Testing Phase:** Testing ensures that the developed system meets the specified requirements and is free of defects.

However, it's crucial to allow for overlaps or iterations between phases to accommodate design changes identified during testing. This approach is often seen in iterative and agile methodologies.

**Q4. For a software development organization that is CMM level-3, which method can be used for estimating activity effort/duration? Explain.**

At CMM level-3 (Defined), organizations use standardized processes and methodologies across projects. Estimation methods include:
- **Function Point Analysis (FPA):** Measures the functionality delivered to the user, allowing effort estimation based on the complexity and size of the project.
- **COCOMO (Constructive Cost Model):** Uses project parameters to estimate effort, duration, and cost. It factors in the size of the software and adjusts for project-specific attributes.

These methods are chosen for their ability to provide accurate estimates based on historical data and defined processes, aligning with the organization's maturity level.

**Q5. Who controls the design review meeting? What are the different metrics collected in the requirements review meeting?**

- **Control of Design Review Meeting:** The design review meeting is typically controlled by the project manager or a designated review leader. This person ensures that the meeting follows the agenda, stays on track, and that all necessary topics are covered.

- **Metrics Collected in Requirements Review Meeting:**
  - Number of requirements reviewed
  - Number of defects found per requirement
  - Severity and priority of defects
  - Time spent on review
  - Effort required for rework

**Q6. From the perspective of software project management, software artifact review/inspection is only one aspect to ensure the quality of the software produced. Explain.**

Software artifact reviews/inspections are essential for identifying defects early in the development process. However, they are part of a broader quality assurance strategy, which includes:
- **Testing:** Unit, integration, system, and acceptance testing to validate functionality and performance.
- **Process adherence:** Ensuring compliance with defined processes and standards.
- **Configuration management:** Managing changes to software artifacts to maintain consistency.
- **Metrics and continuous improvement:** Collecting data on defects, process efficiency, and project performance to identify areas for improvement.

**Q7. What are the constraints that may influence whether we can partition an activity or not?**

Constraints include:
- **Activity interdependencies:** Activities that are highly interdependent may be challenging to partition without affecting the sequence and coordination.
- **Resource availability:** Limited availability of skilled resources can restrict the ability to divide tasks.
- **Complexity and nature of the task:** Some tasks are inherently complex and cannot be easily divided without losing coherence.
- **Timeline:** The project schedule might not allow additional time required for coordinating partitioned activities.

**Q8. Possible actions for code inspection outcomes:**

1. **Rework and bug fixes require more than 50% of original effort:**
   - Conduct root cause analysis to identify underlying issues.
   - Reevaluate and possibly redesign the affected components.
   - Increase focus on initial design and code reviews to prevent recurrence.

2. **Rework and bug fixes require 15% to 20% of original effort:**
   - Schedule rework and address the identified defects.
   - Review and adjust the development and review processes to reduce defect rates.

3. **Rework and bug fixes require less than 5% of original effort:**
   - Complete the rework promptly.
   - Consider the process effective and continue with current practices, while still seeking minor improvements.

**Q9. Milestone trend chart:**

(http://image.huawei.com/tiny-lts/v1/images/076768890077fbdce038645de137feca_860x760.png)
From the milestone trend chart, we can see that seven successive data points above the planned milestone data, indicating a trend of early completions.

**Q10. Effort and duration calculation for tasks:**
| Tasks  | Amount of Work  | Productivity  | Effort  | Duration  | Resources  |
| ------------ | ------------ | ------------ | ------------ | ------------ | ------------ |
| **1 High Level Design (HLD)**  |   |   |   |   |   |
| 1.1 Write HLD Document  | 176 pages  | 2 page/Hour  | 88  | 29.33  | 3  |
| 1.2 Review HLD Document  |   |   |   |   |   |
| 1.2.1 Preparation for HLD Document  |   | 4 pages/Hour  | 44  | 11  | 4  |
| 1.2.2 Review Meeting |   | 6 pages/Hour  | 29.33  | 5.87  | 5  |
| 1.3 Rework | 43 defects  | 5 defect/Hour  | 8.6  | 2.87  | 3  |
| **2 Low Level Design (LLD)**  |   |   |   |   |   |
| 2.1 Write LLD Document | 76 pages  | 2 pages/Hour  | 38  | 19  | 2  |
| 2.2 Review LLD Document |   |   |   |   |   |
| 2.2.1 Preparation for LLD Document |   | 3 pages/Hour  | 25.33  | 6.33  | 4  |
| 2.2.2 Review Meeting |   | 6 pages/Hour  | 16.67  | 3.33  | 5  |
| 2.3 Rework | 43 defects  | 1 defect/Hour  | 43  | 21.5  | 2  |
| **3 Testing**  |   |   |   |   |   |
| 3.1 Write Test Plan | 102 pages  | 5 pages/Hour  | 20.4  | 10.2  | 2  |
| 3.2 Review Test Plan |   |   |   |   |   |
| 3.2.1 Preparation for Test Plan  |   | 10 pages/Hour  | 10.2  | 2.55  | 4  |
| 3.2.2 Review Meeting  |   | 15 pages/Hour  | 6.8  | 1.36  | 5  |
| 3.3 Rework | 75 defects  | 5 defects/Hour  | 15  | 7.5  | 2  |
