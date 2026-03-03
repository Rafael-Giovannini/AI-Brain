using FluentAssertions;
using Microsoft.Extensions.Configuration;
using NSubstitute;
using MotorFinanceiro.Application.Auth.Commands.Login;
using MotorFinanceiro.Application.Common.Exceptions;
using MotorFinanceiro.Domain.Entities;
using MotorFinanceiro.Domain.Interfaces;

namespace MotorFinanceiro.Application.Tests.Auth;

public class LoginHandlerTests
{
    private readonly IUserRepository _userRepository = Substitute.For<IUserRepository>();
    private readonly IPasswordHasher _passwordHasher = Substitute.For<IPasswordHasher>();
    private readonly ITokenService _tokenService = Substitute.For<ITokenService>();
    private readonly IRefreshTokenRepository _refreshTokenRepository = Substitute.For<IRefreshTokenRepository>();
    private readonly IUnitOfWork _unitOfWork = Substitute.For<IUnitOfWork>();
    private readonly IConfiguration _configuration;
    private readonly LoginHandler _handler;

    public LoginHandlerTests()
    {
        _configuration = new ConfigurationBuilder()
            .AddInMemoryCollection(new Dictionary<string, string?>
            {
                ["Jwt:RefreshTokenExpirationDays"] = "7"
            })
            .Build();

        _handler = new LoginHandler(
            _userRepository, _passwordHasher, _tokenService,
            _refreshTokenRepository, _unitOfWork, _configuration);
    }

    [Fact]
    public async Task Handle_ShouldReturnTokens_WhenCredentialsAreValid()
    {
        var user = User.Create("test@email.com", "hashed", "John");
        _userRepository.GetByEmailAsync("test@email.com", Arg.Any<CancellationToken>())
            .Returns(user);
        _passwordHasher.Verify("Password1!", "hashed").Returns(true);
        _tokenService.GenerateAccessToken(user).Returns("access-token");
        _tokenService.GenerateRefreshToken().Returns("refresh-token");
        _tokenService.HashToken("refresh-token").Returns("refresh-hash");

        var command = new LoginCommand("test@email.com", "Password1!");
        var result = await _handler.Handle(command, CancellationToken.None);

        result.AccessToken.Should().Be("access-token");
        result.RefreshToken.Should().Be("refresh-token");
        result.ExpiresAt.Should().BeCloseTo(DateTime.UtcNow.AddDays(7), TimeSpan.FromSeconds(10));
    }

    [Fact]
    public async Task Handle_ShouldThrow_WhenUserNotFound()
    {
        _userRepository.GetByEmailAsync(Arg.Any<string>(), Arg.Any<CancellationToken>())
            .Returns((User?)null);

        var command = new LoginCommand("wrong@email.com", "Password1!");
        var act = () => _handler.Handle(command, CancellationToken.None);

        await act.Should().ThrowAsync<AuthenticationException>()
            .WithMessage("*inválidas*");
    }

    [Fact]
    public async Task Handle_ShouldThrow_WhenPasswordIsWrong()
    {
        var user = User.Create("test@email.com", "hashed", "John");
        _userRepository.GetByEmailAsync(Arg.Any<string>(), Arg.Any<CancellationToken>())
            .Returns(user);
        _passwordHasher.Verify(Arg.Any<string>(), Arg.Any<string>()).Returns(false);

        var command = new LoginCommand("test@email.com", "WrongPass1!");
        var act = () => _handler.Handle(command, CancellationToken.None);

        await act.Should().ThrowAsync<AuthenticationException>()
            .WithMessage("*inválidas*");
    }

    [Fact]
    public async Task Handle_ShouldThrow_WhenAccountIsDeactivated()
    {
        var user = User.Create("test@email.com", "hashed", "John");
        user.Deactivate();
        _userRepository.GetByEmailAsync(Arg.Any<string>(), Arg.Any<CancellationToken>())
            .Returns(user);

        var command = new LoginCommand("test@email.com", "Password1!");
        var act = () => _handler.Handle(command, CancellationToken.None);

        await act.Should().ThrowAsync<AuthenticationException>()
            .WithMessage("*desativada*");
    }
}
