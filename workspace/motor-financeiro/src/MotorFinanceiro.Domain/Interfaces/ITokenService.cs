using MotorFinanceiro.Domain.Entities;

namespace MotorFinanceiro.Domain.Interfaces;

public interface ITokenService
{
    string GenerateAccessToken(User user);
    string GenerateRefreshToken();
    string HashToken(string token);
}
