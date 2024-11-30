package com.wrongweather.moipzy.domain.kakao.service;

import com.wrongweather.moipzy.domain.clothes.category.Color;
import com.wrongweather.moipzy.domain.clothes.category.SmallCategory;
import com.wrongweather.moipzy.domain.style.dto.StyleRecommendResponseDto;
import com.wrongweather.moipzy.domain.style.service.StyleService;
import com.wrongweather.moipzy.domain.weather.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KaKaoService {

    private final StyleService styleService;
    private final WeatherService weatherService;

    public Map<String, Object> getStyleRecommends(String utterance) {
        int userId = 2;

        //utterance로 today, tomorrow 입력받음
        String date = convertDate(utterance);

        //최저기온, 최고기온 순서
        List<Integer> temperatures = weatherService.getWeatherInfo(date);

        List<StyleRecommendResponseDto> recommends = styleService.recommend(userId, temperatures.get(1), temperatures.get(0));

        // JSON 응답 구조 생성
        Map<String, Object> response = new HashMap<>();
        response.put("version", "2.0");

        // 템플릿 설정
        Map<String, Object> template = new HashMap<>();
        List<Map<String, Object>> outputs = new ArrayList<>();


        //recommend 각 요소의 url은 완전한 url임. 그대로 사용하면 됨
        for (StyleRecommendResponseDto recommend : recommends) {
            // 카로셀 아이템들 생성
            Map<String, Object> carousel = new HashMap<>();
            carousel.put("type", "basicCard");

            // 카로셀 아이템 목록
            List<Map<String, Object>> items = new ArrayList<>();

            // 아우터 아이템
            if (recommend.getOuterId() != 0) {
                Map<String, Object> outerItem = new HashMap<>();
                outerItem.put("title", "아우터");
                Color outerColor = recommend.getOuterColor();
                SmallCategory outerSmallCategory = recommend.getOuterSmallCategory();
                String outerDescription = outerColor.name() + outerSmallCategory.name();
                outerItem.put("description", outerDescription);
                Map<String, Object> outerThumbnail = new HashMap<>();
                outerThumbnail.put("imageUrl", recommend.getOuterImgPath());
                outerThumbnail.put("fixedRatio", true);
                outerItem.put("thumbnail", outerThumbnail);
                items.add(outerItem);
            }

            // 상의 아이템
            Map<String, Object> topItem = new HashMap<>();
            topItem.put("title", "상의");
            Color topColor = recommend.getTopColor();
            SmallCategory topSmallCategory = recommend.getTopSmallCategory();
            String topDescription = topColor.name() + topSmallCategory.name();
            topItem.put("description", topDescription);
            Map<String, Object> topThumbnail = new HashMap<>();
            topThumbnail.put("imageUrl", recommend.getTopImgPath());
            topThumbnail.put("fixedRatio", true);
            topItem.put("thumbnail", topThumbnail);
            items.add(topItem);

            // 하의 아이템
            Map<String, Object> bottomItem = new HashMap<>();
            bottomItem.put("title", "하의");
            Color bottomColor = recommend.getBottomColor();
            SmallCategory bottomSmallCategory = recommend.getBottomSmallCategory();
            String bottomDescription = bottomColor.name() + bottomSmallCategory.name();
            bottomItem.put("description", bottomDescription);
            Map<String, Object> bottomThumbnail = new HashMap<>();
            bottomThumbnail.put("imageUrl", recommend.getBottomImgPath());
            bottomThumbnail.put("fixedRatio", true);
            bottomItem.put("thumbnail", bottomThumbnail);
            items.add(bottomItem);

            carousel.put("items", items);
            outputs.add(Map.of("carousel", carousel));
        }

        template.put("outputs", outputs);
        response.put("template", template);

        return response;
    }

    public String convertDate(String date) {
        LocalDate today = LocalDate.now();
        LocalDate resultDate;

        if ("today".equalsIgnoreCase(date)) {
            resultDate = today;
        } else if ("tomorrow".equalsIgnoreCase(date)) {
            resultDate = today.plusDays(1);
        } else {
            throw new IllegalArgumentException("오늘 또는 내일을 입력해주세요");
        }

        //날짜를 "yyyyddyy" 형식으로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return resultDate.format(formatter);
    }
}
