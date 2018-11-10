# Coding Challenger
This is the tool that performs the challenge evaluation and generates the result report. Each challenge contribution a `.jar` file is executed in a separate Java process per challenge. The launching parameters are `-server -Xms512m -Xmx512m -Xss32m`. The implementations are subjected to a varying number of tests per challenge and success percentages are calculated per challenge.

Performance tests are run for each challenge, and the best-of-seven result is recorded. Performance is ranked relative to the best-performing contribution per challenge.

An overall performance value in percent is computed. The top three contributions are shown...

To launch it, run

    java -jar target/cc1-java8-challenger-0.1.0-SNAPSHOT-jar-with-dependencies.jar

### Disclaimer
This is a tool that runs the tests and reports the results. It could be much more beautiful, but I had to get it to do its work.

### Using it in Eclipse
You can run the tests against your code also in Eclipse. Import it into Eclipse, then you can either add your project as a "project dependency" (preferred) or as an external library. Now you can run the tests as regular JUnit tests. Your implementations will be picked up using the Java service loading mechanism.
