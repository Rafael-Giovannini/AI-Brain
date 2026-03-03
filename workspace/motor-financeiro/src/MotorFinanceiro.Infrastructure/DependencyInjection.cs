using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using MotorFinanceiro.Domain.Interfaces;
using MotorFinanceiro.Infrastructure.Persistence;
using MotorFinanceiro.Infrastructure.Persistence.Repositories;
using MotorFinanceiro.Infrastructure.Services;

namespace MotorFinanceiro.Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration configuration)
    {
        // PostgreSQL + EF Core
        services.AddDbContext<MotorFinanceiroDbContext>(options =>
            options.UseNpgsql(
                configuration.GetConnectionString("PostgreSQL"),
                npgsql => npgsql.MigrationsAssembly(typeof(MotorFinanceiroDbContext).Assembly.FullName)));

        // Repositories
        services.AddScoped<IUnitOfWork>(sp => sp.GetRequiredService<MotorFinanceiroDbContext>());
        services.AddScoped<IUserRepository, UserRepository>();
        services.AddScoped<IRefreshTokenRepository, RefreshTokenRepository>();

        // Services
        services.AddSingleton<IPasswordHasher, PasswordHasher>();
        services.AddSingleton<ITokenService, TokenService>();
        services.AddSingleton<IEmailSender, NoOpEmailSender>();

        return services;
    }
}
