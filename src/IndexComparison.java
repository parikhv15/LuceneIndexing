import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexComparison {
	private TrecTextDocumentUtils2 utils;

	public static final String INDEX_DIR = "index_analyzed/";
	public static final String FILE_DIR = "corpus/";
	public static final String STANDARD_AZR = "STANDARD";
	public static final String KEYWORD_AZR = "KEYWORD";
	public static final String SIMPLE_AZR = "SIMPLE";
	public static final String STOP_AZR = "STOP";

	public IndexComparison() {
		utils = new TrecTextDocumentUtils2();
	}

	public static void main(final String a[]) {
		File dirToIndex;
		Directory directory = null;
		IndexComparison luceneUtils = new IndexComparison();

		HashMap<String, Analyzer> analyzerList = new HashMap<String, Analyzer>();
		analyzerList.put("KEYWORD", new KeywordAnalyzer());
		analyzerList.put("SIMPLE", new SimpleAnalyzer());
		analyzerList.put("STOP", new StopAnalyzer());
		analyzerList.put("STANDARD", new StandardAnalyzer());

		try {

			dirToIndex = new File(FILE_DIR);

			Iterator itr = analyzerList.keySet().iterator();
			System.out.println("======================================================");
			System.out.println("Task 2: ");
			System.out.println("======================================================");

			while (itr.hasNext()) {
				String analyzer = (String) itr.next();
				directory = FSDirectory.open(Paths.get(INDEX_DIR + analyzer
						+ "/"));
				luceneUtils.deleteAllIndex(directory);
				luceneUtils.createIndex(dirToIndex, directory,
						analyzerList.get(analyzer));
				luceneUtils.printAnalyzerStats(analyzer, directory);

			}
			directory.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void createIndex(File dirToIndex, Directory directory,
			Analyzer analyzer) {
		IndexWriter iwriter;
		IndexWriterConfig config;

		final File[] filesToIndex = dirToIndex.listFiles();

		try {
			config = new IndexWriterConfig(analyzer);
			config.setOpenMode(OpenMode.CREATE);
			iwriter = new IndexWriter(directory, config);

			for (File file : filesToIndex) {
				String fileString = FileUtils.readFileToString(file, "UTF-8");
//				FileReader fr = new FileReader(file);
//				BufferedReader br = new BufferedReader(fr);
//				String fileString = "";
//				String line;
//				while ((line = br.readLine()) != null){
//					fileString += line;
//				}
//				
				String[] docElements = utils.getElements(fileString, "DOC");

				for (String docElement : docElements) {
					TrecTextDocument2 trecTextDocument = utils
							.parseDocument(docElement);

					Document doc = new Document();
					addFields(doc, trecTextDocument.getText(), "TEXT");

					iwriter.addDocument(doc);
				}
			}

			iwriter.forceMerge(1);
			iwriter.commit();
			iwriter.close();

			System.out.println("File Indexed Successfully...");
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printAnalyzerStats(String analyzer, Directory directory) {
		System.out.println("------------------------------------------------------");
		System.out.println(analyzer+" Analyzer:      [Field: <TEXT>]");
		System.out.println("------------------------------------------------------");
		
		try {
			 DirectoryReader ireader = DirectoryReader.open(directory);

			 Terms vocubulary = MultiFields.getTerms(ireader, "TEXT");
			
			 System.out.println("Number of Tokens for this Field: " + vocubulary.getSumTotalTermFreq());
			 System.out.println("Number of Terms in the Dictionary: " + vocubulary.size());

			ireader.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
	}
	
	public void addField(Document doc, String field, String fieldName) {
		if (field != null) {
			if (fieldName.equalsIgnoreCase("DOCNO"))
				doc.add(new StringField(fieldName, field, Store.YES));
			else
				doc.add(new TextField(fieldName, field, Store.YES));
		}
	}

	public void addFields(Document doc, String[] fields, String fieldName) {
		if (fields != null && fields.length != 0) {
			String collectiveField = "";
			for (String field : fields) {
				collectiveField += field + " ";
			}
//			System.out.println(collectiveField.length());
			doc.add(new TextField(fieldName, collectiveField, Store.YES));
		}
	}

	public void deleteAllIndex(final Directory directory) throws IOException {
		final String existingDirectories[] = directory.listAll();
		for (final String existingDirectory : existingDirectories)
			directory.deleteFile(existingDirectory);
	}
}

class TrecTextDocumentUtils2 {
	public TrecTextDocument2 parseDocument(String documentString) {
		TrecTextDocument2 document = new TrecTextDocument2();

		String docNo = getElement(documentString, "DOCNO");
		String head = getElement(documentString, "HEAD");
		String[] byLine = getElements(documentString, "BYLINE");
		String dateLine = getElement(documentString, "DATELINE");
		String[] text = getElements(documentString, "TEXT");

		document.setDocNo(docNo);
		document.setHead(head);
		document.setByLines(byLine);
		document.setDateLine(dateLine);
		document.setText(text);

		return document;
	}

	public String getElement(String document, String tag) {
		String element = StringUtils.substringBetween(document,
				"<" + tag + ">", "</" + tag + ">");
		return element;
	}

	public String[] getElements(String document, String tag) {
		String elements[] = StringUtils.substringsBetween(document, "<" + tag
				+ ">", "</" + tag + ">");
		return elements;
	}
}

class TrecTextDocument2 {
	private String docNo;
	private String head;
	private String[] byLine;
	private String dateLine;
	private String[] text;

	public TrecTextDocument2() {

	}

	public String getDocNo() {
		return docNo;
	}

	public void setDocNo(String docNo) {
		this.docNo = docNo;
	}

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public String[] getByLines() {
		return byLine;
	}

	public void setByLines(String[] byLine) {
		this.byLine = byLine;
	}

	public String getDateLine() {
		return dateLine;
	}

	public void setDateLine(String dateLine) {
		this.dateLine = dateLine;
	}

	public String[] getText() {
		return text;
	}

	public void setText(String[] text) {
		this.text = text;
	}
}
