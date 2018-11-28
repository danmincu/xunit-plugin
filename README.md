# xunit-plugin - Fork reason and changes
Jenkins xunit plugin

In this fork I've added code to recursively search the source code for all the occurences of the
`quarantined-tests.json` file with the following JSON format

```
[{
  "name": "FooServiceTests.testFooFooMethod",
  "reason": "this test fails all the time."
},
  {
    "name": "FooServiceTests.testFooIntermMethod",
    "reason": "this test fails intermitently."
  }
]
```

The found tests are quarantined and the xunit step won't faile if it encounters them.

Example of a pipeline:
```
node {
   def mvnHome
   stage('Preparation') {
      // Get code from a GitHub repository
      git 'https://github.com/danmincu/quarantineTest.git'
   }
   stage('Build') {
       try{
      // Run the maven build
      if (isUnix()) {
         sh "'${mvnHome}/bin/mvn' -Dmaven.test.failure.ignore clean package"
      } else {
         bat(/"C:\JavaTools\apache-maven\bin\mvn" -Dmaven.test.failure.ignore clean package/)
      }}
      finally {
           step([$class: 'XUnitPublisher', testTimeMargin: '30000', thresholdMode: 1, thresholds:
                    [[$class: 'FailedThreshold', failureNewThreshold: '', failureThreshold: '', unstableNewThreshold: '', unstableThreshold: '0'],
                    [$class: 'SkippedThreshold', failureNewThreshold: '', failureThreshold: '', unstableNewThreshold: '', unstableThreshold: '']],
                    tools : [[$class: 'JUnitType', deleteOutputFiles: true, failIfNotNew: true, pattern: '**/TEST-*.xml', skipNoTestFiles: false, stopProcessingIfError: true]]])
      }
   }
}
```
*** some UI changes to differentiate the failed qurantined tests from the other failures might be required. Currently that information is only available in the logs.