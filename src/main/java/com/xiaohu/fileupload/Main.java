package com.xiaohu.fileupload;

import javax.swing.*;

/**
 * 文件上传GUI应用程序主类
 */
public class Main {
    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 在事件调度线程中启动GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                FileUploadFrame frame = new FileUploadFrame();
                frame.setVisible(true);
            }
        });
    }
} 