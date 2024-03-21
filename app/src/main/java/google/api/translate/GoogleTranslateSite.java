package google.api.translate;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * 인터넷으로 연결되어 있어야 사용 가능
 * 개발중.
 */
public class GoogleTranslateSite {
    private static final String INPUT = "/Users/manjineum/Desktop/00.project/90.hanwha/10.솔루션/30.wne3/00.repository/wne-fw/app/src/test/resources/룰워크벤치.txt";

    public static void main(String[] args) throws Exception {

//        String data = translate("라인", "ko", "en");
//        System.out.println(data);

        translate();
    }

    private static void translate() throws Exception {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {

            File inputFile = new File(INPUT);
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

                String data = translate(line, "ko", "en");
                outputStream.write(createInsertQuery(part, "en_US", line, data).getBytes());

                data = translate(line, "ko", "vi");
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

    private static String translate(String data, String fromLocale, String toLocale) {
        //https://translate.google.com/m?sl=ko&tl=en&q=%EB%B2%88%EC%97%AD%EB%AC%B8%EC%9E%A5
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> parameter = new HashMap<>();
        parameter.put("sl", fromLocale);
        parameter.put("tl", toLocale);


        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://translate.google.com/m");
        MultiValueMap params = new LinkedMultiValueMap<>();
        params.setAll(parameter);
        builder.queryParams(params);

        //HIS에는 FW의 공통 응답을 사용하지 않음
        String response =  restTemplate.getForObject(builder.build().toUri(), String.class);

        System.out.println(response);

        return null;
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
