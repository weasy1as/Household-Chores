import Link from "next/link";
import { createHousehold } from "@/lib/supabase/household/actions";
import {
  getCurrentHousehold,
  getHouseholdMembers,
  getResponsibleMemberForDate,
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

  const today = new Date();
  const tomorrow = new Date(today);
  tomorrow.setDate(today.getDate() + 1);

  const dayAfterTomorrow = new Date(today);
  dayAfterTomorrow.setDate(today.getDate() + 2);

  const todayString = today.toISOString().split("T")[0];
  const tomorrowString = tomorrow.toISOString().split("T")[0];
  const dayAfterTomorrowString = dayAfterTomorrow.toISOString().split("T")[0];

  const todayResponsible = await getResponsibleMemberForDate(
    household.householdId,
    todayString,
  );

  const tomorrowResponsible = await getResponsibleMemberForDate(
    household.householdId,
    tomorrowString,
  );

  const dayAfterTomorrowResponsible = await getResponsibleMemberForDate(
    household.householdId,
    dayAfterTomorrowString,
  );

  return (
    <main className="min-h-screen bg-slate-50 px-4 py-10 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-5xl">
        {/* Page header */}
        <header className="mb-8">
          <p className="text-sm font-semibold uppercase tracking-wide text-indigo-600">
            Household
          </p>

          <section>
            <h2>Duty Test</h2>

            <p>
              Today ({todayString}):{" "}
              {todayResponsible?.user?.displayName ??
                todayResponsible?.user?.email}
            </p>

            <p>
              Tomorrow ({tomorrowString}):{" "}
              {tomorrowResponsible?.user?.displayName ??
                tomorrowResponsible?.user?.email}
            </p>

            <p>
              Day after ({dayAfterTomorrowString}):{" "}
              {dayAfterTomorrowResponsible?.user?.displayName ??
                dayAfterTomorrowResponsible?.user?.email}
            </p>
          </section>
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

        <nav className="flex flex-col gap-3 sm:flex-row" aria-label="Household">
          <Link
            href="/household/members"
            className={`${primaryButtonStyles} w-full sm:w-auto`}
          >
            Manage members
          </Link>
          <Link
            href="/household/rotation"
            className="inline-flex w-full items-center justify-center rounded-lg border border-slate-300 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm transition hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:w-auto"
          >
            Manage rotation
          </Link>
        </nav>
      </div>
    </main>
  );
}
