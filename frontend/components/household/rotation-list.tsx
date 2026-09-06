import { updateRotationPosition } from "@/lib/supabase/household/actions";

type Member = {
  userId: string;
  displayName: string | null;
  role: string;
  status: string;
  rotationPosition: number;
};

type RotationListProps = {
  householdId: string;
  members: Member[];
};

export default function RotationList({
  householdId,
  members,
}: RotationListProps) {
  const activeMembers = members
    .filter((member) => member.status === "ACTIVE")
    .sort((a, b) => a.rotationPosition - b.rotationPosition);

  return (
    <section className="rounded-2xl border border-slate-200 bg-white shadow-sm">
      {/* Header */}
      <div className="border-b border-slate-200 px-6 py-5">
        <div className="flex items-center justify-between gap-4">
          <div>
            <h2 className="text-base font-semibold text-slate-900">Rotation</h2>
            <p className="mt-1 text-sm text-slate-500">
              Manage the order of active household members.
            </p>
          </div>

          <span className="inline-flex shrink-0 rounded-full bg-indigo-50 px-2.5 py-1 text-xs font-semibold text-indigo-700">
            {activeMembers.length}{" "}
            {activeMembers.length === 1 ? "member" : "members"}
          </span>
        </div>
      </div>

      {/* Rotation list */}
      <div className="divide-y divide-slate-100">
        {activeMembers.length === 0 ? (
          <div className="px-6 py-10 text-center">
            <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-slate-100 text-xl">
              ↻
            </div>

            <h3 className="mt-4 text-sm font-semibold text-slate-900">
              No active members
            </h3>

            <p className="mt-1 text-sm text-slate-500">
              Activate a household member to add them to the rotation.
            </p>
          </div>
        ) : (
          activeMembers.map((member, index) => (
            <div
              key={member.userId}
              className="flex flex-col gap-4 px-6 py-4 transition hover:bg-slate-50 sm:flex-row sm:items-center sm:justify-between"
            >
              {/* Position + member */}
              <div className="flex min-w-0 items-center gap-4">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-indigo-600 text-sm font-bold text-white">
                  {index + 1}
                </div>

                <div className="min-w-0">
                  <p className="truncate text-sm font-semibold text-slate-900">
                    {member.displayName ?? member.userId}
                  </p>

                  <p className="mt-0.5 text-xs text-slate-500">
                    Rotation position {index + 1}
                  </p>
                </div>
              </div>

              {/* Move controls */}
              <div className="flex shrink-0 gap-2">
                {index > 0 && (
                  <form
                    action={updateRotationPosition.bind(
                      null,
                      householdId,
                      member.userId,
                      index - 1,
                    )}
                  >
                    <button
                      type="submit"
                      aria-label={`Move ${member.displayName ?? "member"} up`}
                      className="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 shadow-sm transition hover:bg-slate-50 hover:text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
                    >
                      <span aria-hidden="true">↑</span>
                      <span>Move up</span>
                    </button>
                  </form>
                )}

                {index < activeMembers.length - 1 && (
                  <form
                    action={updateRotationPosition.bind(
                      null,
                      householdId,
                      member.userId,
                      index + 1,
                    )}
                  >
                    <button
                      type="submit"
                      aria-label={`Move ${member.displayName ?? "member"} down`}
                      className="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 shadow-sm transition hover:bg-slate-50 hover:text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
                    >
                      <span aria-hidden="true">↓</span>
                      <span>Move down</span>
                    </button>
                  </form>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </section>
  );
}
