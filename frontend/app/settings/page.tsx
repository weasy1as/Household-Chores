import { getCurrentHousehold } from "@/lib/supabase/household/server";
import React from "react";

const page = async () => {
  const household = await getCurrentHousehold();
  return (
    <div className="mx-auto max-w-3xl">
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
    </div>
  );
};

export default page;
