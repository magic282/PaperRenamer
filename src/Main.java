import java.io.File;
import java.io.IOException;

import org.docear.pdf.*;

/**
 * 
 */

/**
 * @author Jeremy
 *
 */
public class Main {

	private static int pdfCnt;
	private static int sucCnt;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		pdfCnt = 0;
		sucCnt = 0;
		for (String arg : args) {
			File f = new File(arg);
			if (!f.exists()) {
				continue;
			}
			if (f.isDirectory()) {
				processDirectory(f);
			} else {
				processFile(f);
			}
		}
		System.out.printf("Renamed %d in %d pdf files.\n", sucCnt, pdfCnt);
	}

	private static void processDirectory(File folder) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				processDirectory(fileEntry);
			} else {
				processFile(fileEntry);
			}
		}
	}
	
	private static String filterFilename(String fn){
		if (fn== null) {
			return fn;
		}
		char windowsBannedChar[] = new char[]{'\\', '/',':','*','?', '"', '<', '>', '|'};
		StringBuilder sb = new StringBuilder();
		for(int i = 0;i<fn.length();++i){
			boolean isInBannedCharSet = false;
			for(int j = 0;j<windowsBannedChar.length;++j){
				if(windowsBannedChar[j] == fn.charAt(i)){
					isInBannedCharSet = true;
					break;
				}
			}
			if(!isInBannedCharSet){
				sb.append(fn.charAt(i));
			}
		}
		return sb.toString();
	}
	

	private static boolean processFile(File file) {
		if (!file.canWrite()) {
			return false;
		}
		if (!file.toString().endsWith(".pdf")) {
			return true;
		}
		++pdfCnt;
		System.out.printf("Processing %s ", file.getName());
		PdfDataExtractor extractor = new PdfDataExtractor(file);
		try {
			String title = extractor.extractTitle();
			title = filterFilename(title);
			if (title != null && isNameLegal(title)) {
				if (renameFile(file, title + ".pdf")) {
					++sucCnt;
					System.out.printf("| [success]\n");
				} else {
					System.out.printf("| [fail]\n");
				}
			}
		} catch (IOException e) {
			System.err.println("Could not extract title for "
					+ file.getAbsolutePath() + ": " + e.getMessage());
			return false;
		}
		return true;
	}

	private static boolean isNameLegal(String name) {
		int cnt = 0;
		final float threshold = (float) 0.8;
		for (int i = 0; i < name.length(); ++i) {
			if (isAsciiPrintable(name.charAt(i))) {
				++cnt;
			}
		}
		if (cnt * 1.0 / name.length() >= threshold) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean isAsciiPrintable(char ch) {
		return ch >= 32 && ch < 127;
	}

	private static boolean renameFile(File file, String newname) {
		// File (or directory) with new name
		File file2 = new File(file.getParent() + File.separatorChar + newname);
		if (file2.exists())
			try {
				throw new java.io.IOException("file exists");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				System.err.println("Rename failed, file exists.");
				return false;
			}

		// Rename file (or directory)
		boolean success = file.renameTo(file2);
		if (success) {
			// File was not successfully renamed
			return true;
		} else {
			System.err.printf("Rename fail for file %s\n",
					file.getAbsolutePath());
			return false;
		}
	}
}
