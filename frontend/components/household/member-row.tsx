import {
  removeMember,
  updateMemberStatus,
} from "@/lib/supabase/household/actions";

type Member = {
  userId: string;
  displayName: string | null;
  role: string;
  status: string;
  rotationPosition: number;
};

type MemberRowProps = {
  householdId: string;
  member: Member;
};

export default function MemberRow({ householdId, member }: MemberRowProps) {
  const isOwner = member.role === "OWNER";
  const isActive = member.status === "ACTIVE";

  return (
    <div className="rounded-xl border border-slate-200 bg-white p-4 transition hover:border-slate-300 hover:shadow-sm">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        {/* Member information */}
        <div className="min-w-0">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-indigo-50 text-sm font-semibold text-indigo-700">
              {(member.displayName ?? member.userId).charAt(0).toUpperCase()}
            </div>

            <div className="min-w-0">
              <p className="truncate text-sm font-semibold text-slate-900">
                {member.displayName ?? member.userId}
              </p>

              <div className="mt-1 flex flex-wrap items-center gap-2">
                <span
                  className={`inline-flex rounded-full px-2 py-0.5 text-xs font-medium ${
                    isOwner
                      ? "bg-indigo-50 text-indigo-700"
                      : "bg-slate-100 text-slate-600"
                  }`}
                >
                  {member.role}
                </span>

                <span
                  className={`inline-flex rounded-full px-2 py-0.5 text-xs font-medium ${
                    isActive
                      ? "bg-emerald-50 text-emerald-700"
                      : "bg-slate-100 text-slate-500"
                  }`}
                >
                  {member.status}
                </span>
              </div>
            </div>
          </div>

          <p className="mt-3 text-xs text-slate-500">
            Rotation position:{" "}
            <span className="font-medium text-slate-700">
              {member.rotationPosition}
            </span>
          </p>
        </div>

        {/* Actions */}
        {!isOwner && (
          <div className="flex shrink-0 gap-2">
            <form
              action={updateMemberStatus.bind(
                null,
                householdId,
                member.userId,
                isActive ? "INACTIVE" : "ACTIVE",
              )}
            >
              <button
                type="submit"
                className={`rounded-lg border px-3 py-2 text-sm font-medium transition focus:outline-none focus:ring-2 focus:ring-offset-2 ${
                  isActive
                    ? "border-slate-300 bg-white text-slate-700 hover:bg-slate-50 focus:ring-slate-400"
                    : "border-emerald-200 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 focus:ring-emerald-500"
                }`}
              >
                {isActive ? "Deactivate" : "Activate"}
              </button>
            </form>

            <form action={removeMember.bind(null, householdId, member.userId)}>
              <button
                type="submit"
                className="rounded-lg border border-red-200 bg-white px-3 py-2 text-sm font-medium text-red-600 transition hover:bg-red-50 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
              >
                Remove
              </button>
            </form>
          </div>
        )}
      </div>
    </div>
  );
}
