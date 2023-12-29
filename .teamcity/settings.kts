import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2023.11"

project {
    vcsRoot(TeamcityBuildVcs)
    buildType(TeamcitySettings_Build)
}

object TeamcitySettings_Build : BuildType({
    id("Build")
    name = "Build"

    vcs {
        TeamcityBuildVcs
        cleanCheckout = true
        excludeDefaultBranchChanges = true
    }

    triggers {
        vcs {
        }
    }

    features {
        perfmon {
        }
    }
})

object TeamcityBuildVcs : GitVcsRoot({
    name = "Teamcity Build"
    url = "https://github.com/jpspringall/teamcity-settings-2"
    branch = "refs/heads/main"
    branchSpec = "refs/heads/*"
    authMethod = password {
        userName = "jpspringall"
        password = "credentialsJSON:e224d815-b2d6-4dc7-9e5c-11f7d85dbd51"
    }
})