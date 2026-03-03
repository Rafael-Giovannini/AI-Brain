using FluentAssertions;
using MotorFinanceiro.Application.Auth.Commands.RegisterUser;

namespace MotorFinanceiro.Application.Tests.Auth;

public class RegisterUserValidatorTests
{
    private readonly RegisterUserValidator _validator = new();

    [Fact]
    public void ShouldPass_WhenAllFieldsAreValid()
    {
        var command = new RegisterUserCommand("test@email.com", "Password1!", "John Doe");

        var result = _validator.Validate(command);

        result.IsValid.Should().BeTrue();
    }

    [Theory]
    [InlineData("")]
    [InlineData("invalid")]
    [InlineData("@nolocal.com")]
    public void ShouldFail_WhenEmailIsInvalid(string email)
    {
        var command = new RegisterUserCommand(email, "Password1!", "John");

        var result = _validator.Validate(command);

        result.IsValid.Should().BeFalse();
    }

    [Theory]
    [InlineData("short1!")]          // < 8 chars
    [InlineData("nouppercase1!")]    // no uppercase
    [InlineData("NoSpecial1")]       // no special char
    [InlineData("NoNumber!!")]       // no number
    public void ShouldFail_WhenPasswordIsWeak(string password)
    {
        var command = new RegisterUserCommand("test@email.com", password, "John");

        var result = _validator.Validate(command);

        result.IsValid.Should().BeFalse();
    }

    [Fact]
    public void ShouldFail_WhenNameIsEmpty()
    {
        var command = new RegisterUserCommand("test@email.com", "Password1!", "");

        var result = _validator.Validate(command);

        result.IsValid.Should().BeFalse();
    }
}
