using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using OneInc.AmazingApi.Api.Converters;

namespace OneInc.AmazingApi.Api.UnitTests.Converters;

[TestFixture]
public class NullableDateTimeConverterTests
{
    private readonly NullableDateTimeConverter _converter = new ();

    [TestCase("2020-12-21")]
    [TestCase("2020-12-21T00:00:00")]
    [TestCase("/Date(1608508800000)/")]
    [TestCase("2020-12-21T00:00:00.000Z")]
    public void DeserializeDateTime(string value)
    {
        var expected = new DateTime(2020, 12, 21);

        var utf8JsonReader = new Utf8JsonReader(
            Encoding.UTF8.GetBytes($"\"{value}\""),
            false,
            new JsonReaderState(new JsonReaderOptions()));

        utf8JsonReader.Read();

        var deserializedDateTime = _converter.Read(
            ref utf8JsonReader,
            typeof(DateTime?),
            new JsonSerializerOptions
            {
                DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull
            });

        Assert.That(deserializedDateTime, Is.EqualTo(expected));
    }
}
