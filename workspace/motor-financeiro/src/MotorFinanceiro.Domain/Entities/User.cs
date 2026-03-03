namespace MotorFinanceiro.Domain.Entities;

public class User : BaseEntity
{
    public string Email { get; private set; } = null!;
    public string PasswordHash { get; private set; } = null!;
    public string Name { get; private set; } = null!;
    public bool IsEmailConfirmed { get; private set; }
    public bool IsActive { get; private set; } = true;
    public DateTime? DeactivatedAt { get; private set; }

    private User() { } // EF Core

    public static User Create(string email, string passwordHash, string name)
    {
        return new User
        {
            Email = email.ToLowerInvariant().Trim(),
            PasswordHash = passwordHash,
            Name = name.Trim()
        };
    }

    public void ConfirmEmail() => IsEmailConfirmed = true;

    public void UpdateName(string name)
    {
        Name = name.Trim();
        MarkUpdated();
    }

    public void UpdatePasswordHash(string passwordHash)
    {
        PasswordHash = passwordHash;
        MarkUpdated();
    }

    public void Deactivate()
    {
        IsActive = false;
        DeactivatedAt = DateTime.UtcNow;
        MarkUpdated();
    }
}
