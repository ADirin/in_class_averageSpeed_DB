package org.example.avgspd;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.example.avgspd.service.LocalizationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class AverageSpeedController {

    @FXML private VBox rootVBox;
    @FXML private Label lblTitle;
    @FXML private Label lblDistant;
    @FXML private Label lblTime;
    @FXML private TextField tfDistant;
    @FXML private TextField tfTime;
    @FXML private Button btnCalculate;
    @FXML private Label lblResult;
    @FXML private Label lblLocalTime;

    private Locale currentLocale = new Locale("en", "UK");
    private Map<String, String> localizedStrings;

    private javafx.beans.value.ChangeListener<String> distantListener;
    private javafx.beans.value.ChangeListener<String> timeListener;

    @FXML
    public void initialize() {
        setLanguage(currentLocale);

        // ✅ FIX: Only clear result when user types, NOT when code clears field
        tfDistant.textProperty().addListener((o, oldVal, newVal) -> {
            if (tfDistant.isFocused()) lblResult.setText("");
        });

        tfTime.textProperty().addListener((o, oldVal, newVal) -> {
            if (tfTime.isFocused()) lblResult.setText("");
        });
    }

    // ---------------- Language Buttons ----------------
    @FXML public void onENClick(ActionEvent e) { setLanguage(new Locale("en", "UK")); }
    @FXML public void onFRClick(ActionEvent e) { setLanguage(new Locale("fr", "FR")); }
    @FXML public void onVIClick(ActionEvent e) { setLanguage(new Locale("vi", "VN")); }
    @FXML public void onURClick(ActionEvent e) { setLanguage(new Locale("ur", "PK")); }
    @FXML public void onFAClick(ActionEvent e) { setLanguage(new Locale("fa", "IR")); }

    // ---------------- Apply Language ----------------
    private void setLanguage(Locale locale) {
        currentLocale = locale;

        localizedStrings = LocalizationService.getLocalizedStrings(locale);

        lblTitle.setText(localizedStrings.getOrDefault("title", "Average Calculator"));
        lblDistant.setText(localizedStrings.getOrDefault("distant", "Distance (km):"));
        lblTime.setText(localizedStrings.getOrDefault("time", "Time (h):"));
        btnCalculate.setText(localizedStrings.getOrDefault("calculate", "Calculate"));

        displayLocalTime(locale);
        applyTextDirection(locale);

        removeDigitListeners();
        applyDigitLocalization(locale);
    }

    private void removeDigitListeners() {
        if (distantListener != null) tfDistant.textProperty().removeListener(distantListener);
        if (timeListener != null) tfTime.textProperty().removeListener(timeListener);
    }

    // ✅ Correct digit conversion for FA/UR/AR
    private void applyDigitLocalization(Locale locale) {
        boolean isEastern = locale.getLanguage().equals("fa") ||
                locale.getLanguage().equals("ur") ||
                locale.getLanguage().equals("ar");

        if (!isEastern) return;

        distantListener = (obs, oldVal, newVal) -> {
            if (newVal == null) return;

            String normalized = convertToWesternDigits(newVal);
            String localized = localizeToEasternDigits(normalized);

            if (!localized.equals(newVal)) tfDistant.setText(localized);
        };

        timeListener = (obs, oldVal, newVal) -> {
            if (newVal == null) return;

            String normalized = convertToWesternDigits(newVal);
            String localized = localizeToEasternDigits(normalized);

            if (!localized.equals(newVal)) tfTime.setText(localized);
        };

        tfDistant.textProperty().addListener(distantListener);
        tfTime.textProperty().addListener(timeListener);
    }

    // ------------ Digit Conversion Helpers ------------
    private String convertToWesternDigits(String input) {
        StringBuilder out = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= '0' && c <= '9') out.append(c);
            else if (c >= '٠' && c <= '٩') out.append(c - '٠');
            else if (c >= '۰' && c <= '۹') out.append(c - '۰');
            else out.append(c);
        }
        return out.toString();
    }

    private String localizeToEasternDigits(String input) {
        return input.replace("0", "۰").replace("1", "۱").replace("2", "۲")
                .replace("3", "۳").replace("4", "۴").replace("5", "۵")
                .replace("6", "۶").replace("7", "۷").replace("8", "۸")
                .replace("9", "۹");
    }

    private String normalizeNumber(String input, Locale locale) {
        if (input == null) return "";
        input = removeDirectionalMarks(input);

        String result = convertToWesternDigits(input);
        result = result.replace("٫", ".").replace("،", ".");

        if (java.text.DecimalFormatSymbols.getInstance(locale).getDecimalSeparator() == ',')
            result = result.replace(",", ".");

        return result.replaceAll("[^0-9.]", "");
    }

    private String removeDirectionalMarks(String s) {
        return s.replace("\u200F","").replace("\u200E","")
                .replace("\u202A","").replace("\u202B","")
                .replace("\u202C","").replace("\u202D","")
                .replace("\u202E","");
    }

    // ---------------- Calculate ----------------
    @FXML
    public void onCalculateClick(ActionEvent e) {

        try {
            String rawDist = tfDistant.getText();
            String rawTime = tfTime.getText();

            String distNorm = normalizeNumber(rawDist, currentLocale);
            String timeNorm = normalizeNumber(rawTime, currentLocale);

            double distant = Double.parseDouble(distNorm);
            double time = Double.parseDouble(timeNorm);

            if (distant <= 0 || time <= 0) {
                lblResult.setText(localizedStrings.get("error_invalid_input"));
                return;
            }

            double avg = distant / time;
            String westernAvg = String.format("%.2f", avg);

            // ✅ Localize digits ONLY for RTL languages
            String localizedAvg = westernAvg;
            if (currentLocale.getLanguage().equals("fa") ||
                    currentLocale.getLanguage().equals("ur") ||
                    currentLocale.getLanguage().equals("ar")) {
                localizedAvg = localizeToEasternDigits(westernAvg);
            }

            String resultText = String.format(
                    localizedStrings.getOrDefault("avg_result", "Average speed: %s"),
                    localizedAvg
            );

            lblResult.setText(resultText);

            // ✅ Save to DB
            org.example.avgspd.database.AverageSpeedDAO.saveRecord(
                    distant,
                    time,
                    avg,
                    rawDist,
                    rawTime,
                    localizedAvg,
                    resultText,
                    currentLocale.getLanguage()
            );

            // ✅ Clear fields WITHOUT erasing lblResult
            tfDistant.clear();
            tfTime.clear();

        } catch (Exception ex) {
            lblResult.setText(localizedStrings.get("error_invalid_input"));
        }
    }

    // ---------------- RTL Layout ----------------
    private void applyTextDirection(Locale locale) {
        boolean isRTL = locale.getLanguage().equals("fa") ||
                locale.getLanguage().equals("ur") ||
                locale.getLanguage().equals("ar") ||
                locale.getLanguage().equals("he");

        Platform.runLater(() -> {
            rootVBox.setNodeOrientation(
                    isRTL ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT
            );

            String alignment = isRTL ?
                    "-fx-text-alignment: right; -fx-alignment: center-right;" :
                    "-fx-text-alignment: left; -fx-alignment: center-left;";

            tfDistant.setStyle(alignment);
            tfTime.setStyle(alignment);
        });
    }

    // ---------------- Time Display ----------------
    private void displayLocalTime(Locale locale) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                localizedStrings.getOrDefault("time_format", "HH:mm:ss")
        ).withLocale(locale);

        lblLocalTime.setText(
                String.format(localizedStrings.getOrDefault("current_time", "Time: %s"), now.format(fmt))
        );
    }
}