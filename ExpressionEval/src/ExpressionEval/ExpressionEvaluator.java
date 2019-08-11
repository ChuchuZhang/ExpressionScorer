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
	public static List<String> evalExpressionsFromFile(String filename) throws FileNotFoundException, IOException {
		List<String> lines = InputHandler.readFile(filename);
		List<String> outputFilenames = new ArrayList<String>();
		for (int i = 0; i < lines.size(); i++) {
			ExpressionScorer.ExpressionVal rtn;
			try {
				List<ExpressionCommon.Token> tokens = InputHandler.tokenize(lines.get(i));
				rtn = ExpressionScorer.evalTokens(tokens);
			} catch (Exception ex) {
				rtn = new ExpressionScorer.ExpressionVal(ex);
			}
			outputFilenames.add(OutputHandler.writeFile(rtn, filename, i));
		}
		return outputFilenames;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		for (String filename : args) {
			try {
				System.out.print("results for file " + filename + ": ");
				System.out.println(evalExpressionsFromFile(filename));
			} catch (Exception e) {
				System.out.print("processing file " + filename + " ecounters exception: ");
				e.printStackTrace();
			}
		}
	}

}
