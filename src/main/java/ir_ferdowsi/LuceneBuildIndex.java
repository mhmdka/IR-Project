package ir_ferdowsi;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * written by Mohammad Kahani & Arman Ghoreshi
 * Student id : 9512762447 & 9512762787
 * lucene @version 7.0.0
 */
public class LuceneBuildIndex {

    public static void main( String[] args ) {
        try {

            // change the following input and output paths to your local ones
            String pathCorpus = "pathToCorpusFile";
            String pathIndex = "pathToIndexFiles";

            Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );

            // Analyzer specifies options for text processing
            Analyzer analyzer = new Analyzer() {
                @Override
                protected TokenStreamComponents createComponents( String fieldName ) {
                    // Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
                    TokenStreamComponents ts = new TokenStreamComponents( new StandardTokenizer() );
                    // Step 2: transforming all tokens into lowercased ones (recommended for the majority of the problems)
                    ts = new TokenStreamComponents( ts.getTokenizer(), new LowerCaseFilter( ts.getTokenStream() ) );
                    // Step 3: whether to remove stop words
                    // Uncomment the following line to remove stop words
                    // ts = new TokenStreamComponents( ts.getTokenizer(), new StopFilter( ts.getTokenStream(), StandardAnalyzer.ENGLISH_STOP_WORDS_SET ) );
                    // Step 4: whether to apply stemming
                    // Uncomment the following line to apply Porter stemmer
                    // ts = new TokenStreamComponents( ts.getTokenizer(), new PorterStemFilter( ts.getTokenStream() ) );
                    return ts;
                }
            };

            IndexWriterConfig config = new IndexWriterConfig( analyzer );
            // Note that IndexWriterConfig.OpenMode.CREATE will override the original index in the folder
            config.setOpenMode( IndexWriterConfig.OpenMode.CREATE );

            IndexWriter ixwriter = new IndexWriter( dir, config );

            // This is the field setting for metadata field.
            FieldType fieldTypeMetadata = new FieldType();
            fieldTypeMetadata.setOmitNorms( true );
            fieldTypeMetadata.setIndexOptions( IndexOptions.DOCS );
            fieldTypeMetadata.setStored( true );
            fieldTypeMetadata.setTokenized( false );
            fieldTypeMetadata.freeze();

            // This is the field setting for normal text field.
            FieldType fieldTypeText = new FieldType();
            fieldTypeText.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS );
            fieldTypeText.setStoreTermVectors( true );
            fieldTypeText.setStoreTermVectorPositions( true );
            fieldTypeText.setTokenized( true );
            fieldTypeText.setStored( true );
            fieldTypeText.freeze();

            // You need to iteratively read each document from the corpus file,
            // create a Document object for the parsed document, and add that
            // Document object by calling addDocument().

            // Well, the following only works for small text files. DO NOT follow this part in your homework!
            InputStream instream = new GZIPInputStream( new FileInputStream( pathCorpus ) );
            String corpusText = new String( IOUtils.toByteArray( instream ), "UTF-8" );
            instream.close();

            Pattern pattern = Pattern.compile(
                    "<DOC>.+?<DOCNO>(.+?)</DOCNO>.+?<TITLE>(.+?)</TITLE>.+?<TEXT>(.+?)</TEXT>.+?</DOC>",
                    Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL
            );

            Matcher matcher = pattern.matcher( corpusText );
            int counter = 0;
            while ( matcher.find() ) {
                System.out.println(counter++);
                String docno = matcher.group( 1 ).trim();
                String title = matcher.group( 2 ).trim();
                String text = matcher.group( 3 ).trim();

                // Create a Document object
                Document d = new Document();
                // Add each field to the document with the appropriate field type options
                d.add( new Field( "docno", docno, fieldTypeMetadata ) );
                d.add( new Field( "title", title, fieldTypeText ) );
                d.add( new Field( "text", text, fieldTypeText ) );
                // Add the document to index.
                ixwriter.addDocument( d );
            }

            ixwriter.close();
            dir.close();

        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

}
