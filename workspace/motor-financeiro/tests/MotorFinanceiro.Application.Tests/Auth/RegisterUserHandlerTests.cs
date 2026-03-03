using FluentAssertions;
using NSubstitute;
using MotorFinanceiro.Application.Auth.Commands.RegisterUser;
using MotorFinanceiro.Application.Common.Exceptions;
using MotorFinanceiro.Domain.Entities;
using MotorFinanceiro.Domain.Interfaces;

namespace MotorFinanceiro.Application.Tests.Auth;

public class RegisterUserHandlerTests
{
    private readonly IUserRepository _userRepository = Substitute.For<IUserRepository>();
    private readonly IPasswordHasher _passwordHasher = Substitute.For<IPasswordHasher>();
    private readonly IUnitOfWork _unitOfWork = Substitute.For<IUnitOfWork>();
    private readonly RegisterUserHandler _handler;

    public RegisterUserHandlerTests()
    {
        _handler = new RegisterUserHandler(_userRepository, _passwordHasher, _unitOfWork);
    }

    [Fact]
    public async Task Handle_ShouldCreateUser_WhenEmailDoesNotExist()
    {
        var command = new RegisterUserCommand("test@email.com", "Password1!", "John Doe");
        _userRepository.ExistsByEmailAsync(Arg.Any<string>(), Arg.Any<CancellationToken>())
            .Returns(false);
        _passwordHasher.Hash("Password1!").Returns("hashed");

        var result = await _handler.Handle(command, CancellationToken.None);

        result.Should().NotBeEmpty();
        await _userRepository.Received(1).AddAsync(Arg.Is<User>(u => u.Email == "test@email.com"), Arg.Any<CancellationToken>());
        await _unitOfWork.Received(1).SaveChangesAsync(Arg.Any<CancellationToken>());
    }

    [Fact]
    public async Task Handle_ShouldThrowConflict_WhenEmailAlreadyExists()
    {
        var command = new RegisterUserCommand("test@email.com", "Password1!", "John Doe");
        _userRepository.ExistsByEmailAsync(Arg.Any<string>(), Arg.Any<CancellationToken>())
            .Returns(true);

        var act = () => _handler.Handle(command, CancellationToken.None);

        await act.Should().ThrowAsync<ConflictException>()
            .WithMessage("*já cadastrado*");
    }

    [Fact]
    public async Task Handle_ShouldNormalizeEmail()
    {
        var command = new RegisterUserCommand("  TEST@EMAIL.COM  ", "Password1!", "John");
        _userRepository.ExistsByEmailAsync("test@email.com", Arg.Any<CancellationToken>())
            .Returns(false);
        _passwordHasher.Hash("Password1!").Returns("hashed");

        await _handler.Handle(command, CancellationToken.None);

        await _userRepository.Received(1).ExistsByEmailAsync("test@email.com", Arg.Any<CancellationToken>());
    }
}
