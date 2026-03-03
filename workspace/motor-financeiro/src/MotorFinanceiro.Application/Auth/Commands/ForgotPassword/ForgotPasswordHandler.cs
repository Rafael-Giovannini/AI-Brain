using MediatR;
using MotorFinanceiro.Domain.Interfaces;

namespace MotorFinanceiro.Application.Auth.Commands.ForgotPassword;

public sealed class ForgotPasswordHandler(
    IUserRepository userRepository,
    IEmailSender emailSender) : IRequestHandler<ForgotPasswordCommand>
{
    public async Task Handle(ForgotPasswordCommand request, CancellationToken cancellationToken)
    {
        var user = await userRepository.GetByEmailAsync(request.Email.ToLowerInvariant().Trim(), cancellationToken);

        // FR-006: Always return success (don't reveal if email exists)
        if (user is null)
            return;

        // TODO: Generate password reset token, store it, build link
        var resetLink = $"https://app.motorfinanceiro.com/reset-password?token=placeholder&userId={user.Id}";
        await emailSender.SendPasswordResetAsync(user.Email, resetLink, cancellationToken);
    }
}
