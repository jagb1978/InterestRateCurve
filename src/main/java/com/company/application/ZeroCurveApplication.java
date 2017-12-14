package com.company.application;

import com.company.ModelFactory;
import com.company.Utils.RatesCurveUtils;
import com.company.beans.*;
import com.company.enums.ModelType;
import com.company.enums.RateBasis;
import com.company.enums.RateType;
import com.company.exceptions.InterpolationException;
import com.company.interfaces.Interpolation;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 *
 * @author Jose Gonzalez
 */
public class ZeroCurveApplication extends Application {
    private Stage window;
    private Label rateLabel = new Label("");
    private Label fwdRateLabel = new Label("");
    private NumberAxis fwdXAxis = new NumberAxis();
    private NumberAxis fwdYAxis = new NumberAxis();
    private LineChart<Number, Number> zeroCurveLineChart = new LineChart<>(new NumberAxis(), new NumberAxis());
    private LineChart<Number, Number> fwdCurveLineChart = new LineChart<>(this.fwdXAxis, this.fwdYAxis);
    private DatePicker maturityDatePicker = new DatePicker();
    private DatePicker fwdChartRateStartDatePicker = new DatePicker();
    private DatePicker fwdChartRateEndDatePicker = new DatePicker();
    private DatePicker fwdRateStartDatePicker = new DatePicker();
    private DatePicker fwdRateEndDatePicker = new DatePicker();
    private static LocalDate startDate;
    private LocalDate today = LocalDate.of(2016, 9, 28);
    private static Interpolation interpolator;

    public static void main(String args[]) throws InterpolationException, IOException {
        launch(args); //sets up program as java fx application
    }

    private static void zeroCurve(ModelType modelType, String curveString) throws InterpolationException, IOException {
        /**
         *  Step 1:   Extracts information from the files
         *
         */
        String filePath = ZeroCurveApplication.class.getResource("/" + curveString + "Rates.csv").getFile();
        List<DataPoint> dataPointList = RatesCurveUtils.getDataPointsFromFile(filePath);
        updateDate(dataPointList.get(0).getMaturity());
        /**
         * Step 2:   Initializes ZeroRates Curve. It only uses Cash Rates initially.
         *           The Zero Curve  Interpolator is also initialize.
         */
        RatesCurve zeroRateCurve = new RatesCurve(dataPointList, RateType.CASH);
        interpolator = ModelFactory.createModel(modelType, zeroRateCurve);
        /**
         * Step 3:
         *           Since there could be intermediate missing points in the Swap Curve,
         *           the missing points need to be inferred (in order to Bootstrap).
         *
         *           For that purpose an Interpolator is used. Please note
         *           that this is an interpolator for the swap curve.
         *           Therefore it is different from the  Interpolator used to
         *           interpolate the Zero Rate Curve.
         */
        RatesCurve swapsRatesCurve = new RatesCurve(dataPointList, RateType.SWAP);
        Interpolation swapInterpolator = ModelFactory.createModel(modelType,swapsRatesCurve);
        RatesCurve fullSwapCurve = RatesCurveUtils.getFullSwapsCurveAfterInterpolatingMissingMaturities(swapsRatesCurve, swapInterpolator);
        /** Step 4:   Bootstraps the zero rates from the Swaps Curve and adds the new points to the Zero Curve.
         *            It uses the Zero Curve Cubic Splines Interpolator to get the zero rates and discount the
         *            Swap Cash Flows.
         *            As new zero rate points are added to the zero curve, the Zero Curve Cubic Splines Interpolator
         *            is updated iteratively.
         */
        for (int index = 0; index < fullSwapCurve.size(); index++) {
            Swap swap = new Swap(fullSwapCurve.getMaturity(index), fullSwapCurve.getRate(index), fullSwapCurve.getCashFlowYearlyFrequency());
            RatePoint zeroRatePoint = new RatePoint(swap.getSwapTermInYears(), swap.bootStrapAndGetZeroRate(interpolator), RateBasis.CONTINOUS);
            zeroRateCurve.add(zeroRatePoint);
            interpolator = ModelFactory.createModel(modelType,zeroRateCurve);
        }
        printResults(interpolator);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("test");
        /// Grid Pane
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20, 5, 10, 10));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        //Choice Box
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll("GBP Zero Curve", "CHF Zero Curve", "ZAR Zero Curve");
        choiceBox.setValue("");
        //Model Choice Box
        ChoiceBox<String> modelChoiceBox = new ChoiceBox<>();
        modelChoiceBox.getItems().addAll( "Cubic Splines", "Monotone Convex");
        modelChoiceBox.setValue("Select Model...");
        //ChoiceBoxResultLabel
        Label labelChoiceBox = new Label("");
        choiceBox.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> updateChartAndDisplayedData(labelChoiceBox, choiceBox.getValue(), modelChoiceBox.getValue()));
        choiceBox.setValue("GBP Zero Curve");
        labelChoiceBox.setFont(Font.font("Verdana", 20));

        modelChoiceBox.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> updateChartAndDisplayedData(labelChoiceBox,choiceBox.getValue(),modelChoiceBox.getValue()));
        ///Date Picker
        maturityDatePicker.setValue(startDate);
        maturityDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateDateSelected());
        Label fwdStartLabel = new Label("Start Date: ");
        fwdChartRateStartDatePicker.setValue(startDate.plusDays(2));
        fwdChartRateStartDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> { getForwardCurveLineChart(); });

        Label fwdEndLabel = new Label("End Date: ");
        this.fwdChartRateEndDatePicker.setValue(startDate.plusYears(30));
        this.fwdChartRateEndDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> { getForwardCurveLineChart(); });

        Label fwdStartRateLabel = new Label("Enter Forward Start Date: ");
        this.fwdRateStartDatePicker.setValue(startDate.plusDays(2));
        this.fwdRateStartDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> { updateFwoDateSelected(); });

        Label fwdEndRateLabel = new Label("Enter Forward End Date: ");
        this.fwdRateEndDatePicker.setValue(startDate.plusDays(730));
        this.fwdRateEndDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> { updateFwoDateSelected(); });

        Label termYearsLabel = new Label("Enter Maturity Date: ");
        Label choiceBoxDescriptionLabel = new Label("Select Curve: ");
        Button closeButton = new Button("   Close   ");
        closeButton.setOnAction(event -> window.close());
        Label applicationTitleLabel = new Label("Zero Curves");
        applicationTitleLabel.setFont(Font.font("Verdana", 25));
        ////
        Label fwdChartLabel =  new Label("Instantaneous Foward Rates");
        fwdChartLabel.setFont(Font.font("Verdana", 20));
        ///
        GridPane.setConstraints(applicationTitleLabel, 0, 0);
        GridPane.setHalignment(closeButton, HPos.RIGHT);
        GridPane.setValignment( this.rateLabel, VPos.TOP);
        labelChoiceBox.setAlignment(Pos.CENTER);
        GridPane.setConstraints(modelChoiceBox,3,0);
        GridPane.setConstraints(choiceBoxDescriptionLabel, 1, 2);
        GridPane.setConstraints(choiceBox, 1, 3);
        GridPane.setConstraints(labelChoiceBox, 3, 3);
        GridPane.setHalignment(labelChoiceBox, HPos.CENTER);
        GridPane.setConstraints(termYearsLabel, 0, 2);
        GridPane.setConstraints( this.maturityDatePicker, 0, 3);
        GridPane.setConstraints(fwdChartLabel, 3, 12);
        GridPane.setConstraints(fwdStartLabel, 3, 7);
        GridPane.setHalignment(fwdStartLabel, HPos.RIGHT);
        GridPane.setConstraints( this.fwdChartRateStartDatePicker, 3, 8);
        GridPane.setValignment( this.fwdChartRateStartDatePicker, VPos.TOP);
        GridPane.setHalignment( this.fwdChartRateStartDatePicker, HPos.RIGHT);
        GridPane.setConstraints(fwdEndLabel, 3, 9);
        GridPane.setHalignment(fwdEndLabel, HPos.RIGHT);
        GridPane.setConstraints( this.fwdChartRateEndDatePicker, 3, 10);
        GridPane.setValignment( this.fwdChartRateEndDatePicker, VPos.TOP);
        GridPane.setHalignment( this.fwdChartRateEndDatePicker, HPos.RIGHT);
        GridPane.setConstraints(fwdStartRateLabel, 0, 7);
        GridPane.setConstraints( this.fwdRateStartDatePicker, 0, 8);
        GridPane.setValignment( this.fwdRateStartDatePicker, VPos.TOP);
        GridPane.setConstraints(fwdEndRateLabel, 1, 7);
        GridPane.setConstraints( this.fwdRateEndDatePicker, 1, 8);
        GridPane.setValignment( this.fwdRateEndDatePicker, VPos.TOP);
        GridPane.setConstraints( this.rateLabel, 0, 5);
        GridPane.setConstraints( this.zeroCurveLineChart, 3, 5);
        GridPane.setConstraints( this.fwdCurveLineChart, 3, 13);
        GridPane.setConstraints( this.fwdRateLabel, 0, 9);
        GridPane.setValignment( this.fwdRateLabel, VPos.TOP);
        GridPane.setConstraints(closeButton, 6, 13);
        //////
        gridPane.getChildren().addAll(fwdEndLabel, fwdStartLabel,  this.fwdChartRateEndDatePicker,  this.fwdChartRateStartDatePicker,  this.fwdCurveLineChart, applicationTitleLabel,
                this.zeroCurveLineChart,  this.maturityDatePicker,  this.rateLabel, choiceBox, labelChoiceBox, choiceBoxDescriptionLabel, termYearsLabel, closeButton,
                this.fwdRateLabel,fwdChartLabel,  this.fwdRateStartDatePicker, this.fwdRateEndDatePicker,fwdStartRateLabel,fwdEndRateLabel, modelChoiceBox);

        Scene scene = new Scene(gridPane, 1250, 850);
        this.window.setScene(scene);
        this. window.show();
    }

    private LineChart<Number, Number> getZeroCurveLineChart() throws InterpolationException {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Zero Rates Curve");
        ObservableList<XYChart.Series<Number, Number>> answer = FXCollections.observableArrayList();
        double printStartTerm = 0;
        double term = 0;
        for (int i = 0; term < interpolator.getLastTerm(); i++) {
            term = printStartTerm + i * 0.25;
            System.out.println("Time Bucket: " + term + " Zero Rate: " + String.format("%.04f", 100 * interpolator.getModeledRate(term)) + "%");
            series.getData().add(new XYChart.Data<>(term, 100 * interpolator.getModeledRate(term)));
        }
        answer.add(series);
        this.zeroCurveLineChart.getData().clear();
        this.zeroCurveLineChart.setData(answer);

        return  this.zeroCurveLineChart;
    }

    private LineChart<Number, Number> getForwardCurveLineChart() {
        try {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Fwd Rates Curve");
            ObservableList<XYChart.Series<Number, Number>> answer = FXCollections.observableArrayList();
            LocalDate endDate =  this.fwdChartRateEndDatePicker.getValue();
            LocalDate startDate =  this.fwdChartRateStartDatePicker.getValue();

            if (startDate != null && endDate != null) {
                Double startTerm = DAYS.between(today, startDate) / 365.0;
                Double endTerm = DAYS.between(today, endDate) / 365.0;
                while (startTerm < endTerm) {
                    startTerm += 0.01;
                    series.getData().add(new XYChart.Data<>(startTerm, 100 * interpolator.getForwardRate(startTerm)));
                }
                ((NumberAxis) this.fwdCurveLineChart.getXAxis()).setForceZeroInRange(false);
                ((NumberAxis) this.fwdCurveLineChart.getYAxis()).setForceZeroInRange(false);
                this.fwdCurveLineChart.getXAxis().setAutoRanging(true);

                answer.add(series);
                this.fwdCurveLineChart.getData().clear();
                this.fwdCurveLineChart.setData(answer);
                this.fwdCurveLineChart.getXAxis().setAutoRanging(true);
                this.fwdCurveLineChart.getYAxis().setAutoRanging(true);
            }
        } catch (InterpolationException e) {
            e.printStackTrace();
        }
        return  this.fwdCurveLineChart;

    }

    private void updateDateSelected() {
        LocalDate localDate = maturityDatePicker.getValue();
        if (localDate != null && localDate.isAfter(startDate.minusDays(1))) {

            LocalDate today = LocalDate.of(2016, 9, 28);
            Double term = DAYS.between(today, localDate) / 365.0;
            try {
                Double rate = interpolator.getModeledRate(term) * 100;
                Double discountFactor = interpolator.getDiscountFactor(term);
                String rateText = "Maturity :\t\t\t" + String.format("%.05f", term) + "  years  \n" + "Zero Rate:\t\t" + String.format("%.04f", rate) + "% \n"
                        + "Discount Factor:\t " + String.format("%.05f", discountFactor);
                rateLabel.setText(rateText);
            } catch (InterpolationException e) {
                e.printStackTrace();
            }
            System.out.println(maturityDatePicker);
        } else {
            rateLabel.setText("Selected Date is not valid");
        }
    }

    private void updateFwoDateSelected() {
        LocalDate end =  this.fwdRateEndDatePicker.getValue();
        LocalDate start =  this.fwdRateStartDatePicker.getValue();
        if (end!= null && start!= null && end.isAfter(startDate.minusDays(1)) && start.isAfter(startDate.minusDays(1))) {
            LocalDate today = LocalDate.of(2016, 9, 28);
            Double startTerm = DAYS.between(today, start) / 365.0;
            Double endTerm = DAYS.between(today, end) / 365.0;
            try {
                Double fwdRate = interpolator.getForwardRate(startTerm, endTerm) * 100;
                String rateText = "Forward Rate :\t\t\t" + String.format("%.05f", fwdRate) + " %";
                this.fwdRateLabel.setText(rateText);
            } catch (InterpolationException e) {
                e.printStackTrace();
            }

        } else {
            this.fwdRateLabel.setText("Selected Date is not valid");
        }
    }

    private void updateChartAndDisplayedData(Label labelChoiceBox, String choiceBoxValue, String modeledChoiceBoxValue) {
        labelChoiceBox.setText(choiceBoxValue);
        ModelType modelType = null;
        String curve ="";
        try {
            switch (choiceBoxValue) {
                case "GBP Zero Curve":
                    curve = "GBP";
                    break;
                case "CHF Zero Curve":
                    curve = "CHF";
                    break;

                case "ZAR Zero Curve":
                    curve = "ZAR";
                    break;
            }
            switch (modeledChoiceBoxValue) {
                case "Linear Interpolation":
                    modelType = ModelType.LINEAR;
                    break;
                case "Cubic Splines":
                    modelType = ModelType.CUBIC_SPLINES;
                    break;
                case "Monotone Convex":
                    modelType = ModelType.MONOTONE_CONVEX;
                    break;
            }
            zeroCurve(modelType,curve);
            getZeroCurveLineChart();
            getForwardCurveLineChart();
            updateDateSelected();
            updateFwoDateSelected();
        } catch (InterpolationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateDate(LocalDate localDate) {
        startDate = localDate;
    }

    private static void printResults(Interpolation interpolator) throws InterpolationException {
        double printStartTerm = 0;
        double term = 0;
        for (int i = 0; term < interpolator.getLastTerm(); i++) {
            term = printStartTerm + i * 0.25;
            System.out.println("Time Bucket: " + term + " Zero Rate: " + String.format("%.04f", 100 * interpolator.getModeledRate(term)) + "%");
        }
    }
}


