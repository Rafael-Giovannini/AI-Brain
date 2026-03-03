using MediatR;

namespace MotorFinanceiro.Application.Auth.Commands.RegisterUser;

public sealed record RegisterUserCommand(
    string Email,
    string Password,
    string Name) : IRequest<Guid>;
