package com.cydroid.systemmanager.rubbishcleaner.util;

import com.cydroid.softmanager.R;

public class FileIconHelper {

	public static int getFileIconByFiletype(String fileType) {
		if ("audio".equals(fileType)) {
			return R.drawable.mime_music;
		}

		if ("video".equals(fileType)) {
			return R.drawable.mime_video;
		}

		if ("image".equals(fileType)) {
			return R.drawable.mime_image;
		}

		if ("zip".equals(fileType)) {
			return R.drawable.mime_zip;
		}

		if ("apk".equals(fileType)) {
			return R.drawable.mime_apk;
		}

		if ("doc".equals(fileType)) {
			return R.drawable.mime_doc;
		}

		if ("others".equals(fileType)) {
			return R.drawable.mime_unknown;
		}

		return R.drawable.mime_unknown;
	}

}
