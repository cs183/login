package com.example.login.chart;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import com.example.login.SensorDevice;
import com.example.login.ThreadLocalVariablesKeeper;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class SensorValueChart extends AbstractDemoChart {

	private XYMultipleSeriesDataset dataSet;
	private SensorDevice sensorDevice;
	private GraphicalView view;
	private int minX;
	private int maxX;
	private float yScaleCoef;
	private float xScaleCoef;
	private static final Integer MAX_ALLOWED = 32767;

	public SensorValueChart() {
		sensorDevice = ThreadLocalVariablesKeeper.getSensorDevice();
	}

	public GraphicalView execute(Context context, int[] array, SharedPreferences prefs) {
		String[] titles = new String[] { "data" };
		List<double[]> x = new ArrayList<double[]>();
		List<double[]> values = new ArrayList<double[]>();
		int count = array.length;
		x.add(new double[count]);
		double[] dataValues = new double[count];
		yScaleCoef = getYScaleCoef(prefs);
		xScaleCoef = getXScaleCoef(prefs, count);
		double max = MAX_ALLOWED * yScaleCoef;
		double min = -MAX_ALLOWED * yScaleCoef;
		for (int i = 0; i < count; i++) {
			x.get(0)[i] = (i + minX) * xScaleCoef;
			dataValues[i] = array[i] * yScaleCoef;
		}
		values.add(dataValues);
		int[] colors = new int[] { Color.GREEN };
		PointStyle[] styles = new PointStyle[] { PointStyle.POINT };
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		setChartSettings(renderer, "Данные", "Порядковый номер", "Значение", minX, maxX, min,
				max, Color.DKGRAY, Color.DKGRAY);
		renderer.setXLabels(20);
		renderer.setYLabels(10);
		renderer.setShowLabels(true);
		renderer.setShowLegend(false);
		renderer.setApplyBackgroundColor(true);
		renderer.setBackgroundColor(Color.BLACK);
		renderer.setShowGrid(true);
		double[] panLimits = { minX, maxX, min, max };
		renderer.setPanLimits(panLimits);
		renderer.setZoomButtonsVisible(true);
		dataSet = buildDataset(titles, x, values);
		view = ChartFactory.getLineChartView(context, dataSet, renderer);
		return view;
	}

	public float getYScaleCoef(SharedPreferences prefs) {
		Integer real = prefs.getInt("maxY", MAX_ALLOWED);
		float yScaleCoef = (float) real / MAX_ALLOWED;
		return yScaleCoef;
	}

	public float getXScaleCoef(SharedPreferences prefs, int dataArrayLength) {
		int[] params = sensorDevice.getParameters();
		int minXCount = prefs.getInt("paramForXStartCount", 6); // from
		int maxXCount = prefs.getInt("paramForXEndCount", 7); // to
		minX = Math.min(params[minXCount], params[maxXCount]);
		maxX = Math.max(params[minXCount], params[maxXCount]);
		if (minX == maxX) {
			minX -= 5;
			maxX += 5;
		}
		return (float) (maxX - minX) / dataArrayLength;
	}

	public void updateDataSet() {
		int[] data = sensorDevice.getData();
		XYSeries series = dataSet.getSeriesAt(0);
		series.clear();
		for (int i = 0; i < data.length; i += 1) {
			series.add(minX + i * xScaleCoef, data[i] * yScaleCoef);
		}
		view.repaint();
	}
}