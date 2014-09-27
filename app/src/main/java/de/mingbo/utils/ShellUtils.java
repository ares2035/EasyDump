package de.mingbo.utils;

import android.text.TextUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shaomingbo on 14-9-24.
 */
public class ShellUtils {

    private ShellUtils() {
    }

    public static final String EOL = System.getProperty("line.separator");
    public static final String EXIT = "exit" + EOL;
    private static final List<String> UID_COMMANDS = Lists.newArrayList("id", "/system/bin/id", "/system/xbin/id");
    private static final List<String> SU_COMMANDS = Lists.newArrayList("su", "/system/bin/su", "/system/xbin/su");
    public static String suShell = null;
    private static final Pattern ID_PATTERN = Pattern.compile("^uid=(\\d+).*", Pattern.DOTALL);

    /**
     * 执行本地脚本命令
     *
     * @param cmd 脚本命令
     * @return 返回脚本输出以及结果
     */
    public static ShellResult nativeExecute(String cmd) throws ShellException {
        int result = -1;
        if (TextUtils.isEmpty(cmd)) {
            return new ShellResult(result, Strings.nullToEmpty(null));
        }

        Process process = null;
        String message = null;
        InputStream is = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            result = process.waitFor();
            /**
             * 定向输出流
             */
            is = result == 0 ? process.getInputStream() : process.getErrorStream();
            message = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
        } catch (Exception e) {
            throw new ShellException(e);
        } finally {
            Closeables.closeQuietly(is);
            if (process != null)
                process.destroy();
        }

        return new ShellResult(result, Strings.nullToEmpty(message));
    }

    /**
     * 使用root 权限执行脚本命令
     *
     * @param cmd 脚本命令
     * @return 返回脚本输出以及结果
     */
    private static ShellResult suExecute(String cmd) throws ShellException {
        int result = -1;
        if (TextUtils.isEmpty(cmd)) {
            return new ShellResult(result, Strings.nullToEmpty(null));
        }

        Process process = null;
        String message = null;
        InputStream is = null;

        try {
            process = Runtime.getRuntime().exec(suShell);
            DataOutputStream shell = new DataOutputStream(process.getOutputStream());
            shell.writeBytes(cmd + EOL);
            shell.flush();
            shell.writeBytes(EXIT);
            shell.flush();
            result = process.waitFor();
            is = result == 0 ? process.getInputStream() : process.getErrorStream();
            message = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
        } catch (Exception e) {
            throw new ShellException(e);
        } finally {
            Closeables.closeQuietly(is);
            if (process != null)
                process.destroy();
        }

        return new ShellResult(result, Strings.nullToEmpty(message));
    }

    /**
     * 手动设置su 路径
     *
     * @param pathToSu
     */
    public void setSu(String pathToSu) {
        if (!TextUtils.isEmpty(pathToSu))
            suShell = pathToSu;
    }

    /**
     * 获取root 权限
     *
     * @return
     */
    public static boolean su() {
        if (TextUtils.isEmpty(suShell)) {
            try {
                SuIfPossible();
            } catch (ShellException e) {
                suShell = null;
            }
        }
        return !TextUtils.isEmpty(suShell);
    }

    /**
     * 查找合适的su 路径
     *
     * @throws ShellException
     */
    private static void SuIfPossible() throws ShellException {
        for (String cmd : SU_COMMANDS) {
            suShell = cmd;
            if (findSU())
                return;
        }
        suShell = null;
    }

    /**
     * 判断当前su 命令是否能获取root 权限
     *
     * @return
     * @throws ShellException
     */
    private static boolean findSU() throws ShellException {
        for (String cmd : UID_COMMANDS) {
            ShellResult result = suExecute(cmd);
            if (result.result != 0)
                continue;
            Matcher m = ID_PATTERN.matcher(result.message);
            if (m.matches()) {
                if ("0".equals(m.group(1)))
                    return true;
            }
        }
        return false;
    }

    /**
     * 模拟sudo 执行脚本命令
     *
     * @param cmd
     * @return
     * @throws ShellException
     */
    public static ShellResult sudo(String cmd) throws ShellException {
        if (su()) {
            return suExecute(cmd);
        } else
            return new ShellResult(-1, Strings.nullToEmpty(null));
    }


    public static final class ShellException extends Exception {
        public ShellException(Throwable t) {
            super(t);
        }
    }

    public static final class ShellResult {
        public int result;
        public String message;

        public ShellResult(int result, String msg) {
            this.result = result;
            this.message = msg;
        }
    }


}
