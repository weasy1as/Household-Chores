import Link from "next/link";
import MemberList from "@/components/household/member-list";
import { addMember } from "@/lib/supabase/household/actions";
import {
  getCurrentHousehold,
  getHouseholdMembers,
} from "@/lib/supabase/household/server";

const inputStyles =
  "w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20";

const labelStyles = "mb-1.5 block text-sm font-medium text-slate-700";

const primaryButtonStyles =
  "inline-flex items-center justify-center rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50";

export default async function HouseholdMembersPage() {
  const household = await getCurrentHousehold();

  if (!household) {
    return (
      <main className="min-h-screen bg-slate-50 px-4 py-10 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-3xl">
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">
            Household members
          </h1>
          <p className="mt-3 text-sm text-slate-600">
            Create a household from the dashboard before managing members.
          </p>
          <Link href="/dashboard" className={`${primaryButtonStyles} mt-6`}>
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
                Household members
              </h1>
              <p className="mt-1 text-sm text-slate-500">
                Manage the people in your household.
              </p>
            </div>
            <Link
              href="/household/rotation"
              className="text-sm font-semibold text-indigo-600"
            >
              View rotation
            </Link>
          </div>
        </header>

        <section className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm sm:p-6">
          <MemberList
            householdId={household.householdId}
            members={members ?? []}
          />
        </section>

        <section className="mt-6 rounded-2xl border border-slate-200 bg-white shadow-sm">
          <div className="border-b border-slate-200 px-4 py-5 sm:px-6">
            <h2 className="text-base font-semibold text-slate-900">
              Add member
            </h2>
            <p className="mt-1 text-sm text-slate-500">
              Invite another person to your household by email.
            </p>
          </div>
          <form
            action={addMember.bind(null, household.householdId)}
            className="p-4 sm:p-6"
          >
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
          </form>
        </section>
      </div>
    </main>
  );
}
