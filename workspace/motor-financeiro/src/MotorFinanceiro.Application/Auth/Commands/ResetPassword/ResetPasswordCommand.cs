using MediatR;

namespace MotorFinanceiro.Application.Auth.Commands.ResetPassword;

public sealed record ResetPasswordCommand(
    Guid UserId,
    string Token,
    string NewPassword) : IRequest;
