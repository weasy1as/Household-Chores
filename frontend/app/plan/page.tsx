"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useEffect, useMemo, useState } from "react";
import { createClient } from "@/lib/supabase/client";

type Household = {
  id: string;
  name: string;
  timezone: string;
  role: "OWNER" | "MEMBER";
};

type DutyReport = {
  reportedByMemberId: string;
  reportedByMemberName: string;
  type: "COVERED" | "PAID" | "SWITCHED";
  otherMemberId: string | null;
  otherMemberName: string | null;
  note: string | null;
};

type Duty = {
  id: string;
  date: string;
  scheduledMemberId: string;
  scheduledMemberName: string;
  status: "PENDING_REVIEW" | "RESOLVED";
  outcome: "COMPLETED" | "COVERED" | "PAID" | "SWITCHED" | "MISSED" | null;
  completedByMemberId: string | null;
  completedByMemberName: string | null;
  report: DutyReport | null;
};

function formatDate(date: Date) {
  return date.toISOString().slice(0, 10);
}

function addDays(date: Date, days: number) {
  const next = new Date(date);
  next.setDate(next.getDate() + days);
  return next;
}

function getDayLabel(value: string) {
  const date = new Date(`${value}T00:00:00`);
  return new Date(date).toLocaleDateString("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
  });
}

export default function PlanPage() {
  const router = useRouter();
  const supabase = createClient();

  const [loading, setLoading] = useState(true);
  const [households, setHouseholds] = useState<Household[]>([]);
  const [selectedHouseholdId, setSelectedHouseholdId] = useState<string>("");
  const [duties, setDuties] = useState<Duty[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    async function load() {
      const {
        data: { session },
      } = await supabase.auth.getSession();

      if (!active) {
        return;
      }

      if (!session) {
        router.replace("/login");
        return;
      }

      const authHeader = "Bearer " + session[["access", "token"].join("_")];

      try {
        const householdResponse = await fetch(
          "http://localhost:8080/api/households",
          {
            headers: {
              Authorization: authHeader,
            },
          },
        );

        if (!householdResponse.ok) {
          throw new Error("Unable to load households");
        }

        const householdData: Household[] = await householdResponse.json();

        if (!active) {
          return;
        }

        setHouseholds(householdData);

        if (householdData.length > 0) {
          setSelectedHouseholdId((current) => current || householdData[0].id);
        }
      } catch (loadError) {
        if (!active) {
          return;
        }
        setError(
          loadError instanceof Error ? loadError.message : "Unknown error",
        );
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    load();

    return () => {
      active = false;
    };
  }, [router, supabase]);

  useEffect(() => {
    if (!selectedHouseholdId) {
      return;
    }

    let active = true;

    async function loadDuties() {
      const {
        data: { session },
      } = await supabase.auth.getSession();

      if (!session || !active) {
        return;
      }

      const authHeader = "Bearer " + session[["access", "token"].join("_")];
      const today = new Date();
      const startDate = formatDate(addDays(today, -1));
      const endDate = formatDate(addDays(today, 6));

      try {
        const response = await fetch(
          `http://localhost:8080/api/households/${selectedHouseholdId}/duties?startDate=${startDate}&endDate=${endDate}`,
          {
            headers: {
              Authorization: authHeader,
            },
          },
        );

        if (!response.ok) {
          throw new Error("Unable to load duties");
        }

        const data: Duty[] = await response.json();

        if (active) {
          setDuties(data);
          setError("");
        }
      } catch (loadError) {
        if (active) {
          setError(
            loadError instanceof Error ? loadError.message : "Unknown error",
          );
        }
      }
    }

    loadDuties();

    return () => {
      active = false;
    };
  }, [selectedHouseholdId, supabase]);

  const selectedHousehold = useMemo(
    () =>
      households.find((household) => household.id === selectedHouseholdId) ??
      null,
    [households, selectedHouseholdId],
  );

  const days = useMemo(() => {
    const start = addDays(new Date(), -1);
    return Array.from({ length: 7 }, (_, index) => {
      const date = addDays(start, index);
      return {
        key: formatDate(date),
        label: getDayLabel(formatDate(date)),
      };
    });
  }, []);

  const dutyMap = useMemo(() => {
    return duties.reduce<Record<string, Duty>>((map, duty) => {
      map[duty.date] = duty;
      return map;
    }, {});
  }, [duties]);

  async function handleSignOut(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await supabase.auth.signOut();
    router.replace("/login");
  }

  const handleCreateHousehold = async () => {
    try {
      const {
        data: { session },
      } = await supabase.auth.getSession();

      if (!session) {
        router.replace("/login");
        return;
      }

      const authHeader = "Bearer " + session[["access", "token"].join("_")];

      const response = await fetch("http://localhost:8080/api/households", {
        method: "POST",
        headers: {
          Authorization: authHeader,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ name: "New Household" }),
      });

      if (!response.ok) {
        throw new Error("Unable to create household");
      }

      const newHousehold: Household = await response.json();
      setHouseholds((prev) => [...prev, newHousehold]);
      setSelectedHouseholdId(newHousehold.id);
    } catch (error) {
      console.error(error);
    }
  };

  if (loading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-zinc-50 px-4 text-zinc-600">
        Loading your plan...
      </main>
    );
  }

  if (!selectedHousehold) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-zinc-50 px-4">
        <div className="w-full max-w-md rounded-2xl border border-zinc-200 bg-white p-6 text-center shadow-sm">
          <h1 className="text-xl font-semibold">No households yet</h1>
          <p className="mt-2 text-sm text-zinc-600">
            Create a household to start tracking kitchen duties.
          </p>
          <form onSubmit={handleCreateHousehold}>
            <label
              htmlFor="householdName"
              className="block text-sm font-medium text-zinc-700"
            >
              Household Name
            </label>
            <input
              id="householdName"
              type="text"
              required
              className="mt-1 w-full rounded-lg border border-zinc-300 bg-white px-3 py-2.5 outline-none transition focus:border-zinc-500"
            />
            <button
              type="submit"
              className="mt-5 inline-flex rounded-md bg-black px-4 py-2 text-sm text-white"
            >
              Create Household
            </button>
          </form>

          <form></form>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-zinc-50 px-4 py-6 text-zinc-900">
      <div className="mx-auto max-w-6xl">
        <header className="mb-6 flex flex-col gap-4 rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-xs font-medium uppercase tracking-[0.2em] text-zinc-500">
              Household plan
            </p>
            <h1 className="mt-1 text-2xl font-semibold">
              {selectedHousehold.name}
            </h1>
          </div>

          <div className="flex items-center gap-3">
            <select
              value={selectedHouseholdId}
              onChange={(event) => setSelectedHouseholdId(event.target.value)}
              className="rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm outline-none"
            >
              {households.map((household) => (
                <option key={household.id} value={household.id}>
                  {household.name}
                </option>
              ))}
            </select>

            <form onSubmit={handleSignOut}>
              <button
                type="submit"
                className="rounded-md border border-zinc-300 px-3 py-2 text-sm font-medium text-zinc-700"
              >
                Sign out
              </button>
            </form>
          </div>
        </header>

        {error && (
          <div className="mb-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        <section className="mb-6 grid gap-4 md:grid-cols-3">
          <div className="rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm">
            <p className="text-xs uppercase tracking-[0.2em] text-zinc-500">
              Today
            </p>
            <p className="mt-3 text-2xl font-semibold">
              {dutyMap[formatDate(new Date())]?.scheduledMemberName ??
                "No duty"}
            </p>
          </div>

          <div className="rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm">
            <p className="text-xs uppercase tracking-[0.2em] text-zinc-500">
              Tomorrow
            </p>
            <p className="mt-3 text-2xl font-semibold">
              {dutyMap[formatDate(addDays(new Date(), 1))]
                ?.scheduledMemberName ?? "No duty"}
            </p>
          </div>

          <div className="rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm">
            <p className="text-xs uppercase tracking-[0.2em] text-zinc-500">
              Needs review
            </p>
            <p className="mt-3 text-2xl font-semibold">
              {duties.filter((duty) => duty.status === "PENDING_REVIEW").length}
            </p>
          </div>
        </section>

        <section className="rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold">Upcoming duties</h2>
            <span className="text-sm text-zinc-500">Next 7 days</span>
          </div>

          <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
            {days.map((day) => {
              const duty = dutyMap[day.key];
              const isToday = day.key === formatDate(new Date());

              return (
                <div
                  key={day.key}
                  className={`rounded-xl border p-4 ${
                    isToday
                      ? "border-black bg-zinc-900 text-white"
                      : "border-zinc-200 bg-zinc-50"
                  }`}
                >
                  <p
                    className={`text-xs uppercase tracking-[0.2em] ${isToday ? "text-zinc-300" : "text-zinc-500"}`}
                  >
                    {day.label}
                  </p>

                  <p className="mt-3 text-lg font-semibold">
                    {duty?.scheduledMemberName ?? "No assignment"}
                  </p>

                  <div className="mt-3 space-y-1 text-sm">
                    <p className={isToday ? "text-zinc-200" : "text-zinc-600"}>
                      {duty?.status === "PENDING_REVIEW"
                        ? "Needs review"
                        : (duty?.outcome ?? "Scheduled")}
                    </p>
                    {duty?.report && (
                      <p
                        className={isToday ? "text-zinc-300" : "text-zinc-500"}
                      >
                        {duty.report.type}: {duty.report.reportedByMemberName}
                      </p>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </section>
      </div>
    </main>
  );
}
