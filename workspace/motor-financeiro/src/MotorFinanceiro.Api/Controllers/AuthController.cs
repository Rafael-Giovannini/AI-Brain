using FluentValidation;
using MediatR;
using Microsoft.AspNetCore.Mvc;
using MotorFinanceiro.Application.Auth.Commands.ConfirmEmail;
using MotorFinanceiro.Application.Auth.Commands.ForgotPassword;
using MotorFinanceiro.Application.Auth.Commands.Login;
using MotorFinanceiro.Application.Auth.Commands.Logout;
using MotorFinanceiro.Application.Auth.Commands.RefreshToken;
using MotorFinanceiro.Application.Auth.Commands.RegisterUser;
using MotorFinanceiro.Application.Auth.Commands.ResetPassword;
using MotorFinanceiro.Application.Common.Exceptions;

namespace MotorFinanceiro.Api.Controllers;

[ApiController]
[Route("api/v1/auth")]
public class AuthController(IMediator mediator) : ControllerBase
{
    [HttpPost("register")]
    public async Task<IActionResult> Register([FromBody] RegisterUserCommand command, CancellationToken ct)
    {
        try
        {
            var userId = await mediator.Send(command, ct);
            return Created($"/api/v1/users/{userId}", new { id = userId });
        }
        catch (ConflictException ex)
        {
            return Conflict(new { error = ex.Message });
        }
        catch (ValidationException ex)
        {
            return BadRequest(new { errors = ex.Errors.Select(e => e.ErrorMessage) });
        }
    }

    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] LoginRequest request, CancellationToken ct)
    {
        try
        {
            var command = new LoginCommand(
                request.Email,
                request.Password,
                Request.Headers.UserAgent,
                HttpContext.Connection.RemoteIpAddress?.ToString());

            var response = await mediator.Send(command, ct);
            return Ok(response);
        }
        catch (AuthenticationException ex)
        {
            return Unauthorized(new { error = ex.Message });
        }
        catch (ValidationException ex)
        {
            return BadRequest(new { errors = ex.Errors.Select(e => e.ErrorMessage) });
        }
    }

    [HttpPost("refresh")]
    public async Task<IActionResult> Refresh([FromBody] RefreshRequest request, CancellationToken ct)
    {
        try
        {
            var command = new RefreshTokenCommand(
                request.RefreshToken,
                Request.Headers.UserAgent,
                HttpContext.Connection.RemoteIpAddress?.ToString());

            var response = await mediator.Send(command, ct);
            return Ok(response);
        }
        catch (AuthenticationException ex)
        {
            return Unauthorized(new { error = ex.Message });
        }
    }

    [HttpPost("logout")]
    public async Task<IActionResult> Logout([FromBody] LogoutRequest request, CancellationToken ct)
    {
        await mediator.Send(new LogoutCommand(request.RefreshToken), ct);
        return NoContent();
    }

    [HttpPost("confirm-email")]
    public async Task<IActionResult> ConfirmEmail([FromBody] ConfirmEmailCommand command, CancellationToken ct)
    {
        try
        {
            await mediator.Send(command, ct);
            return Ok(new { message = "Email confirmado com sucesso." });
        }
        catch (NotFoundException ex)
        {
            return NotFound(new { error = ex.Message });
        }
    }

    [HttpPost("forgot-password")]
    public async Task<IActionResult> ForgotPassword([FromBody] ForgotPasswordRequest request, CancellationToken ct)
    {
        await mediator.Send(new ForgotPasswordCommand(request.Email), ct);
        // FR-006: Always return same response regardless of email existence
        return Ok(new { message = "Se o email estiver cadastrado, você receberá instruções para redefinir sua senha." });
    }

    [HttpPost("reset-password")]
    public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordCommand command, CancellationToken ct)
    {
        try
        {
            await mediator.Send(command, ct);
            return Ok(new { message = "Senha redefinida com sucesso." });
        }
        catch (NotFoundException ex)
        {
            return NotFound(new { error = ex.Message });
        }
        catch (ValidationException ex)
        {
            return BadRequest(new { errors = ex.Errors.Select(e => e.ErrorMessage) });
        }
    }
}

// Request DTOs for API layer (separate from MediatR commands)
public sealed record LoginRequest(string Email, string Password);
public sealed record RefreshRequest(string RefreshToken);
public sealed record LogoutRequest(string RefreshToken);
public sealed record ForgotPasswordRequest(string Email);
