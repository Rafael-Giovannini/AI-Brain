using MediatR;
using MotorFinanceiro.Application.Common.Exceptions;
using MotorFinanceiro.Domain.Interfaces;

namespace MotorFinanceiro.Application.Auth.Commands.ConfirmEmail;

public sealed class ConfirmEmailHandler(
    IUserRepository userRepository,
    IUnitOfWork unitOfWork) : IRequestHandler<ConfirmEmailCommand>
{
    public async Task Handle(ConfirmEmailCommand request, CancellationToken cancellationToken)
    {
        var user = await userRepository.GetByIdAsync(request.UserId, cancellationToken)
            ?? throw new NotFoundException("User", request.UserId);

        user.ConfirmEmail();
        await unitOfWork.SaveChangesAsync(cancellationToken);
    }
}
