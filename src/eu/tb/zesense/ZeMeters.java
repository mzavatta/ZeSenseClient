package eu.tb.zesense;
/* --------------------
* MeterChartDemo2.java
* --------------------
* (C) Copyright 2005, by Object Refinery Limited.
*
*/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

/**
* A simple demonstration application showing how to create a meter chart.
*/
public class ZeMeters extends ApplicationFrame {

    public DefaultValueDataset accelDataset;
    public DefaultValueDataset locationDataset, loctionBufferDataset;
    XYSeriesCollection accelBufferDataset;
    XYSeries accelBufferSeries;
    XYSeries accelUnderflowSeries;
    
    
    public DefaultValueDataset proxDataset;
    XYSeriesCollection proxBufferDataset;
    XYSeries proxBufferSeries;
    
    public DefaultValueDataset lightDataset;
    XYSeriesCollection lightBufferDataset;
    XYSeries lightBufferSeries;
    
    public DefaultValueDataset orientDataset;
    XYSeriesCollection orientBufferDataset;
    XYSeries orientBufferSeries;
    
    public DefaultValueDataset gyroDataset;
    XYSeriesCollection gyroBufferDataset;
    XYSeries gyroBufferSeries;
    
    /**
     * Creates a new demo.
     *
     * @param title  the frame title.
     */
    public ZeMeters(String title) {
        super(title);		
        JPanel chartPanel = createDemoPanel();
        //chartPanel.setMaximumSize(new Dimension(100,100));
        chartPanel.setPreferredSize(new Dimension(650, 650));
        setContentPane(chartPanel);
    }
    
    /**
     * Creates a panel for the demo (used by SuperDemo.java).
     *
     * @return A panel.
     */
    public JPanel createDemoPanel() {
    	accelBufferSeries = new XYSeries("Accel Buffer");
    	proxBufferSeries = new XYSeries("Prox Buffer");
    	lightBufferSeries = new XYSeries("Light Buffer");
    	orientBufferSeries = new XYSeries("Orient Buffer");
    	gyroBufferSeries = new XYSeries("Gyro Buffer");
    	accelUnderflowSeries = new XYSeries("Accel Underflow");
    	XYSeriesCollection accelBufferDataset = new XYSeriesCollection();
    	XYSeriesCollection proxBufferDataset = new XYSeriesCollection();
    	XYSeriesCollection lightBufferDataset = new XYSeriesCollection();
    	XYSeriesCollection orientBufferDataset = new XYSeriesCollection();
    	XYSeriesCollection gyroBufferDataset = new XYSeriesCollection();
    	accelBufferDataset.addSeries(accelBufferSeries);
    	accelBufferDataset.addSeries(accelUnderflowSeries);
    	proxBufferDataset.addSeries(proxBufferSeries);
    	lightBufferDataset.addSeries(lightBufferSeries);
    	orientBufferDataset.addSeries(orientBufferSeries);
      	gyroBufferDataset.addSeries(gyroBufferSeries);
        accelDataset = new DefaultValueDataset(0.0);
        locationDataset = new DefaultValueDataset(0.0);
        proxDataset = new DefaultValueDataset(0.0);
        lightDataset = new DefaultValueDataset(0.0);
        orientDataset = new DefaultValueDataset(0.0);
        gyroDataset = new DefaultValueDataset(0.0);
        JFreeChart accelChart = createAccelChart(accelDataset, "Accelerometer");
        JFreeChart locationChart = createChart(locationDataset, "Location");
        JFreeChart proxChart = createProxChart(proxDataset, "Proximity");
        JFreeChart lightChart = createLightChart(lightDataset, "Light");
        JFreeChart orientChart = createOrientChart(orientDataset, "Orientation");
        JFreeChart gyroChart = createGyroChart(gyroDataset, "Gyroscope");
        JFreeChart accelBufferChart = ChartFactory.createXYLineChart(
        		"Accel Buffer", // chart title
        		"X", // x axis label
        		"Y", // y axis label
        		accelBufferDataset, // data
        		PlotOrientation.VERTICAL,
        		false, // include legend
        		false, // tooltips
        		false // urls
        		);
        JFreeChart proxBufferChart = ChartFactory.createXYLineChart(
        		"Prox Buffer", // chart title
        		"X", // x axis label
        		"Y", // y axis label
        		proxBufferDataset, // data
        		PlotOrientation.VERTICAL,
        		false, // include legend
        		false, // tooltips
        		false // urls
        		);
        JFreeChart lightBufferChart = ChartFactory.createXYLineChart(
        		"Light Buffer", // chart title
        		"X", // x axis label
        		"Y", // y axis label
        		lightBufferDataset, // data
        		PlotOrientation.VERTICAL,
        		false, // include legend
        		false, // tooltips
        		false // urls
        		);
        JFreeChart orientBufferChart = ChartFactory.createXYLineChart(
        		"Orient Buffer", // chart title
        		"X", // x axis label
        		"Y", // y axis label
        		orientBufferDataset, // data
        		PlotOrientation.VERTICAL,
        		false, // include legend
        		false, // tooltips
        		false // urls
        		);
        JFreeChart gyroBufferChart = ChartFactory.createXYLineChart(
        		"Gyro Buffer", // chart title
        		"X", // x axis label
        		"Y", // y axis label
        		gyroBufferDataset, // data
        		PlotOrientation.VERTICAL,
        		false, // include legend
        		false, // tooltips
        		false // urls
        		);
        JPanel panel = new JPanel(new GridLayout(4, 4));
        /*JSlider slider = new JSlider(0, 100, 50);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(5);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider s = (JSlider) e.getSource();
                dataset.setValue(new Integer(s.getValue()));
            }
        });*/
        panel.add(new ChartPanel(accelChart));
        panel.add(new ChartPanel(proxChart));
        panel.add(new ChartPanel(lightChart));
        //panel.add(new ChartPanel(orientChart));
        panel.add(new ChartPanel(gyroChart));
        //panel.add(new ChartPanel(locationChart));
        panel.add(new ChartPanel(accelBufferChart));
        panel.add(new ChartPanel(proxBufferChart));
        panel.add(new ChartPanel(lightBufferChart));
        //panel.add(new ChartPanel(orientBufferChart));
        panel.add(new ChartPanel(gyroBufferChart));
        //panel.add(BorderLayout.SOUTH, slider);
        return panel;
    }
   
   
    MeterPlot plot;
    MeterPlot accelPlot;
    MeterPlot proxPlot;
    MeterPlot lightPlot;
    MeterPlot orientPlot;
    MeterPlot gyroPlot;
    
    /**
     * Creates a sample chart.
     *
     * @param dataset  a dataset.
     *
     * @return The chart.
     */
    private JFreeChart createChart(ValueDataset dataset, String name) {
        MeterPlot plot = new MeterPlot(dataset);
        plot.setRange(new Range(-20, 20));
        plot.addInterval(new MeterInterval("High", new Range(15, 20)));
        plot.addInterval(new MeterInterval("Low", new Range(-20, -15)));
        plot.setDialOutlinePaint(Color.white);
        JFreeChart chart = new JFreeChart(name,
                JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        return chart;
    }
    
    private JFreeChart createAccelChart(ValueDataset dataset, String name) {
        MeterPlot plot = new MeterPlot(dataset);
        accelPlot = plot;
        plot.setRange(new Range(-20, 20));
        plot.addInterval(new MeterInterval("High", new Range(15, 20)));
        plot.addInterval(new MeterInterval("Low", new Range(-20, -15)));
        plot.setDialOutlinePaint(Color.white);
        JFreeChart chart = new JFreeChart(name,
                JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        return chart;
    }
    
    private JFreeChart createProxChart(ValueDataset dataset, String name) {
        MeterPlot plot = new MeterPlot(dataset);
        proxPlot = plot;
        plot.setRange(new Range(0, 8));
        //plot.setDialShape(DialShape.CHORD);
        plot.addInterval(new MeterInterval("High", new Range(7, 8)));
        plot.addInterval(new MeterInterval("Low", new Range(0, 1)));
        plot.setDialOutlinePaint(Color.white);
        JFreeChart chart = new JFreeChart(name,
                JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        return chart;
    }
    
    private JFreeChart createLightChart(ValueDataset dataset, String name) {
        MeterPlot plot = new MeterPlot(dataset);
        lightPlot = plot;
        //plot.setDialShape(DialShape.CIRCLE);
        plot.setRange(new Range(0, 2500));
        plot.addInterval(new MeterInterval("High", new Range(2100, 2500)));
        plot.addInterval(new MeterInterval("Low", new Range(0, 400)));
        plot.setDialOutlinePaint(Color.white);
        JFreeChart chart = new JFreeChart(name,
                JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        return chart;
    }
    
    private JFreeChart createOrientChart(ValueDataset dataset, String name) {
        MeterPlot plot = new MeterPlot(dataset);
        orientPlot = plot;
        plot.setRange(new Range(-180, 180));
        plot.addInterval(new MeterInterval("High", new Range(160, 180)));
        plot.addInterval(new MeterInterval("Low", new Range(-180, -160)));
        plot.setDialOutlinePaint(Color.white);
        JFreeChart chart = new JFreeChart(name,
                JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        return chart;
    }
    
    private JFreeChart createGyroChart(ValueDataset dataset, String name) {
        MeterPlot plot = new MeterPlot(dataset);
        gyroPlot = plot;
        //plot.setDialShape(DialShape.PIE);
        plot.setNeedlePaint(Color.GRAY);
        plot.setRange(new Range(-10, 10));
        plot.addInterval(new MeterInterval("High", new Range(8, 10)));
        plot.addInterval(new MeterInterval("Low", new Range(-10, -8)));
        plot.setDialOutlinePaint(Color.white);
        JFreeChart chart = new JFreeChart(name,
                JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        return chart;
    }
   



}