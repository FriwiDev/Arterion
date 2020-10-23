package me.friwi.arterion.website.stats.chart;

import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.DatabaseStatComponent;
import me.friwi.arterion.plugin.util.database.enums.StatContextType;
import me.friwi.arterion.plugin.util.database.enums.StatType;
import me.friwi.arterion.plugin.util.database.enums.TimeSlotUnit;
import me.friwi.arterion.website.WebApplication;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ChartGenerator {
    private static final String[] colors = new String[]{
            "rgb(255, 85, 85)",
            "rgb(85, 255, 255)",
            "rgb(85, 255, 85)",
            "rgb(255, 255, 85)",
            "rgb(85, 85, 255)",
            "rgb(255, 170, 0)",
            "rgb(170, 0, 170)",
            "rgb(0, 170, 170)",
            "rgb(255, 85, 255)",
            "rgb(0, 0, 170)",
            "rgb(0, 170, 0)",
            "rgb(170, 0, 0)"
    };
    private static final String[] backgroundColors = new String[]{
            "rgba(255, 85, 85, 0.65)",
            "rgba(85, 255, 255, 0.65)",
            "rgba(85, 255, 85, 0.65)",
            "rgba(255, 255, 85, 0.65)",
            "rgba(85, 85, 255, 0.65)",
            "rgba(255, 170, 0, 0.65)",
            "rgba(170, 0, 170, 0.65)",
            "rgba(0, 170, 170, 0.65)",
            "rgba(255, 85, 255, 0.65)",
            "rgba(0, 0, 170, 0.65)",
            "rgba(0, 170, 0, 0.65)",
            "rgba(170, 0, 0, 0.65)"
    };
    private static final int DEFAULT_WIDTH = 1000;
    private static final int DEFAULT_HEIGHT = 250;

    public static String generateChart(boolean timeDependent, String id, boolean isPlayerSum, StatContextType contextType, UUID targetContext, UUID[] targetParties, String[] partyNames, StatType type, MessageSource messageSource, Locale locale) {
        if (timeDependent) {
            return generateChart(id, isPlayerSum, contextType, targetContext, targetParties, partyNames, type, messageSource, locale);
        } else {
            return generateChartBar(id, isPlayerSum, contextType, targetContext, targetParties, partyNames, type, messageSource, locale);
        }
    }

    public static String generateChart(String id, boolean isPlayerSum, StatContextType contextType, UUID targetContext, UUID[] targetParties, String[] partyNames, StatType type, MessageSource messageSource, Locale locale) {
        //TimeSlotUnit timeslot = TimeSlotUnit.ONE_MINUTE;
        //if (type == StatType.HITS || type == StatType.CLICKS)
        //timeslot = TimeSlotUnit.TEN_SECONDS;
        TimeSlotUnit timeslot = TimeSlotUnit.TEN_SECONDS;
        return generateChart(id, isPlayerSum, contextType, targetContext, targetParties, partyNames, type,
                messageSource.getMessage("chart.timeslot." + timeslot.name().toLowerCase(), new Object[0], locale),
                messageSource.getMessage("chart.stat." + type.name().toLowerCase(), new Object[0], locale));
    }

    public static String generateChartBar(String id, boolean isPlayerSum, StatContextType contextType, UUID targetContext, UUID[] targetParties, String[] partyNames, StatType type, MessageSource messageSource, Locale locale) {
        return generateChartBar(id, isPlayerSum, contextType, targetContext, targetParties, partyNames, type,
                messageSource.getMessage("chart.stat." + type.name().toLowerCase(), new Object[0], locale));
    }

    public static String generateChart(String id, boolean isPlayerSum, StatContextType contextType, UUID targetContext, UUID[] targetParties, String[] partyNames, StatType type, String xAxis, String yAxis) {
        Database db = WebApplication.getDatabase();
        List<DatabaseStatComponent> stats = db.findAllByColumn(DatabaseStatComponent.class, new String[]{
                "contextType", "targetContext", "statType"
        }, new Object[]{
                contextType, targetContext, type
        });
        if (stats.size() == 0) {
            return null;
        }
        //Fetch borders
        long minX = Long.MAX_VALUE;
        long maxX = Long.MIN_VALUE;
        for (DatabaseStatComponent stat : stats) {
            if (stat.getTimeSlot() < minX) minX = stat.getTimeSlot();
            if (stat.getTimeSlot() > maxX) maxX = stat.getTimeSlot();
        }
        //Allocate array
        long[][] values = new long[targetParties.length][];
        for (int i = 0; i < values.length; i++) {
            long[] ins = new long[(int) (maxX - minX + 2)];
            if (type.getDefaultValue() != 0) {
                long a = type.getDefaultValue();
                for (int j = 0; j < ins.length; j++) ins[j] = a;
            }
            values[i] = ins;
        }
        //Fill in data
        for (DatabaseStatComponent stat : stats) {
            for (int i = 0; i < targetParties.length; i++) {
                if (isPlayerSum && targetParties[i].equals(stat.getTargetObjectParty())) {
                    int j = (int) (stat.getTimeSlot() - minX + 1);
                    values[i][j] += stat.getValue();
                    break;
                } else if (!isPlayerSum && targetParties[i].equals(stat.getTargetObject())) {
                    int j = (int) (stat.getTimeSlot() - minX + 1);
                    values[i][j] += stat.getValue();
                    break;
                }
            }
        }
        return generateChart(id, partyNames, values, xAxis, yAxis, minX);
    }

    public static String generateChartBar(String id, boolean isPlayerSum, StatContextType contextType, UUID targetContext, UUID[] targetParties, String[] partyNames, StatType type, String yAxis) {
        Database db = WebApplication.getDatabase();
        List<DatabaseStatComponent> stats = db.findAllByColumn(DatabaseStatComponent.class, new String[]{
                "contextType", "targetContext", "statType"
        }, new Object[]{
                contextType, targetContext, type
        });
        if (stats.size() == 0) {
            return null;
        }
        //Allocate array
        long[] values = new long[targetParties.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = type.getDefaultValue();
        }
        //Fill in data
        for (DatabaseStatComponent stat : stats) {
            for (int i = 0; i < targetParties.length; i++) {
                if (isPlayerSum && targetParties[i].equals(stat.getTargetObjectParty())) {
                    values[i] += stat.getValue();
                    break;
                } else if (!isPlayerSum && targetParties[i].equals(stat.getTargetObject())) {
                    values[i] += stat.getValue();
                    break;
                }
            }
        }
        return generateChartBar(id, partyNames, values, yAxis);
    }

    public static String generateChart(String id, String[] participants, long values[][], String xAxis, String yAxis, long xOffset) {
        return generateChart(id, DEFAULT_WIDTH, DEFAULT_HEIGHT, participants, values, xAxis, yAxis, xOffset);
    }

    public static String generateChart(String id, int width, int height, String[] participants, long values[][], String xAxis, String yAxis, long xOffset) {
        String build = "<canvas class=\"chartjs\" height=\"";
        build += height;
        build += "\" id=\"";
        build += id;
        build += "\" style=\"display: block; width: ";
        build += width;
        build += "px; height: ";
        build += height;
        build += "px;\" width=\"";
        build += width;
        build += "\"></canvas>\n" +
                "                    <script>new Chart(document.getElementById(\"";
        build += id;
        build += "\"), {\n" +
                "                        type: 'line',\n" +
                "                        data: {\n" +
                "                            datasets: [";
        for (int i = 0; i < participants.length; i++) {
            build += "{\n" +
                    "                                label: '";
            build += participants[i];
            build += "',\n" +
                    "                                data: [";
            long[] v = values[i];
            for (int j = 0; j < v.length; j++) {
                build += "{x: ";
                build += (j + xOffset);
                build += ", y: ";
                build += v[j];
                build += "}" + (j < v.length - 1 ? ", " : "");
            }
            build += "],\n" +
                    "                                fill: false,\n" +
                    "                                borderColor: '";
            build += colors[i % colors.length];
            build += "',\n" +
                    "                                backgroundColor: '";
            build += colors[i % colors.length];
            build += "',\n" +
                    "                                lineTension: 0.1\n" +
                    "                            }";
            if (i < participants.length - 1) {
                build += ", ";
            }
        }
        build += "]\n" +
                "                        },\n" +
                "                        options: {\n" +
                "                            scales: {\n" +
                "                                xAxes: [{\n" +
                "                                    id: '";
        build += xAxis;
        build += "',\n" +
                "                                    type: 'linear',\n" +
                "                                    position: 'bottom',\n" +
                "                                    scaleLabel: {\n" +
                "                                        display: true,\n" +
                "                                        labelString: '";
        build += xAxis;
        build += "'\n" +
                "                                    }" +
                "                                }],\n" +
                "                                yAxes: [{\n" +
                "                                    id: '";
        build += yAxis;
        build += "',\n" +
                "                                    type: 'linear',\n" +
                "                                    position: 'left',\n" +
                "                                    scaleLabel: {\n" +
                "                                        display: true,\n" +
                "                                        labelString: '";
        build += yAxis;
        build += "'\n" +
                "                                    },\n" +
                "                                    ticks: {\n" +
                "                                        beginAtZero: true\n" +
                "                                    }\n" +
                "                                }]\n" +
                "                            },\n" +
                "                            tooltips: {\n" +
                "                                mode: 'x-axis',\n" +
                "                                itemSort: (a, b, data) => b.yLabel - a.yLabel,\n" +
                "                                callbacks: {\n" +
                "                                    title: function (tooltipItems) {\n" +
                "                                        return '";
        build += xAxis;
        build += " ' + (tooltipItems[0].index + " + xOffset + ");\n" +
                "                                    }\n" +
                "                                }\n" +
                "                            }\n" +
                "                        }\n" +
                "                    });</script>";
        return build;
    }

    public static String generateChartBar(String id, String[] participants, long values[], String yAxis) {
        return generateChartBar(id, DEFAULT_WIDTH, DEFAULT_HEIGHT, participants, values, yAxis);
    }

    public static String generateChartBar(String id, int width, int height, String[] participants, long values[], String yAxis) {
        String build = "<canvas class=\"chartjs\" height=\"";
        build += height;
        build += "\" id=\"";
        build += id;
        build += "\" style=\"display: block; width: ";
        build += width;
        build += "px; height: ";
        build += height;
        build += "px;\" width=\"";
        build += width;
        build += "\"></canvas>";
        build += "<script>new Chart(document.getElementById(\"" + id + "\"), {\n" +
                "                        \"type\": \"bar\",\n" +
                "                        \"data\": {\n" +
                "                            \"labels\": [";
        for (int i = 0; i < participants.length; i++) {
            build += "\"" + participants[i] + "\"";
            if (i < participants.length - 1) build += ", ";
        }
        build += "],\n" +
                "                            \"datasets\": [{\n" +
                "                                \"label\": \"";
        build += yAxis;
        build += "\",\n" +
                "                                \"data\": [";
        for (int i = 0; i < values.length; i++) {
            build += "" + values[i] + "";
            if (i < values.length - 1) build += ", ";
        }
        build += "],\n" +
                "                                \"fill\": false,\n" +
                "                                \"backgroundColor\": [";
        for (int i = 0; i < values.length; i++) {
            build += "\"" + backgroundColors[i % backgroundColors.length] + "\"";
            if (i < values.length - 1) build += ", ";
        }
        build += "],\n" +
                "                                \"borderColor\": [";
        for (int i = 0; i < values.length; i++) {
            build += "\"" + colors[i % colors.length] + "\"";
            if (i < values.length - 1) build += ", ";
        }
        build += "],\n" +
                "                                \"borderWidth\": 1\n" +
                "                            }]\n" +
                "                        },\n" +
                "                        \"options\": {" +
                "                           \"legend\": {\n" +
                "                                \"display\": false\n" +
                "                            },\n" +
                "                               \"scales\": {\"yAxes\": [{\n" +
                "                                    id: '";
        build += yAxis;
        build += "',\n" +
                "                                    type: 'linear',\n" +
                "                                    position: 'left',\n" +
                "                                    scaleLabel: {\n" +
                "                                        display: true,\n" +
                "                                        labelString: '";
        build += yAxis;
        build += "'\n" +
                "                                    },\n" +
                "                                    ticks: {\n" +
                "                                        beginAtZero: true\n" +
                "                                    }\n" +
                "                                }]}}\n" +
                "                    });</script>";
        return build;
    }
}
