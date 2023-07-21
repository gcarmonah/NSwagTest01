# OneInc.AmazingApi

> PLease, put the relevant description for your solution here. Don't be lazy. This description would be extremely helpful for 
everyone who gonna start to work with your solution from scratch.

The documentation, if it is greater than the single file, could be placed into `doc` dir and linked inside if this root 
`README.md` page


## Basic solution structure

| Item                         | Desctiption                                                                                                                                                                |
| ---------------------------: | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `OneIncSolutionTemplate.sln` | the root solution file, into which all the projects would be added                                                                                                         |
| `.ci-cd`                     | custom scripts for continuous delivery and deployment processes                                                                                                            |
| `.docker`                    | Docker-specific code. Compose config, compose scripts, env, etc.                                                                                                           |
| `.teamcity`                  | "TeamCity configuraiton as a code" for your solution. Build configurations can use custom scripts from `.ci-cd` folder. Here only the Kotlin scripts would be stored       |
| `src/`                       | all the source code of you app                                                                                                                                             |
| `doc/`                       | documentation for your app, preferably technical (how to setup, run, debug, etc. your app). Business doc usually stored in our WIki                                        |
| `test/`                      | tests sources code. Separated from `src/` to easily manage CI/CD and separate real business logic from tests logic                                                         |
| `README.md`                  | The entry point to document you application                                                                                                                                |
| `.dockerignore`              | One, Inc. specific Docker ignore file. Initially, it is just a generally recommended .dockerignore but it could be extended (if needed) during the time.                   |
| `.editorconfig`              | code-style and analysis rules                                                                                                                                              |
| `.gitignore`                 | One, Inc. specific .gitignore file.                                                                                                                                        |
| `Directory.Build.props`      | common properties for all projects in your solution ([official documentation](https://learn.microsoft.com/en-us/visualstudio/msbuild/customize-by-directory?view=vs-2022)) |
| `Directory.Packages.props`   | NuGet versions [central package management](https://learn.microsoft.com/en-us/nuget/consume-packages/central-package-management) file                                      |
| `NuGet.config`               | custom NuGet package manager configuration. Describes allowed feeds to obtain NuGet packages.                                                                              |