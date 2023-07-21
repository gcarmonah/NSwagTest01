ARG feed=proget.oneincsystems.com/dp.dckr
FROM $feed/library/oneinc-dotnet-aspnet:6.0-bullseye-slim AS base
WORKDIR /app
EXPOSE 80
EXPOSE 443

# https://github.com/moby/moby/issues/15858#issuecomment-614157331
# Prefiltering stage using find -exec and cp --parents to copy out
# the project files in their proper directory structure.
FROM $feed/library/oneinc-dotnet-aspnet:6.0-bullseye-slim AS dotnet-prep
WORKDIR /
COPY . ./build/
RUN mkdir ./project && cd ./build && \
  find . -type f -a \( -iname "NuGet.Config" -o -iname "*.csproj" -o -iname "*.props" \) \
    -exec cp --parents "{}" ../project/ \;

# New build stage, independent cache
FROM $feed/library/oneinc-dotnet-sdk:6.0-bullseye-slim AS build
ARG configuration=Debug
WORKDIR /build

# Copy only the project files with correct directory structure
COPY --from=dotnet-prep ./project ./
RUN dotnet restore "src/OneInc.AmazingApi.Api/OneInc.AmazingApi.Api.csproj" --configfile "NuGet.Config"

COPY . .
WORKDIR "/build/src/OneInc.AmazingApi.Api"
RUN dotnet publish "OneInc.AmazingApi.Api.csproj" -c $configuration -o /app/publish --no-restore

FROM base AS final
WORKDIR /app
COPY --from=build /app/publish .
ENTRYPOINT ["dotnet", "OneInc.AmazingApi.Api.dll"]

