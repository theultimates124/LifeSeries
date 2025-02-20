package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.trivia;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class TriviaQuestionManager {
    private static final String defaultEasyTrivia = "[\n\t{\n\t\t\"question\": \"easyquestion1\",\n\t\t\"answers\": [\"Answer 1\", \"Answer 2\", \"Answer 3\", \"Answer 4\"],\n\t\t\"correct_answer_index\": 0\n\t},\n\t{\n\t\t\"question\": \"easyquestion2\",\n\t\t\"answers\": [\"Answerr 1\", \"Answerr 2\", \"Answerr 3\", \"Answerr 4\"],\n\t\t\"correct_answer_index\": 1\n\t},\n\t{\n\t\t\"question\": \"easyquestion3\",\n\t\t\"answers\": [\"Answerrr 1\", \"Answerrr 2\", \"Answerrr 3\", \"Answerrr 4\"],\n\t\t\"correct_answer_index\": 2\n\t}\n]";
    private static final String defaultNormalTrivia = "[\n\t{\n\t\t\"question\": \"normalquestion1\",\n\t\t\"answers\": [\"Answer 1\", \"Answer 2\", \"Answer 3\", \"Answer 4\"],\n\t\t\"correct_answer_index\": 0\n\t},\n\t{\n\t\t\"question\": \"normalquestion2\",\n\t\t\"answers\": [\"Answerr 1\", \"Answerr 2\", \"Answerr 3\", \"Answerr 4\"],\n\t\t\"correct_answer_index\": 1\n\t},\n\t{\n\t\t\"question\": \"normalquestion3\",\n\t\t\"answers\": [\"Answerrr 1\", \"Answerrr 2\", \"Answerrr 3\", \"Answerrr 4\"],\n\t\t\"correct_answer_index\": 2\n\t}\n]";
    private static final String defaultHardTrivia = "[\n\t{\n\t\t\"question\": \"hardquestion1\",\n\t\t\"answers\": [\"Answer 1\", \"Answer 2\", \"Answer 3\", \"Answer 4\"],\n\t\t\"correct_answer_index\": 0\n\t},\n\t{\n\t\t\"question\": \"hardquestion2\",\n\t\t\"answers\": [\"Answerr 1\", \"Answerr 2\", \"Answerr 3\", \"Answerr 4\"],\n\t\t\"correct_answer_index\": 1\n\t},\n\t{\n\t\t\"question\": \"hardquestion3\",\n\t\t\"answers\": [\"Answerrr 1\", \"Answerrr 2\", \"Answerrr 3\", \"Answerrr 4\"],\n\t\t\"correct_answer_index\": 2\n\t}\n]";
    private File FILE;
    private File FOLDER;
    public TriviaQuestionManager(String folder, String file) {
        FILE = new File(folder + "/" + file);
        FOLDER = new File(folder);
        if (!FOLDER.exists()) {
            FOLDER.mkdirs();
        }
        if (!FILE.exists()) {
            try {
                FILE.createNewFile();
                if (file.startsWith("easy-")) {
                    setFileContent(defaultEasyTrivia);
                }
                else if (file.startsWith("normal-")) {
                    setFileContent(defaultNormalTrivia);
                }
                else if (file.startsWith("hard-")) {
                    setFileContent(defaultHardTrivia);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void setFileContent(String content) {
        FileWriter myWriter;
        try {
            myWriter = new FileWriter(FILE, false);
            myWriter.write(content);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<TriviaQuestion> getTriviaQuestions() throws IOException {
        String content = new String(Files.readAllBytes(FILE.toPath()));
        Gson gson = new Gson();
        return gson.fromJson(content, new TypeToken<List<TriviaQuestion>>() {}.getType());
    }
}