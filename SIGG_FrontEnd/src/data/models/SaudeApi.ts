// Resposta de GET /health/db no backend (HealthCheckController)
export default interface SaudeApi {
  banco: 'UP' | 'DOWN'
  erro?: string
}
