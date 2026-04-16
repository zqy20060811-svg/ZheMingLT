# 微信小程序图标文件指南

## 图标文件准备

### 尺寸要求
- 图标尺寸：81x81 像素
- 图片格式：PNG
- 建议使用透明背景

### 图标类型
为每个标签栏项目准备两个图标：
1. **默认状态图标**：未选中时显示
2. **选中状态图标**：选中时显示

### 图标命名规范
- 首页图标：`tab-home.png`（默认）、`tab-home-active.png`（选中）
- 发布图标：`tab-publish.png`（默认）、`tab-publish-active.png`（选中）

## 配置方法

### 1. 放置图标文件
将准备好的图标文件放入以下目录：
```
Wen-WeiXing/images/
```

### 2. 修改 app.json
在 `tabBar` 配置中添加 `iconPath` 和 `selectedIconPath`：

```json
"tabBar": {
  "list": [
    {
      "pagePath": "pages/index/index",
      "text": "首页",
      "iconPath": "images/tab-home.png",
      "selectedIconPath": "images/tab-home-active.png"
    },
    {
      "pagePath": "pages/create-post/create-post",
      "text": "发布",
      "iconPath": "images/tab-publish.png",
      "selectedIconPath": "images/tab-publish-active.png"
    }
  ],
  "backgroundColor": "#ffffff",
  "selectedColor": "#4a6fa5",
  "color": "#666666"
},
```

### 3. 重新编译小程序
- 在微信开发者工具中点击编译按钮
- 查看标签栏是否显示自定义图标

## 其他注意事项

### 1. 开发阶段
- 如果暂时没有图标文件，可以移除 `iconPath` 和 `selectedIconPath` 配置
- 小程序会使用默认的标签栏图标

### 2. 图标设计建议
- 保持图标风格一致
- 使用简洁明了的图标
- 确保图标在不同状态下有明显区别
- 考虑图标在不同设备上的显示效果

### 3. 性能优化
- 图标文件大小建议控制在 10KB 以下
- 可以使用图标字体替代图片图标，减少加载时间

### 4. 兼容性
- 确保图标在 iOS 和 Android 设备上都能正常显示
- 测试不同屏幕尺寸下的显示效果

## 示例图标

如果需要示例图标，可以使用以下方法获取：
1. 使用在线图标库（如 Iconfont、Font Awesome）
2. 使用设计工具（如 Sketch、Figma）创建
3. 使用在线图标生成工具

## 故障排除

如果图标不显示，检查以下几点：
- 图标文件路径是否正确
- 图标文件格式是否正确
- 图标文件是否存在
- 微信开发者工具是否已重新编译

如果问题仍然存在，可以暂时移除图标配置，使用默认图标。