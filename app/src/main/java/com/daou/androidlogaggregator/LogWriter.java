package com.daou.androidlogaggregator;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogWriter {

    private static String logDirPath = Environment.getExternalStorageDirectory().getPath() + "/logs";
    private static String configPath = Environment.getExternalStorageDirectory().getPath() +"/log/log_config.xfg";
    private static String fileName = "Log";
    private static int logLevel = ConstValues.TYPE_ERROR;
    private static boolean isStackPrintOut = true;

    /**
     * 로그 파일을 저장할 위치와 이름을 설정한다.
     * @param dirPath 로그 파일 저장 경로
     * @param name 로그 파일 이름, 파일 생성 시 뒤에 _작성일자가 붙는다.
     */
    public static void setFileInfo(String dirPath, String name) {
        if(dirPath != null && !dirPath.equals("")) {
            logDirPath = dirPath;
        }
        if(fileName != null && !fileName.equals("")) {
            fileName = name;
        }
    }

    private static String getFormattedFilePath() {
        String formattedPath = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar c = Calendar.getInstance();
        formattedPath = logDirPath + "/" + fileName + "_"+sdf.format(c.getTime())+ ".txt";
        return formattedPath;
    }

    public static void e(String tag, String msg) {
        writeLog(ConstValues.TYPE_ERROR, tag, msg);
        if(isStackPrintOut) {
            Log.e(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if(logLevel == ConstValues.TYPE_WARNING || logLevel == ConstValues.TYPE_INFO || logLevel == ConstValues.TYPE_DEBUG) {
            writeLog(ConstValues.TYPE_WARNING, tag, msg);
            if (isStackPrintOut) {
                Log.e(tag, msg);
            }
        }
    }

    public static void i(String tag, String msg) {
        if(logLevel == ConstValues.TYPE_INFO || logLevel == ConstValues.TYPE_DEBUG) {
            writeLog(ConstValues.TYPE_INFO, tag, msg);
            if (isStackPrintOut) {
                Log.e(tag, msg);
            }
        }
    }

    public static void d(String tag, String msg) {
        if(logLevel == ConstValues.TYPE_DEBUG) {
            writeLog(ConstValues.TYPE_DEBUG, tag, msg);
            if (isStackPrintOut) {
                Log.e(tag, msg);
            }
        }
    }

    private static void writeLog(int type, String tag, String msg) {
        try{
            File file = new File(getFormattedFilePath());
            FileOutputStream fos;
            if(file.exists()){
                fos = new FileOutputStream(file, true);
            }else{
                fos = new FileOutputStream(file);
                // 파일에 OS 버젼과 기종을 기록한다.
                String metaInfo = "[android ver : " + Build.VERSION.SDK_INT +  "model : " + Build.MODEL + "]";
                fos.write(metaInfo.getBytes());
            }
            String nMessage = "[" + new Timestamp(System.currentTimeMillis())+ "] " + "[" + type + "] " + "[" + tag + "] " + msg+"\n";

            fos.write(nMessage.getBytes());
            fos.close();

        }catch (Exception e) {
            Log.i("INFO" , e.getMessage() + " / tag " +  tag+ ", /message : " + msg);
        }
    }

    /**
     * 로그레벨을 변경한 설정으로 계속 기록해야 할 경우 사용
     * 기존 설정을 가져오려면 loadConfig()를 사용한다.
     * @param level Custom Log Level. ERROR = 0 / WARNING = 1 / TYPE_INFO = 2 / TYPE_DEBUG = 3
     * @param isStackPrint 로그파일 외 'Logcat'에도 실시간으로 출력 여부
     */
    public static void setCustomLogOption(int level, boolean isStackPrint) {
        if(logLevel >= 0 && logLevel <4){
            logLevel = level;
        } else {
            logLevel = ConstValues.TYPE_ERROR;
        }
        isStackPrintOut = isStackPrint;

        writeConfig(level, isStackPrint);
    }

    /**
     * 로그레벨을 설정 후 저장하여 사용 중일때 기존 설정을 가져온다.
     */
    private static void loadConfig() {
        File f = new File(configPath);
        if ( f.isFile() ){
            try{
                BufferedReader br = new BufferedReader( new FileReader(f));
                while(br.read() != -1){
                    String l = br.readLine();
                    if( l.contains("level=")){
                        String v = getLastDelimiterTailString(l, "=");
                        if( v != null && ( v.equals("0")
                                        || v.equals("!")
                                        || v.equals("2")
                                        || v.equals("3")) ){
                            logLevel = Integer.parseInt(v);
                        }
                    } else if( l.contains("stack_out=")){
                        String v = getLastDelimiterTailString(l, "=");
                        isStackPrintOut = v != null && v.equals("true");
                    }
                }
            }catch (Exception e) {
                Log.e("LogWrite", "loadConfig ex-"+e.getMessage());
            }
        } else {
            logLevel = ConstValues.TYPE_WARNING;
        }
        Log.i("INFO","load config LogLevel= "+logLevel+", StackOut="+isStackPrintOut);
    }

    private static void writeConfig(int logLevel, boolean isStactPrintOut) {
        try{
            File file = new File(configPath);
            FileOutputStream fos;
            //기존 설정파일이 있어도 덮어쓴다.
            fos = new FileOutputStream(file);
            String nMessage = "level=" + logLevel + " / " + "stack_out=" + isStactPrintOut + " " + "\n";
            fos.write(nMessage.getBytes());
            fos.close();
        }catch (Exception e) {
            Log.i("INFO" , e.getMessage() + " / logLevel : " +  logLevel+ ", /isStackPrintOut : " + isStactPrintOut);
        }
    }

    private static String getLastDelimiterTailString(String source, String delimiter) {
        String strReturn = null;
        if (source == null || source.equals(""))
            return null;

        int iFind = source.lastIndexOf(delimiter);
        strReturn = source.substring(iFind + 1, source.length());
        return strReturn;
    }
}
