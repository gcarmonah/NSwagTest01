namespace OneInc.AmazingApi.Api;

/// <summary>
///     Example model for HelloController
/// </summary>
/// <param name="Name"></param>
public record HelloResponse(string Name)
{
    /// <summary>
    ///
    /// </summary>
    public DateTime Now { get; } = DateTime.UtcNow;
}
