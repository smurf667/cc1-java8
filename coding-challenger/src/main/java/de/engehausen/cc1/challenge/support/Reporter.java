package de.engehausen.cc1.challenge.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Writes the slide set with an introduction and the test results.
 * <p>This is a bit hacky and messy. I ran a bit out of coding
 * virtue here, but it works - which is what counts!</p>
 */
public class Reporter {

	/** markup tags for generating some report slides dynamically */
	private enum Tag {
		section, h1, h3, h4, p, ol, ul, li, span, small
	}
	/* attributes for some tags */
	private static String[] ATTR_BACK_OK = { "data-background", "#252" };
	private static String[] ATTR_BACK_NOK = { "data-background", "#521" };
	private static String[] ATTR_CLASS_NONE = { "class", "" };
	private static String[] ATTR_TEXT_OK = { "class", "ccok" };
	private static String[] ATTR_TEXT_NOK = { "class", "ccnok" };
	private static String[] ATTR_TEXT_GREY = { "class", "ccgrey" };
	private static String[] ATTR_TEXT_GREY_NORMAL = { "class", "ccgrey ccnormal" };
	private static String[] ATTR_TEXT_ORANGE = { "class", "ccorange" };
	private static String[] ATTR_TEXT_ORANGE_NORMAL = { "class", "ccorange ccnormal" };
	private static String[] ATTR_TILE = { "class", "cctile" };
	private static String[] ATTR_TILE_DISABLED = { "class", "cctile ccdisable" };
	private static String[] ATTR_LINE = { "style", "line-height: 1.5em; height: auto;" };
	private static String[] ATTR_BLUE_ZOOM = { "data-transition", "zoom", "data-background", "#125" };
	
	private static final String nonCompetitor = "0xCAFED00D";

	/**
	 * Writes the report as a ReavalJS slide show.
	 * There is a report template inside this project which
	 * is unzipped and then a part of the slides is dynamically
	 * generated.
	 * @param testData the JSON test result data
	 * @param reportFolder the folder to write the report files to
	 * @throws IOException in case of error
	 */
	public void writeReport(final File testData, final File reportFolder) throws IOException {
		unzipReportTemplate(reportFolder);
		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, List<TestResult>> results = new TreeMap<>();
		results.putAll(mapper.readValue(testData, new TypeReference<Map<String, List<TestResult>>>() { /* empty */}));
		final Map<String, AtomicInteger> correctnessResults = new TreeMap<>();
		final Map<String, Map<String, Double>> performanceResults = new TreeMap<>();
		final File indexFile = new File(reportFolder, "index.html");
		final String[] index = splitIndex(indexFile);
		final PrintStream output = new PrintStream(indexFile);
		output.println(index[0]);
		try {
			writeParticipants(output, new ArrayList<>(results.keySet()));
			results
				.entrySet()
				.stream()
				.forEach(
					entry -> writeReportSingle(output, correctnessResults, performanceResults, entry.getKey(), entry.getValue())
				);
			writeRanking(output, correctnessResults, performanceResults);
		} finally {
			output.println(index[1]);
			output.close();
		}
	}

	/**
	 * Reads the <code>index.html</code> template and splits it into
	 * a head and tail section. The middle will be dynamically generated
	 * @param index the index file
	 * @return an array containing the head and tail section of the index file
	 * @throws IOException in case of error
	 */
	protected String[] splitIndex(final File index) throws IOException {
		return new String(
			Files.readAllBytes(Paths.get(index.getAbsolutePath())),
			StandardCharsets.UTF_8
		).split("DYNAMIC_CONTENT");
	}

	/**
	 * Unzips the report template to the report folder.
	 * @param reportFolder the folder to write the report files to
	 * @throws IOException in case of error
	 */
	protected void unzipReportTemplate(final File reportFolder) throws IOException {
		final byte[] buffer = new byte[4096];
		final ZipInputStream zis = new ZipInputStream(getClass().getResourceAsStream("/report.zip"));
		try {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				final File target = new File(reportFolder, entry.getName());
				if (entry.isDirectory()) {
					target.mkdir();
				} else {
					final FileOutputStream fos = new FileOutputStream(target);
					try {
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
					} finally {
						fos.close();
					}
				}
			}
		} finally {
			zis.close();
		}
	}

	/**
	 * Outputs the ranking report to the slides.
	 * @param out the stream to output to
	 * @param results the test results
	 */
	protected void writeRanking(final PrintStream out, final Map<String, AtomicInteger> correctness, final Map<String, Map<String, Double>> performance) {
		final Map<String, Double> corrPercentages = new NullableMap<>(Double.valueOf(0)); // artifactId -> percentage
		final OptionalInt optMax = correctness.values().stream().mapToInt(ai -> ai.get()).max();
		final double maxCorrect = optMax.getAsInt();
		// correctness value (all correct == 1.00)
		for (Map.Entry<String, AtomicInteger> entry : correctness.entrySet()) {
			corrPercentages.put(entry.getKey(), Double.valueOf(entry.getValue().get()/maxCorrect));
		}
		// determine best performance times
		final Map<String, Double> bestOf = new HashMap<>(); // best perf time per test
		for (Map<String, Double> map : performance.values()) {
			for (Map.Entry<String, Double> entry : map.entrySet()) {
				final String key = entry.getKey();
				final Double min = bestOf.get(key);
				if (min == null) {
					bestOf.put(key, entry.getValue());
				} else {
					final Double current = entry.getValue();
					if (current.doubleValue() < min.doubleValue()) {
						bestOf.put(key, current);
					}
				}
			}
		}
		final Map<String, Double> perfPercentages = new NullableMap<>(Double.valueOf(0)); // artifactId -> perf percentage
		final double perfTotal = bestOf.size();
		for (Map.Entry<String, Map<String, Double>> perfEntry : performance.entrySet()) {
			final String artifactId = perfEntry.getKey();
			double value = 0;
			for (Map.Entry<String, Double> entry : perfEntry.getValue().entrySet()) {
				final Double best = bestOf.get(entry.getKey());
				value += best.doubleValue()/entry.getValue().doubleValue();
			}
			perfPercentages.put(artifactId, Double.valueOf(value/perfTotal));
		}
		final Map<String, Double> temp = new TreeMap<>();
		for (String artifactId : corrPercentages.keySet()) {
			final double all = (corrPercentages.get(artifactId).doubleValue() + perfPercentages.get(artifactId).doubleValue())/2;
			temp.put(artifactId, Double.valueOf(all));
		}
		final Double zero = Double.valueOf(0);
		final Map<String, Double> total = new TreeMap<String, Double>(
			(a, b) -> temp.getOrDefault(b, zero).compareTo(temp.getOrDefault(a, zero))
		);
		total.putAll(temp);
		total.remove(nonCompetitor);
		
		final DecimalFormat percent = new DecimalFormat("##0.00%");
		enclose(out, Tag.section, () -> {
			enclose(out, Tag.h3, "Overall ranking (top ten)");
			enclose(out, Tag.small, () -> {
				outputResult(
					out,
					percent,
					getTilesAttr(nonCompetitor),
					nonCompetitor,
					temp.getOrDefault(nonCompetitor, zero).doubleValue(),
					corrPercentages.getOrDefault(nonCompetitor, zero).doubleValue(),
					perfPercentages.getOrDefault(nonCompetitor, zero).doubleValue()
				);
			});
			// attributes to make each entry appear
			// individually (in reverse order)
			final String[] attrs = {
				"class", "fragment",
				"data-fragment-index", null
			};
			final int limit = 10;
			final int[] counter = { limit };
			enclose(out, Tag.ol, () -> {
				total
					.entrySet()
					.stream()
					.limit(limit)
					.forEach(item -> {
						attrs[3] = Integer.toString(counter[0]--);
						enclose(out, Tag.li, () -> {
							final String artifactId = item.getKey();
							outputResult(
								out,
								percent,
								getTilesAttr(artifactId),
								artifactId,
								total.get(artifactId).doubleValue(),
								corrPercentages.get(artifactId).doubleValue(),
								perfPercentages.get(artifactId).doubleValue()
							);
						}, attrs);
					});
			}, ATTR_LINE);
		}, ATTR_BLUE_ZOOM);
		enclose(out, Tag.section, () -> {
			enclose(out, Tag.h3, "Reference: Ranking");
			enclose(out, Tag.small, () -> {
				final int[] counter = { 1 };
				final int max = total.size();
				enclose(out, Tag.p, () -> {
					total
					.entrySet()
					.stream()
					.forEach(item -> {
						final String artifactId = item.getKey();
						out.print(counter[0]++);
						out.print(". ");
						outputResult(
							out,
							percent,
							getTilesAttr(artifactId),
							artifactId,
							total.get(artifactId).doubleValue(),
							corrPercentages.get(artifactId).doubleValue(),
							perfPercentages.get(artifactId).doubleValue()
						);
						if (counter[0] < max) {
							out.print(" / ");
						}
					});				
				}, ATTR_LINE);
			});
		});
	}

	/**
	 * Outputs a result entry (artifactID as a tile, percentages)
	 * @param out the output stream
	 * @param percent the formatter
	 * @param attr the span attributes
	 * @param artifactId the artifact id
	 * @param total total percentage (0..1)
	 * @param corr correctness percentage
	 * @param perf performance percentage
	 */
	private void outputResult(final PrintStream out, final DecimalFormat percent, final String[] attr, final String artifactId, final double total, final double corr, final double perf) {
		enclose(out, Tag.span, attr, artifactId);
		enclose(out, Tag.span, ATTR_TEXT_ORANGE, percent.format(total));
		enclose(out, Tag.span, ATTR_TEXT_GREY, "("+percent.format(corr)+", "+percent.format(perf)+")");
	}

	/**
	 * Adds a slide with all participants to the report.
	 * @param out the stream to output to
	 * @param participants a list with the participants
	 */
	protected void writeParticipants(final PrintStream out, final List<String> participants) {
		final int max = participants.size();
		final List<List<String>> lines = new ArrayList<>(max/4);
		for (int i = 0; i < max; i+=4) {
			lines.add(participants.subList(i, Math.min(max, i+4)));
		}
		enclose(out, Tag.section, () -> {
				enclose(out, Tag.h1, "Challenge participants");
				enclose(out, Tag.h3, () ->
					enclose(out, Tag.span, ATTR_TEXT_GREY_NORMAL, "There are ", Integer.toString(max), " contestants:")
				);
				for (final List<String> line : lines) {
					enclose(out, Tag.p, () -> {
						line
							.stream()
							.forEach(item -> enclose(out, Tag.span, getTilesAttr(item), item));
					});
				}
				enclose(out, Tag.h3, () ->
					enclose(out, Tag.span, ATTR_TEXT_ORANGE_NORMAL, "Let's see the results...")
				);
			}, ATTR_BLUE_ZOOM);
	}

	/**
	 * Adds the report for a single artifact to the slides.
	 * @param out the stream to output to
	 * @param correctnessResult the correctness result map
	 * @param performanceResult the performance result map
	 * @param artifactId the ID of the artifact
	 * @param results the test results
	 */
	protected void writeReportSingle(final PrintStream out, final Map<String, AtomicInteger> correctnessResult, final Map<String, Map<String, Double>> performanceResult, final String artifactId, final List<TestResult> results) {
		// vertically nested per report
		final AtomicInteger ok = new AtomicInteger();
		final AtomicInteger nok = new AtomicInteger();
		final List<String> performanceItems = new ArrayList<>();
		final StringBuilder builder = new StringBuilder(256);
		for (TestResult result : results) {
			ok.addAndGet(result.ok.get());
			nok.addAndGet(result.nok.get());
			for (TestResult method : result.methods) {
				if (method.millis != null) {
					addPerformance(performanceResult, artifactId, result.name, method);
					builder.setLength(0);
					builder
						.append("<code>")
						.append(result.name)
						.append('.')
						.append(method.name)
						.append("</code> took ")
						.append(Integer.toString(method.millis.intValue()))
						.append("ms");
					performanceItems.add(builder.toString());
				}
			}
		}
		correctnessResult.put(artifactId, ok);
		enclose(out, Tag.section, () -> {
			enclose(out, Tag.section, () -> {
				enclose(out, Tag.h3, "<span class=\"ccnormal\">Results for ", artifactId, "</span>");
				if (nok.get() == 0) {
					enclose(out, Tag.p, "All ", ok.toString(), " tests were successful!");
				} else {
					enclose(out, Tag.p, ok.toString(), " tests were successful and ", nok.toString(), " tests failed.");
				}
				if (!performanceItems.isEmpty()) {
					enclose(out, Tag.ol, () -> {
						for (String item : performanceItems) {
							enclose(out, Tag.li, item);
						}
					});
				}
				enclose(out, Tag.p, "Find a detailed report below.");
			}, nok.get() == 0 ? ATTR_BACK_OK : ATTR_CLASS_NONE);
			for (TestResult singleTest : results) {
				enclose(out, Tag.section, () -> {
					enclose(out, Tag.h3, "<code class=\"ccnormal\">", singleTest.name, "</code>");
					enclose(out, Tag.ul, () -> {
						for (TestResult method : singleTest.methods) {
							final String code;
							if (method.trace == null) {
								code = "<code>";
							} else {
								code = "<code data-stacktrace=\""+method.trace+"\">";
							}
							if (method.millis == null) {
								enclose(out, Tag.li, method.trace == null ? ATTR_TEXT_OK : ATTR_TEXT_NOK, code, method.name, "</code>");
							} else {
								enclose(out, Tag.li, method.trace == null ? ATTR_TEXT_OK : ATTR_TEXT_NOK, code, method.name, " (", Integer.toString(method.millis.intValue()) ,"ms)</code>");
							}
						}
					});
				}, singleTest.nok.get() == 0 ? ATTR_BACK_OK : ATTR_BACK_NOK);
			}
		});
	}
	
	protected String[] getTilesAttr(final String artifactId) {
		if (nonCompetitor.equals(artifactId)) {
			return ATTR_TILE_DISABLED;
		}
		return ATTR_TILE;
	}
	
	protected void addPerformance(final Map<String, Map<String, Double>> performanceResult, final String artifactId, final String test, final TestResult method) {
		Map<String, Double> map = performanceResult.get(artifactId);
		if (map == null) {
			map = new TreeMap<>();
			performanceResult.put(artifactId, map);
		}
		map.put(test+'.'+method.name, method.millis);
	}

	protected void enclose(final PrintStream out, final Tag tag, final Runnable runner, final String... attributes) {
		startTag(out, tag, attributes);
		runner.run();
		closeTag(out, tag);
	}
	protected void enclose(final PrintStream out, final Tag tag, final String... content) {
		startTag(out, tag);
		for (String str : content) {
			out.print(str);
		}
		closeTag(out, tag);
	}
	protected void enclose(final PrintStream out, final Tag tag, final String[] attributes, final String... content) {
		startTag(out, tag, attributes);
		for (String str : content) {
			out.print(str);
		}
		closeTag(out, tag);
	}
	
	protected void startTag(final PrintStream out, final Tag tag, final String... attributes) {
		out.println();
		out.print('<');
		out.print(tag.name());
		if (attributes != null) {
			for (int i = 0; i < attributes.length; i+= 2) {
				out.print(' ');
				out.print(attributes[i]);
				out.print("=\"");
				out.print(attributes[i+1]);
				out.print('"');
			}
		}
		out.print('>');
	}
	protected void closeTag(final PrintStream out, final Tag tag) {
		out.print("</");
		out.print(tag.name());
		out.print('>');
	}

	private static class NullableMap<K, V> extends TreeMap<K, V> {
		
		private static final long serialVersionUID = 1L;
		private final V empty;
		
		public NullableMap(final V nullElem) {
			super();
			empty = nullElem;
		}

		@Override
		public V get(final Object key) {
			final V result = super.get(key);
			return result!=null?result:empty;
		}
		
	}

}
