using MediatR;

namespace MotorFinanceiro.Application.Auth.Commands.ConfirmEmail;

public sealed record ConfirmEmailCommand(Guid UserId) : IRequest;
