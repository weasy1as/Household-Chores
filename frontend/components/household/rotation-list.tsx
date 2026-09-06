import {
  setRotationStartingPoint,
  updateRotationPosition,
} from "@/lib/supabase/household/actions";

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
  rotationStartPosition: number;
};

export default function RotationList({
  householdId,
  members,
  rotationStartPosition,
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
              Choose where the rotation starts and manage the order.
            </p>
          </div>

          <span className="inline-flex shrink-0 rounded-full bg-indigo-50 px-2.5 py-1 text-xs font-semibold text-indigo-700">
            {activeMembers.length}{" "}
            {activeMembers.length === 1 ? "member" : "members"}
          </span>
        </div>
      </div>

      {/* Members */}
      <div className="divide-y divide-slate-100">
        {activeMembers.length === 0 ? (
          <div className="px-6 py-10 text-center">
            <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-slate-100 text-xl text-slate-500">
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
          activeMembers.map((member, index) => {
            const isStartingPoint =
              member.rotationPosition === rotationStartPosition;

            return (
              <div
                key={member.userId}
                className={`px-6 py-5 transition ${
                  isStartingPoint ? "bg-indigo-50/60" : "hover:bg-slate-50"
                }`}
              >
                <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                  {/* Member */}
                  <div className="flex min-w-0 items-center gap-4">
                    <div
                      className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-sm font-bold ${
                        isStartingPoint
                          ? "bg-indigo-600 text-white"
                          : "bg-slate-100 text-slate-700"
                      }`}
                    >
                      {index + 1}
                    </div>

                    <div className="min-w-0">
                      <div className="flex flex-wrap items-center gap-2">
                        <p className="truncate text-sm font-semibold text-slate-900">
                          {member.displayName ?? member.userId}
                        </p>

                        {isStartingPoint && (
                          <span className="inline-flex items-center gap-1 rounded-full bg-indigo-100 px-2 py-0.5 text-xs font-semibold text-indigo-700">
                            <span aria-hidden="true">★</span>
                            Starting point
                          </span>
                        )}
                      </div>

                      <p className="mt-0.5 text-xs text-slate-500">
                        Rotation position {index + 1}
                      </p>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="flex flex-wrap items-center gap-2">
                    {isStartingPoint ? (
                      <span className="inline-flex items-center rounded-lg bg-indigo-100 px-3 py-2 text-sm font-medium text-indigo-700">
                        Current start
                      </span>
                    ) : (
                      <form
                        action={setRotationStartingPoint.bind(
                          null,
                          householdId,
                          member.rotationPosition,
                        )}
                      >
                        <button
                          type="submit"
                          className="inline-flex items-center gap-1.5 rounded-lg border border-indigo-200 bg-white px-3 py-2 text-sm font-medium text-indigo-700 shadow-sm transition hover:bg-indigo-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
                        >
                          <span aria-hidden="true">★</span>
                          Start here
                        </button>
                      </form>
                    )}

                    <div className="hidden h-6 w-px bg-slate-200 sm:block" />

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
                          aria-label={`Move ${
                            member.displayName ?? "member"
                          } up`}
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
                          aria-label={`Move ${
                            member.displayName ?? "member"
                          } down`}
                          className="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 shadow-sm transition hover:bg-slate-50 hover:text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
                        >
                          <span aria-hidden="true">↓</span>
                          <span>Move down</span>
                        </button>
                      </form>
                    )}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </section>
  );
}
