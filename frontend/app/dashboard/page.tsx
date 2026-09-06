import MemberList from "@/components/household/member-list";
import RotationList from "@/components/household/rotation-list";
import { addMember, createHousehold } from "@/lib/supabase/household/actions";
import {
  getCurrentHousehold,
  getHouseholdMembers,
} from "@/lib/supabase/household/server";

const inputStyles =
  "w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20";

const labelStyles = "mb-1.5 block text-sm font-medium text-slate-700";

const primaryButtonStyles =
  "inline-flex items-center justify-center rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50";

export default async function DashboardPage() {
  const household = await getCurrentHousehold();

  if (!household) {
    return (
      <main className="min-h-screen bg-slate-50 px-4 py-10 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-3xl">
          <div className="mb-8">
            <p className="text-sm font-semibold uppercase tracking-wide text-indigo-600">
              Household
            </p>
            <h1 className="mt-1 text-3xl font-bold tracking-tight text-slate-900">
              Dashboard
            </h1>
          </div>

          <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
            <div className="mb-8">
              <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-indigo-50 text-xl">
                🏠
              </div>

              <h2 className="text-xl font-semibold text-slate-900">
                No household yet
              </h2>

              <p className="mt-2 max-w-xl text-sm leading-6 text-slate-600">
                You are not currently a member of a household. Create a new
                household to get started.
              </p>
            </div>

            <form action={createHousehold} className="space-y-5">
              <div>
                <label htmlFor="name" className={labelStyles}>
                  Household name
                </label>
                <input
                  id="name"
                  name="name"
                  type="text"
                  required
                  placeholder="e.g. The Copenhagen Home"
                  className={inputStyles}
                />
              </div>

              <div>
                <label htmlFor="timezone" className={labelStyles}>
                  Timezone
                </label>
                <input
                  id="timezone"
                  name="timezone"
                  type="text"
                  defaultValue="Europe/Copenhagen"
                  required
                  className={inputStyles}
                />
                <p className="mt-1.5 text-xs text-slate-500">
                  Used for household schedules and date/time calculations.
                </p>
              </div>

              <div className="pt-2">
                <button type="submit" className={primaryButtonStyles}>
                  Create household
                </button>
              </div>
            </form>
          </section>
        </div>
      </main>
    );
  }

  const members = await getHouseholdMembers(household.householdId);

  return (
    <main className="min-h-screen bg-slate-50 px-4 py-10 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-5xl">
        {/* Page header */}
        <header className="mb-8">
          <p className="text-sm font-semibold uppercase tracking-wide text-indigo-600">
            Household
          </p>

          <div className="mt-1 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <h1 className="text-3xl font-bold tracking-tight text-slate-900">
                {household.householdName}
              </h1>
              <p className="mt-1 text-sm text-slate-500">Household dashboard</p>
            </div>
          </div>
        </header>

        {/* Household details */}
        <section className="mb-6 rounded-2xl border border-slate-200 bg-white shadow-sm">
          <div className="border-b border-slate-200 px-6 py-5">
            <h2 className="text-base font-semibold text-slate-900">
              Household details
            </h2>
          </div>

          <div className="grid grid-cols-1 divide-y divide-slate-200 sm:grid-cols-3 sm:divide-x sm:divide-y-0">
            <div className="px-6 py-5">
              <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                Timezone
              </p>
              <p className="mt-1 text-sm font-medium text-slate-900">
                {household.timezone}
              </p>
            </div>

            <div className="px-6 py-5">
              <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                Your role
              </p>
              <p className="mt-1">
                <span className="inline-flex rounded-full bg-indigo-50 px-2.5 py-1 text-xs font-semibold text-indigo-700">
                  {household.role}
                </span>
              </p>
            </div>

            <div className="px-6 py-5">
              <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                Status
              </p>
              <p className="mt-1">
                <span className="inline-flex rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700">
                  {household.status}
                </span>
              </p>
            </div>
          </div>
        </section>

        {/* Members */}
        <section className="mb-6 rounded-2xl border border-slate-200 bg-white shadow-sm">
          <div className="border-b border-slate-200 px-6 py-5">
            <h2 className="text-base font-semibold text-slate-900">
              Household members
            </h2>
            <p className="mt-1 text-sm text-slate-500">
              Manage the people in your household.
            </p>
          </div>

          <div className="p-6">
            <MemberList
              householdId={household.householdId}
              members={members ?? []}
            />
          </div>
          <RotationList
            householdId={household.householdId}
            members={members ?? []}
            rotationStartPosition={household.rotationStartPosition}
          />
        </section>

        {/* Add member */}
        <section className="rounded-2xl border border-slate-200 bg-white shadow-sm">
          <div className="border-b border-slate-200 px-6 py-5">
            <h2 className="text-base font-semibold text-slate-900">
              Add member
            </h2>
            <p className="mt-1 text-sm text-slate-500">
              Invite another person to your household by email.
            </p>
          </div>

          <form
            action={addMember.bind(null, household.householdId)}
            className="p-6"
          >
            <div className="max-w-xl">
              <label htmlFor="email" className={labelStyles}>
                Member email
              </label>

              <div className="flex flex-col gap-3 sm:flex-row">
                <input
                  id="email"
                  name="email"
                  type="email"
                  required
                  placeholder="name@example.com"
                  className={inputStyles}
                />

                <button
                  type="submit"
                  className={`${primaryButtonStyles} shrink-0`}
                >
                  Add member
                </button>
              </div>
            </div>
          </form>
        </section>
      </div>
    </main>
  );
}
