package ExpressionEval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * This class offers helper function to output evaluation result to files.
 *
 */
public class OutputHandler {
	/**
	 * Write evaluation result to a output file. If the evaluation succeeds, this
	 * will write the value and binary expression tree; otherwise print the error
	 * message to the file.
	 * 
	 * @param result
	 *            Expression evaluation result or exception.
	 * @param originalFilename
	 *            Absolute path of the original filename .
	 * @param originalLineNum
	 *            The line in the file where the expression presents.
	 * @return The absolute filename of the output file.
	 */
	public static String writeFile(ExpressionScorer.ExpressionVal result, String originalFilename, int originalLineNum)
			throws FileNotFoundException, IOException {
		// Split filename from extension
		String[] tokens = originalFilename.split("\\.(?=[^\\.]+$)");
		StringBuilder fileNameBuilder = new StringBuilder(tokens[0]);
		fileNameBuilder.append("_");
		fileNameBuilder.append(originalLineNum);
		String outputFilename = fileNameBuilder.toString();
		File fout = new File(outputFilename);
		FileOutputStream fos = new FileOutputStream(fout);

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		if (result.exp == null) {
			bw.write(String.valueOf(result.value));
			bw.newLine();
			bw.write(result.expressionTree.toString());
		} else {
			bw.write(result.exp.getMessage());
		}
		bw.close();
		return outputFilename;
	}
}
