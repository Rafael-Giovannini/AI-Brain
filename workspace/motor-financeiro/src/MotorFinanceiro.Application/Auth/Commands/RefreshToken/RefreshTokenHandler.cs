using MediatR;
using Microsoft.Extensions.Configuration;
using MotorFinanceiro.Application.Auth.DTOs;
using MotorFinanceiro.Application.Common.Exceptions;
using MotorFinanceiro.Domain.Interfaces;

namespace MotorFinanceiro.Application.Auth.Commands.RefreshToken;

public sealed class RefreshTokenHandler(
    IRefreshTokenRepository refreshTokenRepository,
    IUserRepository userRepository,
    ITokenService tokenService,
    IUnitOfWork unitOfWork,
    IConfiguration configuration) : IRequestHandler<RefreshTokenCommand, AuthResponse>
{
    public async Task<AuthResponse> Handle(RefreshTokenCommand request, CancellationToken cancellationToken)
    {
        var tokenHash = tokenService.HashToken(request.Token);
        var storedToken = await refreshTokenRepository.GetByTokenHashAsync(tokenHash, cancellationToken)
            ?? throw new AuthenticationException("Token inválido.");

        // FR-004: Reuse detection — if token is already revoked, revoke entire family
        if (storedToken.IsRevoked)
        {
            await refreshTokenRepository.RevokeAllByFamilyIdAsync(storedToken.FamilyId, cancellationToken);
            await unitOfWork.SaveChangesAsync(cancellationToken);
            throw new AuthenticationException("Token reutilizado — todas as sessões foram revogadas.");
        }

        if (storedToken.IsExpired)
            throw new AuthenticationException("Token expirado.");

        var user = await userRepository.GetByIdAsync(storedToken.UserId, cancellationToken)
            ?? throw new AuthenticationException("Usuário não encontrado.");

        if (!user.IsActive)
            throw new AuthenticationException("Conta desativada.");

        // Rotate: revoke old token, create new one in same family
        var newRawToken = tokenService.GenerateRefreshToken();
        var newTokenHash = tokenService.HashToken(newRawToken);
        var refreshDays = configuration.GetValue("Jwt:RefreshTokenExpirationDays", 7);
        var expiresAt = DateTime.UtcNow.AddDays(refreshDays);

        var newRefreshToken = Domain.Entities.RefreshToken.Create(
            user.Id,
            newTokenHash,
            storedToken.FamilyId,
            storedToken.Generation + 1,
            expiresAt,
            request.UserAgent,
            request.IpAddress);

        storedToken.Revoke(newRefreshToken.Id);
        await refreshTokenRepository.AddAsync(newRefreshToken, cancellationToken);
        await unitOfWork.SaveChangesAsync(cancellationToken);

        var accessToken = tokenService.GenerateAccessToken(user);
        return new AuthResponse(accessToken, newRawToken, expiresAt);
    }
}
