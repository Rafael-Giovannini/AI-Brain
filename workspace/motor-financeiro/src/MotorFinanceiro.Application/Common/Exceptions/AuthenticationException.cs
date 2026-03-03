namespace MotorFinanceiro.Application.Common.Exceptions;

public sealed class AuthenticationException(string message) : Exception(message);
