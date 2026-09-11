import Link from "next/link";
import RotationList from "@/components/household/rotation-list";
import {
  getCurrentHousehold,
  getHouseholdMembers,
} from "@/lib/supabase/household/server";

export default async function HouseholdRotationPage() {
  const household = await getCurrentHousehold();

  if (!household) {
    return (
      <main className="min-h-screen bg-slate-50 px-4 py-10 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-3xl">
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">
            Household rotation
          </h1>
          <p className="mt-3 text-sm text-slate-600">
            Create a household from the dashboard before managing rotation.
          </p>
          <Link
            href="/dashboard"
            className="mt-6 inline-flex items-center justify-center rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
          >
            Back to dashboard
          </Link>
        </div>
      </main>
    );
  }

  const members = await getHouseholdMembers(household.householdId);

  return (
    <main className="min-h-screen bg-slate-50 px-4 py-8 sm:px-6 sm:py-10 lg:px-8">
      <div className="mx-auto max-w-5xl">
        <header className="mb-6">
          <Link
            href="/dashboard"
            className="text-sm font-medium text-indigo-600"
          >
            Back to dashboard
          </Link>
          <p className="mt-5 text-sm font-semibold uppercase tracking-wide text-indigo-600">
            {household.householdName}
          </p>
          <div className="mt-1 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <h1 className="text-3xl font-bold tracking-tight text-slate-900">
                Household rotation
              </h1>
              <p className="mt-1 text-sm text-slate-500">
                Choose where the rotation starts and manage the order.
              </p>
            </div>
            <Link
              href="/household/members"
              className="text-sm font-semibold text-indigo-600"
            >
              View members
            </Link>
          </div>
        </header>

        <RotationList
          householdId={household.householdId}
          members={members ?? []}
          rotationStartPosition={household.rotationStartPosition}
        />
      </div>
    </main>
  );
}
