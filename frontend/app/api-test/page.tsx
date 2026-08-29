"use client";

import { useState } from "react";
import { createClient } from "@/lib/supabase/client";

export default function ApiTestPage() {
  const [result, setResult] = useState("");
  const [error, setError] = useState("");

  async function testBackend() {
    setResult("");
    setError("");

    const supabase = createClient();

    const {
      data: { session },
    } = await supabase.auth.getSession();

    if (!session) {
      setError("You are not logged in.");
      return;
    }

    const response = await fetch("http://localhost:8080/api/test", {
      headers: {
        Authorization: `Bearer ${session.access_token}`,
      },
    });

    if (!response.ok) {
      setError(`Backend returned ${response.status}`);
      return;
    }

    const data = await response.json();

    setResult(JSON.stringify(data, null, 2));
  }

  return (
    <main>
      <h1>Backend Authentication Test</h1>

      <button onClick={testBackend}>Test Backend</button>

      {error && <p>{error}</p>}

      {result && <pre>{result}</pre>}
    </main>
  );
}
