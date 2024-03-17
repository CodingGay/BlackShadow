package com.wind.meditor;

import android.content.Context;

import com.wind.meditor.core.FileProcessor;
import com.wind.meditor.property.AttributeItem;
import com.wind.meditor.property.ModificationProperty;
import com.wind.meditor.utils.FileTypeUtils;
import com.wind.meditor.utils.Log;
import com.wind.meditor.utils.NodeValue;

import java.io.File;


/**
 * @author Windysha
 */
public class ManifestEditorMain {
    public static Context context;

    public static String doMain(Context context, File srcFilePath) {
        ManifestEditorMain.context = context;
        String src = srcFilePath.getAbsolutePath();
        boolean isManifestFile = FileTypeUtils.isAndroidManifestFile(src);
        boolean isApkFile = false;
        if (!isManifestFile) {
            isApkFile = FileTypeUtils.isApkFile(src);
        }

        ModificationProperty property = new ModificationProperty();
        property.addManifestAttribute(new AttributeItem(NodeValue.Manifest.PACKAGE, context.getPackageName()).setNamespace(null));

        String output = new File(srcFilePath.getAbsolutePath() + "_modify.apk").getAbsolutePath();
        if (isManifestFile) {
            Log.i("Start to process manifest file ");
            FileProcessor.processManifestFile(src, output, property);
        } else if (isApkFile) {
            Log.i("Start to process apk.");
            FileProcessor.processApkFile(src, output, property);
        }
        if (context.getPackageManager().getPackageArchiveInfo(output, 0) == null) {
            return null;
        }
        return output;
    }
}
