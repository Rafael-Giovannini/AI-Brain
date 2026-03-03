using MediatR;
using MotorFinanceiro.Domain.Interfaces;

namespace MotorFinanceiro.Application.Auth.Commands.Logout;

public sealed class LogoutHandler(
    IRefreshTokenRepository refreshTokenRepository,
    ITokenService tokenService,
    IUnitOfWork unitOfWork) : IRequestHandler<LogoutCommand>
{
    public async Task Handle(LogoutCommand request, CancellationToken cancellationToken)
    {
        var tokenHash = tokenService.HashToken(request.RefreshToken);
        var storedToken = await refreshTokenRepository.GetByTokenHashAsync(tokenHash, cancellationToken);

        if (storedToken is null || storedToken.IsRevoked)
            return; // already logged out or invalid — no-op

        // Revoke entire family to invalidate all sessions for this login chain
        await refreshTokenRepository.RevokeAllByFamilyIdAsync(storedToken.FamilyId, cancellationToken);
        await unitOfWork.SaveChangesAsync(cancellationToken);
    }
}
