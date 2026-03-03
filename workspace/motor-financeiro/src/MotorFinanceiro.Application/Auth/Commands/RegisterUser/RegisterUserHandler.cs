using MediatR;
using MotorFinanceiro.Application.Common.Exceptions;
using MotorFinanceiro.Domain.Entities;
using MotorFinanceiro.Domain.Interfaces;

namespace MotorFinanceiro.Application.Auth.Commands.RegisterUser;

public sealed class RegisterUserHandler(
    IUserRepository userRepository,
    IPasswordHasher passwordHasher,
    IUnitOfWork unitOfWork) : IRequestHandler<RegisterUserCommand, Guid>
{
    public async Task<Guid> Handle(RegisterUserCommand request, CancellationToken cancellationToken)
    {
        var emailNormalized = request.Email.ToLowerInvariant().Trim();

        if (await userRepository.ExistsByEmailAsync(emailNormalized, cancellationToken))
            throw new ConflictException("Email já cadastrado.");

        var hash = passwordHasher.Hash(request.Password);
        var user = User.Create(emailNormalized, hash, request.Name);

        await userRepository.AddAsync(user, cancellationToken);
        await unitOfWork.SaveChangesAsync(cancellationToken);

        return user.Id;
    }
}
