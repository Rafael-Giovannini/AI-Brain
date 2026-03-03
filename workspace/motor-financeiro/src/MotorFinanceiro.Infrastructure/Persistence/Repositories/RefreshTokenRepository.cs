using Microsoft.EntityFrameworkCore;
using MotorFinanceiro.Domain.Entities;
using MotorFinanceiro.Domain.Interfaces;

namespace MotorFinanceiro.Infrastructure.Persistence.Repositories;

public class RefreshTokenRepository(MotorFinanceiroDbContext context) : IRefreshTokenRepository
{
    public async Task<RefreshToken?> GetByTokenHashAsync(string tokenHash, CancellationToken ct = default)
        => await context.RefreshTokens.FirstOrDefaultAsync(t => t.TokenHash == tokenHash, ct);

    public async Task<IReadOnlyList<RefreshToken>> GetByFamilyIdAsync(Guid familyId, CancellationToken ct = default)
        => await context.RefreshTokens.Where(t => t.FamilyId == familyId).ToListAsync(ct);

    public async Task<IReadOnlyList<RefreshToken>> GetActiveByUserIdAsync(Guid userId, CancellationToken ct = default)
        => await context.RefreshTokens
            .Where(t => t.UserId == userId && !t.IsRevoked && t.ExpiresAt > DateTime.UtcNow)
            .ToListAsync(ct);

    public async Task AddAsync(RefreshToken token, CancellationToken ct = default)
        => await context.RefreshTokens.AddAsync(token, ct);

    public async Task RevokeAllByFamilyIdAsync(Guid familyId, CancellationToken ct = default)
    {
        var tokens = await context.RefreshTokens
            .Where(t => t.FamilyId == familyId && !t.IsRevoked)
            .ToListAsync(ct);

        foreach (var token in tokens)
            token.Revoke();
    }

    public async Task RevokeAllByUserIdAsync(Guid userId, CancellationToken ct = default)
    {
        var tokens = await context.RefreshTokens
            .Where(t => t.UserId == userId && !t.IsRevoked)
            .ToListAsync(ct);

        foreach (var token in tokens)
            token.Revoke();
    }
}
