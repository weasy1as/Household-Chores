import {
  getCurrentHousehold,
  getdutyToday,
  getHouseholdMembers,
} from "@/lib/supabase/household/server";
import ResolveDutyForm from "@/components/duty/resolve-duty-form";

export default async function DutyPage() {
  const household = await getCurrentHousehold();
  console.log(household);

  if (!household) {
    return (
      <main className="min-h-screen bg-slate-50 px-4 py-10 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-3xl">
          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h1 className="text-2xl font-bold text-slate-900">Resolve duty</h1>

            <p className="mt-2 text-sm text-slate-600">
              You are not currently a member of a household.
            </p>
          </div>
        </div>
      </main>
    );
  }

  const [duty, members] = await Promise.all([
    getdutyToday(household.householdId),
    getHouseholdMembers(household.householdId),
  ]);
  console.log(members);
  return (
    <main className="min-h-screen bg-slate-50 px-4 py-10 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-3xl">
        <header className="mb-8">
          <p className="text-sm font-semibold uppercase tracking-wide text-indigo-600">
            Household
          </p>

          <h1 className="mt-1 text-3xl font-bold tracking-tight text-slate-900">
            Resolve duty
          </h1>

          <p className="mt-2 text-sm text-slate-500">
            Resolve the household duty for today.
          </p>
        </header>

        {!duty ? (
          <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-base font-semibold text-slate-900">
              No duty found
            </h2>

            <p className="mt-2 text-sm text-slate-600">
              There is currently no duty for today.
            </p>
          </section>
        ) : (
          <ResolveDutyForm
            householdId={household.householdId}
            duty={duty}
            members={members ?? []}
          />
        )}
      </div>
    </main>
  );
}
