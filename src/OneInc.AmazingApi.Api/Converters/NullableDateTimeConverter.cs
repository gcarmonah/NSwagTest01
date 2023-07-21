using System.ComponentModel.DataAnnotations;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace OneInc.AmazingApi.Api.Converters;

/// <inheritdoc />
public class NullableDateTimeConverter : JsonConverter<DateTime?>
{
    /// <inheritdoc />
    public override DateTime? Read(ref Utf8JsonReader reader, Type typeToConvert, JsonSerializerOptions options)
    {
        if (reader.TryGetDateTime(out var value))
        {
            return value;
        }

        var dateTimeString = reader.GetString();

        if (dateTimeString == null)
        {
            return null;
        }

        if (dateTimeString.StartsWith("/Date"))
        {
            dateTimeString = dateTimeString.Replace("/Date(", "");
            dateTimeString = dateTimeString.Replace(")/", "");
            var epoch = Convert.ToInt64(dateTimeString);
            var dateTimeOffset = DateTimeOffset.FromUnixTimeMilliseconds(epoch);

            return dateTimeOffset.UtcDateTime;
        }

        throw new ValidationException($"Could not convert value to DateTime?: {reader.ValueSpan.ToString()}");
    }

    /// <inheritdoc />
    public override void Write(Utf8JsonWriter writer, DateTime? value, JsonSerializerOptions options)
    {
    }
}
