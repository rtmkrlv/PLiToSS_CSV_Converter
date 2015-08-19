/**
 * Created by rtmkrlv on 07.08.2015.
/*/
import java.io.IOException;
import java.io.Reader;
import org.supercsv.io.Tokenizer;
import org.supercsv.prefs.CsvPreference;

public class PLiTokenizer extends Tokenizer {

    public PLiTokenizer(Reader reader, CsvPreference preferences) {
        super(reader, preferences);
    }

    @Override
    protected String readLine() throws IOException {
        final String line = super.readLine();
        if (line == null) {
            return null;
        }

        final char quote = (char) getPreferences().getQuoteChar();
        final char delimiter = (char) getPreferences().getDelimiterChar();

        final StringBuilder b = new StringBuilder(line);

        int i = 0;
        int j;
        char c;
        boolean q;

        while (i < b.length()) {
            c = b.charAt(i++);
            if (c == quote) {
                while (i < b.length()) {
                    c = b.charAt(i++);
                    if (c == quote && (i == b.length() || b.charAt(i) == delimiter)) {
                        i++;
                        break;
                    }
                }
            } else if (c != delimiter){
                j = i - 1;
                q = false;
                while (i < b.length()) {
                    c = b.charAt(i++);
                    if (c == quote) {
                        q = true;
                        b.insert(i++, quote);
                    } else if (c == delimiter) {
                        break;
                    }
                }
                if (q) {
                    b.insert(j, quote);
                    b.insert(i, quote);
                    i += 2;
                }
            }
        }

        return b.toString();
    }
}
