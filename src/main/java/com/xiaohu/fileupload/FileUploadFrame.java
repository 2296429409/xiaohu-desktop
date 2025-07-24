package com.xiaohu.fileupload;

import com.xiaohu.fileupload.api.HuaBanApi;
import com.xiaohu.fileupload.pojo.TsUploadResult;
import com.xiaohu.fileupload.pojo.UploadResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 文件上传界面主窗口
 */
public class FileUploadFrame extends JFrame {

    private JTextField cookieField;
    private JTextField boardIdField;
    private JTextField threadCountField;
    private JTextField retryCountField;
    private JTextField keyFileField;
    private JButton selectKeyFileButton;
    private File selectedKeyFile;
    private JTextArea fileListArea;
    private JButton addFileButton;
    private JButton removeFileButton;
    private JButton uploadButton;
    private JButton clearButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    private List<File> selectedFiles;

    public FileUploadFrame() {
        selectedFiles = new ArrayList<>();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        setTitle("文件上传工具");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // 初始化组件
        cookieField = new JTextField();
        boardIdField = new JTextField();
        threadCountField = new JTextField("3"); // 默认3个线程
        retryCountField = new JTextField("1"); // 默认重试1次
        keyFileField = new JTextField();
        keyFileField.setEditable(false);
        selectKeyFileButton = new JButton("选择");
        fileListArea = new JTextArea();
        fileListArea.setEditable(false);
        fileListArea.setLineWrap(true);
        fileListArea.setWrapStyleWord(true);

        addFileButton = new JButton("添加文件");
        removeFileButton = new JButton("移除文件");
        uploadButton = new JButton("开始上传");
        clearButton = new JButton("清空列表");

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        statusLabel = new JLabel("准备就绪");
        statusLabel.setForeground(Color.BLUE);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // 创建页签面板
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 文件上传页签
        JPanel uploadPanel = createUploadPanel();
        tabbedPane.addTab("文件上传", uploadPanel);

        // 数据查询页签
        VideoQueryPanel queryPanel = new VideoQueryPanel();
        tabbedPane.addTab("数据查询", queryPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createUploadPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 上传配置面板
        JPanel configPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        configPanel.setBorder(BorderFactory.createTitledBorder("上传配置"));

        // URL面板
        JPanel urlPanel = new JPanel(new BorderLayout(5, 0));
        urlPanel.add(new JLabel("上传URL:"), BorderLayout.WEST);
        JLabel urlLabel = new JLabel("https://api.huaban.com/upload");
        urlLabel.setForeground(Color.GRAY);
        urlPanel.add(urlLabel, BorderLayout.CENTER);

        // Cookie面板
        JPanel cookiePanel = new JPanel(new BorderLayout(5, 0));
        cookiePanel.add(new JLabel("Cookie:"), BorderLayout.WEST);
        cookiePanel.add(cookieField, BorderLayout.CENTER);

        // 文件库ID面板
        JPanel boardIdPanel = new JPanel(new BorderLayout(5, 0));
        boardIdPanel.add(new JLabel("文件库ID:"), BorderLayout.WEST);
        boardIdPanel.add(boardIdField, BorderLayout.CENTER);

        // 上传线程数面板
        JPanel threadCountPanel = new JPanel(new BorderLayout(5, 0));
        threadCountPanel.add(new JLabel("上传线程数:"), BorderLayout.WEST);
        threadCountPanel.add(threadCountField, BorderLayout.CENTER);

        // 重试次数面板
        JPanel retryCountPanel = new JPanel(new BorderLayout(5, 0));
        retryCountPanel.add(new JLabel("重试次数:"), BorderLayout.WEST);
        retryCountPanel.add(retryCountField, BorderLayout.CENTER);

        // 秘钥文件面板
        JPanel keyFilePanel = new JPanel(new BorderLayout(5, 0));
        keyFilePanel.add(new JLabel("秘钥文件:"), BorderLayout.WEST);
        keyFilePanel.add(keyFileField, BorderLayout.CENTER);
        keyFilePanel.add(selectKeyFileButton, BorderLayout.EAST);

        configPanel.add(urlPanel);
        configPanel.add(cookiePanel);
        configPanel.add(boardIdPanel);
        configPanel.add(threadCountPanel);
        configPanel.add(retryCountPanel);
        configPanel.add(keyFilePanel);

        // 文件列表面板
        JPanel fileListPanel = new JPanel(new BorderLayout(5, 5));
        fileListPanel.setBorder(BorderFactory.createTitledBorder("文件列表"));

        JScrollPane scrollPane = new JScrollPane(fileListArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        fileListPanel.add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addFileButton);
        buttonPanel.add(removeFileButton);
        buttonPanel.add(clearButton);

        fileListPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 操作面板
        JPanel operationPanel = new JPanel(new BorderLayout(5, 5));
        operationPanel.setBorder(BorderFactory.createTitledBorder("操作"));

        JPanel uploadButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        uploadButtonPanel.add(uploadButton);
        operationPanel.add(uploadButtonPanel, BorderLayout.NORTH);

        operationPanel.add(progressBar, BorderLayout.CENTER);
        operationPanel.add(statusLabel, BorderLayout.SOUTH);

        // 组装主面板
        mainPanel.add(configPanel, BorderLayout.NORTH);
        mainPanel.add(fileListPanel, BorderLayout.CENTER);
        mainPanel.add(operationPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private void setupEventHandlers() {
        addFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFiles();
            }
        });

        removeFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedFile();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFileList();
            }
        });

        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadFiles();
            }
        });

        selectKeyFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectKeyFile();
            }
        });
    }

    private void addFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                if (!selectedFiles.contains(file)) {
                    selectedFiles.add(file);
                }
            }
            updateFileListDisplay();
        }
    }

    private void removeSelectedFile() {
        String selectedText = fileListArea.getSelectedText();
        if (selectedText != null && !selectedText.trim().isEmpty()) {
            // 简单的移除逻辑，实际应用中可能需要更复杂的实现
            JOptionPane.showMessageDialog(this,
                    "请使用清空按钮重新选择文件",
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "请先选择要移除的文件",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearFileList() {
        selectedFiles.clear();
        updateFileListDisplay();
    }

    private void selectKeyFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("图片文件", "jpg", "jpeg", "png", "gif", "bmp"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedKeyFile = fileChooser.getSelectedFile();
            keyFileField.setText(selectedKeyFile.getAbsolutePath());
        }
    }


    private void updateFileListDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedFiles.size(); i++) {
            File file = selectedFiles.get(i);
            sb.append(i + 1).append(". ").append(file.getAbsolutePath())
                    .append(" (").append(formatFileSize(file.length())).append(")\n");
        }
        fileListArea.setText(sb.toString());
        statusLabel.setText("已选择 " + selectedFiles.size() + " 个文件");
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private void uploadFiles() {
        String uploadUrl = "https://api.huaban.com/upload";
        String cookie = cookieField.getText().trim();
        String boardId = boardIdField.getText().trim();
        int threadCount = Integer.parseInt(threadCountField.getText().trim());
        int retryCount = Integer.parseInt(retryCountField.getText().trim());

        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "请先选择要上传的文件",
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (boardId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "请输入文件库ID",
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 开始上传
        startUpload(uploadUrl, cookie, boardId, threadCount, retryCount, selectedKeyFile);
    }

    private void startUpload(String uploadUrl, String cookie, String boardId, int threadCount, int retryCount, File keyFile) {
        uploadButton.setEnabled(false);
        progressBar.setValue(0);
        statusLabel.setText("正在上传...");
        int imgKey = FileHandle.getImgWordKey(keyFile);

        // 使用后台线程进行上传
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            private final List<Long> uploadedFileIds = new ArrayList<>();
            private final Map<String, String> tsFileKeys = new HashMap<>(); // 存储ts文件名和对应的key

            @Override
            protected Void doInBackground() throws Exception {
                int totalFiles = selectedFiles.size();
                int processedFiles = 0;

                for (int i = 0; i < totalFiles; i++) {
                    File sourceFile = selectedFiles.get(i);

                    try {
                        // 检查是否为视频文件
                        if (FileHandle.isVideoFile(sourceFile)) {
                            // 视频文件转m3u8
                            String m3u8Path = FfmpegUtil.run(sourceFile.getAbsolutePath());
                            File m3u8File = new File(m3u8Path);

                            if (m3u8File.exists()) {
                                // 获取ts文件列表
                                List<File> tsFiles = FileHandle.getTsFiles(m3u8File.getParentFile());

                                // 使用多线程上传ts文件，并收集fileId
                                List<Long> tsFileIds = uploadTsFilesWithThreads(uploadUrl, cookie, tsFiles, tsFileKeys, threadCount, retryCount, keyFile);

                                // 将ts文件的fileId添加到上传列表中
                                uploadedFileIds.addAll(tsFileIds);

                                // 更新m3u8文件
                                FileHandle.updateM3u8File(m3u8File, tsFileKeys, imgKey);

                                // 清理临时文件
                                FileHandle.cleanupTempFiles(m3u8File.getParentFile());
                            }
                        } else {
                            // 普通文件自动跳过
                            System.out.println("跳过非视频文件: " + sourceFile.getName());
                            SwingUtilities.invokeLater(() -> statusLabel.setText("跳过非视频文件: " + sourceFile.getName()));
                        }

                        processedFiles++;
                        publish(processedFiles * 100 / totalFiles);

                        // 模拟上传延迟
                        Thread.sleep(100);
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(FileUploadFrame.this,
                                "处理文件失败: " + sourceFile.getName() + "\n" + e.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE));
                    }
                }

                // 如果有成功上传的文件，保存到文件库
                if (!uploadedFileIds.isEmpty()) {
                    try {
                        HuaBanApi.saveFilesToBoard(cookie, boardId, uploadedFileIds);
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(FileUploadFrame.this,
                                "保存到文件库失败: " + e.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE));
                    }
                }

                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                if (!chunks.isEmpty()) {
                    progressBar.setValue(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                uploadButton.setEnabled(true);
                statusLabel.setText("上传完成！");
                JOptionPane.showMessageDialog(FileUploadFrame.this,
                        "文件上传完成！",
                        "成功",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        };

        worker.execute();
    }

    /**
     * 使用多线程上传ts文件
     */
    private List<Long> uploadTsFilesWithThreads(String uploadUrl, String cookie, List<File> tsFiles,
                                                Map<String, String> tsFileKeys, int threadCount, int retryCount, File keyFile) {
        if (tsFiles.isEmpty()) {
            return new ArrayList<>();
        }

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<UploadResult>> futures = new ArrayList<>();
        AtomicInteger completedCount = new AtomicInteger(0);
        List<Long> fileIds = new ArrayList<>();

        try {
            // 提交所有上传任务
            for (File tsFile : tsFiles) {
                Future<UploadResult> future = executor.submit(() -> {
                    try {
                        // 如果选择了秘钥文件，先对ts文件进行加密
                        File fileToUpload = tsFile;
                        if (keyFile != null && keyFile.exists()) {
                            SwingUtilities.invokeLater(() -> statusLabel.setText("正在加密ts文件: " + tsFile.getName()));

                            // 对单个ts文件进行加密
                            fileToUpload = FileHandle.encryptTsFile(tsFile, keyFile);
                        }

                        TsUploadResult tsResult = HuaBanApi.uploadTsFileToServer(uploadUrl, cookie, fileToUpload, retryCount);
                        int completed = completedCount.incrementAndGet();

                        // 更新进度
                        SwingUtilities.invokeLater(() -> statusLabel.setText("正在上传ts文件: " + completed + "/" + tsFiles.size() +
                                " (" + tsFile.getName() + ")"));

                        return new UploadResult(tsFile.getName(), tsResult.getKey(), null, tsResult.getFileId());
                    } catch (Exception e) {
                        int completed = completedCount.incrementAndGet();
                        SwingUtilities.invokeLater(() -> statusLabel.setText("上传ts文件失败: " + completed + "/" + tsFiles.size() +
                                " (" + tsFile.getName() + ")"));
                        return new UploadResult(tsFile.getName(), null, e.getMessage(), null);
                    }
                });
                futures.add(future);
            }

            // 等待所有任务完成
            for (Future<UploadResult> future : futures) {
                try {
                    UploadResult result = future.get();
                    if (result.getKey() != null) {
                        tsFileKeys.put(result.getFileName(), result.getKey());
                        if (result.getFileId() != null) {
                            fileIds.add(result.getFileId());
                        }
                    } else {
                        System.err.println("上传ts文件失败: " + result.getFileName() + " - " + result.getError());
                    }
                } catch (Exception e) {
                    System.err.println("获取上传结果失败: " + e.getMessage());
                }
            }

        } finally {
            executor.shutdown();
        }

        return fileIds;
    }
} 
