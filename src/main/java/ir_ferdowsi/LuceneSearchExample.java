package ir_ferdowsi;
import edu.vt.cs.ir.utils.LuceneUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

/**
 * written by Mohammad Kahani & Arman Ghoreshi
 * Student id : 9512762447 9512762787
 * lucene @version 7.0.0
 */
public class LuceneSearchExample {

    public static void main( String[] args ) {
        try {

            String pathIndex = "pathToIndexFiles";
            BufferedWriter writer = new BufferedWriter(new FileWriter("pathToTFIDFResults"));
            //uncomment to save results to KM_result. note that this should be done for applying LMDirichlet similarity
            //BufferedWriter writer = new BufferedWriter(new FileWriter("pathToLMResults"));
            String result;
            // Just like building an index, we also need an Analyzer to process the query strings
            Analyzer analyzer = new Analyzer() {
                @Override
                protected TokenStreamComponents createComponents( String fieldName ) {
                    // Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
                    TokenStreamComponents ts = new TokenStreamComponents( new StandardTokenizer() );
                    // Step 2: transforming all tokens into lowercased ones (recommended for the majority of the problems)
                    ts = new TokenStreamComponents( ts.getTokenizer(), new LowerCaseFilter( ts.getTokenStream() ) );
                    // Step 3: whether to remove stop words
                    // Uncomment the following line to remove stop words
                    //ts = new TokenStreamComponents( ts.getTokenizer(), new StopFilter( ts.getTokenStream(), StandardAnalyzer.ENGLISH_STOP_WORDS_SET ) );
                    // Step 4: whether to apply stemming
                    // Uncomment the following line to apply Porter stemmer
                    //ts = new TokenStreamComponents( ts.getTokenizer(), new PorterStemFilter( ts.getTokenStream() ) );
                    return ts;
                }
            };
            int top = 10; // Let's just retrieve the talk 10 results
            String field = "text"; // the field you hope to search for
            QueryParser parser = new QueryParser( field, analyzer ); // a query parser that transforms a text string into Lucene's query object

            String qstr; // this is the textual search query
            Query query; // this is Lucene's query object

            // Okay, now let's open an index and search for documents
            Directory dir = FSDirectory.open( new File( pathIndex ).toPath() );
            IndexReader index = DirectoryReader.open( dir );

            // you need to create a Lucene searcher
            IndexSearcher searcher = new IndexSearcher( index );

            // Lucene's default ranking model is VSM, but it has also implemented a wide variety of retrieval models.
            //uncomment to apply LMDirichlet similarity and comment tfidf similarity instead
            //searcher.setSimilarity( new LMDirichletSimilarity());
            searcher.setSimilarity(new TFIDFSimilarity() {
                @Override
                public float tf(float v) {

                    return (float)(Math.log(v+1));
                }

                @Override
                public float idf(long l, long l1) {
                    double result = ((double)l1 + 1 ) / ((int)l + 1 );
                    return (float) Math.log(result);
                }

                @Override
                public float lengthNorm(int i) {
                    return (float) (1/Math.sqrt((double) i));
                }

                @Override
                public float sloppyFreq(int i) {
                    return 0;
                }

                @Override
                public float scorePayload(int i, int i1, int i2, BytesRef bytesRef) {
                    return 0;
                }
            });
            int i = 0;

            //getting 10 optional queries
            while (i < 10){
                System.out.println("please enter your quere: \n"+"number of queries so far :"+i+"\n");
                Scanner input = new Scanner(System.in);
                qstr = input.nextLine();
                query = parser.parse(qstr);
                TopDocs docs = searcher.search( query, top ); // retrieve the top 10 results; retrieved results are stored in TopDoc
                int rank = 1;
                for ( ScoreDoc scoreDoc : docs.scoreDocs ) {
                    int docid = scoreDoc.doc;
                    double score = scoreDoc.score;
                    String docno = LuceneUtils.getDocno( index, "docno", docid );
                    result = i+"\t"+"Q0"+"\t"+docno+"\t"+rank+"\t"+score+"\t"+"Ferdowsi\n";
                    writer.write(result);
                    rank++;
                }
                System.out.println("\n\n\n###########Next Query############\n\n\n");
                i++;

            }
            writer.close();
            index.close();
            dir.close();

        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

}
