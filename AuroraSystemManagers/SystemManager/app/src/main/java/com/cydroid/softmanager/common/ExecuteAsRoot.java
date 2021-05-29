// Gionee <liuyb> <2014-3-24> add for CR01133928 begin
package com.cydroid.softmanager.common;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.cydroid.softmanager.utils.Log;

public class ExecuteAsRoot {

    private static final String TAG = "ExecuteAsRoot";

    public static final String execute(String currCommand) {
        String result = null;
        List<String> errorMsg = new ArrayList<String>();
        try {
            if (null != currCommand) {
                Process process = Runtime.getRuntime().exec("cyeesu");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes(currCommand + "\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                        StandardCharsets.UTF_8));
                int read;
                char[] buffer = new char[4096];
                StringBuffer output = new StringBuffer();
                while ((read = reader.read(buffer)) > 0) {
                    output.append(buffer, 0, read);
                }
                reader.close();

                BufferedReader errorResult = new BufferedReader(new InputStreamReader(
                        process.getErrorStream(), StandardCharsets.UTF_8));
                String s;
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.add(s);
                }
                errorResult.close();
                Log.e(TAG, "error info = " + errorMsg);

                try {
                    int suProcessRetval = process.waitFor();
                    if (255 != suProcessRetval) {
                        result = output.toString();
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error executing root action", ex);
                }
                process.destroy();
            }
        } catch (IOException ex) {
            Log.w(TAG, "Can't get root access", ex);
        } catch (SecurityException ex) {
            Log.w(TAG, "Can't get root access", ex);
        } catch (Exception ex) {
            Log.w(TAG, "Error executing internal operation", ex);
        }

        return result;
    }

    public static final String execute(ArrayList<String> commands) {
        String result = null;
        try {
            if (null != commands && commands.size() > 0) {
                Process process = Runtime.getRuntime().exec("cyeesu");

                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                for (String currCommand : commands) {
                    os.writeBytes(currCommand + "\n");
                    os.flush();
                }

                os.writeBytes("exit\n");
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                        StandardCharsets.UTF_8));
                int read;
                char[] buffer = new char[4096];
                StringBuffer output = new StringBuffer();
                while ((read = reader.read(buffer)) > 0) {
                    output.append(buffer, 0, read);
                }
                reader.close();

                try {
                    int suProcessRetval = process.waitFor();
                    if (255 != suProcessRetval) {
                        result = output.toString();
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error executing root action", ex);
                }
                process.destroy();
            }
        } catch (IOException ex) {
            Log.w(TAG, "Can't get root access", ex);
        } catch (SecurityException ex) {
            Log.w(TAG, "Can't get root access", ex);
        } catch (Exception ex) {
            Log.w(TAG, "Error executing internal operation", ex);
        }

        return result;
    }

    public static String do_exec(String[] args) {
        String result = "";
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.w(TAG, "Can't get root access", e);
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }
}
// Gionee <liuyb> <2014-3-24> add for CR01133928 end
