using Microsoft.Extensions.Logging;
using MotorFinanceiro.Domain.Interfaces;

namespace MotorFinanceiro.Infrastructure.Services;

public sealed class NoOpEmailSender(ILogger<NoOpEmailSender> logger) : IEmailSender
{
    public Task SendEmailConfirmationAsync(string email, string name, string confirmationLink, CancellationToken ct = default)
    {
        logger.LogInformation("[EMAIL-STUB] Confirmation to {Email}: {Link}", email, confirmationLink);
        return Task.CompletedTask;
    }

    public Task SendPasswordResetAsync(string email, string resetLink, CancellationToken ct = default)
    {
        logger.LogInformation("[EMAIL-STUB] Password reset to {Email}: {Link}", email, resetLink);
        return Task.CompletedTask;
    }
}
