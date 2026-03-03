using MediatR;

namespace MotorFinanceiro.Application.Auth.Commands.Logout;

public sealed record LogoutCommand(string RefreshToken) : IRequest;
