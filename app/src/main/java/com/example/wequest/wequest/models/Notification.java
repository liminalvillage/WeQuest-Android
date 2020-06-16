package com.example.wequest.wequest.models;

import java.util.List;

public class Notification {

    /**
     * app_id : f3efcaa3-03d9-48cb-b9ee-744a40c0034d
     * filters : [{"field":"tag","key":"email","relation":"=","value":"mahamadhusen93@gmail.com"}]
     * data : {"uid":"etxyMg3XL0Rsc9GHU4VU8wZorBS2","key":"01"}
     * contents : {"en":"New Supplier"}
     * headings : {"en":"hello there"}
     */

    @com.google.gson.annotations.SerializedName("app_id")
    private String appId;
    @com.google.gson.annotations.SerializedName("data")
    private DataBean data;
    @com.google.gson.annotations.SerializedName("contents")
    private ContentsBean contents;
    @com.google.gson.annotations.SerializedName("headings")
    private HeadingsBean headings;
    @com.google.gson.annotations.SerializedName("filters")
    private List<FiltersBean> filters;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public ContentsBean getContents() {
        return contents;
    }

    public void setContents(ContentsBean contents) {
        this.contents = contents;
    }

    public HeadingsBean getHeadings() {
        return headings;
    }

    public void setHeadings(HeadingsBean headings) {
        this.headings = headings;
    }

    public List<FiltersBean> getFilters() {
        return filters;
    }

    public void setFilters(List<FiltersBean> filters) {
        this.filters = filters;
    }
}
