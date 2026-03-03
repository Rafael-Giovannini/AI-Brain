using Microsoft.EntityFrameworkCore;
using MotorFinanceiro.Domain.Entities;
using MotorFinanceiro.Domain.Interfaces;

namespace MotorFinanceiro.Infrastructure.Persistence;

public class MotorFinanceiroDbContext : DbContext, IUnitOfWork
{
    public MotorFinanceiroDbContext(DbContextOptions<MotorFinanceiroDbContext> options)
        : base(options) { }

    public DbSet<User> Users => Set<User>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.ApplyConfigurationsFromAssembly(typeof(MotorFinanceiroDbContext).Assembly);
        base.OnModelCreating(modelBuilder);
    }

    public override async Task<int> SaveChangesAsync(CancellationToken ct = default)
    {
        foreach (var entry in ChangeTracker.Entries<BaseEntity>()
            .Where(e => e.State == EntityState.Modified))
        {
            entry.Entity.MarkUpdated();
        }

        return await base.SaveChangesAsync(ct);
    }
}
