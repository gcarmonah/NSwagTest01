using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;

namespace OneInc.AmazingApi.Api.Authorization;

internal static class AuthenticationAndAuthorizationConfiguration
{
    public static void AddAuthenticationAndAuthorization(this IServiceCollection services, IConfiguration configuration)
    {
        var authenticationOptions = configuration.GetRequiredSection("Authentication").Get<AuthenticationOptions>();

        if (!string.Equals(
                authenticationOptions.AuthenticationType,
                AuthenticationTypes.SharedSecret,
                StringComparison.OrdinalIgnoreCase))
        {
            throw new NotSupportedException("Only SharedSecret authentication is supported");
        }

        services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
            .AddJwtBearer(
                options =>
                {
                    options.Authority = authenticationOptions.IdentityProviderUrl;

                    options.TokenValidationParameters = new TokenValidationParameters
                    {
                        ValidateIssuer = true,
                        ValidateAudience = true,
                        ValidIssuer = authenticationOptions.IdentityProviderUrl,
                        ValidAudience = AuthorizationConstants.AmazingApiApiAudience,
                    };
                });

        services.AddAuthorization(
            options =>
            {
                options.AddPolicy(
                    AuthorizationConstants.AmazingApiReadPolicy,
                    policy =>
                    {
                        policy.RequireAuthenticatedUser();
                        policy.AddAuthenticationSchemes(JwtBearerDefaults.AuthenticationScheme);

                        policy.RequireClaim(
                            AuthorizationConstants.ScopeClaimType,
                            AuthorizationConstants.AmazingApiReadClaim,
                            AuthorizationConstants.AmazingApiWriteClaim);
                    });

                options.AddPolicy(
                    AuthorizationConstants.AmazingApiWritePolicy,
                    policy =>
                    {
                        policy.RequireAuthenticatedUser();
                        policy.AddAuthenticationSchemes(JwtBearerDefaults.AuthenticationScheme);
                        policy.RequireClaim(AuthorizationConstants.ScopeClaimType, AuthorizationConstants.AmazingApiWriteClaim);
                    });
            });
    }
}
