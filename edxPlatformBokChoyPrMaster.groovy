//package devops

//import static org.edx.jenkins.dsl.
import static JenkinsPublicConstants.JENKINS_PUBLIC_LOG_ROTATOR
import static JenkinsPublicConstants.JENKINS_PUBLIC_JUNIT_REPORTS

buildFlowJob('edx-platform-bok-choy-pr-TESTING') {
    //TODO: Include comment about skipbuildphrase being in the global config
    logRotator JENKINS_PUBLIC_LOG_ROTATOR() //Discard build after 14 days
    concurrentBuild() //concurrent builds can happen
    label('flow-worker-bokchoy') //restrict to flow-worker-bokchoy
    checkoutRetryCount(5)
    multiscm {
        git { //using git on the branch and url, clean before checkout
            remote {
                github('edx/testeng-ci')
            }
            branch('*/master')
            browser()
            extensions {
                relativeTargetDirectory('testeng-ci')
                cleanBeforeCheckout()
            }
        }
        git { //using git on the branch and url, clone, clean before checkout
            remote {
                github('aphanse/hello-world')
                refspec('+refs/pull/*:refs/remotes/origin/pr/*')
            }
            branch('\${ghprbActualCommit}')
            browser()
            extensions {
/*                cloneOptions {
                    reference('\$HOME/edx-platform-clone/.git')
                    timeout(10)
                }
*/                cleanBeforeCheckout()
//                relativeTargetDirectory('edx-platform')
            }
        }
    }
    triggers { //trigger when change pushed to GitHub
        pullRequest {
            admins(["aphanse"])
            useGitHubHooks()
            triggerPhrase('[jJ]enkins run bokchoy')
            //skip build phrase
            cron('H/5 * * * *')
            userWhitelist(["aphanse"])
            orgWhitelist(["edX"])
            extensions {
                commitStatus {
                    context('jenkins/bokchoy')
                }
            }
        }
    }
    dslFile('testeng-ci/jenkins/flow/pr/edx-platform-bok-choy-pr.groovy')
    publishers { //publish JUnit Test report
        archiveJunit(JENKINS_PUBLIC_JUNIT_REPORTS)
    }
}
