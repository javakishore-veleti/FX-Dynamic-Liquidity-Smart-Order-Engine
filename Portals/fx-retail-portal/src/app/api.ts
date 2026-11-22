/** Middleware base URLs — same host as the portal (any interface / remote host). */
const host =
  typeof window !== 'undefined' && window.location?.hostname
    ? window.location.hostname
    : 'localhost';

export const API = {
  quote: `http://${host}:8081`,
  qualify: `http://${host}:8082`,
  routeExec: `http://${host}:8083`,
  admin: `http://${host}:8084`,
} as const;
