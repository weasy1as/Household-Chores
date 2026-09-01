Kitchen Rotation — Project Specification
1. Product idea
   A private, mobile-friendly web application for your household to manage the daily responsibility of cleaning the kitchen.
   The household currently manages the rotation from memory, which causes problems:
   People forget whose turn it is.
   Someone may discover too late that they were responsible.
   People need to ask others who is supposed to clean.
   Switching/covering arrangements can be forgotten.
   There is no reliable history of what happened.
   Changes to the rotation are difficult to manage manually.
   The application becomes the household's single source of truth for:
   Who is responsible, what happened, and what happens next.
   It will initially be a website/PWA, rather than a native mobile application, so household members can save it to their phone's home screen.

2. Users and authentication
   Accounts
   Each person has their own account.
   Account information:
   User
   ├── id
   ├── username
   ├── passwordHash
   └── displayName
   For MVP:
   Username + password
   No email requirement
   One account per person
   Users stay logged in on their devices
   Passwords are never stored directly; only secure password hashes are stored
   There is no need to support a person belonging to multiple households right now.
   This is a single-household application for your home, although we should structure the code cleanly enough that expanding it later isn't impossible.

3. Household
   One person creates the household.
   That person becomes:
   Owner
   The owner is still a normal participant in the kitchen rotation.
   The only difference is that they have administrative privileges.
   Example:
   Household

Owner / Member
Member
Member
Member
The owner can:
Add members
Remove members
Activate/deactivate members
Change the rotation
Choose the starting person
Review duties
Approve/override outcomes
Correct historical outcomes
Normal members cannot perform those administrative operations.

4. Joining the household
   For MVP, keep joining simple.
   The owner creates the household and receives a household invite code.
   A new person:
   Create account
   ↓
   Enter household code
   ↓
   Join household
   The owner can then arrange the member's position in the rotation.
   No email invitation system is necessary for MVP.

5. Initial household setup
   When creating the household:
   Step 1
   Owner creates household.
   Step 2
   Owner adds/invites all members.
   Step 3
   Owner arranges everyone into the desired rotation order.
   Example:
   Alex
   Sarah
   John
   Maria
   Meaning:
   Alex → Sarah → John → Maria
   Step 4
   Owner chooses who is responsible today.
   The rotation doesn't necessarily have to begin with the first person in the list.

6. Rotation
   The household has a persistent rotation order.
   Example:
   A → B → C → D
   The rotation order itself is stable.
   The calendar assignments are derived from that order.
   The key principle is:
   The rotation order stays the same unless the owner explicitly changes it.

7. Active and inactive members
   Members can be:
   ACTIVE
   INACTIVE
   The owner controls this.
   Active
   An active person participates normally in the rotation.
   Inactive
   An inactive person:
   Is skipped by the rotation
   Does not receive duties
   Does not receive duty reminders
   Cannot be selected for switching
   Cannot be selected for covering
   Cannot be selected for paid coverage
   Can still log in
   Can still see the household
   Can still see the schedule
   Can still see history
   Inactive does not mean removed from the household.
   It simply means:
   "This person isn't currently participating in kitchen duties."

8. Inactive person during their duty
   If someone becomes inactive while they are currently responsible:
   Before:

A → B → C → D

Today: A
Owner makes A inactive.
The system immediately finds the next active person:
Today: B
No special reassignment process is required.

9. Reactivating someone
   When an inactive person becomes active again:
   They return to their position in the rotation.
   They don't receive a special/makeup duty.
   They simply wait until the rotation naturally reaches them.
   Example:
   Rotation:

A → B → C → D

B inactive:

A → C → D
If B becomes active while C is next:
C → D → A → B
B waits for their normal position.

10. Adding members
    The owner can add a new member.
    The owner chooses where they enter the rotation.
    Example:
    Existing:

A → B → C → D

Add E between B and C:

A → B → E → C → D
The change applies immediately to future duties.
Past history does not change.

11. Removing members
    The owner can remove a member from the household/rotation.
    The person's historical records remain.
    Example:
    Before:

A → B → C → D

C removed:

A → B → D
Future scheduling simply continues using the remaining members.
If C had future duties, those future assignments are recalculated.
There is no complicated reassignment workflow.

12. Changing the rotation
    The owner can rearrange the rotation at any time.
    Example:
    Before:

A → B → C → D

After:

C → A → D → B
The new order affects future duties only.
Past history remains exactly as it was.
This is an important rule:
Changing today's/future rotation must never rewrite history.

13. The kitchen duty
    There is only one task:
    Clean the kitchen.
    There is no checklist for MVP.
    No:
    dishes checklist
    floor checklist
    trash checklist
    points
    quality scores
    gamification
    The duty is simply:
    Clean kitchen

14. Normal duty
    Example:
    Monday
    Scheduled: A
    A cleans the kitchen.
    The owner later reviews Monday and records:
    Scheduled: A
    Completed by: A
    Reason: Completed
    The member doesn't have to press a "Done" button.
    This is intentional.
    The person shouldn't have to remember another thing.

15. Member reports
    A member can provide information about their duty.
    They can do this during their assigned day.
    They can report:
    Switch
    "I switched with B."
    Cover
    "B is covering for me."
    Paid coverage
    "B is doing it for me and I paid them."
    They can also add a note.
    Example:
    "I have work late tonight, so B is covering for me."

16. No confirmation between members
    The application does not create an approval process between the two people.
    For example:
    A reports:

B is covering for me.
B does not have to accept the request.
The application simply records A's report.
The owner later decides what actually happened.
This keeps the app focused on recording household activity rather than managing interpersonal agreements.

17. Notes
    Members can attach a note to their specific duty day.
    Example:
    Monday — A

Note:
"I won't be home until late."
Notes are editable until the owner finalizes the day's outcome.
Once the owner finalizes the duty, the member can no longer modify the note.

18. Owner review
    This is one of the most important parts of the application.
    The application does not automatically decide what happened.
    Instead, the owner reviews the day.
    For example:
    Monday

Scheduled:
A

A reported:
"B covered for me."

Note:
"Had work late."

Owner:

What actually happened?

[ Completed ]
[ Switched ]
[ Covered ]
[ Paid coverage ]
[ Missed ]
The owner is the authority.
The owner's decision becomes the official record.

19. Owner can override member reports
    A member's report is only information.
    The owner can completely disagree with it.
    Example:
    A reported:

Switched with B
But the owner knows A actually cleaned the kitchen.
The owner can record:
Scheduled: A
Completed by: A
Reason: Completed
The member's report does not override the owner's decision.

20. Owner can correct mistakes
    After a duty has been finalized, the owner can still correct it.
    Example:
    Monday

Previously:
Scheduled: A
Completed by: A
Reason: Completed
Owner realizes later:
Actually B cleaned it.
Owner can change it to:
Scheduled: A
Completed by: B
Reason: Covered
Normal members cannot modify official outcomes.

21. Duty outcome types
    The official history should essentially answer three questions:
    Who was scheduled?
    Who actually did it?
    Why was it different?
    Possible outcomes:
    Normal
    Scheduled: A
    Completed by: A
    Reason: Completed
    Switch
    Scheduled: A
    Completed by: B
    Reason: Switched
    A and B exchange their duties.
    Example:
    Monday A
    Tuesday B

After switch:

Monday B
Tuesday A
The underlying rotation remains unchanged.
Covered
Scheduled: A
Completed by: B
Reason: Covered
B does A's duty but B keeps their own normal upcoming duty.
Example:
Monday A
Tuesday B

B covers Monday:

Monday B
Tuesday B
Paid coverage
Exactly like coverage:
Scheduled: A
Completed by: B
Reason: Paid coverage
The only difference is that A paid B.
The application does not handle the money.
No:
payment processing
payment balances
transactions
payment provider
Only the fact that it was paid coverage is recorded.
Missed
Scheduled: A
Completed by: —
Reason: Missed
This one has a special effect on future scheduling.

22. Switching vs covering
    These are deliberately different.
    Switch
    Both people's assignments effectively exchange.
    A's day → B
    B's day → A
    Rotation remains unchanged.
    Cover
    Another person performs the duty, but their own scheduled duty remains theirs.
    A's day → B
    B's day → B
    The rotation remains unchanged.

23. Missed duties
    The application does not automatically mark someone as having missed their duty.
    Instead:
    The owner must decide.
    The next day, the owner gets an unresolved review.
    Example:
    Monday
    A was responsible.

Tuesday
Monday is still unresolved.
The schedule initially remains:
Monday A
Tuesday B
Wednesday C
The app waits for the owner.

24. No review deadline
    There is no deadline for the owner.
    If the owner doesn't review Monday until Wednesday, that's okay.
    The unresolved duty remains pending until the owner handles it.
    The system doesn't automatically decide that A missed it.

25. When owner confirms "Missed"
    Once the owner selects:
    Missed
    the schedule immediately changes.
    Example:
    Original:

Monday    A
Tuesday   B
Wednesday C
Thursday  D
Owner confirms:
Monday = Missed
New schedule:
Monday    A — Missed
Tuesday   A — Makeup
Wednesday B
Thursday  C
Friday    D
A continues to be responsible until they successfully complete their duty.

26. Multiple missed days
    If A also fails Tuesday:
    Monday    A — Missed
    Tuesday   A — Missed
    Wednesday A — Makeup
    Thursday  B
    Friday    C
    Saturday D
    The duty continues moving forward until A actually completes it.
    Fortunately, this probably won't happen often, but the system should support it.

27. Vacation / inactivity vs missed duty
    These are fundamentally different.
    Missed
    Someone was supposed to do the duty but didn't.
    Result:
    They owe a makeup duty.
    Inactive
    Someone isn't participating.
    Result:
    They are skipped.
    They don't owe anything when they become active again.

28. Schedule calculation philosophy
    A very important architectural principle emerges from all of this:
    The rotation is the source of the normal schedule, while confirmed events create exceptions.
    Examples:
    Normal rotation
    A → B → C → D
    Inactive:
    A → C → D
    Missed:
    A's duty moves forward
    Switch:
    Specific assignment changes
    Cover:
    Specific day's performer changes
    The system shouldn't randomly mutate the underlying rotation every time something happens.

29. Dashboard
    The main page should be designed primarily for mobile.
    The first section shows:
    Yesterday
    YESTERDAY

Alex

✓ Completed
or:
YESTERDAY

Alex

⚠ Needs review
[ Review ]
Today
TODAY

You

🧹 Your turn
If you're not responsible:
TODAY

Sarah
Tomorrow
TOMORROW

John
Then the remaining four days:
UPCOMING

Fri    Sarah
Sat    John
Sun    Maria
Mon    Alex
So the dashboard gives 7 days total.
There is no need for:
countdowns
statistics
streaks
gamification
"your next duty in X days"

30. Action center
    The dashboard also shows actions relevant to the current user.
    For a normal member:
    Your duty today

[ Report a change ]
[ Add note ]
"Report a change" can lead to:
What happened?

[ Switched ]
[ Someone covered me ]
[ Paid coverage ]
For the owner:
ACTION REQUIRED

2 duties need review

[ Review ]
Only relevant actions should appear.
The goal is:
Open the app → immediately know what matters.

31. Owner review queue
    The owner should have a clear list of unresolved duties.
    Example:
    ACTION REQUIRED

Yesterday
Alex
Needs review

2 days ago
Sarah
Needs review

[ Review ]
Once the owner resolves a duty, it disappears from the pending queue.
There is no need to notify the owner about already-resolved duties.

32. Visibility
    Every household member can see:
    Yesterday
    Today
    Upcoming 7 days
    Who is responsible
    Full 30-day history
    What happened
    Who actually completed a duty
    Reasons
    Relevant notes/reports
    The household is intentionally transparent.
    The restriction is primarily around who can modify official information.

33. History
    The application shows the last 30 days of history.
    Example:
    Aug 28
    Scheduled: Alex
    Completed by: Alex
    Reason: Completed

Aug 27
Scheduled: Sarah
Completed by: John
Reason: Covered

Aug 26
Scheduled: John
Completed by: Maria
Reason: Paid coverage

Aug 25
Scheduled: Maria
Completed by: —
Reason: Missed
History answers:
Who was supposed to do it?
Who actually did it?
Why?
Member notes/reports can provide additional context.
Past records aren't rewritten when the rotation changes.

34. Notifications
    The application should eventually remind the person responsible for today's duty.
    The desired schedule is:
    08:00
    15:00
    20:00
    Same times for everyone.
    But notifications stop being sent once the duty is officially resolved.
    Example:
    08:00 → reminder
    15:00 → reminder

A cleans kitchen

Owner resolves duty

20:00 → no reminder
If the day ends without resolution:
08:00 → reminder
15:00 → reminder
20:00 → reminder

Then:

STOP
It does not continue sending reminders into the following days.
The system waits for the owner to review the unresolved duty.

35. SMS
    SMS is not part of the core MVP implementation initially.
    Desired future behavior:
    08:00 → SMS
    15:00 → SMS
    20:00 → SMS
    But only if a free/very cheap service is realistically available.
    We should build the application's notification logic in a way that doesn't tightly couple it to SMS.
    Possible future notification methods:
    Notification system
    │
    ├── SMS
    ├── Web Push
    └── Email
    But we'll deal with the actual provider later.

36. Future features intentionally NOT in MVP
    We've explicitly decided not to over-engineer.
    Potential future features:
    Multiple household administrators
    Granular permissions
    Full calendar/schedule view
    Multiple households per account
    Email invitations
    SMS notifications
    More sophisticated notifications
    Payment processing
    Household-wide messages
    Cleaning checklists
    Statistics
    Gamification
    Advanced audit logs
    These are not MVP requirements.

37. Core philosophy
    The application should follow a few principles:
1. The app doesn't assume reality
   It doesn't automatically decide:
   "You didn't clean, therefore you missed."
   The owner confirms what actually happened.
2. Members provide context
   Members can say:
   "I switched with B."
   But that's a report, not the official outcome.
3. Owner controls official outcomes
   The owner can:
   Confirm
   Change
   Override
   Correct
4. Don't mutate the past
   Changing the rotation doesn't rewrite history.
5. Don't create unnecessary workflows
   No:
   Request → Accept → Approve → Confirm → Complete
   when a simple:
   Report → Owner reviews
   does the job.
6. The app should reduce remembering
   The people responsible shouldn't have to maintain the system themselves.
   The application should remind them, while the owner maintains the official record.

38. What the MVP ultimately needs to accomplish
    A household member should be able to open the website and immediately answer:
    Who's cleaning today?
    Who's cleaning tomorrow?
    When is my next turn?
    And if they're responsible:
    I need to clean today.
    If something changes:
    I can quickly tell the owner what happened.
    The owner should be able to open the app and immediately answer:
    What needs my attention?
    Then:
    What actually happened yesterday?
    and record it.
    The system then handles the scheduling consequences.

39. The core product loop
    Ultimately, the entire application revolves around this loop:
    ┌─────────────────────┐
    │  Rotation determines │
    │  today's responsible │
    │       person         │
    └──────────┬──────────┘
    ↓
    ┌─────────────────────┐
    │ Member sees their   │
    │       duty          │
    └──────────┬──────────┘
    ↓
    ┌─────────────────────┐
    │   Kitchen gets      │
    │      cleaned        │
    └──────────┬──────────┘
    ↓
    ┌─────────────────────┐
    │ Owner reviews what  │
    │      happened       │
    └──────────┬──────────┘
    ↓
    ┌─────────────────────┐
    │ Official outcome    │
    │      recorded       │
    └──────────┬──────────┘
    ↓
    ┌─────────────────────┐
    │ Future schedule     │
    │ updated if needed   │
    └──────────┬──────────┘
    │
    └──────────────→ next day
    And that is the heart of the application.

