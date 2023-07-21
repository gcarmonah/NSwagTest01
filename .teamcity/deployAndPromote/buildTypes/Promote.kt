package deployAndPromote.buildTypes

import buildAndPublish.buildTypes.BuildAndPublish
import jetbrains.buildServer.configs.kotlin.AbsoluteId
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.DslContext
import jetbrains.buildServer.configs.kotlin.FailureAction

object Promote : BuildType({
    templates(AbsoluteId("BuildNumberTemplate"), AbsoluteId("PromoteToFolsomTemplate"))
    id = AbsoluteId("AmazingApi_DeployAndPromote_Promote")
    name = "Promote to Folsom"
    description = "Promote release to Folsom Octopus: https://dc2-proc-cd01"

    vcs {
        root(DslContext.settingsRoot)
    }

    dependencies {
        snapshot(BuildAndPublish) {
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }
})
