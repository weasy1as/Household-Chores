"use client";

import { useState } from "react";
import { resolveDuty } from "@/lib/supabase/household/actions";

type Outcome = "COMPLETED" | "COVERED" | "PAID" | "SWITCHED" | "MISSED";

type Member = {
  id?: string;
  userId?: string;
  user?: {
    displayName?: string;
    email?: string;
  };
};

type Duty = {
  id: string;
  date: string;
  status: string;
  scheduledMember?: Member;
  outcome?: Outcome;
};

type Props = {
  householdId: string;
  duty: Duty;
  members: Member[];
};

const outcomes: {
  value: Outcome;
  label: string;
  description: string;
}[] = [
  {
    value: "COMPLETED",
    label: "Completed",
    description: "The duty was completed.",
  },
  {
    value: "COVERED",
    label: "Covered",
    description: "Another household member covered it.",
  },
  {
    value: "PAID",
    label: "Paid",
    description: "The duty was resolved through payment.",
  },
  {
    value: "SWITCHED",
    label: "Switched",
    description: "The duty was switched with another member.",
  },
  {
    value: "MISSED",
    label: "Missed",
    description: "The duty was not completed.",
  },
];

export default function ResolveDutyForm({ householdId, duty, members }: Props) {
  const [outcome, setOutcome] = useState<Outcome>("COMPLETED");
  const [completedByMemberId, setCompletedByMemberId] = useState("");
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setPending(true);
    setError(null);
    setSuccess(false);
    console.log(members);

    try {
      await resolveDuty(
        householdId,
        duty.id,
        outcome,
        completedByMemberId || undefined,
      );

      setSuccess(true);
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Failed to resolve duty.",
      );
    } finally {
      setPending(false);
    }
  }

  const scheduledMemberName =
    duty.scheduledMember?.user?.displayName ??
    duty.scheduledMember?.user?.email ??
    "Unknown member";

  return (
    <section className="rounded-2xl border border-slate-200 bg-white shadow-sm">
      <div className="border-b border-slate-200 px-6 py-5">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
              Duty date
            </p>

            <h2 className="mt-1 text-xl font-semibold text-slate-900">
              {duty.date}
            </h2>
          </div>

          <span className="inline-flex w-fit rounded-full bg-amber-50 px-3 py-1 text-xs font-semibold text-amber-700">
            {duty.status}
          </span>

          {duty.status === "RESOLVED" && (
            <span className="inline-flex w-fit rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">
              {duty.outcome}
            </span>
          )}
        </div>
      </div>

      <div className="p-6">
        <div className="rounded-xl bg-slate-50 p-4">
          <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
            Scheduled member
          </p>

          <p className="mt-1 text-base font-semibold text-slate-900">
            {scheduledMemberName}
          </p>
        </div>

        <form onSubmit={handleSubmit} className="mt-6 space-y-6">
          <div>
            <label
              htmlFor="outcome"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Outcome
            </label>

            <div className="space-y-3">
              {outcomes.map((item) => (
                <label
                  key={item.value}
                  className={`flex cursor-pointer items-start gap-3 rounded-xl border p-4 transition ${
                    outcome === item.value
                      ? "border-indigo-500 bg-indigo-50"
                      : "border-slate-200 bg-white hover:border-slate-300"
                  }`}
                >
                  <input
                    type="radio"
                    name="outcome"
                    value={item.value}
                    checked={outcome === item.value}
                    onChange={() => setOutcome(item.value)}
                    className="mt-1 h-4 w-4 border-slate-300 text-indigo-600 focus:ring-indigo-500"
                  />

                  <span>
                    <span className="block text-sm font-semibold text-slate-900">
                      {item.label}
                    </span>

                    <span className="mt-0.5 block text-sm text-slate-500">
                      {item.description}
                    </span>
                  </span>
                </label>
              ))}
            </div>
          </div>

          {outcome != "MISSED" && (
            <div>
              <label
                htmlFor="completedByMember"
                className="mb-1.5 block text-sm font-medium text-slate-700"
              >
                Completed by
              </label>

              <select
                id="completedByMember"
                value={completedByMemberId}
                onChange={(event) => setCompletedByMemberId(event.target.value)}
                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 shadow-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20"
              >
                <option value="">Select member</option>

                {members.map((member) => {
                  const memberId = member.memberId;

                  if (!memberId) {
                    return null;
                  }

                  const name =
                    member?.displayName ?? member?.email ?? "Unknown member";

                  return (
                    <option key={memberId} value={memberId}>
                      {name}
                    </option>
                  );
                })}
              </select>

              <p className="mt-1.5 text-xs text-slate-500">
                Select who actually completed the duty.
              </p>
            </div>
          )}

          {error && (
            <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
              {error}
            </div>
          )}

          {success && (
            <div className="rounded-lg border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-700">
              Duty resolved successfully.
            </div>
          )}

          <button
            type="submit"
            disabled={pending}
            className="inline-flex items-center justify-center rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {pending ? "Resolving..." : "Resolve duty"}
          </button>
        </form>
      </div>
    </section>
  );
}
