package _self

enum class TeamCityTemplateSteps(val runnerId: String) {
    EXTRACT_BUILD_NUMBER("RUNNER_166"),
    SET_BUILD("RUNNER_219"),
    PRELIMINARY_MERGE_FROM_DEVELOP("RUNNER_145"),
    PRELIMINARY_MERGE_MERGE_REQUEST("RUNNER_147"),
    TC_NOTE("RUNNER_43"),
    CHECK_OUTDATED_BRANCH("RUNNER_21"),
    CHECK_OCTOPUS_TENANT("RUNNER_319"),
    SET_OCTOPUS_ENVIRONMENT_TO_STATUS("RUNNER_165"),
    GET_DEPLOYMENT_TASK_URL("RUNNER_24")
}