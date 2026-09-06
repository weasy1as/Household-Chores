export function createHoushold(request: Request): Promise<Response> {
  return fetch("/api/households", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(request),
  });
}
