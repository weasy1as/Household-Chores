"use server";

import { revalidatePath } from "next/cache";
import { createClient } from "@/lib/supabase/server";

const API_URL = "http://localhost:8080";

export async function createHousehold(formData: FormData) {
  const name = formData.get("name");
  const timezone = formData.get("timezone");

  if (typeof name !== "string" || !name.trim()) {
    throw new Error("Household name is required");
  }

  if (typeof timezone !== "string" || !timezone.trim()) {
    throw new Error("Timezone is required");
  }

  const supabase = await createClient();

  const {
    data: { session },
  } = await supabase.auth.getSession();

  if (!session) {
    throw new Error("You must be logged in");
  }

  const response = await fetch(`${API_URL}/api/households`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${session.access_token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      name: name.trim(),
      timezone: timezone.trim(),
    }),
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error("Failed to create household");
  }

  revalidatePath("/dashboard");
}
export async function addMember(householdId: string, formData: FormData) {
  const email = formData.get("email");

  if (typeof email !== "string" || !email.trim()) {
    throw new Error("Email is required");
  }

  const supabase = await createClient();

  const {
    data: { session },
  } = await supabase.auth.getSession();

  if (!session) {
    throw new Error("You must be logged in");
  }

  const response = await fetch(
    `${API_URL}/api/households/${householdId}/members`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${session.access_token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        email: email.trim(),
      }),
      cache: "no-store",
    },
  );

  if (!response.ok) {
    const errorText = await response.text();

    throw new Error(`Failed to add member: ${response.status} ${errorText}`);
  }

  revalidatePath("/dashboard");
}

export async function updateMemberStatus(
  householdId: string,
  userId: string,
  status: "ACTIVE" | "INACTIVE",
) {
  const supabase = await createClient();

  const {
    data: { session },
  } = await supabase.auth.getSession();

  if (!session) {
    throw new Error("You must be logged in");
  }

  const response = await fetch(
    `${API_URL}/api/households/${householdId}/members/${userId}/status?status=${status}`,
    {
      method: "PATCH",
      headers: {
        Authorization: `Bearer ${session.access_token}`,
        "Content-Type": "application/json",
      },
      cache: "no-store",
    },
  );

  if (!response.ok) {
    const errorText = await response.text();

    throw new Error(
      `Failed to update member status: ${response.status} ${errorText}`,
    );
  }

  revalidatePath("/dashboard");
}

export async function removeMember(householdId: string, userId: string) {
  const supabase = await createClient();

  const {
    data: { session },
  } = await supabase.auth.getSession();

  if (!session) {
    throw new Error("You must be logged in");
  }

  const response = await fetch(
    `${API_URL}/api/households/${householdId}/members/${userId}`,
    {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${session.access_token}`,
        "Content-Type": "application/json",
      },
      cache: "no-store",
    },
  );

  if (!response.ok) {
    const errorText = await response.text();

    throw new Error(`Failed to remove member: ${response.status} ${errorText}`);
  }

  revalidatePath("/dashboard");
}
export async function updateRotationPosition(
  householdId: string,
  userId: string,
  position: number,
) {
  const supabase = await createClient();

  const {
    data: { session },
  } = await supabase.auth.getSession();

  if (!session) {
    throw new Error("You must be logged in");
  }

  const response = await fetch(
    `${API_URL}/api/households/${householdId}/members/${userId}/rotation-position?position=${position}`,
    {
      method: "PATCH",
      headers: {
        Authorization: `Bearer ${session.access_token}`,
        "Content-Type": "application/json",
      },
      cache: "no-store",
    },
  );

  if (!response.ok) {
    const errorText = await response.text();

    throw new Error(
      `Failed to update rotation position: ${response.status} ${errorText}`,
    );
  }

  revalidatePath("/dashboard");
}

export async function setRotationStartingPoint(
  householdId: string,
  startPosition: number,
) {
  const supabase = await createClient();

  const {
    data: { session },
  } = await supabase.auth.getSession();

  if (!session) {
    throw new Error("You must be logged in");
  }

  const response = await fetch(
    `${API_URL}/api/households/${householdId}/rotation-start?startPosition=${startPosition}`,
    {
      method: "PATCH",
      headers: {
        Authorization: `Bearer ${session.access_token}`,
        "Content-Type": "application/json",
      },
      cache: "no-store",
    },
  );

  if (!response.ok) {
    const errorText = await response.text();

    throw new Error(
      `Failed to set rotation starting point: ${response.status} ${errorText}`,
    );
  }

  revalidatePath("/dashboard");
}

export async function resolveDuty(
  householdId: string,
  dutyId: string,
  outcome: "COMPLETED" | "COVERED" | "PAID" | "SWITCHED" | "MISSED",
  completedByMemberId?: string,
) {
  const supabase = await createClient();

  const {
    data: { session },
  } = await supabase.auth.getSession();

  if (!session) {
    throw new Error("You must be logged in");
  }

  const response = await fetch(
    `${API_URL}/api/households/${householdId}/duties/${dutyId}/resolve`,
    {
      method: "PUT",
      headers: {
        Authorization: `Bearer ${session.access_token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        outcome,
        completedByMemberId: completedByMemberId ?? null,
      }),
      cache: "no-store",
    },
  );

  if (!response.ok) {
    const errorText = await response.text();

    throw new Error(`Failed to resolve duty: ${response.status} ${errorText}`);
  }

  revalidatePath("/dashboard");

  return response.json();
}
