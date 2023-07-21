package _self

import jetbrains.buildServer.configs.kotlin.AbsoluteId
import jetbrains.buildServer.configs.kotlin.ParameterDisplay
import jetbrains.buildServer.configs.kotlin.Project
import jetbrains.buildServer.configs.kotlin.projectFeatures.buildReportTab

object Project : Project({
    description = "OneInc.AmazingApi application build project"

    params {
        text("BuildTemplate.ProjectName", "AmazingApi", display = ParameterDisplay.HIDDEN, allowEmpty = true)
        text("ProjectName", "%BuildTemplate.ProjectName%", display = ParameterDisplay.HIDDEN, allowEmpty = true)
        text("BuildTemplate.BuildId", "%teamcity.build.id%", display = ParameterDisplay.HIDDEN, allowEmpty = true)
        text("DockerImageDotnetSdk", "%DockerFeedBaseImages%/library/oneinc-dotnet-sdk:6.0.300-bullseye-slim", display = ParameterDisplay.HIDDEN, allowEmpty = true)
    }

    features {
        buildReportTab {
            id = "PROJECT_EXT_25"
            title = "Code Coverage"
            startPage = "coverage.zip!lcov-report/index.html"
        }
    }

    subProject(buildAndPublish.Project)
    subProject(deployAndPromote.Project)
})
