using MediatR;
using Microsoft.Extensions.Configuration;
using MotorFinanceiro.Application.Auth.DTOs;
using MotorFinanceiro.Application.Common.Exceptions;
using MotorFinanceiro.Domain.Interfaces;

namespace MotorFinanceiro.Application.Auth.Commands.Login;

public sealed class LoginHandler(
    IUserRepository userRepository,
    IPasswordHasher passwordHasher,
    ITokenService tokenService,
    IRefreshTokenRepository refreshTokenRepository,
    IUnitOfWork unitOfWork,
    IConfiguration configuration) : IRequestHandler<LoginCommand, AuthResponse>
{
    public async Task<AuthResponse> Handle(LoginCommand request, CancellationToken cancellationToken)
    {
        var user = await userRepository.GetByEmailAsync(request.Email.ToLowerInvariant().Trim(), cancellationToken)
            ?? throw new AuthenticationException("Credenciais inválidas.");

        if (!user.IsActive)
            throw new AuthenticationException("Conta desativada.");

        if (!passwordHasher.Verify(request.Password, user.PasswordHash))
            throw new AuthenticationException("Credenciais inválidas.");

        var accessToken = tokenService.GenerateAccessToken(user);
        var rawRefreshToken = tokenService.GenerateRefreshToken();
        var refreshTokenHash = tokenService.HashToken(rawRefreshToken);

        var refreshDays = configuration.GetValue("Jwt:RefreshTokenExpirationDays", 7);
        var expiresAt = DateTime.UtcNow.AddDays(refreshDays);
        var familyId = Guid.CreateVersion7();

        var refreshToken = Domain.Entities.RefreshToken.Create(
            user.Id,
            refreshTokenHash,
            familyId,
            generation: 0,
            expiresAt,
            request.UserAgent,
            request.IpAddress);

        await refreshTokenRepository.AddAsync(refreshToken, cancellationToken);
        await unitOfWork.SaveChangesAsync(cancellationToken);

        return new AuthResponse(accessToken, rawRefreshToken, expiresAt);
    }
}
