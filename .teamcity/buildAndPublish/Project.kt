package buildAndPublish

import buildAndPublish.buildTypes.*
import jetbrains.buildServer.configs.kotlin.AbsoluteId
import jetbrains.buildServer.configs.kotlin.Project

object Project : Project({
    id = AbsoluteId("AmazingApi_BuildAndPublish")
    name = "Build and Publish"
    description = "Configurations to Build and Publish OneInc.AmazingApi"

    buildType(BuildAndPublish)
})
