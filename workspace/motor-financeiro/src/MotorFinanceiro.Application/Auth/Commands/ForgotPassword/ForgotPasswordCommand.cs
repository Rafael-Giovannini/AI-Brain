using MediatR;

namespace MotorFinanceiro.Application.Auth.Commands.ForgotPassword;

// FR-006: Does NOT reveal whether email exists
public sealed record ForgotPasswordCommand(string Email) : IRequest;
