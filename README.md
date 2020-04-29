# black-box-tester

API tester.

A java program, built as a fat jar, run from the command line. An json input file is used to definre a set of tests.

**To Build**
```shell script
mvn clean installl
```

**To run**
```shell script
java -jar target/black-box-tester-1.0.jar testInput.json
```

** Example Test Definition File**

```json
{
  "verbose": "true",
  "host": "postman-echo.com",
  "httpTests": [
    {
      "description": "Get Test",
      "url": "https://${host}/get?foo1=bar1&foo2=bar2",
      "method": "GET",
      "expected": {
        "httpStatus": 200,
        "contains": [
          "\"foo1\":\"bar1\"",
          "\"foo2\":\"bar2\""
        ]
      }
    },
    {
      "description": "Post Test",
      "url": "https://${host}/post?hand=wave",
      "method": "POST",
      "body": "{\"message\":\"hello Greg\"}",
      "expected": {
        "httpStatus": 200,
        "contains": [
          "\"hand\": \"wave\""
        ]
      }
    }
  ]
}
```
