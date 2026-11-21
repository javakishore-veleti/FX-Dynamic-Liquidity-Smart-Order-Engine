/** Middleware / agent base URLs — same host as the portal (any interface / remote host). */
const host =
  typeof window !== 'undefined' && window.location?.hostname
    ? window.location.hostname
    : 'localhost';

export const API = {
  admin: `http://${host}:8084`,
  topology: `http://${host}:8085`,
  topologyMcp: `http://${host}:8091`,
  agentOrchestrator: `http://${host}:8092`,
  topologyMcpPython: `http://${host}:8191`,
  agentOrchestratorPython: `http://${host}:8192`,
} as const;
