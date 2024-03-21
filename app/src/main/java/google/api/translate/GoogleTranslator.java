package google.api.translate;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.io.*;
import java.util.Set;
import java.util.TreeSet;

/**
 * 저장 확인용 쿼리
 * select a.service_name, a.layout_code, a.message_key
 * 	, a.message_content as ko_content
 * 	, b.message_content as en_content
 * 	, c.message_content as vi_content
 * from his.tbl_layout_message a
 * left join (
 * 	select service_name, message_key, layout_code, locale_code, message_content from his.tbl_layout_message
 * 	where
 * 		service_name = 'hrs'
 * 		and locale_code = 'en_US'
 * ) b on (a.service_name = b.service_name
 * 	and a.layout_code = b.layout_code
 * 	and a.message_key = b.message_key)
 * left join (
 * 	select service_name, message_key, layout_code, locale_code, message_content from his.tbl_layout_message
 * 	where
 * 		service_name = 'hrs'
 * 		and locale_code = 'vi_VN'
 * ) c on (a.service_name = c.service_name
 * 	and a.layout_code = c.layout_code
 * 	and a.message_key = c.message_key)
 * where
 * 	a.service_name = 'hrs'
 * 	and a.locale_code = 'ko_KR'
 * ;
 *
 */
public class GoogleTranslator {
    private static final String JSON_PATH = "/Users/manjineum/Desktop/my/google-api/translateproject-417704-6b8769fdeade.json";
    private static final String INPUT = "/app/src/test/resources/룰워크벤치.txt";

    public static void main(String[] args) throws Exception {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(new File(JSON_PATH));
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(inputStream);
            Translate translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();

            File inputFile = new File(System.getProperty("user.dir") + INPUT);
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));

            File outputFile = new File(inputFile.getAbsolutePath() + ".out");
            outputStream = new FileOutputStream(outputFile);


            Set<String> duplicateChecker = new TreeSet<>();

            String part = null;
            String line = null;
            while((line = reader.readLine()) != null) {
                if(line.startsWith("//")) {
                    part = line.substring(line.indexOf("//") + 2);
                    duplicateChecker.clear();
                    continue;
                }

                if(duplicateChecker.contains(line)) {
                    continue;
                }
                duplicateChecker.add(line);

                outputStream.write(createInsertQuery(part, "ko_KR", line, line).getBytes());

                String data = translate(translate, line, "ko", "en");
                outputStream.write(createInsertQuery(part, "en_US", line, data).getBytes());

                data = translate(translate, line, "ko", "vi");
                outputStream.write(createInsertQuery(part, "vi_VN", line, data).getBytes());

                outputStream.flush();
            }

            outputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }

    }

    private static String translate(Translate translate, String data, String fromLocale, String toLocale) {
        Translation translation = translate.translate(data, Translate.TranslateOption.sourceLanguage(fromLocale),
                Translate.TranslateOption.targetLanguage(toLocale));
        String translatedText = translation.getTranslatedText();
        return translatedText.replaceAll("&#39;", "\'");
    }

    private static String createInsertQuery(String part, String localeCode, String key, String content) {
        StringBuilder builder = new StringBuilder();
        builder.append("insert into his.tbl_layout_message (service_name, layout_code, locale_code, message_key, message_content, create_date , update_date) values ('hrs','")
                .append(part).append("', '").append(localeCode).append("','").append(key).append("', '").append(content).append("', now(), now());\r\n");
        return builder.toString();
    }


//    private static void trans1() {
//        // json 파일에서 GoogleCredentials 객체 생성
//        try (InputStream serviceAccountStream = new FileInputStream(new File(JSON_PATH))) {
//            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
//            // Translate 서비스 생성
//            Translate translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();
//            // 번역 api 코드 추가
//            Translation translation = translate.translate("한글", Translate.TranslateOption.sourceLanguage("ko"),
//                    Translate.TranslateOption.targetLanguage("vi"));
//            String translatedText = translation.getTranslatedText();
//            translatedText = translatedText.replaceAll("&#39;", "\'");
//
//            System.out.println(translatedText);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    //무료 변역
    // api: https://translate.google.com/m?sl=ko&tl=vi&q=%EB%B2%88%EC%97%AD%EB%AC%B8%EC%9E%A5
}
