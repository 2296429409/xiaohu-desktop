package com.xiaohu.fileupload;

import com.xiaohu.fileupload.pojo.VideoData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 视频编辑对话框
 */
public class VideoEditDialog extends JDialog {
    private VideoData video;
    private boolean confirmed = false;
    
    // 表单组件
    private JTextField nameField;
    private JTextField dateField;
    private JTextField codeField;
    private JTextField urlField;
    private JTextField previewField;
    private JTextField imgField;
    private JTextField performerField;
    private JTextField durationField;
    private JTextField typesField;
    private JTextField remarkField;
    private JTextField fileField;
    
    private JButton saveButton;
    private JButton cancelButton;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public VideoEditDialog(Frame owner, VideoData video) {
        super(owner, video == null ? "新增视频" : "修改视频", true);
        this.video = video;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadData();
        
        setSize(500, 600);
        setLocationRelativeTo(owner);
    }

    private void initializeComponents() {
        nameField = new JTextField(20);
        dateField = new JTextField(20);
        codeField = new JTextField(20);
        urlField = new JTextField(20);
        previewField = new JTextField(20);
        imgField = new JTextField(20);
        performerField = new JTextField(20);
        durationField = new JTextField(20);
        typesField = new JTextField(20);
        remarkField = new JTextField(20);
        fileField = new JTextField(20);
        
        saveButton = new JButton("保存");
        cancelButton = new JButton("取消");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 表单面板
        JPanel formPanel = new JPanel(new GridLayout(12, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("视频信息"));
        
        formPanel.add(new JLabel("名称:"));
        formPanel.add(nameField);
        
        formPanel.add(new JLabel("日期 (yyyy-MM-dd HH:mm:ss.SSS):"));
        formPanel.add(dateField);
        
        formPanel.add(new JLabel("编码:"));
        formPanel.add(codeField);
        
        formPanel.add(new JLabel("URL:"));
        formPanel.add(urlField);
        
        formPanel.add(new JLabel("预览:"));
        formPanel.add(previewField);
        
        formPanel.add(new JLabel("图片:"));
        formPanel.add(imgField);
        
        formPanel.add(new JLabel("演员:"));
        formPanel.add(performerField);
        
        formPanel.add(new JLabel("时长:"));
        formPanel.add(durationField);
        
        formPanel.add(new JLabel("类型:"));
        formPanel.add(typesField);
        
        formPanel.add(new JLabel("备注:"));
        formPanel.add(remarkField);
        
        formPanel.add(new JLabel("文件:"));
        formPanel.add(fileField);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    private void setupEventHandlers() {
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateAndSave()) {
                    confirmed = true;
                    dispose();
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void loadData() {
        if (video != null) {
            // 修改模式，加载现有数据
            nameField.setText(video.getName() != null ? video.getName() : "");
            dateField.setText(video.getDate() != null ? DATE_FORMAT.format(video.getDate()) : "");
            codeField.setText(video.getCode() != null ? video.getCode() : "");
            urlField.setText(video.getUrl() != null ? video.getUrl() : "");
            previewField.setText(video.getPreview() != null ? video.getPreview() : "");
            imgField.setText(video.getImg() != null ? video.getImg() : "");
            performerField.setText(video.getPerformer() != null ? video.getPerformer() : "");
            durationField.setText(video.getDuration() != null ? video.getDuration() : "");
            typesField.setText(video.getTypes() != null ? video.getTypes() : "");
            remarkField.setText(video.getRemark() != null ? video.getRemark() : "");
            fileField.setText(video.getFile() != null ? video.getFile() : "");
        } else {
            // 新增模式，设置默认值
            dateField.setText(DATE_FORMAT.format(new Date()));
        }
    }

    private boolean validateAndSave() {
        // 验证必填字段
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入视频名称", "验证失败", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        
        // 验证日期格式
        Date date = null;
        if (!dateField.getText().trim().isEmpty()) {
            try {
                date = DATE_FORMAT.parse(dateField.getText().trim());
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "日期格式不正确，请使用 yyyy-MM-dd HH:mm:ss.SSS 格式", "验证失败", JOptionPane.WARNING_MESSAGE);
                dateField.requestFocus();
                return false;
            }
        }
        
        // 创建或更新VideoData对象
        if (video == null) {
            video = new VideoData();
        }
        
        video.setName(nameField.getText().trim());
        video.setDate(date);
        video.setCode(codeField.getText().trim());
        video.setUrl(urlField.getText().trim());
        video.setPreview(previewField.getText().trim());
        video.setImg(imgField.getText().trim());
        video.setPerformer(performerField.getText().trim());
        video.setDuration(durationField.getText().trim());
        video.setTypes(typesField.getText().trim());
        video.setRemark(remarkField.getText().trim());
        video.setFile(fileField.getText().trim());
        video.setUpdateTime(new Date()); // 这里保持Date对象，在数据库操作时会格式化为字符串
        
        // 保存到数据库
        boolean success;
        if (video.getId() == null) {
            // 新增
            success = VideoDataService.addVideo(video);
        } else {
            // 修改
            success = VideoDataService.updateVideo(video);
        }
        
        if (success) {
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "保存失败！", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }
} 