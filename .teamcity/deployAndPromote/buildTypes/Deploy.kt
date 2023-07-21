package deployAndPromote.buildTypes

import _self.TeamCityTemplateSteps
import buildAndPublish.buildTypes.BuildAndPublish
import jetbrains.buildServer.configs.kotlin.*

object Deploy : BuildType({
    templates(
        AbsoluteId("CheckOctopusTenant"),
        AbsoluteId("DeployWithoutDbBaseTemplate"),
        AbsoluteId("MergeRequestsSupportTemplate")
    )
    id = AbsoluteId("AmazingApi_DeployAndPromote_Deploy")
    name = "Deploy"
    description = "Deploy to QA Octopus"

    buildNumberPattern = "${BuildAndPublish.depParamRefs.buildNumber}"
    maxRunningBuilds = 1

    params {
        text(
            "Octopus.Deploy.CmdArguments",
            "--logLevel=verbose %Octopus.Delpoy.Main.CmdArguments%",
            display = ParameterDisplay.HIDDEN,
            allowEmpty = true
        )
        text(
            "Octopus.Tenants.Tag",
            "Personal Environment Type/QA",
            display = ParameterDisplay.HIDDEN,
            allowEmpty = false
        )
        text("GetDeploymentTask.StepName", "Deploy", display = ParameterDisplay.HIDDEN, allowEmpty = true)
        text(
            "Octopus.Tenants",
            "%A0.Octopus.Environment.PersonalUserName%_%A0.Octopus.Environment.PersonalNumber%",
            display = ParameterDisplay.HIDDEN,
            allowEmpty = false
        )
        text(
            "Octopus.Environment.Name",
            "%A0.Octopus.Environment.Name%",
            display = ParameterDisplay.HIDDEN,
            allowEmpty = false
        )
    }

    vcs {
        root(AbsoluteId("AmazingApi_AllBranches"))

        cleanCheckout = true
        showDependenciesChanges = true
    }
    steps {
        step {
            name = "Deploy"
            id = "RUNNER_198"
            type = "octopus.deploy.release"
            param("octopus_additionalcommandlinearguments", "%Octopus.Deploy.CmdArguments%")
            param("octopus_version", "3.0+")
            param("octopus_host", "%Octopus.URL%")
            param("octopus_project_name", "%ProjectName%")
            param("octoups_tenants", "%Octopus.Tenants%")
            param("octopus_deployto", "%A0.Octopus.Environment.Name%")
            param("secure:octopus_apikey", "%Octopus.APIKey%")
            param("octopus_releasenumber", "%build.number%")
        }
        stepsOrder = arrayListOf(
            TeamCityTemplateSteps.CHECK_OCTOPUS_TENANT.runnerId,
            TeamCityTemplateSteps.CHECK_OUTDATED_BRANCH.runnerId,
            "RUNNER_198",
            TeamCityTemplateSteps.SET_OCTOPUS_ENVIRONMENT_TO_STATUS.runnerId,
            TeamCityTemplateSteps.GET_DEPLOYMENT_TASK_URL.runnerId
        )
    }
    dependencies {
        snapshot(AbsoluteId("AmazingApi_BuildAndPublish_BuildAndPublish")) {
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }

    disableSettings(TeamCityTemplateSteps.CHECK_OUTDATED_BRANCH.runnerId)
})
