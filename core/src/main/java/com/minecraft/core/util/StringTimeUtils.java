/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package com.minecraft.core.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTimeUtils {

    public enum Type {
        SIMPLIFIED, NORMAL
    }

    public static long getMonthsFromTimeString(String timeString) {
        long totalMonths = 0;

        // Define patterns for years, months, and days
        Pattern pattern = Pattern.compile("(\\d+)([yYmMdD])");
        Matcher matcher = pattern.matcher(timeString);

        // Parse the timeString
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            char unit = Character.toLowerCase(matcher.group(2).charAt(0));

            switch (unit) {
                case 'y':
                    totalMonths += value * 12;
                    break;
                case 'm':
                    totalMonths += value;
                    break;
                case 'd':
                    totalMonths += value / 30;
                    break;
            }
        }

        return totalMonths;
    }

    public static String toMillis(final double d) {
        final String string = String.valueOf(d);
        final StringBuilder sb = new StringBuilder();
        boolean stop = false;
        char[] charArray;
        for (int length = (charArray = string.toCharArray()).length, i = 0; i < length; ++i) {
            final char c = charArray[i];
            if (stop) {
                return sb.append(c).toString();
            }
            if (c == '.') {
                stop = true;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static String fromLong(Type type, final long lenth) {
        if (type == Type.NORMAL) {
            final int days = (int) TimeUnit.SECONDS.toDays(lenth);
            final long hours = TimeUnit.SECONDS.toHours(lenth) - days * 24;
            final long minutes = TimeUnit.SECONDS.toMinutes(lenth) - TimeUnit.SECONDS.toHours(lenth) * 60L;
            final long seconds = TimeUnit.SECONDS.toSeconds(lenth) - TimeUnit.SECONDS.toMinutes(lenth) * 60L;
            String totalDay = String.valueOf(days) + ((days == 1) ? " dia " : " dias ");
            String totalHours = String.valueOf(hours) + ((hours == 1L) ? " hora " : " horas ");
            String totalMinutes = String.valueOf(minutes) + ((minutes == 1L) ? " minuto " : " minutos ");
            String totalSeconds = String.valueOf(seconds) + ((seconds == 1L) ? " segundo" : " segundos");
            if (days == 0) {
                totalDay = "";
            }
            if (hours == 0L) {
                totalHours = "";
            }
            if (minutes == 0L) {
                totalMinutes = "";
            }
            if (seconds == 0L) {
                totalSeconds = "";
            }
            String restingTime = String.valueOf(totalDay) + totalHours + totalMinutes + totalSeconds;
            restingTime = restingTime.trim();
            if (restingTime.equals("")) {
                restingTime = "0 segundos";
            }
            return restingTime;
        } else {
            final int days = (int) TimeUnit.SECONDS.toDays(lenth);
            final long hours = TimeUnit.SECONDS.toHours(lenth) - days * 24;
            final long minutes = TimeUnit.SECONDS.toMinutes(lenth) - TimeUnit.SECONDS.toHours(lenth) * 60L;
            final long seconds = TimeUnit.SECONDS.toSeconds(lenth) - TimeUnit.SECONDS.toMinutes(lenth) * 60L;
            String totalDay = String.valueOf(days + "d ");
            String totalHours = String.valueOf(hours + "h ");
            String totalMinutes = String.valueOf(minutes + "m ");
            String totalSeconds = String.valueOf(seconds + "s");
            if (days == 0) {
                totalDay = "";
            }
            if (hours == 0L) {
                totalHours = "";
            }
            if (minutes == 0L) {
                totalMinutes = "";
            }
            if (seconds == 0L) {
                totalSeconds = "";
            }
            String restingTime = String.valueOf(totalDay) + totalHours + totalMinutes + totalSeconds;
            restingTime = restingTime.trim();
            if (restingTime.equals("")) {
                restingTime = "0s";
            }
            return restingTime;
        }

    }

    public static String getDifferenceFormat(final long time) {
        if (time <= 0L) {
            return "";
        }
        final long day = TimeUnit.SECONDS.toDays(time);
        final long hours = TimeUnit.SECONDS.toHours(time) - day * 24L;
        final long minutes = TimeUnit.SECONDS.toMinutes(time) - TimeUnit.SECONDS.toHours(time) * 60L;
        final long seconds = TimeUnit.SECONDS.toSeconds(time) - TimeUnit.SECONDS.toMinutes(time) * 60L;
        final StringBuilder sb = new StringBuilder();
        if (day > 0L) {
            sb.append(day).append(" ").append("dia" + ((day > 1L) ? "s" : "")).append(" ");
        }
        if (hours > 0L) {
            sb.append(hours).append(" ").append("hora" + ((hours > 1L) ? "s" : "")).append(" ");
        }
        if (minutes > 0L) {
            sb.append(minutes).append(" ").append("minuto" + ((minutes > 1L) ? "s" : "")).append(" ");
        }
        if (seconds > 0L) {
            sb.append(seconds).append(" ").append("segundo" + ((seconds > 1L) ? "s" : ""));
        }
        return sb.toString();
    }

    public static String formatDifference(Type type, final long time) {
        final long timeLefting = time - System.currentTimeMillis();
        final long seconds = timeLefting / 1000L;
        return fromLong(type, seconds);
    }

    public static String formatDifference(final Type type, final long highTime, long lowTime) {
        final long timeLefting = highTime - lowTime;
        final long seconds = timeLefting / 1000L;
        return fromLong(type, seconds);
    }

    public static long parseDateDiff(final String time, final boolean future) throws Exception {
        if (time.equalsIgnoreCase("n") || time.equalsIgnoreCase("never"))
            return -1L;
        final Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?(?:([0-9]+)\\s*(?:s[a-z]*)?)?", 2);
        final Matcher m = timePattern.matcher(time);
        int years = 0;
        int months = 0;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        boolean found = false;
        while (m.find()) {
            if (m.group() != null) {
                if (m.group().isEmpty()) {
                    continue;
                }
                for (int i = 0; i < m.groupCount(); ++i) {
                    if (m.group(i) != null && !m.group(i).isEmpty()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }
                if (m.group(1) != null && !m.group(1).isEmpty()) {
                    years = Integer.parseInt(m.group(1));
                }
                if (m.group(2) != null && !m.group(2).isEmpty()) {
                    months = Integer.parseInt(m.group(2));
                }
                if (m.group(3) != null && !m.group(3).isEmpty()) {
                    weeks = Integer.parseInt(m.group(3));
                }
                if (m.group(4) != null && !m.group(4).isEmpty()) {
                    days = Integer.parseInt(m.group(4));
                }
                if (m.group(5) != null && !m.group(5).isEmpty()) {
                    hours = Integer.parseInt(m.group(5));
                }
                if (m.group(6) != null && !m.group(6).isEmpty()) {
                    minutes = Integer.parseInt(m.group(6));
                }
                if (m.group(7) != null && !m.group(7).isEmpty()) {
                    seconds = Integer.parseInt(m.group(7));
                    break;
                }
                break;
            }
        }
        if (!found) {
            throw new Exception("Illegal Time");
        }
        if (years > 20) {
            throw new Exception("Illegal Time");
        }
        final Calendar c = new GregorianCalendar();
        if (years > 0) {
            c.add(1, years * (future ? 1 : -1));
        }
        if (months > 0) {
            c.add(2, months * (future ? 1 : -1));
        }
        if (weeks > 0) {
            c.add(3, weeks * (future ? 1 : -1));
        }
        if (days > 0) {
            c.add(5, days * (future ? 1 : -1));
        }
        if (hours > 0) {
            c.add(11, hours * (future ? 1 : -1));
        }
        if (minutes > 0) {
            c.add(12, minutes * (future ? 1 : -1));
        }
        if (seconds > 0) {
            c.add(13, seconds * (future ? 1 : -1));
        }
        return c.getTimeInMillis();
    }

    public static String formatTimePortuguese(int i) {
        if (i >= 60) {
            int minutes = i / 60;
            int seconds = i - minutes * 60;
            if (seconds == 0) {
                if (minutes > 1) {
                    return minutes + " minutos";
                } else {
                    return minutes + " minuto";
                }
            }
            String min = "minuto";
            String second = "segundo";
            if (minutes > 1)
                min = min + "s";
            if (seconds > 1)
                second = second + "s";
            return minutes + " " + min + " e " + seconds + " " + second;
        }
        if (i > 1)
            return i + " segundos";
        return i + " segundo";
    }

    public static String compareTwoDates(long date1, long date2) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(date1);
        Calendar now = new GregorianCalendar();
        now.setTimeInMillis(date2);
        return formatDateDiff(now, c);
    }


    private static String formatDateDiff(Calendar fromDate, Calendar toDate) {
        boolean future = false;
        if (toDate.equals(fromDate)) {
            return "now";
        } else {
            if (toDate.after(fromDate)) {
                future = true;
            }

            StringBuilder sb = new StringBuilder();
            int[] types = new int[]{1, 2, 5, 11, 12, 13};
            String[] names = new String[]{"ano", "anos", "mês", "meses", "dia", "dias", "hora", "horas", "minuto", "minutos", "segundo", "segundos"};
            int accuracy = 0;

            for (int i = 0; i < types.length && accuracy <= 2; ++i) {
                int diff = dateDiff(types[i], fromDate, toDate, future);
                if (diff > 0) {
                    ++accuracy;
                    sb.append(" ").append(diff).append(" ").append(names[i * 2 + (diff > 1 ? 1 : 0)]);
                }
            }

            return sb.length() == 0 ? "now" : sb.toString().trim();
        }
    }

    private static int dateDiff(int type, Calendar fromDate, Calendar toDate, boolean future) {
        int year = 1;
        int fromYear = fromDate.get(year);
        int toYear = toDate.get(year);
        if (Math.abs(fromYear - toYear) > 100000) {
            toDate.set(year, fromYear + (future ? 100000 : -100000));
        }

        int diff = 0;

        long savedDate;
        for (savedDate = fromDate.getTimeInMillis(); future && !fromDate.after(toDate) || !future && !fromDate.before(toDate); ++diff) {
            savedDate = fromDate.getTimeInMillis();
            fromDate.add(type, future ? 1 : -1);
        }

        --diff;
        fromDate.setTimeInMillis(savedDate);
        return diff;
    }

}
