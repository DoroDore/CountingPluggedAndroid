package com.example.countingplugged;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.identity.document.Document;
import com.google.android.material.textfield.TextInputEditText;

//All my PDF Handling Imports
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;



public class MainActivity extends AppCompatActivity {

    // Declare the assets on screen
    private TextView answer;
    private static TextInputEditText editText;
    private EditText editNumText;
    private Button button1, button2, backButton, getUniqueWordsButton, getSentenceCountButton, paragraphGenerationButton, exportButton;
    private static final HashSet<String> commonWords = new HashSet<>();
    private static final HashMap<String, Integer> words = new HashMap<>();

    // Create the stored values of all app functions to prevent need to rerun code
    private static int wordCount = -1;
    private static int sentenceCount = -1;
    private static int uniqueWords = -1;
    private static ArrayList<Word> topFive;
    private static String paragraph, currentFileName;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Load common words
        loadCommonWords();

        // Binds the actual assets on screen to the code versions of them
        answer = findViewById(R.id.textView);
        editText = findViewById(R.id.textInputEditText);
        editNumText = findViewById(R.id.editTextNumber);
        button1 = findViewById(R.id.buttonGetTopWord);
        button2 = findViewById(R.id.buttonGetFiveWords);
        backButton = findViewById(R.id.backButton);
        getUniqueWordsButton = findViewById(R.id.buttonUniqueWords);
        getSentenceCountButton = findViewById(R.id.buttonSentenceCount);
        paragraphGenerationButton = findViewById(R.id.buttonGenerateConfirm);
        exportButton = findViewById(R.id.buttonExport);

        // Sets the function of clicking button1 from now on.
        button1.setOnClickListener(view -> {
            answer.setTextSize(TypedValue.COMPLEX_UNIT_PT, 8);
            int count = getWordCount(editText.getText().toString());
            if (count > 0) {
                answer.setText("There are " + count + " words in the text file \"" + editText.getText().toString() + "\".");
            } else {
                answer.setText("No words found.");
            }
        });
        button2.setOnClickListener(view -> {
            answer.setTextSize(TypedValue.COMPLEX_UNIT_PT, 8);
            readToMap(editText.getText().toString());
            ArrayList<Word> topFive = getTop(words, commonWords, 5);
            if (!topFive.isEmpty()) {
                String htmlText = "The top five common words in the text file \"" + editText.getText().toString() + "\" are<br><br>" +
                        "1. \"<font color='blue'><b>" + topFive.get(0).getWord() + "</b></font>\" with <font color='blue'><b>" + topFive.get(0).getCount() + "</b></font> occurrences.<br>" +
                        "2. \"<font color='blue'><b>" + topFive.get(1).getWord() + "</b></font>\" with <font color='blue'><b>" + topFive.get(1).getCount() + "</b></font> occurrences.<br>" +
                        "3. \"<font color='blue'><b>" + topFive.get(2).getWord() + "</b></font>\" with <font color='blue'><b>" + topFive.get(2).getCount() + "</b></font> occurrences.<br>" +
                        "4. \"<font color='blue'><b>" + topFive.get(3).getWord() + "</b></font>\" with <font color='blue'><b>" + topFive.get(3).getCount() + "</b></font> occurrences.<br>" +
                        "5. \"<font color='blue'><b>" + topFive.get(4).getWord() + "</b></font>\" with <font color='blue'><b>" + topFive.get(4).getCount() + "</b></font> occurrences.<br>";

                answer.setText(Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY));
            }
            else {
                answer.setText("No words found.");
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HomePage.class);
                startActivity(intent);
            }
        });
        getUniqueWordsButton.setOnClickListener(view -> {
            answer.setTextSize(TypedValue.COMPLEX_UNIT_PT, 8);
            int count = getUniqueWords(editText.getText().toString());
            if (count > 0) {
                answer.setText("There are " + count + " unique words in the text file \"" + editText.getText().toString() + "\".");
            } else {
                answer.setText("No unique words found.");
            }
        });
        getSentenceCountButton.setOnClickListener(view -> {
            answer.setTextSize(TypedValue.COMPLEX_UNIT_PT, 8);
            int count = getSentenceCount(editText.getText().toString());
            if (count > 0) {
                answer.setText("There are " + count + " sentences in the text file \"" + editText.getText().toString() + "\".");
            } else {
                answer.setText("No sentences found.");
            }
        });
        paragraphGenerationButton.setOnClickListener(view -> {
            String numText = editNumText.getText().toString();
            String text = editText.getText().toString();

            // Check if the input fields are not empty
            if (numText.isEmpty() || text.isEmpty()) {
                // Handle the case when the input fields are empty
                Toast.makeText(getApplicationContext(), "Please enter valid input", Toast.LENGTH_SHORT).show();
                return; // Exit the method early to prevent further execution
            }

            try {
                int temperature = Integer.parseInt(numText);
                readToMap(text);
                String paragraph = generateParagraph(temperature);
                answer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                answer.setText(paragraph);
            } catch (NumberFormatException e) {
                // Handle the case when the input for temperature is not a valid integer
                Toast.makeText(getApplicationContext(), "Please enter a valid temperature", Toast.LENGTH_SHORT).show();
            }
        });
        exportButton.setOnClickListener(view -> {
            createPdf();
            Toast.makeText(getApplicationContext(), "PDF exported successfully", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Read a file and store the words in a map
     */
    public void readToMap(String filePath) {
        words.clear();
        try {
            if (filePath.endsWith(".pdf")) {
                //Toast.makeText(this, "Read PDF", Toast.LENGTH_LONG).show();
                readPdfToMap(filePath);
            } else {
                AssetManager assetManager = getAssets();
                Scanner scanner = new Scanner(assetManager.open(filePath));
                scanner.useDelimiter("[\\p{Punct}\\s&&[^'’]]+");
                while (scanner.hasNext()) {
                    String word = scanner.next().toLowerCase();
                    words.put(word, words.getOrDefault(word, 0) + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the word count of a file
     * @param filePath
     * @return word count
     */
    public int getWordCount(String filePath) {
        newTextChecker();
        int count = 0;
        try {
            AssetManager assetManager = getAssets();
            Scanner scanner = new Scanner(assetManager.open(filePath));
            scanner.useDelimiter("[\\p{Punct}\\s&&[^'’]]+");
            while (scanner.hasNext()) {
                scanner.next();
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentFileName = editText.getText().toString();
        wordCount = count;
        return count;
    }
    public int getSentenceCount(String filePath) {
        newTextChecker();
        int count = 0;
        try {
            AssetManager assetManager = getAssets();
            Scanner scanner = new Scanner(assetManager.open(filePath));
            scanner.useDelimiter("[.!?]");
            while (scanner.hasNext()) {
                scanner.next();
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentFileName = editText.getText().toString();
        sentenceCount = count;
        return count;
    }
    public int getUniqueWords(String filePath) {
        newTextChecker();
        HashSet<String> uniqueWordsMap = new HashSet<>();
        HashSet<String> bannedWords = new HashSet<>();
        try {
            AssetManager assetManager = getAssets();
            Scanner scanner = new Scanner(assetManager.open(filePath));
            scanner.useDelimiter("[\\p{Punct}\\s&&[^'’]]+");
            while (scanner.hasNext()) {
                String word = scanner.next().toLowerCase(); // Store the scanned word
                if (!uniqueWordsMap.contains(word) && !bannedWords.contains(word)) {
                    uniqueWordsMap.add(word);
                } else {
                    uniqueWordsMap.remove(word);
                    bannedWords.add(word);
                }
            }
            scanner.close(); // Close the scanner after use
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentFileName = editText.getText().toString();
        uniqueWords = uniqueWordsMap.size();
        return uniqueWordsMap.size();
    }
    /**
     * Load common words from assets
     */
    public void loadCommonWords() {
        try {
            AssetManager assetManager = getAssets();
            BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open("commonWords.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                commonWords.add(line.toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Word> getTop(Map<String, Integer> words, HashSet<String> commonWords, int limit) {
        newTextChecker();
        ArrayList<Word> topWords = new ArrayList<>();
        Map<String, Integer> filteredWords = words.entrySet().stream()
                .filter(entry -> !commonWords.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<Map.Entry<String, Integer>> sortedWords = new ArrayList<>(filteredWords.entrySet());
        sortedWords.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedWords) {
            if (count == limit) {
                break;
            }
            Word word = new Word(entry.getKey(), entry.getValue());
            topWords.add(word);
            count++;
        }
        currentFileName = editText.getText().toString();
        topFive = topWords;
        return topWords;
    }
    public String generateParagraph(int temperature) {
        readToMap(editText.getText().toString());
        ArrayList<Word> wordGroup = getTop(words, commonWords, words.size()/10*temperature); //This makes it so that less common words get cut out the lower the temp
        String thing = "";
        Random rand = new Random();
        for (int i = 0; i < 100; i++) {
            int index = rand.nextInt(wordGroup.size());
            thing += wordGroup.get(index).getWord() + " ";
        }
        //Stores it for later
        paragraph = thing;
        return thing;
    }
    public void readPdfToMap(String fileName) {
        words.clear();
        try {
            // Access PDF from assets
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open(fileName);
            PdfReader reader = new PdfReader(inputStream);
            StringBuilder text = new StringBuilder();

            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                text.append(PdfTextExtractor.getTextFromPage(reader, i));
            }
            reader.close();

            // Populate words map
            Scanner scanner = new Scanner(text.toString());
            scanner.useDelimiter("[\\p{Punct}\\s&&[^'’]]+");
            while (scanner.hasNext()) {
                String word = scanner.next().toLowerCase();
                words.put(word, words.getOrDefault(word, 0) + 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readTextFromPdf(String fileName) {
        try {
            // Access PDF from assets
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open(fileName);
            PdfReader reader = new PdfReader(inputStream);
            StringBuilder text = new StringBuilder();

            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                text.append(PdfTextExtractor.getTextFromPage(reader, i));
            }
            reader.close();

            return text.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    public static void newTextChecker() {
        if (!editText.getText().toString().equals(currentFileName)) {
            wordCount = -1;
            uniqueWords = -1;
            sentenceCount = -1;
            topFive = null;
            paragraph = null;
        }
    }
    public void finalExportCheck() {
        if (wordCount == -1) {
            getWordCount(editText.getText().toString());
        }
        if (uniqueWords == -1) {
            getUniqueWords(editText.getText().toString());
        }
        if (sentenceCount == -1) {
            getSentenceCount(editText.getText().toString());
        }
        if (topFive == null || topFive.isEmpty()) {
            topFive = getTop(words, commonWords, 5);
        }
        if (paragraph == null) {
            generateParagraph(5);
        }
    }

    public void createPdf() {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(12);

        finalExportCheck();
        int yPosition = 50;
        int pageWidth = pageInfo.getPageWidth();
        int padding = 50;
        int maxWidth = pageWidth - 2 * padding;

        String[] strings = {
                "Word Count: " + wordCount,
                "Unique Words: " + uniqueWords,
                "Sentence Count: " + sentenceCount,
                "Top Five Words: "
        };

        for (String str : strings) {
            yPosition = drawTextWrapped(canvas, paint, str, padding, yPosition, maxWidth);
            yPosition += 30;
        }

        if (topFive != null && !topFive.isEmpty()) {
            for (int i = 0; i < 5; i++) {
                String str = "[" + (i + 1) + "] " + topFive.get(i).getWord() + " - " + topFive.get(i).getCount();
                yPosition = drawTextWrapped(canvas, paint, str, padding, yPosition, maxWidth);
                yPosition += 30;
            }
        }

        document.finishPage(page);

        FileOutputStream outputStream = null;
        try {
            File directory = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (directory != null && !directory.exists()) {
                boolean dirCreated = directory.mkdirs();
                Log.d("PDFCreation", "Directory created: " + dirCreated);
            }

            File outputFile = new File(directory, "example.pdf");
            outputStream = new FileOutputStream(outputFile);
            document.writeTo(outputStream);
            Toast.makeText(this, "PDF saved to: " + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            Log.d("PDFCreation", "PDF saved to: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e("PDFCreation", "Error: " + e.getMessage(), e);
        } finally {
            document.close();
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e("PDFCreation", "Error closing stream: " + e.getMessage(), e);
                }
            }
        }
    }

    private int drawTextWrapped(Canvas canvas, Paint paint, String text, int x, int y, int maxWidth) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (paint.measureText(line + word) <= maxWidth) {
                line.append(word).append(" ");
            } else {
                canvas.drawText(line.toString(), x, y, paint);
                y += paint.descent() - paint.ascent(); // Move to next line
                line = new StringBuilder(word + " ");
            }
        }
        // Draw the last line
        if (!line.toString().isEmpty()) {
            canvas.drawText(line.toString(), x, y, paint);
        }
        return y;
    }

}