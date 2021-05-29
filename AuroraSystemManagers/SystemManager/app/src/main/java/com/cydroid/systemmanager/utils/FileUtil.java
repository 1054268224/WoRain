package com.cydroid.systemmanager.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtil {
	public static void recurDelete(File file) {
		if (!file.exists()) {
			return;
		} else {
			if (file.isFile()) {
				file.delete();
				return;
			}
		}

		if (file.isDirectory()) {
			File[] childFile = file.listFiles();
			if (childFile == null || childFile.length == 0) {
				file.delete();
				return;
			}

			for (File f : childFile) {
				recurDelete(f);
			}
			file.delete();
		}
	}

	public static void CopyFileFromAssets(Context myContext,
			String ASSETS_NAME, String savePath, String saveName) {
		String filename = savePath + "/" + saveName;

		File dir = new File(savePath);
		if (!dir.exists())
			dir.mkdir();
		dir.setExecutable(true, false);
		dir.setReadable(true, false);
		dir.setWritable(true);

		try {
			if (!(new File(filename)).exists()) {
				InputStream is = myContext.getResources().getAssets()
						.open(ASSETS_NAME);
				FileOutputStream fos = new FileOutputStream(filename);
				byte[] buffer = new byte[7168];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			}
			File file = new File(filename);
			file.setExecutable(true, false);
			file.setReadable(true, false);
			file.setWritable(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isExistsFile(String filePath) {
		File dir = new File(filePath);
		return dir.exists();
	}

    // Chenyee xionghg 20180109 modify for CSW1702A-2298 begin
    // 添加图库缓存目录，包名为com.cydroid.gallery，却使用com.android.gallery3d目录，被sdk认定为卸载残留
    public final static String[] WHITE_LIST_FILE_KEY = new String[] {"ThemePark", "Android/data/com.android.gallery3d"};
    // Chenyee xionghg 20180109 modify for CSW1702A-2298 end
    public final static String[] WHITE_LIST_PATH_KEY = new String[] {"cyee"};

    public static boolean isWhiteListFile(String path) {
        boolean result = false;
        if (path == null || path.equals("")) {
            return false;
        }
        for (String key : WHITE_LIST_FILE_KEY) {
            if (path.contains(key)) {
                result = true;
                break;
            }
        }

        for (String key : WHITE_LIST_PATH_KEY) {
            String[] dirs = path.split("/");
            for (String dir : dirs) {
                if (dir.equals(key)) {
                    return true;
                }
            }
        }
        return result;
    }
}
