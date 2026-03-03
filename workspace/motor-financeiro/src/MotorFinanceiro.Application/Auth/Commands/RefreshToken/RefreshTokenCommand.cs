using MediatR;
using MotorFinanceiro.Application.Auth.DTOs;

namespace MotorFinanceiro.Application.Auth.Commands.RefreshToken;

public sealed record RefreshTokenCommand(
    string Token,
    string? UserAgent = null,
    string? IpAddress = null) : IRequest<AuthResponse>;
