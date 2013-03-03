package eu.tb.zesense;
/* --------------------
* MeterChartDemo2.java
* --------------------
* (C) Copyright 2005, by Object Refinery Limited.
*
*/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
* A simple demonstration application showing how to create a meter chart.
*/
public class ZeMeters extends ApplicationFrame {

    public DefaultValueDataset accelDataset;
    public DefaultValueDataset locationDataset, loctionBufferDataset;
    XYSeriesCollection accelBufferDataset;
    XYSeries accelBufferSeries;
   
    /**
     * Creates a new demo.
     *
     * @param title  the frame title.
     */
    public ZeMeters(String title) {
        super(title);		
        JPanel chartPanel = createDemoPanel();
        //chartPanel.setMaximumSize(new Dimension(100,100));
        chartPanel.setPreferredSize(new Dimension(1000, 500));
        setContentPane(chartPanel);
    }
    
    /**
     * Creates a panel for the demo (used by SuperDemo.java).
     *
     * @return A panel.
     */
    public JPanel createDemoPanel() {
    	accelBufferSeries = new XYSeries("Accel Buffer");
    	//accelBufferSeries.add(1.0, 1.0);
    	//accelBufferSeries.add(2.0, 4.0);
    	XYSeriesCollection accelBufferDataset = new XYSeriesCollection();
    	accelBufferDataset.addSeries(accelBufferSeries);
        accelDataset = new DefaultValueDataset(0.0);
        locationDataset = new DefaultValueDataset(0.0);
        JFreeChart accelChart = createChart(accelDataset, "Accelerometer");
        JFreeChart locationChart = createChart(locationDataset, "Location");
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
        JPanel panel = new JPanel(new GridLayout(3, 3));
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
        panel.add(new ChartPanel(locationChart));
        panel.add(new ChartPanel(accelBufferChart));
        //panel.add(BorderLayout.SOUTH, slider);
        return panel;
    }
   
   
    /**
     * Creates a sample chart.
     *
     * @param dataset  a dataset.
     *
     * @return The chart.
     */
    private static JFreeChart createChart(ValueDataset dataset, String name) {
        MeterPlot plot = new MeterPlot(dataset);
        plot.addInterval(new MeterInterval("High", new Range(80.0, 100.0)));
        plot.setDialOutlinePaint(Color.white);
        JFreeChart chart = new JFreeChart(name,
                JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        return chart;
    }
   



}