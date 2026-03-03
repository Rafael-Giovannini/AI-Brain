namespace MotorFinanceiro.Application.Auth.DTOs;

public sealed record AuthResponse(
    string AccessToken,
    string RefreshToken,
    DateTime ExpiresAt);
