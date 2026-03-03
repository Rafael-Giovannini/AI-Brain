namespace MotorFinanceiro.Application.Common.Exceptions;

public sealed class NotFoundException(string entity, object key)
    : Exception($"Entity \"{entity}\" ({key}) was not found.");
