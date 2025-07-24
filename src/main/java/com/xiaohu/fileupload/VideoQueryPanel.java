package com.xiaohu.fileupload;

import com.xiaohu.fileupload.api.GithubApi;
import com.xiaohu.fileupload.api.JavBusApi;
import com.xiaohu.fileupload.pojo.MovieVo;
import com.xiaohu.fileupload.pojo.VideoData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * 视频数据查询面板
 */
public class VideoQueryPanel extends JPanel {
    private JTextField nameField;
    private JTextField codeField;
    private JTextField performerField;
    private JTextField typesField;
    private JButton searchButton;
    private JButton resetButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton importButton;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JLabel pageLabel;
    private JButton prevButton;
    private JButton nextButton;
    private JComboBox<String> pageSizeCombo;
    
    private int currentPage = 1;
    private int pageSize = 10;
    private int totalCount = 0;
    private int totalPages = 0;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public VideoQueryPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadData();
    }

    private void initializeComponents() {
        // 搜索组件
        nameField = new JTextField(15);
        codeField = new JTextField(15);
        performerField = new JTextField(15);
        typesField = new JTextField(15);
        searchButton = new JButton("搜索");
        resetButton = new JButton("重置");
        addButton = new JButton("新增");
        editButton = new JButton("修改");
        deleteButton = new JButton("删除");
        importButton = new JButton("导入m3u8");

        // 表格
        String[] columnNames = {"ID", "名称", "日期", "编码", "URL", "预览", "图片", "演员", "时长", "类型", "更新时间", "备注", "文件"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 设置表格不可编辑
            }
        };
        dataTable = new JTable(tableModel);
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataTable.getTableHeader().setReorderingAllowed(false);
        
        // 添加鼠标点击事件监听器，用于图片阅览
        dataTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { // 双击
                    int row = dataTable.rowAtPoint(e.getPoint());
                    int col = dataTable.columnAtPoint(e.getPoint());
                    
                    // 检查是否点击的是图片列（第7列，索引为6）
                    if (col == 6 && row >= 0) {
                        String imgPath = (String) tableModel.getValueAt(row, col);
                        System.out.println("点击图片列，URL: " + imgPath);
                        if (imgPath != null && !imgPath.trim().isEmpty()) {
                            showImageDialog(imgPath);
                        } else {
                            JOptionPane.showMessageDialog(VideoQueryPanel.this, "图片URL为空", "提示", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    // 检查是否点击的是预览列（第6列，索引为5）
                    else if (col == 5 && row >= 0) {
                        String previewUrls = (String) tableModel.getValueAt(row, col);
                        System.out.println("点击预览列，URLs: " + previewUrls);
                        if (previewUrls != null && !previewUrls.trim().isEmpty()) {
                            showPreviewDialog(previewUrls);
                        } else {
                            JOptionPane.showMessageDialog(VideoQueryPanel.this, "预览URL为空", "提示", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

        // 分页组件
        totalLabel = new JLabel("总记录数: 0");
        pageLabel = new JLabel("第 1 页，共 1 页");
        prevButton = new JButton("上一页");
        nextButton = new JButton("下一页");
        pageSizeCombo = new JComboBox<>(new String[]{"5", "10", "20", "50"});
        pageSizeCombo.setSelectedItem("10");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 搜索面板
        JPanel searchPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("搜索"));
        
        // 第一行：搜索字段
        JPanel searchFieldsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchFieldsPanel.add(new JLabel("名称:"));
        searchFieldsPanel.add(nameField);
        searchFieldsPanel.add(new JLabel("编号:"));
        searchFieldsPanel.add(codeField);
        searchFieldsPanel.add(new JLabel("演员:"));
        searchFieldsPanel.add(performerField);
        searchFieldsPanel.add(new JLabel("类型:"));
        searchFieldsPanel.add(typesField);
        
        // 第二行：按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(searchButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(importButton);
        
        searchPanel.add(searchFieldsPanel);
        searchPanel.add(buttonPanel);

        // 表格面板
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("数据列表"));
        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // 分页面板
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paginationPanel.setBorder(BorderFactory.createTitledBorder("分页"));
        paginationPanel.add(totalLabel);
        paginationPanel.add(new JLabel("每页显示:"));
        paginationPanel.add(pageSizeCombo);
        paginationPanel.add(prevButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(nextButton);

        // 组装主面板
        add(searchPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(paginationPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        // 搜索按钮事件
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                loadData();
            }
        });

        // 重置按钮事件
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nameField.setText("");
                codeField.setText("");
                performerField.setText("");
                typesField.setText("");
                currentPage = 1;
                loadData();
            }
        });

        // 新增按钮事件
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showVideoDialog(null);
            }
        });

        // 修改按钮事件
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = dataTable.getSelectedRow();
                if (selectedRow >= 0) {
                    VideoData video = getVideoDataFromRow(selectedRow);
                    showVideoDialog(video);
                } else {
                    JOptionPane.showMessageDialog(VideoQueryPanel.this, "请先选择要修改的记录", "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // 删除按钮事件
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = dataTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int result = JOptionPane.showConfirmDialog(VideoQueryPanel.this, 
                        "确定要删除选中的记录吗？", "确认删除", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        VideoData video = getVideoDataFromRow(selectedRow);
                        VideoDataService.deleteVideo(video.getId());
                        loadData();
                    }
                } else {
                    JOptionPane.showMessageDialog(VideoQueryPanel.this, "请先选择要删除的记录", "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // 导入按钮事件
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importM3u8Files();
            }
        });

        // 上一页按钮事件
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentPage > 1) {
                    currentPage--;
                    loadData();
                }
            }
        });

        // 下一页按钮事件
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentPage < totalPages) {
                    currentPage++;
                    loadData();
                }
            }
        });

        // 每页大小改变事件
        pageSizeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pageSize = Integer.parseInt((String) pageSizeCombo.getSelectedItem());
                currentPage = 1;
                loadData();
            }
        });

        // 回车键搜索
        nameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                loadData();
            }
        });
        
        codeField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                loadData();
            }
        });
        
        performerField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                loadData();
            }
        });
        
        typesField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage = 1;
                loadData();
            }
        });
    }

    /**
     * 加载数据
     */
    private void loadData() {
        // 清空表格
        tableModel.setRowCount(0);

        // 获取搜索条件
        String name = nameField.getText().trim();
        String code = codeField.getText().trim();
        String performer = performerField.getText().trim();
        String types = typesField.getText().trim();

        // 获取数据
        List<VideoData> videos = VideoDataService.searchVideos(name, code, performer, types, currentPage, pageSize);
        totalCount = VideoDataService.getTotalCount(name, code, performer, types);
        totalPages = (int) Math.ceil((double) totalCount / pageSize);

        // 填充表格
        for (VideoData video : videos) {
            Vector<Object> row = new Vector<>();
            row.add(video.getId());
            row.add(video.getName());
            row.add(video.getDate() != null ? DATE_FORMAT.format(video.getDate()) : "");
            row.add(video.getCode());
            row.add(video.getUrl());
            row.add(video.getPreview());
            row.add(video.getImg());
            row.add(video.getPerformer());
            row.add(video.getDuration());
            row.add(video.getTypes());
            row.add(video.getUpdateTime() != null ? DATETIME_FORMAT.format(video.getUpdateTime()) : "");
            row.add(video.getRemark());
            row.add(video.getFile());
            tableModel.addRow(row);
        }

        // 更新分页信息
        updatePaginationInfo();
    }

    /**
     * 更新分页信息
     */
    private void updatePaginationInfo() {
        totalLabel.setText("总记录数: " + totalCount);
        pageLabel.setText("第 " + currentPage + " 页，共 " + totalPages + " 页");
        
        // 更新按钮状态
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
    }

    /**
     * 从表格行获取视频数据
     */
    private VideoData getVideoDataFromRow(int row) {
        VideoData video = new VideoData();
        video.setId((Integer) tableModel.getValueAt(row, 0));
        video.setName((String) tableModel.getValueAt(row, 1));
        
        // 获取日期
        String dateStr = (String) tableModel.getValueAt(row, 2);
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                video.setDate(DATE_FORMAT.parse(dateStr));
            } catch (Exception e) {
                // 日期解析失败，设为null
            }
        }
        
        video.setCode((String) tableModel.getValueAt(row, 3));
        video.setUrl((String) tableModel.getValueAt(row, 4));
        video.setPreview((String) tableModel.getValueAt(row, 5));
        video.setImg((String) tableModel.getValueAt(row, 6));
        video.setPerformer((String) tableModel.getValueAt(row, 7));
        video.setDuration((String) tableModel.getValueAt(row, 8));
        video.setTypes((String) tableModel.getValueAt(row, 9));
        
        // 获取更新时间
        String updateTimeStr = (String) tableModel.getValueAt(row, 10);
        if (updateTimeStr != null && !updateTimeStr.trim().isEmpty()) {
            try {
                video.setUpdateTime(DATETIME_FORMAT.parse(updateTimeStr));
            } catch (Exception e) {
                // 时间解析失败，设为null
            }
        }
        
        video.setRemark((String) tableModel.getValueAt(row, 11));
        video.setFile((String) tableModel.getValueAt(row, 12));
        
        return video;
    }

    /**
     * 显示视频编辑对话框
     */
    private void showVideoDialog(VideoData video) {
        VideoEditDialog dialog = new VideoEditDialog((Frame) SwingUtilities.getWindowAncestor(this), video);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            loadData();
        }
    }

    /**
     * 显示图片阅览对话框
     */
    private void showImageDialog(String imgUrl) {
        // 非模态加载提示
        JDialog loadingDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "加载中", false);
        loadingDialog.setLayout(new BorderLayout());
        JLabel loadingLabel = new JLabel("正在加载图片...", JLabel.CENTER);
        loadingLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        loadingDialog.add(loadingLabel, BorderLayout.CENTER);
        loadingDialog.setSize(200, 100);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        loadingDialog.setVisible(true);

        // 异步加载图片
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                System.out.println("后台线程开始下载图片: " + imgUrl);
                
                // 设置代理
                java.net.Proxy proxy = new java.net.Proxy(
                    java.net.Proxy.Type.HTTP, 
                    new java.net.InetSocketAddress("127.0.0.1", 7890)
                );
                
                // 创建连接
                java.net.URL url = new java.net.URL(imgUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection(proxy);
                
                // 设置请求头
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
                connection.setRequestProperty("Referer", imgUrl);
                connection.setRequestProperty("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
                connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(30000);
                
                // 读取图片数据
                java.io.InputStream inputStream = connection.getInputStream();
                java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                
                // 创建ImageIcon
                byte[] imageData = outputStream.toByteArray();
                ImageIcon icon = new ImageIcon(imageData);
                System.out.println("图片下载完成，尺寸: " + icon.getIconWidth() + "x" + icon.getIconHeight());
                return icon;
            }
            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    ImageIcon imageIcon = get();
                    if (imageIcon != null && imageIcon.getImage() != null) {
                        // 创建图片预览对话框
                        JDialog imageDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(VideoQueryPanel.this), "图片阅览", true);
                        imageDialog.setLayout(new BorderLayout());
                        Image image = imageIcon.getImage();
                        int imgWidth = image.getWidth(null);
                        int imgHeight = image.getHeight(null);
                        int maxWidth = 800, maxHeight = 600;
                        if (imgWidth > maxWidth || imgHeight > maxHeight) {
                            double scale = Math.min((double) maxWidth / imgWidth, (double) maxHeight / imgHeight);
                            imgWidth = (int) (imgWidth * scale);
                            imgHeight = (int) (imgHeight * scale);
                            image = image.getScaledInstance(imgWidth, imgHeight, Image.SCALE_SMOOTH);
                            imageIcon = new ImageIcon(image);
                        }
                        JLabel imageLabel = new JLabel(imageIcon);
                        imageLabel.setHorizontalAlignment(JLabel.CENTER);
                        JScrollPane scrollPane = new JScrollPane(imageLabel);
                        scrollPane.setPreferredSize(new Dimension(imgWidth + 20, imgHeight + 40));
                        JButton closeButton = new JButton("关闭");
                        closeButton.addActionListener(e -> imageDialog.dispose());
                        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                        buttonPanel.add(closeButton);
                        imageDialog.add(scrollPane, BorderLayout.CENTER);
                        imageDialog.add(buttonPanel, BorderLayout.SOUTH);
                        imageDialog.pack();
                        imageDialog.setLocationRelativeTo(VideoQueryPanel.this);
                        imageDialog.setVisible(true);
                    } else {
                        throw new Exception("图片加载失败");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(VideoQueryPanel.this, "无法加载图片: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * 显示预览图片对话框（支持多张图片）
     */
    private void showPreviewDialog(String previewUrls) {
        // 解析预览URL，用"、"分隔
        String[] urls = previewUrls.split("、");
        if (urls.length == 0) {
            JOptionPane.showMessageDialog(this, "预览URL格式错误", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 非模态加载提示
        JDialog loadingDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "加载中", false);
        loadingDialog.setLayout(new BorderLayout());
        JLabel loadingLabel = new JLabel("正在加载预览图片...", JLabel.CENTER);
        loadingLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        loadingDialog.add(loadingLabel, BorderLayout.CENTER);
        loadingDialog.setSize(250, 100);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        loadingDialog.setVisible(true);

        // 异步加载所有图片
        SwingWorker<List<ImageIcon>, Void> worker = new SwingWorker<List<ImageIcon>, Void>() {
            @Override
            protected List<ImageIcon> doInBackground() throws Exception {
                List<ImageIcon> imageIcons = new ArrayList<>();
                for (int i = 0; i < urls.length; i++) {
                                            try {
                            String url = urls[i].trim();
                            if (!url.isEmpty()) {
                                System.out.println("后台线程开始下载预览图片 " + (i + 1) + ": " + url);
                                
                                // 设置代理
                                java.net.Proxy proxy = new java.net.Proxy(
                                    java.net.Proxy.Type.HTTP, 
                                    new java.net.InetSocketAddress("127.0.0.1", 7890)
                                );
                                
                                // 创建连接
                                java.net.URL imgUrl = new java.net.URL(url);
                                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) imgUrl.openConnection(proxy);
                                
                                // 设置请求头
                                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
                                connection.setRequestProperty("Referer", url);
                                connection.setRequestProperty("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
                                connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
                                connection.setConnectTimeout(10000);
                                connection.setReadTimeout(30000);
                                
                                // 读取图片数据
                                java.io.InputStream inputStream = connection.getInputStream();
                                java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                }
                                inputStream.close();
                                
                                // 创建ImageIcon
                                byte[] imageData = outputStream.toByteArray();
                                ImageIcon icon = new ImageIcon(imageData);
                                System.out.println("预览图片 " + (i + 1) + " 下载完成，尺寸: " + icon.getIconWidth() + "x" + icon.getIconHeight());
                                imageIcons.add(icon);
                            }
                        } catch (Exception e) {
                            System.out.println("预览图片 " + (i + 1) + " 下载失败: " + e.getMessage());
                            // 继续下载其他图片，不中断
                        }
                }
                return imageIcons;
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    List<ImageIcon> imageIcons = get();
                    if (imageIcons != null && !imageIcons.isEmpty()) {
                        showPreviewGallery(imageIcons, urls);
                    } else {
                        JOptionPane.showMessageDialog(VideoQueryPanel.this, "所有预览图片加载失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(VideoQueryPanel.this, "加载预览图片失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * 显示预览图片画廊
     */
    private void showPreviewGallery(List<ImageIcon> imageIcons, String[] urls) {
        JDialog galleryDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "预览图片画廊", true);
        galleryDialog.setLayout(new BorderLayout());

        // 创建图片显示面板
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < imageIcons.size(); i++) {
            ImageIcon originalIcon = imageIcons.get(i);
            Image image = originalIcon.getImage();
            
            // 调整图片大小，缩略图显示
            int thumbWidth = 200;
            int thumbHeight = 150;
            int imgWidth = image.getWidth(null);
            int imgHeight = image.getHeight(null);
            
            if (imgWidth > thumbWidth || imgHeight > thumbHeight) {
                double scale = Math.min((double) thumbWidth / imgWidth, (double) thumbHeight / imgHeight);
                imgWidth = (int) (imgWidth * scale);
                imgHeight = (int) (imgHeight * scale);
                image = image.getScaledInstance(imgWidth, imgHeight, Image.SCALE_SMOOTH);
            }
            
            ImageIcon thumbIcon = new ImageIcon(image);
            JLabel imageLabel = new JLabel(thumbIcon);
            imageLabel.setBorder(BorderFactory.createEtchedBorder());
            
            // 添加点击事件，显示大图
            final int index = i;
            imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        showLargeImage(originalIcon, "预览图片 " + (index + 1));
                    }
                }
            });
            
            // 添加图片标题
            JPanel imageContainer = new JPanel(new BorderLayout());
            imageContainer.add(imageLabel, BorderLayout.CENTER);
            JLabel titleLabel = new JLabel("图片 " + (i + 1), JLabel.CENTER);
            titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            imageContainer.add(titleLabel, BorderLayout.SOUTH);
            
            imagePanel.add(imageContainer);
        }

        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(imagePanel);
        scrollPane.setPreferredSize(new Dimension(800, 400));

        // 添加按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> galleryDialog.dispose());
        buttonPanel.add(closeButton);

        galleryDialog.add(scrollPane, BorderLayout.CENTER);
        galleryDialog.add(buttonPanel, BorderLayout.SOUTH);

        galleryDialog.pack();
        galleryDialog.setLocationRelativeTo(this);
        galleryDialog.setVisible(true);
    }

    /**
     * 显示大图
     */
    private void showLargeImage(ImageIcon imageIcon, String title) {
        JDialog largeImageDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        largeImageDialog.setLayout(new BorderLayout());

        Image image = imageIcon.getImage();
        int imgWidth = image.getWidth(null);
        int imgHeight = image.getHeight(null);
        
        // 调整图片大小，最大显示800x600
        int maxWidth = 800;
        int maxHeight = 600;
        
        if (imgWidth > maxWidth || imgHeight > maxHeight) {
            double scale = Math.min((double) maxWidth / imgWidth, (double) maxHeight / imgHeight);
            imgWidth = (int) (imgWidth * scale);
            imgHeight = (int) (imgHeight * scale);
            image = image.getScaledInstance(imgWidth, imgHeight, Image.SCALE_SMOOTH);
            imageIcon = new ImageIcon(image);
        }

        JLabel imageLabel = new JLabel(imageIcon);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        scrollPane.setPreferredSize(new Dimension(imgWidth + 20, imgHeight + 40));

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> largeImageDialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);

        largeImageDialog.add(scrollPane, BorderLayout.CENTER);
        largeImageDialog.add(buttonPanel, BorderLayout.SOUTH);

        largeImageDialog.pack();
        largeImageDialog.setLocationRelativeTo(this);
        largeImageDialog.setVisible(true);
    }

    /**
     * 导入m3u8文件
     */
    private void importM3u8Files() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("M3U8文件", "m3u8"));
        fileChooser.setDialogTitle("选择要导入的m3u8文件");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            if (files.length > 0) {
                // 显示进度对话框
                JDialog progressDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "导入进度", false);
                progressDialog.setLayout(new BorderLayout());
                
                JProgressBar progressBar = new JProgressBar(0, files.length);
                progressBar.setStringPainted(true);
                JLabel statusLabel = new JLabel("正在导入...", JLabel.CENTER);
                
                progressDialog.add(statusLabel, BorderLayout.NORTH);
                progressDialog.add(progressBar, BorderLayout.CENTER);
                
                progressDialog.setSize(300, 100);
                progressDialog.setLocationRelativeTo(this);
                progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                
                // 在后台线程中执行导入
                SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        final int[] successCount = {0};
                        final int[] failCount = {0};
                        
                        for (int i = 0; i < files.length; i++) {
                            File file = files[i];
                            try {
                                // 读取文件内容
                                String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), "UTF-8");
                                
                                // 创建VideoData对象
                                VideoData video = new VideoData();
                                String name = file.getName().replace(".m3u8", "");
                                MovieVo movieVo = JavBusApi.getVideoInfoByName(name);
                                if (movieVo!=null){
                                    video.setName(movieVo.getTitle().replaceAll(movieVo.getId()+"| ", ""));
                                    video.setDate(movieVo.getDate());
                                    video.setCode(movieVo.getId());
                                    if (movieVo.getSamples() != null && !movieVo.getSamples().isEmpty()) {
                                        List<String> preview = movieVo.getSamples().stream().map(MovieVo.Sample::getSrc).collect(Collectors.toList());
                                        video.setPreview(String.join("、",preview));
                                    }
                                    video.setImg(movieVo.getImg());
                                    if (movieVo.getStars() != null && !movieVo.getStars().isEmpty()) {
                                        List<String> star = movieVo.getStars().stream().map(MovieVo.Star::getName).collect(Collectors.toList());
                                        video.setPerformer(String.join("、",star));
                                    }
                                    video.setDuration(movieVo.getVideoLength());
                                    if (movieVo.getGenres() != null && !movieVo.getGenres().isEmpty()) {
                                        List<String> genres = movieVo.getGenres().stream().map(MovieVo.Genre::getName).collect(Collectors.toList());
                                        video.setTypes(String.join("、",genres));
                                    }
                                }else {
                                    video.setName(name);
                                    video.setDate(new Date());
                                    video.setCode("");
                                    video.setPreview("");
                                    video.setImg("");
                                    video.setPerformer("");
                                    video.setDuration("");
                                    video.setTypes("");
                                }
                                video.setUrl("https://2296429409.github.io/app_m3u8/"+file.getName());
                                video.setRemark(file.getName());
                                video.setFile(content); // 文件字段取文件内容
                                video.setUpdateTime(new Date());
                                
                                // 保存到数据库
                                if (VideoDataService.addVideo(video)) {
                                    successCount[0]++;
                                } else {
                                    failCount[0]++;
                                }

                                try {
                                    //上传github
                                    GithubApi.uploadFile(file);
                                    GithubApi.updateFile();
                                }catch (Exception e){
                                    System.err.println("上传文件失败: " + file.getName() + " - " + e.getMessage());
                                }

                                // 更新进度
                                publish(i + 1);
                                
                            } catch (Exception e) {
                                System.err.println("导入文件失败: " + file.getName() + " - " + e.getMessage());
                                failCount[0]++;
                                publish(i + 1);
                            }
                        }
                        
                        // 显示结果
                        SwingUtilities.invokeLater(() -> {
                            progressDialog.dispose();
                            JOptionPane.showMessageDialog(VideoQueryPanel.this, 
                                String.format("导入完成！\n成功: %d 个文件\n失败: %d 个文件", successCount[0], failCount[0]),
                                "导入结果", JOptionPane.INFORMATION_MESSAGE);
                            loadData(); // 刷新数据列表
                        });
                        
                        return null;
                    }
                    
                    @Override
                    protected void process(List<Integer> chunks) {
                        if (!chunks.isEmpty()) {
                            int progress = chunks.get(chunks.size() - 1);
                            progressBar.setValue(progress);
                            statusLabel.setText(String.format("正在导入... (%d/%d)", progress, files.length));
                        }
                    }
                };
                
                progressDialog.setVisible(true);
                worker.execute();
            }
        }
    }
} 