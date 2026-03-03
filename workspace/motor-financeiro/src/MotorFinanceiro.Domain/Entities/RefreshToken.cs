namespace MotorFinanceiro.Domain.Entities;

public class RefreshToken : BaseEntity
{
    public Guid UserId { get; private set; }
    public string TokenHash { get; private set; } = null!;
    public Guid FamilyId { get; private set; }
    public int Generation { get; private set; }
    public bool IsRevoked { get; private set; }
    public Guid? ReplacedById { get; private set; }
    public string? UserAgent { get; private set; }
    public string? IpAddress { get; private set; }
    public DateTime ExpiresAt { get; private set; }

    // Navigation
    public User User { get; private set; } = null!;
    public RefreshToken? ReplacedBy { get; private set; }

    private RefreshToken() { } // EF Core

    public static RefreshToken Create(
        Guid userId,
        string tokenHash,
        Guid familyId,
        int generation,
        DateTime expiresAt,
        string? userAgent = null,
        string? ipAddress = null)
    {
        return new RefreshToken
        {
            UserId = userId,
            TokenHash = tokenHash,
            FamilyId = familyId,
            Generation = generation,
            ExpiresAt = expiresAt,
            UserAgent = userAgent,
            IpAddress = ipAddress
        };
    }

    public bool IsExpired => DateTime.UtcNow >= ExpiresAt;
    public bool IsActive => !IsRevoked && !IsExpired;

    public void Revoke(Guid? replacedById = null)
    {
        IsRevoked = true;
        ReplacedById = replacedById;
    }
}
