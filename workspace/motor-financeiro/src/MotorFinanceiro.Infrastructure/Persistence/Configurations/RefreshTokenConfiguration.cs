using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using MotorFinanceiro.Domain.Entities;

namespace MotorFinanceiro.Infrastructure.Persistence.Configurations;

public class RefreshTokenConfiguration : IEntityTypeConfiguration<RefreshToken>
{
    public void Configure(EntityTypeBuilder<RefreshToken> builder)
    {
        builder.ToTable("refresh_tokens");

        builder.HasKey(t => t.Id);

        builder.Property(t => t.TokenHash)
            .HasMaxLength(128)
            .IsRequired();

        builder.Property(t => t.FamilyId)
            .IsRequired();

        builder.Property(t => t.Generation)
            .HasDefaultValue(0)
            .IsRequired();

        builder.Property(t => t.IsRevoked)
            .HasDefaultValue(false)
            .IsRequired();

        builder.Property(t => t.UserAgent)
            .HasMaxLength(500);

        builder.Property(t => t.IpAddress)
            .HasMaxLength(45);

        builder.Property(t => t.ExpiresAt)
            .IsRequired();

        builder.Property(t => t.CreatedAt)
            .HasDefaultValueSql("now()");

        // Indexes
        builder.HasIndex(t => t.UserId)
            .HasDatabaseName("idx_refresh_tokens_user");

        builder.HasIndex(t => t.TokenHash)
            .HasDatabaseName("idx_refresh_tokens_token_hash");

        builder.HasIndex(t => t.FamilyId)
            .HasDatabaseName("idx_refresh_tokens_family");

        // Relationships
        builder.HasOne(t => t.User)
            .WithMany()
            .HasForeignKey(t => t.UserId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasOne(t => t.ReplacedBy)
            .WithOne()
            .HasForeignKey<RefreshToken>(t => t.ReplacedById)
            .OnDelete(DeleteBehavior.SetNull);
    }
}
