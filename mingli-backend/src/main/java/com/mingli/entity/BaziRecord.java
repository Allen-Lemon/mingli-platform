package com.mingli.entity;

import java.io.Serializable;
import java.util.Date;

/** 排盘记录 */
public class BaziRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String gender;
    private String birthTime;
    private String calendarType;
    private java.math.BigDecimal tzOffset;
    private java.math.BigDecimal longitude;
    private String cityName;
    private Integer trueSolar;
    private String yearGz;
    private String monthGz;
    private String dayGz;
    private String hourGz;
    private String dayMaster;
    private String chartJson;
    private String analysisJson;
    private Date createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getBirthTime() { return birthTime; }
    public void setBirthTime(String birthTime) { this.birthTime = birthTime; }
    public String getCalendarType() { return calendarType; }
    public void setCalendarType(String calendarType) { this.calendarType = calendarType; }
    public java.math.BigDecimal getTzOffset() { return tzOffset; }
    public void setTzOffset(java.math.BigDecimal tzOffset) { this.tzOffset = tzOffset; }
    public java.math.BigDecimal getLongitude() { return longitude; }
    public void setLongitude(java.math.BigDecimal longitude) { this.longitude = longitude; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public Integer getTrueSolar() { return trueSolar; }
    public void setTrueSolar(Integer trueSolar) { this.trueSolar = trueSolar; }
    public String getYearGz() { return yearGz; }
    public void setYearGz(String yearGz) { this.yearGz = yearGz; }
    public String getMonthGz() { return monthGz; }
    public void setMonthGz(String monthGz) { this.monthGz = monthGz; }
    public String getDayGz() { return dayGz; }
    public void setDayGz(String dayGz) { this.dayGz = dayGz; }
    public String getHourGz() { return hourGz; }
    public void setHourGz(String hourGz) { this.hourGz = hourGz; }
    public String getDayMaster() { return dayMaster; }
    public void setDayMaster(String dayMaster) { this.dayMaster = dayMaster; }
    public String getChartJson() { return chartJson; }
    public void setChartJson(String chartJson) { this.chartJson = chartJson; }
    public String getAnalysisJson() { return analysisJson; }
    public void setAnalysisJson(String analysisJson) { this.analysisJson = analysisJson; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
