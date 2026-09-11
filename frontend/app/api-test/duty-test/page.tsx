import {
  getCurrentHousehold,
  getdutyToday,
  getSchedule,
} from "@/lib/supabase/household/server";
import { generateDutyForDate } from "@/lib/supabase/household/server";

export default async function DutyTestPage() {
  const household = await getCurrentHousehold();

  if (!household) {
    return (
      <main className="min-h-screen bg-slate-50 px-4 py-10">
        <div className="mx-auto max-w-3xl">
          <h1 className="text-2xl font-bold text-slate-900">
            Duty endpoint test
          </h1>

          <p className="mt-2 text-slate-600">No household found.</p>
        </div>
      </main>
    );
  }

  const date = "2026-09-12";

  const duty = await generateDutyForDate(household.householdId, date);
  const todayDuty = await getdutyToday(household.householdId);

  const schedule = getSchedule(
    household.householdId,
    "2026-09-11",
    "2026-09-19",
  );

  return (
    <main className="min-h-screen bg-slate-50 px-4 py-10 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-3xl">
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <p className="text-sm font-semibold uppercase tracking-wide text-indigo-600">
            Household
          </p>

          <h1 className="mt-1 text-2xl font-bold text-slate-900">
            Duty endpoint test
          </h1>

          <div className="mt-6">
            <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
              Endpoint
            </p>

            <p className="mt-1 break-all font-mono text-sm text-slate-900">
              POST /api/households/{household.householdId}/duties/generate?date=
              {date}
            </p>
          </div>

          <div className="mt-6">
            <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
              Response
            </p>

            <pre className="mt-2 overflow-x-auto rounded-lg bg-slate-900 p-4 text-sm text-slate-100">
              {JSON.stringify(duty, null, 2)}
            </pre>
          </div>
          <section>
            <h2>Today’s Duty</h2>

            <p>Date: {todayDuty?.date}</p>

            <p>
              Responsible:{" "}
              {todayDuty?.scheduledMember?.user?.displayName ??
                todayDuty?.scheduledMember?.user?.email}
            </p>

            <p>Status: {todayDuty?.status}</p>
          </section>
          <section>
            <h2>Schedule</h2>
            <pre className="mt-2 overflow-x-auto rounded-lg bg-slate-900 p-4 text-sm text-slate-100">
              {JSON.stringify(await schedule, null, 2)}
            </pre>
          </section>
        </div>
      </div>
    </main>
  );
}
