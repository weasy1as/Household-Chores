"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { createClient } from "@/lib/supabase/client";

export default function HomePage() {
  const router = useRouter();

  useEffect(() => {
    async function redirectUser() {
      const supabase = createClient();
      const {
        data: { session },
      } = await supabase.auth.getSession();

      router.replace(session ? "/plan" : "/login");
    }

    redirectUser();
  }, [router]);

  return null;
}
