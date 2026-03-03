using MediatR;
using MotorFinanceiro.Application.Common.Exceptions;
using MotorFinanceiro.Domain.Interfaces;

namespace MotorFinanceiro.Application.Auth.Commands.ResetPassword;

public sealed class ResetPasswordHandler(
    IUserRepository userRepository,
    IPasswordHasher passwordHasher,
    IRefreshTokenRepository refreshTokenRepository,
    IUnitOfWork unitOfWork) : IRequestHandler<ResetPasswordCommand>
{
    public async Task Handle(ResetPasswordCommand request, CancellationToken cancellationToken)
    {
        // TODO: Validate reset token (stored hash + expiration)
        var user = await userRepository.GetByIdAsync(request.UserId, cancellationToken)
            ?? throw new NotFoundException("User", request.UserId);

        var hash = passwordHasher.Hash(request.NewPassword);
        user.UpdatePasswordHash(hash);

        // FR-007: Invalidate all sessions after password change
        await refreshTokenRepository.RevokeAllByUserIdAsync(user.Id, cancellationToken);
        await unitOfWork.SaveChangesAsync(cancellationToken);
    }
}
