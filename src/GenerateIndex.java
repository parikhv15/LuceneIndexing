import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class GenerateIndex {
	private TrecTextDocumentUtils utils;

	public static final String INDEX_DIR = "index/";
	public static final String FILE_DIR = "corpus/";
	
	public GenerateIndex() {
		utils = new TrecTextDocumentUtils();
	}

	public static void main(final String a[]) {
		File dirToIndex;
		Directory directory;
		GenerateIndex luceneUtils = new GenerateIndex();

		try {
			directory = FSDirectory.open(Paths.get(INDEX_DIR));
			luceneUtils.deleteAllIndex(directory);
			dirToIndex = new File(FILE_DIR);
			luceneUtils.createIndex(dirToIndex, directory);

			System.out.println("======================================================");
			System.out.println("Task 1:");
			System.out.println("======================================================");
			
			System.out.println("Total Documents in Corpus: " + luceneUtils.getDocumentCount(directory));
			directory.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void createIndex(final File dirToIndex, final Directory directory) {
		Analyzer analyzer;
		IndexWriter iwriter;
		IndexWriterConfig config;

		final File[] filesToIndex = dirToIndex.listFiles();

		analyzer = new StandardAnalyzer();
		try {
			config = new IndexWriterConfig(analyzer);
			iwriter = new IndexWriter(directory, config);

			for (File file : filesToIndex) {
				String fileString = FileUtils.readFileToString(file);
				String[] docElements = utils.getElements(fileString, "DOC");

				for (String docElement : docElements) {
					TrecTextDocument trecTextDocument = utils
							.parseDocument(docElement);

					Document doc = new Document();

					addField(doc, trecTextDocument.getDocNo(), "DOCNO");
					addField(doc, trecTextDocument.getHead(), "HEAD");
					addFields(doc, trecTextDocument.getByLines(), "BYLINE");
					addField(doc, trecTextDocument.getDateLine(), "DATELINE");
					addField(doc, trecTextDocument.getText(), "TEXT");

					iwriter.addDocument(doc);
				}
			}

			iwriter.close();

			System.out.println("File Indexed Successfully...");
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getDocumentCount(Directory directory) {
		int maxDoc = 0;
		
		try {
			DirectoryReader ireader = DirectoryReader.open(directory);
			maxDoc = ireader.maxDoc();
			
			ireader.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return maxDoc;
	}

	public void addField(Document doc, String field, String fieldName) {
		if (field != null) {
			if (fieldName == "DOC")
				doc.add(new StringField(fieldName, field, Store.YES));
			else
				doc.add(new TextField(fieldName, field, Store.YES));
		}
	}

	public void addFields(Document doc, String[] fields, String fieldName) {
		if (fields != null && fields.length != 0) {
			for (final String field : fields) {
				doc.add(new TextField(fieldName, field, Store.YES));
			}
		}
	}

	public void deleteAllIndex(final Directory directory) throws IOException {
		final String existingDirectories[] = directory.listAll();
		for (final String existingDirectory : existingDirectories)
			directory.deleteFile(existingDirectory);
	}
}

class TrecTextDocumentUtils {
	public TrecTextDocument parseDocument(String documentString) {
		TrecTextDocument document = new TrecTextDocument();

		String docNo = getElement(documentString, "DOCNO");
		String head = getElement(documentString, "HEAD");
		String[] byLine = getElements(documentString, "BYLINE");
		String dateLine = getElement(documentString, "DATELINE");
		String text = getElement(documentString, "TEXT");

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

class TrecTextDocument {
	private String docNo;
	private String head;
	private String[] byLine;
	private String dateLine;
	private String text;

	public TrecTextDocument() {

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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
