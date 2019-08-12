package ExpressionEval;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The entry point of the expression evaluator. This class evaluates expressions
 * given filenames and output to files, one file per expression.
 *
 */
public class ExpressionEvaluator {
	/**
	 * Given a filename, parse and evaluate the expression line by line and output the results to files.
	 * @param filename		The input filename
	 * @param useCacheValue  If applying the cache optimization to improve performance.
	 * @return A list of filenames which represents the output file of the expressions.
	 */
	public static List<String> evalExpressionsFromFile(String filename, boolean useCachedValue)
			throws FileNotFoundException, IOException {
		List<String> lines = InputHandler.readFile(filename);
		List<String> outputFilenames = new ArrayList<String>();
		for (int i = 0; i < lines.size(); i++) {
			ExpressionScorer.ExpressionVal rtn;
			try {
				List<ExpressionCommon.Token> tokens = InputHandler.tokenize(lines.get(i));
				rtn = ExpressionScorer.evalTokens(tokens, useCachedValue);
			} catch (Exception ex) {
				rtn = new ExpressionScorer.ExpressionVal(ex);
			}
			outputFilenames.add(OutputHandler.writeFile(rtn, filename, i));
		}
		return outputFilenames;
	}

	/**
	 * Evaluate expressions and benchmark with given filenames. This will first run
	 * evaluation without cache and then with cache. The filenames are passed by
	 * |args| and the repeat time of the benchmark is specified by environment
	 * variable |REPEAT_TIME|.
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String value = System.getenv("REPEAT_TIME");
		Integer benchmarkRepeatTime;
		try {
			benchmarkRepeatTime = Integer.valueOf(value);
		} catch (Exception ex) {
			System.out.println("Invalid repeat time for benchmark");
			return;
		}
		long before = System.currentTimeMillis();
		for (int i = 0; i < benchmarkRepeatTime; i++) {
			for (String filename : args) {
				try {
					List<String> filenames = evalExpressionsFromFile(filename, false);
					if (i == benchmarkRepeatTime - 1) {
						System.out.print("results for file " + filename + ": ");
						System.out.println(filenames);
					}
				} catch (Exception e) {
					System.out.print("processing file " + filename + " ecounters exception: ");
					e.printStackTrace();
				}
			}
		}
		long now = System.currentTimeMillis();
		System.out.println("Seconds elapsed for evaluation without cache: " + (now - before) / 1000F + " seconds.");

		before = System.currentTimeMillis();
		for (int i = 0; i < benchmarkRepeatTime; i++) {
			for (String filename : args) {
				try {
					List<String> filenames = evalExpressionsFromFile(filename, true);
					if (i == benchmarkRepeatTime - 1) {
						System.out.print("results for file " + filename + ": ");
						System.out.println(filenames);
					}
				} catch (Exception e) {
					System.out.print("processing file " + filename + " ecounters exception: ");
					e.printStackTrace();
				}
			}
		}
		now = System.currentTimeMillis();
		System.out.println("Seconds elapsed for evaluation with cache: " + (now - before) / 1000F + " seconds.");
	}

}
