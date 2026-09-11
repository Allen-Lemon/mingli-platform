package com.mingli.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/** 排盘/分析请求 */
public class AnalyzeRequest {

    private String name;
    /** M 男 / F 女 */
    private String gender = "M";

    @NotNull(message = "出生年份不能为空")
    @Min(1900) @Max(2100)
    private Integer year;

    @NotNull(message = "出生月份不能为空")
    @Min(1) @Max(12)
    private Integer month;

    @NotNull(message = "出生日期不能为空")
    @Min(1) @Max(31)
    private Integer day;

    @Min(0) @Max(23)
    private Integer hour = 12;

    @Min(0) @Max(59)
    private Integer minute = 0;

    /** 出生地经度（东经为正），用于真太阳时校正 */
    private Double longitude = 120.0;
    /** 时区偏移，默认东八区 */
    private Double tzOffset = 8.0;
    private String cityName;
    /** 是否启用真太阳时 */
    private Boolean useTrueSolarTime = true;
    /** 是否保存到数据库 */
    private Boolean save = false;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public Integer getDay() { return day; }
    public void setDay(Integer day) { this.day = day; }
    public Integer getHour() { return hour; }
    public void setHour(Integer hour) { this.hour = hour; }
    public Integer getMinute() { return minute; }
    public void setMinute(Integer minute) { this.minute = minute; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getTzOffset() { return tzOffset; }
    public void setTzOffset(Double tzOffset) { this.tzOffset = tzOffset; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public Boolean getUseTrueSolarTime() { return useTrueSolarTime; }
    public void setUseTrueSolarTime(Boolean useTrueSolarTime) { this.useTrueSolarTime = useTrueSolarTime; }
    public Boolean getSave() { return save; }
    public void setSave(Boolean save) { this.save = save; }
}
