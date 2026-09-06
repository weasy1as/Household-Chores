import { addMember, createHousehold } from "@/lib/supabase/household/actions";
import {
  getCurrentHousehold,
  getHouseholdMembers,
} from "@/lib/supabase/household/server";

export default async function DashboardPage() {
  const household = await getCurrentHousehold();

  if (!household) {
    return (
      <main>
        <h1>Dashboard</h1>

        <h2>No household yet</h2>

        <p>You are not currently a member of a household.</p>

        <p>Create a new household to get started.</p>

        <form action={createHousehold}>
          <div>
            <label htmlFor="name">Household name</label>
            <input id="name" name="name" type="text" required />
          </div>

          <div>
            <label htmlFor="timezone">Timezone</label>
            <input
              id="timezone"
              name="timezone"
              type="text"
              defaultValue="Europe/Copenhagen"
              required
            />
          </div>

          <button type="submit">Create household</button>
        </form>
      </main>
    );
  }

  const members = await getHouseholdMembers(household.householdId);

  return (
    <main>
      <h1>Dashboard</h1>

      <h2>{household.householdName}</h2>

      <p>Timezone: {household.timezone}</p>
      <p>Role: {household.role}</p>
      <p>Status: {household.status}</p>

      <h2>Members</h2>

      {members?.map((member: any) => (
        <div key={member.userId}>
          <p>{member.displayName ?? member.userId}</p>
          <p>Role: {member.role}</p>
          <p>Status: {member.status}</p>
          <p>Rotation position: {member.rotationPosition}</p>
        </div>
      ))}
      <h2>Add member</h2>

      <form action={addMember.bind(null, household.householdId)}>
        <label htmlFor="email">Member email</label>

        <input id="email" name="email" type="email" required />

        <button type="submit">Add member</button>
      </form>
    </main>
  );
}
