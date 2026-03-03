using FluentAssertions;
using MotorFinanceiro.Domain.Entities;

namespace MotorFinanceiro.Domain.Tests.Entities;

public class UserTests
{
    [Fact]
    public void Create_ShouldNormalizeEmail()
    {
        var user = User.Create("  TEST@Email.COM  ", "hash", "John");

        user.Email.Should().Be("test@email.com");
    }

    [Fact]
    public void Create_ShouldTrimName()
    {
        var user = User.Create("test@email.com", "hash", "  John Doe  ");

        user.Name.Should().Be("John Doe");
    }

    [Fact]
    public void Create_ShouldSetDefaults()
    {
        var user = User.Create("test@email.com", "hash", "John");

        user.Id.Should().NotBeEmpty();
        user.IsEmailConfirmed.Should().BeFalse();
        user.IsActive.Should().BeTrue();
        user.DeactivatedAt.Should().BeNull();
        user.CreatedAt.Should().BeCloseTo(DateTime.UtcNow, TimeSpan.FromSeconds(5));
    }

    [Fact]
    public void ConfirmEmail_ShouldSetFlag()
    {
        var user = User.Create("test@email.com", "hash", "John");

        user.ConfirmEmail();

        user.IsEmailConfirmed.Should().BeTrue();
    }

    [Fact]
    public void UpdateName_ShouldTrimAndUpdateTimestamp()
    {
        var user = User.Create("test@email.com", "hash", "John");
        var originalUpdatedAt = user.UpdatedAt;

        user.UpdateName("  Jane Doe  ");

        user.Name.Should().Be("Jane Doe");
        user.UpdatedAt.Should().BeOnOrAfter(originalUpdatedAt);
    }

    [Fact]
    public void UpdatePasswordHash_ShouldUpdateHash()
    {
        var user = User.Create("test@email.com", "oldhash", "John");

        user.UpdatePasswordHash("newhash");

        user.PasswordHash.Should().Be("newhash");
    }

    [Fact]
    public void Deactivate_ShouldSetInactiveAndTimestamp()
    {
        var user = User.Create("test@email.com", "hash", "John");

        user.Deactivate();

        user.IsActive.Should().BeFalse();
        user.DeactivatedAt.Should().NotBeNull();
        user.DeactivatedAt.Should().BeCloseTo(DateTime.UtcNow, TimeSpan.FromSeconds(5));
    }
}
