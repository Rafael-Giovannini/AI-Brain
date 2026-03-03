using FluentAssertions;
using MotorFinanceiro.Domain.Entities;

namespace MotorFinanceiro.Domain.Tests.Entities;

public class RefreshTokenTests
{
    [Fact]
    public void Create_ShouldSetAllProperties()
    {
        var userId = Guid.NewGuid();
        var familyId = Guid.NewGuid();
        var expiresAt = DateTime.UtcNow.AddDays(7);

        var token = RefreshToken.Create(userId, "hash123", familyId, 0, expiresAt, "Chrome", "127.0.0.1");

        token.UserId.Should().Be(userId);
        token.TokenHash.Should().Be("hash123");
        token.FamilyId.Should().Be(familyId);
        token.Generation.Should().Be(0);
        token.ExpiresAt.Should().Be(expiresAt);
        token.UserAgent.Should().Be("Chrome");
        token.IpAddress.Should().Be("127.0.0.1");
        token.IsRevoked.Should().BeFalse();
        token.ReplacedById.Should().BeNull();
    }

    [Fact]
    public void IsActive_ShouldReturnTrue_WhenNotRevokedAndNotExpired()
    {
        var token = RefreshToken.Create(Guid.NewGuid(), "hash", Guid.NewGuid(), 0, DateTime.UtcNow.AddDays(7));

        token.IsActive.Should().BeTrue();
    }

    [Fact]
    public void IsActive_ShouldReturnFalse_WhenRevoked()
    {
        var token = RefreshToken.Create(Guid.NewGuid(), "hash", Guid.NewGuid(), 0, DateTime.UtcNow.AddDays(7));

        token.Revoke();

        token.IsActive.Should().BeFalse();
        token.IsRevoked.Should().BeTrue();
    }

    [Fact]
    public void IsExpired_ShouldReturnTrue_WhenPastExpiration()
    {
        var token = RefreshToken.Create(Guid.NewGuid(), "hash", Guid.NewGuid(), 0, DateTime.UtcNow.AddMinutes(-1));

        token.IsExpired.Should().BeTrue();
        token.IsActive.Should().BeFalse();
    }

    [Fact]
    public void Revoke_ShouldSetReplacedById()
    {
        var token = RefreshToken.Create(Guid.NewGuid(), "hash", Guid.NewGuid(), 0, DateTime.UtcNow.AddDays(7));
        var replacementId = Guid.NewGuid();

        token.Revoke(replacementId);

        token.IsRevoked.Should().BeTrue();
        token.ReplacedById.Should().Be(replacementId);
    }
}
