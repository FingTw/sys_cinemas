package com.example.cinema.admin.domain.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class Genre {
    private String id;
    private String name;
    private String code;

    @Builder
    public Genre(String id, String name, String code) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : LOWER_CASE_CODE(name);
        this.name = name;
        this.code = (code != null && !code.trim().isEmpty()) ? code : LOWER_CASE_CODE(name);
    }

    private static String LOWER_CASE_CODE(String name) {
        if (name == null) return java.util.UUID.randomUUID().toString();
        // Chuyển tiếng Việt có dấu thành không dấu hoặc chuyển thường, loại bỏ khoảng trắng
        String code = name.toLowerCase()
                .replaceAll("[áàảãạăắằẳẵặâấầẩẫậ]", "a")
                .replaceAll("[éèẻẽẹêếềểễệ]", "e")
                .replaceAll("[íìỉĩị]", "i")
                .replaceAll("[óòỏõọôốồổỗộơớờởỡợ]", "o")
                .replaceAll("[úùủũụưứừửữự]", "u")
                .replaceAll("[ýỳỷỹỵ]", "y")
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-");
        if (code.endsWith("-")) code = code.substring(0, code.length() - 1);
        if (code.startsWith("-")) code = code.substring(1);
        return code.isEmpty() ? java.util.UUID.randomUUID().toString() : code;
    }

    public void updateDetails(String name, String code) {
        this.name = name;
        this.code = (code != null && !code.trim().isEmpty()) ? code : LOWER_CASE_CODE(name);
    }
}
