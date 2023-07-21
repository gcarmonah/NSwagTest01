using System.Reflection;
using System.Text.Json.Serialization;
using Microsoft.AspNetCore.ResponseCompression;
using NLog;
using NLog.Extensions.Logging;
using NLog.Web;
using NSwag;
using OneInc.AmazingApi.Api.Authorization;
using OneInc.AmazingApi.Api.Converters;
using ILogger = Microsoft.Extensions.Logging.ILogger;
using NullLogger = Microsoft.Extensions.Logging.Abstractions.NullLogger;

var builder = WebApplication.CreateBuilder(args);

var environmentName = Environment.GetEnvironmentVariable("ASPNETCORE_ENVIRONMENT");

builder.Configuration.AddJsonFile("appsettings.json", true, true)
    .AddJsonFile($"appsettings.{environmentName}.json", true, true)
    .AddJsonFile("configmaps/appsettings.json", true, true)
    .AddJsonFile("nlog.json", true, true)
    .AddJsonFile("configmaps/nlog.json", true, true)
    .AddEnvironmentVariables();

LogManager.Configuration = new NLogLoggingConfiguration(builder.Configuration.GetSection("NLog"));

builder.Services.AddHealthChecks();

builder.Services.AddSingleton<ILoggerFactory, NLogLoggerFactory>();

builder.Services.AddSingleton<ILogger>(NullLogger.Instance);

builder.Logging.ClearProviders();

builder.WebHost.UseNLog();

builder.Services.AddAuthenticationAndAuthorization(builder.Configuration);

builder.Services.AddResponseCompression(
    options =>
    {
        options.Providers.Add<GzipCompressionProvider>();
        options.EnableForHttps = true;
    });


/*builder.Services.AddSwaggerGen(
    options =>
    {
        options.OperationFilter<SwaggerOperationIdFilter>();

        options.SwaggerDoc(
            "v1",
            new OpenApiInfo
            {
                Version = "v1",
                Title = "AmazingApi",
                Description = "AmazingApi"
            });

        // Set the comments path for the Swagger JSON and UI.
        var xmlWebApiFile = $"{Assembly.GetExecutingAssembly().GetName().Name}.xml";
        var xmlPath = Path.Combine(AppContext.BaseDirectory, xmlWebApiFile);
        options.IncludeXmlComments(xmlPath);
        options.SchemaFilter<OpenApiEnumSchemaFilter>();

        options.AddSecurityDefinition(
            "Bearer",
            new OpenApiSecurityScheme
            {
                In = ParameterLocation.Header,
                Description = "Please insert JWT with Bearer into field",
                Name = "Authorization",
                Type = SecuritySchemeType.ApiKey
            });

        options.AddSecurityRequirement(
            new OpenApiSecurityRequirement
            {
                {
                    new OpenApiSecurityScheme
                    {
                        Reference = new OpenApiReference
                        {
                            Type = ReferenceType.SecurityScheme,
                            Id = "Bearer"
                        }
                    },
                    new string[] { }
                }
            });
    });*/

builder.Services.AddSwaggerDocument(config =>
{
    config.AddSecurity(
        "Bearer",
        new OpenApiSecurityScheme
        {
            In = OpenApiSecurityApiKeyLocation.Header,
            Description = "Please insert JWT with Bearer into field",
            Name = "Authorization",
            Type = OpenApiSecuritySchemeType.ApiKey
        }
    );
    config.PostProcess = document =>
    {
        document.Info.Version = "v1";
        document.Info.Title = "Amazing API";
        document.Info.Description = "A simple ASP.NET Core web API";
        document.Info.TermsOfService = "None";
    };
});

builder.Services.AddControllers()
    .AddJsonOptions(
        options =>
        {
            options.JsonSerializerOptions.Converters.Add(new JsonStringEnumConverter());
            options.JsonSerializerOptions.Converters.Add(new NullableDateTimeConverter());
            options.JsonSerializerOptions.DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull;
        });

builder.Logging.ClearProviders();

builder.WebHost.UseNLog();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    /*app.UseSwagger();
    app.UseSwaggerUI();*/
    app.UseOpenApi();
    app.UseSwaggerUi3();
    app.UseDeveloperExceptionPage();
}

app.UseRouting();
app.UseResponseCompression();

app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();
app.MapHealthChecks("/health/readiness");
app.MapHealthChecks("/health/liveness");
app.MapHealthChecks("/health/startup");

app.UseExceptionHandler("/error");

app.Run();
