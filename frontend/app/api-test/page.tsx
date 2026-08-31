"use client";

import { useState } from "react";
import { createClient } from "@/lib/supabase/client";

export default function ApiTestPage() {
  const [result, setResult] = useState("");
  const [loading, setLoading] = useState(false);

  async function createHousehold() {
    setLoading(true);
    setResult("");

    try {
      const supabase = createClient();

      const {
        data: { session },
      } = await supabase.auth.getSession();

      if (!session) {
        setResult("You are not logged in.");
        return;
      }

      const response = await fetch("http://localhost:8080/api/households", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${session.access_token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          name: "My Household",
          timezone: "Europe/Copenhagen",
        }),
      });

      const data = await response.json();

      setResult(
        JSON.stringify(
          {
            status: response.status,
            data,
          },
          null,
          2,
        ),
      );
    } catch (error) {
      setResult(
        error instanceof Error ? error.message : "Something went wrong.",
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <main>
      <h1>API Test</h1>

      <button onClick={createHousehold} disabled={loading}>
        {loading ? "Creating..." : "Create Household"}
      </button>

      {result && <pre>{result}</pre>}
    </main>
  );
}
