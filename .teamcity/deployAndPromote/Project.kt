package deployAndPromote

import deployAndPromote.buildTypes.*
import jetbrains.buildServer.configs.kotlin.Project
import jetbrains.buildServer.configs.kotlin.AbsoluteId

object Project : Project({
    id = AbsoluteId("AmazingApi_DeployAndPromote")
    name = "Deploy and Promote"
    description =
        "Deploy to [QA Octopus](http://dp-cd01.az2oneinc.io). Promote to [Folsom Octopus](https://dc2-proc-cd01.oneinc.local)"

    buildType(Deploy)
    buildType(Promote)
    buildTypesOrder = arrayListOf(Deploy, Promote)
}) {
}
