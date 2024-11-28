package com.example.day_starter.model.weather;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {

    @SerializedName("response")
    private Response response;

    public static class Response {
        @SerializedName("header")
        private Header header;
        @SerializedName("body")
        private Body body;

        // Getters and Setters
        public Header getHeader() {
            return header;
        }

        public void setHeader(Header header) {
            this.header = header;
        }

        public Body getBody() {
            return body;
        }

        public void setBody(Body body) {
            this.body = body;
        }

        public static class Header {
            @SerializedName("resultCode")
            private String resultCode;
            @SerializedName("resultMsg")
            private String resultMsg;

            // Getters and Setters
            public String getResultCode() {
                return resultCode;
            }

            public void setResultCode(String resultCode) {
                this.resultCode = resultCode;
            }

            public String getResultMsg() {
                return resultMsg;
            }

            public void setResultMsg(String resultMsg) {
                this.resultMsg = resultMsg;
            }
        }

        public static class Body {
            @SerializedName("dataType")
            private String dataType;
            @SerializedName("items")
            private Items items;

            public String getDataType() {
                return dataType;
            }

            public void setDataType(String dataType) {
                this.dataType = dataType;
            }

            public Items getItems() {
                return items;
            }

            public void setItems(Items items) {
                this.items = items;
            }

            public static class Items {
                @SerializedName("item")
                private List<Item> item;

                public List<Item> getItem() {
                    return item;
                }

                public void setItem(List<Item> item) {
                    this.item = item;
                }

                public static class Item {
                    @SerializedName("baseDate")
                    private String baseDate;
                    @SerializedName("baseTime")
                    private String baseTime;
                    @SerializedName("category")
                    private String category;
                    @SerializedName("fcstDate")
                    private String fcstDate;
                    @SerializedName("fcstTime")
                    private String fcstTime;
                    @SerializedName("fcstValue")
                    private String fcstValue;
                    @SerializedName("nx")
                    private int nx;
                    @SerializedName("ny")
                    private int ny;

                    // 카테고리 상수 추가
                    public static final String TEMP = "TMP";    // 기온
                    public static final String WIND_EW = "UUU"; // 동서바람성분
                    public static final String WIND_SN = "VVV"; // 남북바람성분
                    public static final String WIND_DIR = "VEC"; // 풍향
                    public static final String WIND_SPD = "WSD"; // 풍속

                    // Getters and Setters
                    public String getBaseDate() {
                        return baseDate;
                    }

                    public void setBaseDate(String baseDate) {
                        this.baseDate = baseDate;
                    }

                    public String getBaseTime() {
                        return baseTime;
                    }

                    public void setBaseTime(String baseTime) {
                        this.baseTime = baseTime;
                    }

                    public String getCategory() {
                        return category;
                    }

                    public void setCategory(String category) {
                        this.category = category;
                    }

                    public String getFcstDate() {
                        return fcstDate;
                    }

                    public void setFcstDate(String fcstDate) {
                        this.fcstDate = fcstDate;
                    }

                    public String getFcstTime() {
                        return fcstTime;
                    }

                    public void setFcstTime(String fcstTime) {
                        this.fcstTime = fcstTime;
                    }

                    public String getFcstValue() {
                        return fcstValue;
                    }

                    public void setFcstValue(String fcstValue) {
                        this.fcstValue = fcstValue;
                    }

                    public int getNx() {
                        return nx;
                    }

                    public void setNx(int nx) {
                        this.nx = nx;
                    }

                    public int getNy() {
                        return ny;
                    }

                    public void setNy(int ny) {
                        this.ny = ny;
                    }

                    // 카테고리 확인 도움 메서드들
                    public boolean isTemperature() {
                        return TEMP.equals(category);
                    }

                    public boolean isWindSpeed() {
                        return WIND_SPD.equals(category);
                    }

                    // fcstValue를 숫자로 변환하는 도움 메서드
                    public double getFcstValueAsDouble() {
                        try {
                            return Double.parseDouble(fcstValue);
                        } catch (NumberFormatException e) {
                            return 0.0;
                        }
                    }

                    public int getFcstValueAsInt() {
                        try {
                            return Integer.parseInt(fcstValue);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    }
                }
            }
        }
    }

    // Getter for response
    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "WeatherResponse{" +
                "response=" + response +  // response 객체의 내용을 포함
                '}';
    }
}

