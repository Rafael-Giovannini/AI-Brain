using FluentAssertions;
using MotorFinanceiro.Domain.Entities;

namespace MotorFinanceiro.Domain.Tests;

public class UserTests
{
    [Fact]
    public void Create_ShouldSetEmailLowercase()
    {
        var user = User.Create("Test@Example.COM", "hash", "John");
        user.Email.Should().Be("test@example.com");
    }

    [Fact]
    public void Create_ShouldSetNameTrimmed()
    {
        var user = User.Create("test@example.com", "hash", "  John  ");
        user.Name.Should().Be("John");
    }

    [Fact]
    public void Create_ShouldGenerateId()
    {
        var user = User.Create("test@example.com", "hash", "John");
        user.Id.Should().NotBeEmpty();
    }

    [Fact]
    public void ConfirmEmail_ShouldSetIsEmailConfirmed()
    {
        var user = User.Create("test@example.com", "hash", "John");
        user.ConfirmEmail();
        user.IsEmailConfirmed.Should().BeTrue();
    }

    [Fact]
    public void Deactivate_ShouldSetIsActiveFalse()
    {
        var user = User.Create("test@example.com", "hash", "John");
        user.Deactivate();
        user.IsActive.Should().BeFalse();
        user.DeactivatedAt.Should().NotBeNull();
    }
}
