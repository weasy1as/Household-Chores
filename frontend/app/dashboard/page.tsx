import Link from "next/link";
import { createHousehold } from "@/lib/supabase/household/actions";
import {
  getCurrentHousehold,
  getdutyToday,
  getSchedule,
} from "@/lib/supabase/household/server";

const inputStyles =
  "w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20";

const labelStyles = "mb-1.5 block text-sm font-medium text-slate-700";

const primaryButtonStyles =
  "inline-flex items-center justify-center rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50";

const secondaryButtonStyles =
  "inline-flex items-center justify-center rounded-lg border border-slate-300 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm transition hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2";

function getDateStringInTimezone(timezone: string) {
  const formatter = new Intl.DateTimeFormat("en-CA", {
    timeZone: timezone,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });

  return formatter.format(new Date());
}

function addDays(dateString: string, days: number) {
  const [year, month, day] = dateString.split("-").map(Number);

  const date = new Date(Date.UTC(year, month - 1, day + days));

  return date.toISOString().split("T")[0];
}

function formatDate(dateString: string) {
  const [year, month, day] = dateString.split("-").map(Number);

  const date = new Date(Date.UTC(year, month - 1, day));

  return new Intl.DateTimeFormat("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
    timeZone: "UTC",
  }).format(date);
}

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

  const todayString = getDateStringInTimezone(household.timezone);

  const endDateString = addDays(todayString, 6);

  const todayDuty = await getdutyToday(household.householdId);

  const schedule = await getSchedule(
    household.householdId,
    todayString,
    endDateString,
  );

  const upcomingSchedule = schedule.filter(
    (entry: {
      date: string;
      scheduledMemberId: string;
      scheduledMemberName: string | null;
    }) => entry.date !== todayString,
  );

  const todayMemberName =
    todayDuty?.scheduledMember?.user?.displayName ??
    todayDuty?.scheduledMember?.user?.email ??
    "Unknown";

  const todayStatus =
    todayDuty?.status === "RESOLVED" ? todayDuty.outcome : "Needs review";

  return (
    <main className="min-h-screen bg-slate-50 px-4 pb-10 pt-6 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-5xl">
        {/* Header */}
        <header className="mb-6">
          <p className="text-sm font-semibold uppercase tracking-wide text-indigo-600">
            Household
          </p>

          <div className="mt-1 flex items-start justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">
                {household.householdName}
              </h1>

              <p className="mt-1 text-sm text-slate-500">Kitchen duty</p>
            </div>

            <span className="rounded-full bg-indigo-50 px-2.5 py-1 text-xs font-semibold text-indigo-700">
              {household.role}
            </span>
          </div>
        </header>

        {/* Today's duty */}
        <section className="mb-6">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-900">Today</h2>

            <span className="text-sm text-slate-500">
              {formatDate(todayString)}
            </span>
          </div>

          <div className="rounded-2xl border border-indigo-100 bg-white p-5 shadow-sm">
            <div className="flex items-start gap-4">
              <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-indigo-50 text-xl">
                🧹
              </div>

              <div className="min-w-0 flex-1">
                <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                  Assigned to
                </p>

                <p className="mt-1 truncate text-xl font-bold text-slate-900">
                  {todayMemberName}
                </p>

                <p className="mt-1 text-sm text-slate-500">Clean the kitchen</p>
              </div>
            </div>

            <div className="mt-5 flex items-center justify-between border-t border-slate-100 pt-4">
              <span className="text-sm text-slate-500">Status</span>

              <span
                className={
                  todayDuty?.status === "RESOLVED"
                    ? "rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700"
                    : "rounded-full bg-amber-50 px-3 py-1 text-xs font-semibold text-amber-700"
                }
              >
                {todayStatus}
              </span>
            </div>

            {todayDuty?.status !== "RESOLVED" && (
              <div className="mt-4">
                <button
                  type="button"
                  className={`${secondaryButtonStyles} w-full`}
                >
                  Report something
                </button>
              </div>
            )}
          </div>
        </section>

        {/* Upcoming */}
        <section className="mb-6">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-900">Upcoming</h2>

            <span className="text-xs text-slate-500">Next 6 days</span>
          </div>

          <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
            {upcomingSchedule.map(
              (
                entry: {
                  date: string;
                  scheduledMemberId: string;
                  scheduledMemberName: string | null;
                },
                index: number,
              ) => (
                <div
                  key={entry.date}
                  className={`flex items-center justify-between gap-4 px-5 py-4 ${
                    index !== upcomingSchedule.length - 1
                      ? "border-b border-slate-100"
                      : ""
                  }`}
                >
                  <div className="min-w-0">
                    <p className="text-sm font-medium text-slate-500">
                      {formatDate(entry.date)}
                    </p>

                    <p className="mt-0.5 truncate text-base font-semibold text-slate-900">
                      {entry.scheduledMemberName ?? "Unknown"}
                    </p>
                  </div>

                  <span className="shrink-0 text-sm text-slate-400">
                    Kitchen
                  </span>
                </div>
              ),
            )}
          </div>
        </section>

        {/* Owner review */}
        {household.role === "OWNER" && (
          <section className="mb-6">
            <div className="rounded-2xl border border-amber-200 bg-amber-50 p-5">
              <div className="flex items-start gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-white text-lg">
                  👀
                </div>

                <div>
                  <h2 className="font-semibold text-slate-900">Owner review</h2>

                  <p className="mt-1 text-sm leading-5 text-slate-600">
                    Duties are resolved by the household owner after the day is
                    finished.
                  </p>
                </div>
              </div>
            </div>
          </section>
        )}

        {/* Household management */}
        <section>
          <div className="mb-3">
            <h2 className="text-lg font-semibold text-slate-900">Household</h2>
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <Link href="/household/members" className={secondaryButtonStyles}>
              Manage members
            </Link>

            <Link href="/household/rotation" className={secondaryButtonStyles}>
              Manage rotation
            </Link>
          </div>
        </section>
      </div>
    </main>
  );
}
