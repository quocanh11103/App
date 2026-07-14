package com.example.nickhercore;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SalahWcupRepository {

    private final SalahWcupApiService apiService;

    private static Map<String, String> photoCache = null;

    public SalahWcupRepository() {
        apiService = SalahWcupClient.getApiService();
    }

    public interface PhotoMapCallback {
        void onSuccess(Map<String, String> photoMap);
        void onError(String message);
    }

    public void getPlayerPhotoMap(final PhotoMapCallback callback) {
        if (photoCache != null && !photoCache.isEmpty()) {
            callback.onSuccess(photoCache);
            return;
        }

        apiService.getData("physical").enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Không tải được ảnh cầu thủ. Mã lỗi: " + response.code());
                    return;
                }

                try {
                    Map<String, String> map = new HashMap<>();
                    collectPhotos(response.body(), map);
                    photoCache = map;
                    callback.onSuccess(map);
                } catch (Exception e) {
                    callback.onError("Lỗi xử lý ảnh cầu thủ: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                callback.onError("Lỗi mạng ảnh cầu thủ: " + t.getMessage());
            }
        });
    }

    public void getHighlightUrl(int matchNo, final FootballRepository.DetailCallback callback) {
        apiService.getMatch("match", matchNo).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Không tải được highlight. Mã lỗi: " + response.code());
                    return;
                }

                try {
                    String url = findHighlightUrl(response.body());

                    if (url == null || url.trim().isEmpty()) {
                        callback.onError("Trận này chưa có highlight.");
                    } else {
                        callback.onSuccess(url);
                    }
                } catch (Exception e) {
                    callback.onError("Lỗi xử lý highlight: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                callback.onError("Lỗi mạng highlight: " + t.getMessage());
            }
        });
    }

    private void collectPhotos(JsonElement element, Map<String, String> map) {
        if (element == null || element.isJsonNull()) {
            return;
        }

        if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();

            for (JsonElement e : arr) {
                collectPhotos(e, map);
            }

            return;
        }

        if (!element.isJsonObject()) {
            return;
        }

        JsonObject obj = element.getAsJsonObject();

        String name = getStringAny(obj, "name", "player", "fullName", "fullname");
        String team = getStringAny(obj, "team", "country", "nationality");
        String photo = getStringAny(obj, "photo", "image", "avatar", "headshot", "portrait", "picture");

        if (!name.isEmpty() && !photo.isEmpty() && photo.startsWith("http")) {
            map.put(normalizeKey(name), photo);

            if (!team.isEmpty()) {
                map.put(normalizeKey(team + "|" + name), photo);
            }
        }

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            JsonElement child = entry.getValue();

            if (child != null && (child.isJsonArray() || child.isJsonObject())) {
                collectPhotos(child, map);
            }
        }
    }

    public String getPhotoFromMap(Map<String, String> map, String team, String playerName) {
        if (map == null || playerName == null) {
            return "";
        }

        String keyWithTeam = normalizeKey(team + "|" + playerName);
        String keyNameOnly = normalizeKey(playerName);

        if (map.containsKey(keyWithTeam)) {
            return map.get(keyWithTeam);
        }

        if (map.containsKey(keyNameOnly)) {
            return map.get(keyNameOnly);
        }

        return "";
    }

    private String findHighlightUrl(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return "";
        }

        if (element.isJsonPrimitive()) {
            String value = element.getAsString();

            if (isVideoUrl(value)) {
                return value;
            }

            return "";
        }

        if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();

            for (JsonElement e : arr) {
                String result = findHighlightUrl(e);

                if (!result.isEmpty()) {
                    return result;
                }
            }

            return "";
        }

        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                String key = entry.getKey().toLowerCase(Locale.ROOT);
                JsonElement value = entry.getValue();

                if (key.contains("highlight")
                        || key.contains("youtube")
                        || key.contains("video")
                        || key.contains("embed")
                        || key.contains("watch")
                        || key.contains("url")) {
                    String direct = elementToString(value);

                    if (isVideoUrl(direct)) {
                        return direct;
                    }
                }

                String sub = findHighlightUrl(value);

                if (!sub.isEmpty()) {
                    return sub;
                }
            }
        }

        return "";
    }

    private boolean isVideoUrl(String value) {
        if (value == null) {
            return false;
        }

        String v = value.toLowerCase(Locale.ROOT);

        return value.startsWith("http")
                && (v.contains("youtube.com")
                || v.contains("youtu.be")
                || v.contains("highlight")
                || v.contains("video"));
    }

    private String getStringAny(JsonObject obj, String... keys) {
        for (String key : keys) {
            try {
                if (obj.has(key) && obj.get(key) != null && !obj.get(key).isJsonNull()) {
                    String value = elementToString(obj.get(key));

                    if (!value.isEmpty() && !"null".equalsIgnoreCase(value)) {
                        return value;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return "";
    }

    private String elementToString(JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return "";
        }

        if (e.isJsonPrimitive()) {
            return e.getAsString();
        }

        return e.toString();
    }

    private String normalizeKey(String text) {
        if (text == null) {
            return "";
        }

        String value = text.trim().toLowerCase(Locale.ROOT);
        value = Normalizer.normalize(value, Normalizer.Form.NFD);
        value = value.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        value = value.replace("đ", "d");
        value = value.replaceAll("[^a-z0-9| ]", "");
        value = value.replaceAll("\\s+", " ");

        return value.trim();
    }
}