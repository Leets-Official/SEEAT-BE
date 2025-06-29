package com.seeat.server.domain.review.external;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class NaverOcrApi {

    @Value("${naver.service.url}")
    private String apiUrl; // 네이버 OCR API URL

    @Value("${naver.service.secretKey}")
    private String naverSecretKey; // 네이버 OCR 시크릿키

    /**
     * 네이버 OCR API를 호출하여 이미지에서 텍스트를 추출합니다.
     *
     * @param httpMethod HTTP 메서드 (예: "POST")
     * @param filePath   이미지 파일 경로
     * @param ext        파일 확장자 (예: "jpg", "png")
     * @return 추출된 텍스트 리스트
     */
    public List<String> callApi(String httpMethod, String filePath, String ext) {
        List<String> parseData = new ArrayList<>();
        HttpURLConnection con = null;

        try {
            // API URL로 연결
            URL url = new URL(apiUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setReadTimeout(30000);
            con.setRequestMethod(httpMethod);

            // 멀티파트 폼 데이터 경계값 생성
            String boundary = "----" + UUID.randomUUID().toString().replaceAll("-", "");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setRequestProperty("X-OCR-SECRET", naverSecretKey);

            // 요청 파라미터 JSON 생성
            JSONObject json = new JSONObject();
            json.put("version", "V2");
            json.put("requestId", UUID.randomUUID().toString());
            json.put("timestamp", System.currentTimeMillis());

            JSONObject image = new JSONObject();
            image.put("format", ext);
            image.put("name", "demo");
            JSONArray images = new JSONArray();
            images.add(image);
            json.put("images", images);

            String postParams = json.toString();

            // 멀티파트 데이터 전송
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                File file = new File(filePath);
                writeMultiPart(wr, postParams, file, boundary);
            }

            // 응답 코드 확인
            int responseCode = con.getResponseCode();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            responseCode == 200 ? con.getInputStream() : con.getErrorStream(),
                            StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();

                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }

                parseData = jsonParse(response.toString());
                log.info("응답 {}", response);
            }

        } catch (Exception e) {
            log.error("OCR API 호출 실패: {}", e.getMessage(), e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return parseData;
    }

    /**
     * 멀티파트 폼 데이터를 출력 스트림에 작성합니다.
     */
    private void writeMultiPart(OutputStream out, String jsonMessage, File file, String boundary) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"message\"\r\n\r\n");
        sb.append(jsonMessage).append("\r\n");

        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        out.flush();

        // 파일이 있으면 파일 데이터도 전송
        if (file != null && file.isFile()) {
            out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            String fileHeader = "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n" +
                    "Content-Type: application/octet-stream\r\n\r\n";
            out.write(fileHeader.getBytes(StandardCharsets.UTF_8));
            out.flush();

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                out.write("\r\n".getBytes(StandardCharsets.UTF_8));
            }

            out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        }
        out.flush();
    }

    /**
     * 커스텀 템플릿 OCR 응답에서 title, fields의 inferText만 한 번씩 추출 (중복 없음)
     */
    private List<String> jsonParse(String response) throws ParseException, org.json.simple.parser.ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jobj = (JSONObject) parser.parse(response);

        // 에러 메시지가 있으면 예외 발생
        if (jobj.containsKey("errorMessage")) {
            log.error("OCR API 에러: {}", jobj.get("errorMessage"));
            throw new IllegalStateException("OCR API 에러: " + jobj.get("errorMessage"));
        }

        List<String> result = new ArrayList<>();

        // images 배열에서 첫 번째 객체만 사용
        JSONArray imagesArray = (JSONArray) jobj.get("images");
        if (imagesArray == null || imagesArray.isEmpty()) {
            log.error("OCR API 응답에 images 필드가 없습니다. 응답: {}", jobj.toJSONString());
            throw new IllegalStateException("OCR 결과가 없습니다.");
        }
        JSONObject imageObj = (JSONObject) imagesArray.get(0);

        // title의 inferText 추출
        if (imageObj.containsKey("title")) {
            JSONObject titleObj = (JSONObject) imageObj.get("title");
            Object titleText = titleObj.get("inferText");
            if (titleText != null) {
                result.add(titleText.toString());
            }
        }

        // fields의 inferText 추출
        if (imageObj.containsKey("fields")) {
            JSONArray fieldsArray = (JSONArray) imageObj.get("fields");
            for (Object fieldObj : fieldsArray) {
                JSONObject field = (JSONObject) fieldObj;
                Object fieldText = field.get("inferText");
                if (fieldText != null) {
                    result.add(fieldText.toString());
                }
            }
        }

        return result;
    }
}
