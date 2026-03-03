using System.Security.Cryptography;
using Microsoft.AspNetCore.Cryptography.KeyDerivation;
using MotorFinanceiro.Domain.Interfaces;

namespace MotorFinanceiro.Infrastructure.Services;

public sealed class PasswordHasher : IPasswordHasher
{
    private const int SaltSize = 16;
    private const int HashSize = 32;
    private const int Iterations = 100_000;
    private const KeyDerivationPrf Prf = KeyDerivationPrf.HMACSHA512;

    public string Hash(string password)
    {
        var salt = RandomNumberGenerator.GetBytes(SaltSize);
        var hash = KeyDerivation.Pbkdf2(password, salt, Prf, Iterations, HashSize);

        var result = new byte[SaltSize + HashSize];
        salt.CopyTo(result, 0);
        hash.CopyTo(result, SaltSize);

        return Convert.ToBase64String(result);
    }

    public bool Verify(string password, string storedHash)
    {
        var decoded = Convert.FromBase64String(storedHash);
        if (decoded.Length != SaltSize + HashSize)
            return false;

        var salt = decoded[..SaltSize];
        var expectedHash = decoded[SaltSize..];

        var actualHash = KeyDerivation.Pbkdf2(password, salt, Prf, Iterations, HashSize);
        return CryptographicOperations.FixedTimeEquals(actualHash, expectedHash);
    }
}
