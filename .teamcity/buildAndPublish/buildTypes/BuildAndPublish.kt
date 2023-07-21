package buildAndPublish.buildTypes

import _self.TeamCityTemplateSteps
import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.buildSteps.DotnetTestStep
import jetbrains.buildServer.configs.kotlin.buildSteps.dotnetTest
import jetbrains.buildServer.configs.kotlin.buildSteps.powerShell
import jetbrains.buildServer.configs.kotlin.triggers.VcsTrigger
import jetbrains.buildServer.configs.kotlin.triggers.vcs

object BuildAndPublish : BuildType({
    id = AbsoluteId("AmazingApi_BuildAndPublish_BuildAndPublish")
    templates(
        AbsoluteId("TCNoteTemplate"),
        AbsoluteId("BuildNumberTemplate"),
        AbsoluteId("PreliminaryMergeTemplate"),
        AbsoluteId("MergeRequestsSupportTemplate")
    )
    name = "Build and Publish"

    params {
        text(
            "Octopus.CreateRelease.CmdArguments",
            """--defaultPackageVersion=%build.number% --ignoreExisting --releaseNotes="%Octopus.ReleaseNotes%"""",
            display = ParameterDisplay.HIDDEN,
            allowEmpty = true
        )
        text(
            "Octopus.ReleaseNotes",
            "**Build log:** [TeamCity](%TeamCity.BuildUrl%) **Branch name:** %teamcity.build.branch% **Commit hash:** %build.vcs.number.AmazingApi_AllBranches%%",
            display = ParameterDisplay.HIDDEN,
            allowEmpty = true
        )
        text("BuildTemplate.ProjectName", "AmazingApi", display = ParameterDisplay.HIDDEN, allowEmpty = true)
    }
    vcs {
        root(AbsoluteId("AmazingApi_AllBranches"))

        cleanCheckout = true
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {

        dotnetTest {
            name = "Build and unit-tests in docker"
            id = "RUNNER_83"
            filter = ".UnitTests"
            configuration = "%BuildConfiguration%"
            dockerImage = "%DockerImageDotnetSdk%"
            dockerImagePlatform = DotnetTestStep.ImagePlatform.Linux
            param("dotNetCoverage.dotCover.filters", "-:*.UnitTests")
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.DotNetCliTool.2021.2.2%")
        }
        powerShell {
            name = "Build and push artifacts"
            id = "RUNNER_109"
            executionMode = BuildStep.ExecutionMode.RUN_ON_SUCCESS
            formatStderrAsError = true
            scriptMode = file {
                path = "./.ci-cd/scripts/BuildAndPushArtifacts.ps1"
            }
            scriptArgs = """
            -DockerRepository "%DockerFeed%"
            -DockerFeed "%DockerFeedBaseImages%"
            -BuildConfiguration "%BuildConfiguration%"
            -TeamcityVersion "%system.teamcity.version%"
            -BuildNumber "%build.number%"
            -OctopusPath "%teamcity.tool.octopus-commandline%\Octo.exe"
            -OctopusUrl "%Octopus.URL%"
            -OctopusApiKey "%Octopus.APIKey%"
        """.trimIndent()
        }
        step {
            name = "Push Build Info"
            id = "RUNNER_188"
            type = "octopus.metadata"
            param("octopus_packageid", "OneInc.AmazingApi.Api")
            param("octopus_packageversion", "%build.number%")
            param("octopus_host", "%Octopus.URL%")
            param("octopus_forcepush", "IgnoreIfExists")
            param("secure:octopus_apikey", "%Octopus.APIKey%")
        }
        step {
            name = "Create release from merge-request"
            id = "RUNNER_69"
            type = "octopus.create.release"
            conditions {
                exists("teamcity.pullRequest.target.branch")
            }
            param("octopus_additionalcommandlinearguments", "%Octopus.CreateRelease.CmdArguments%")
            param("octopus_version", "3.0+")
            param("octopus_host", "%Octopus.URL%")
            param("octopus_project_name", "%ProjectName%")
            param("secure:octopus_apikey", "%Octopus.APIKey%")
            param("octopus_releasenumber", "%build.number%")
            param("octopus_git_commit", "%build.vcs.number%")
            param("octopus_git_ref", "%teamcity.pullRequest.source.branch%")
        }

        step {
            name = "Create release from branch"
            id = "a3fdc93e-91bd-434d-80a3-389214d685f3"
            type = "octopus.create.release"
            conditions {
                doesNotExist("teamcity.pullRequest.target.branch")
            }
            param("octopus_additionalcommandlinearguments", "%Octopus.CreateRelease.CmdArguments%")
            param("octopus_version", "3.0+")
            param("octopus_host", "%Octopus.URL%")
            param("octopus_project_name", "%ProjectName%")
            param("secure:octopus_apikey", "%Octopus.APIKey%")
            param("octopus_releasenumber", "%build.number%")
            param("octopus_git_commit", "%build.vcs.number%")
            param("octopus_git_ref", "%teamcity.build.branch%")
        }
        stepsOrder = arrayListOf(
            TeamCityTemplateSteps.EXTRACT_BUILD_NUMBER.runnerId,
            TeamCityTemplateSteps.SET_BUILD.runnerId,
            TeamCityTemplateSteps.PRELIMINARY_MERGE_FROM_DEVELOP.runnerId,
            TeamCityTemplateSteps.PRELIMINARY_MERGE_MERGE_REQUEST.runnerId,
            "RUNNER_83",
            "RUNNER_109",
            "RUNNER_188",
            "RUNNER_69",
            TeamCityTemplateSteps.TC_NOTE.runnerId
        )
    }

    triggers {
        vcs {
            id = "66b68841-6c85-48cc-ba04-4df71e665fe8"
            enabled = true
            quietPeriodMode = VcsTrigger.QuietPeriodMode.USE_DEFAULT
        }
    }

    features {
        dockerSupport {
            id = "DockerSupport"
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_10"
            }
        }
    }

    disableSettings(
        TeamCityTemplateSteps.PRELIMINARY_MERGE_FROM_DEVELOP.runnerId,
        TeamCityTemplateSteps.PRELIMINARY_MERGE_MERGE_REQUEST.runnerId,
    )
})
