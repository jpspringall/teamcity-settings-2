
import CommonSteps.createParameters
import CommonSteps.printPullRequestNumber
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.Project
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.project
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.ui.add
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.version

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

var project = Project {
    vcsRoot(TeamcityBuildVcs)
    buildType(Build)
    buildType(PullRequestBuild)
}

object Build : BuildType({
    name = "Master Build"

    vcs {
        root(TeamcityBuildVcs)
        cleanCheckout = true
        excludeDefaultBranchChanges = true
    }

    createParameters("")

    printPullRequestNumber()

    triggers {
        vcs {
        }
    }

    features {}
})

object PullRequestBuild : BuildType({
    name = "Pull Request Build"

    vcs {
        root(TeamcityBuildVcs)
        cleanCheckout = true
        excludeDefaultBranchChanges = true
    }
        createParameters( "+:refs/pull/*/merge")

    printPullRequestNumber()

    triggers {
        vcs {
        }
    }

    features {
        commitStatusPublisher {
            vcsRootExtId = "${TeamcityBuildVcs.id}"
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = vcsRoot()
            }
        }
        pullRequests {
            vcsRootExtId = "${TeamcityBuildVcs.id}"
            provider = github {
                authType = vcsRoot()
                filterSourceBranch = "refs/pull/*/merge"
                filterAuthorRole = PullRequests.GitHubRoleFilter.MEMBER
            }
        }
    }
})

object TeamcityBuildVcs : GitVcsRoot({
    name = "Teamcity Build"
    url = "https://github.com/jpspringall/teamcity-settings-2"
    branch = "refs/heads/main"
    branchSpec = "%git.branch.specification%"
    agentCleanPolicy = GitVcsRoot.AgentCleanPolicy.ALWAYS
    checkoutPolicy = GitVcsRoot.AgentCheckoutPolicy.NO_MIRRORS
    authMethod = password {
        userName = "jpspringall"
        password = "credentialsJSON:e224d815-b2d6-4dc7-9e5c-11f7d85dbd51"
    }
})

for (bt : BuildType in project.buildTypes ) {
    val gitSpec = bt.params.findRawParam("git.branch.specification")
    if (gitSpec != null && gitSpec.value.isNotBlank()) {
        bt.vcs.branchFilter = """
            +:*
            -:<default>
        """.trimIndent()
    }
    if (bt.name == "Pull Request Build" || bt.name == "Master Build") {
        bt.features.add {
            feature {
                type = "xml-report-plugin"
                param("verbose", "true")
                param("xmlReportParsing.reportType", "trx")
                param("xmlReportParsing.reportDirs","%system.teamcity.build.checkoutDir%/test-results/**/*.trx")
            }
        }
    }
//    if (bt.name == "Pull Request Build" || bt.name == "Master Build")
//    {
//        bt.features.add {  xmlReport {
//            reportType = XmlReport.XmlReportType.TRX
//            rules = "%system.teamcity.build.checkoutDir%/test-results/**/*.trx" //Remember to match this in test output
//            verbose = true
//        } }
//    }
}

project(project)