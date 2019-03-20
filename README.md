# Progressbar360play
360手机助手下载进度条


## 效果如下：

![GIF](./docs/progreebar.gif)



## 用法

1. 导入库工程
2. 在布局文件添加如下代码：

		<com.nicekun.progreebarlib.WaterProgressBar
            android:id="@+id/waterProgressBar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_centerVertical="true"
            android:layout_marginStart="12dp"
            android:layout_marginEnd="12dp"
            app:barBackgroundColor="@color/barBackgroundColor"
            app:barForgroundColor="@color/barForgroundColor"
            app:barIndicatorColor="@color/barIndicatorColor" />

3. 设置进度：
	
		mProgressBar = findViewById(R.id.waterProgressBar);
		mProgressBar.setProgress(45);