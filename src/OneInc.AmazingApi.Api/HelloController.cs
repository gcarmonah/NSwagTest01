using System.Net;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using OneInc.AmazingApi.Api.Authorization;

namespace OneInc.AmazingApi.Api;

/// <summary>
///     Hello controller. Just an example
/// </summary>
public class HelloController : Controller
{
    /// <summary>
    ///     Example controller method with Anonymous acces
    /// </summary>
    /// <param name="name"></param>
    /// <returns></returns>
    [HttpGet]
    [Route("/{name}")]
    [ProducesResponseType(typeof(HelloResponse), (int)HttpStatusCode.OK)]
    public ActionResult<HelloResponse> Index([FromRoute] string name)
    {
        return Ok(new HelloResponse(name));
    }

    /// <summary>
    /// 
    /// </summary>
    /// <returns></returns>
    [HttpGet]
    [Route("/generic")]
    [ProducesResponseType(typeof(HelloResponse), (int)HttpStatusCode.OK)]
    public ActionResult<HelloResponse> Generic()
    {
        return Ok(new HelloResponse("Generic name"));
    }

    /// <summary>
    ///     Example controller method with Anonymous aithorized access
    /// </summary>
    /// <returns></returns>
    [HttpGet]
    [Route("/secret")]
    [Authorize(AuthorizationConstants.AmazingApiReadPolicy)]
    [ProducesResponseType(typeof(SecretResponse), (int)HttpStatusCode.OK)]
    [ProducesResponseType((int)HttpStatusCode.Unauthorized)]
    public ActionResult<SecretResponse> Secret()
    {
        return Ok(new SecretResponse());
    }
}
