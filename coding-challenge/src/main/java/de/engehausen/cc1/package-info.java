/**
 * <h2>Welcome to the Coding Challenge!</h2>
 * <p>Why this challenge? It is a good opportunity to get your hands
 * dirty with actual Java 8 code, something where you not just read about it.
 * You can dive into some functional programming, which is new to Java 8 and
 * requires a change in your mind as well. It helps you become
 * a better programmer. Please read this <a href="http://norvig.com/21-days.html">good article</a>
 * by <a href="https://en.wikipedia.org/wiki/Peter_Norvig">Peter Norvig</a>.</p>
 * <p>This module contains the challenges and associated APIs. The
 * challenges will help you build or increase your Java 8 knowledge.
 * These are the challenges you can provide implementations for:</p>
 * <ol>
 * <li>{@link de.engehausen.cc1.challenge.Squares}</li>
 * <li>{@link de.engehausen.cc1.challenge.TopTenWords}</li>
 * <li>{@link de.engehausen.cc1.challenge.WordSpliteratorProvider}</li>
 * <li>{@link de.engehausen.cc1.challenge.MazeSolver}</li>
 * </ol>
 * <p>The challenges range from easy to hard. The more of them you
 * implement, the higher your chances of winning!</p>
 * <p>You can create your own project that will be depending on this project
 * by using the following command (<code>mvn install</code> this project first!):</p>
 * <pre style="color: red;">mvn archetype:generate -DarchetypeGroupId=de.engehausen -DarchetypeArtifactId=cc1-java8 -DarchetypeVersion=0.1.0-SNAPSHOT -B -DartifactId=&lt;your-project-name&gt;</pre>
 * <p>This will generate a Maven project for you to develop in. 
 * Please choose a non-generic value for <code>artifactId</code> so there will
 * be no clashes, e.g. your name or something funny or nerdy.</p>
 * <p>In the generated project your implementations are registered as <a href="https://docs.oracle.com/javase/tutorial/ext/basics/spi.html">Java Services</a>
 * and will be loaded using the service loader mechanism during the challenge.
 * Your solutions will be tested for correctness and speed.</p>
 * <p>To submit your entry you need to provide a <code>.jar</code> file which
 * contains your implementations. The project you have set up with the
 * command above ensures this and can be easily built with Maven.</p>
 * <p>In the hope of future coding challenges, the content of this one is put into
 * the <code>cc01</code> package.</p>
 * <p><b>Good luck and happy coding!</b></p>
 */
package de.engehausen.cc1;
