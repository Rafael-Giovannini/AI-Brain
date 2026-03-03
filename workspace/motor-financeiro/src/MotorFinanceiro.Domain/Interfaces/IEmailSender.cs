namespace MotorFinanceiro.Domain.Interfaces;

public interface IEmailSender
{
    Task SendEmailConfirmationAsync(string email, string name, string confirmationLink, CancellationToken ct = default);
    Task SendPasswordResetAsync(string email, string resetLink, CancellationToken ct = default);
}
