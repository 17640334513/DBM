package uf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.JsonUtil;

public class Chart {

	private String titleText;
	private List<String> legendDataList = new ArrayList<>();
	private List<String> xAxisDataList = new ArrayList<>();
	private Map<String, UFMap> seriesMap = new HashMap<>();

	public void addXAxisData(String xAxisData) {
		xAxisDataList.add(xAxisData);
	}
	
	public void addSeriesData(String seriesData, String name, String type) {
		UFMap serieMap = seriesMap.get(name);
		if(serieMap == null){
			legendDataList.add(name);
			serieMap = new UFMap().add("type", type).add("name", name).add("showAllSymbol", true);
			List<String> seriesDataList = new ArrayList<>();
			seriesDataList.add(seriesData);
			seriesMap.put(name, serieMap.add("data", seriesDataList));
		}else{
			List<String> seriesDataList = serieMap.getT("data");
			seriesDataList.add(seriesData);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder json = new StringBuilder("{");
		json.append("\"titleText\":").append(JsonUtil.toJson(titleText)).append(",")
			.append("\"legendDataList\":").append(JsonUtil.toJson(legendDataList)).append(",")
			.append("\"xAxisDataList\":").append(JsonUtil.toJson(xAxisDataList)).append(",")
			.append("\"seriesList\":").append(JsonUtil.toJson(seriesMap.values())).append("}");
		return json.toString();
	}
	
	public String getTitleText() {
		return titleText;
	}
	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}
	public List<String> getLegendDataList() {
		return legendDataList;
	}
	public void setLegendDataList(List<String> legendDataList) {
		this.legendDataList = legendDataList;
	}
	public List<String> getxAxisDataList() {
		return xAxisDataList;
	}
	public void setxAxisDataList(List<String> xAxisDataList) {
		this.xAxisDataList = xAxisDataList;
	}
	public Map<String, UFMap> getSeriesMap() {
		return seriesMap;
	}
	public void setSeriesMap(Map<String, UFMap> seriesMap) {
		this.seriesMap = seriesMap;
	}
	
}
