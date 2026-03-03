using MediatR;
using MotorFinanceiro.Application.Auth.DTOs;

namespace MotorFinanceiro.Application.Auth.Commands.Login;

public sealed record LoginCommand(
    string Email,
    string Password,
    string? UserAgent = null,
    string? IpAddress = null) : IRequest<AuthResponse>;
